#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>
#define LOG_TAG "GuitarFX"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace oboe;

class GuitarAudioEngine : public AudioStreamCallback {
public:
    float mDistortion = 0.5f, mReverb = 0.3f, mDelay = 0.2f, mVolume = 0.75f;
    bool mIsOn = false;
    std::shared_ptr<AudioStream> mStream;
    std::vector<float> mDelayBuffer;
    size_t mDelayIndex = 0;
    float mReverbPrev = 0.0f;

    Result open() {
        AudioStreamBuilder b;
        b.setDirection(Direction::InputOutput)
         ->setSampleRate(48000)
         ->setPerformanceMode(PerformanceMode::LowLatency)
         ->setSharingMode(SharingMode::Exclusive)
         ->setFormat(AudioFormat::Float)
         ->setChannelCount(ChannelCount::Mono);
        Result r = b.openStream(&mStream);
        if (r == Result::OK) {
            mStream->setCallback(this);
            mDelayBuffer.resize((size_t)(48000 * 0.5f), 0.0f);
            LOGI("✅ Low-Latency Stream Opened");
        }
        return r;
    }
    Result start() { return mStream ? mStream->requestStart() : Result::ErrorNull; }
    void stop() { if (mStream) mStream->requestStop(); }
    void setPower(bool on) { mIsOn = on; }
    DataCallbackResult onAudioReady(AudioStream*, void* data, int32_t n) override {
        float* buf = static_cast<float*>(data);
        for (int i = 0; i < n; i++) {
            float x = buf[i];
            if (!mIsOn) { buf[i] = x * mVolume; continue; }
            
            // Distortion
            if (mDistortion > 0.01f) {
                float g = 1.0f + mDistortion * 8.0f;
                x = std::tanh(x * g) / std::tanh(g);
            }
            
            // Delay
            if (mDelay > 0.01f) {
                float d = mDelayBuffer[mDelayIndex] * mDelay * 0.5f;
                mDelayBuffer[mDelayIndex] = x;
                mDelayIndex = (mDelayIndex + 1) % mDelayBuffer.size();
                x += d;
            }
            
            // Reverb
            if (mReverb > 0.01f) {
                mReverbPrev = mReverbPrev * mReverb + x * (1.0f - mReverb);
                x = x * (1.0f - mReverb * 0.5f) + mReverbPrev * mReverb * 0.5f;
            }
            
            buf[i] = x * mVolume;
        }
        return DataCallbackResult::Continue;
    }
};

static GuitarAudioEngine* gEng = nullptr;

extern "C" {
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_nativeInit(JNIEnv*, jobject) {
        if (!gEng) gEng = new GuitarAudioEngine();
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_nativeStart(JNIEnv*, jobject) {
        if (gEng) { gEng->open(); gEng->start(); }
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_nativeStop(JNIEnv*, jobject) {
        if (gEng) gEng->stop();
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setPower(JNIEnv*, jobject, jboolean on) {
        if (gEng) gEng->setPower(on);
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setDistortion(JNIEnv*, jobject, jfloat v) {
        if (gEng) gEng->mDistortion = v;
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setReverb(JNIEnv*, jobject, jfloat v) {
        if (gEng) gEng->mReverb = v;
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setDelay(JNIEnv*, jobject, jfloat v) {
        if (gEng) gEng->mDelay = v;
    }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setVolume(JNIEnv*, jobject, jfloat v) {
        if (gEng) gEng->mVolume = v;
    }
}

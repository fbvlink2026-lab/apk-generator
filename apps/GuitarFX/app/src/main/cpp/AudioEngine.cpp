#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "GuitarFX-Audio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace oboe;

class GuitarAudioEngine : public AudioStreamCallback {
public:
    GuitarAudioEngine() {
        mDistortion = 0.5f;
        mReverb = 0.3f;
        mDelay = 0.2f;
        mVolume = 0.75f;
        mIsOn = false;
    }

    Result openStream() {
        AudioStreamBuilder builder;
        builder.setDirection(Direction::InputOutput)
               ->setSampleRate(48000)
               ->setSampleRateConversionQuality(SampleRateConversionQuality::Fastest)
               ->setPerformanceMode(PerformanceMode::LowLatency)
               ->setSharingMode(SharingMode::Exclusive)
               ->setFormat(AudioFormat::Float)
               ->setChannelCount(ChannelCount::Mono);

        Result result = builder.openStream(&mStream);
        if (result == Result::OK && mStream) {
            mStream->setCallback(this);
            mSampleRate = mStream->getSampleRate();
            mDelayBuffer.resize((size_t)(mSampleRate * 0.5f), 0.0f);
            mDelayIndex = 0;
            LOGI("Stream opened: %d Hz", mSampleRate);
        }
        return result;
    }

    Result start() {
        if (!mStream) return Result::ErrorNull;
        Result r = mStream->requestStart();
        mIsRunning = (r == Result::OK);
        return r;
    }

    void stop() {
        if (mStream) {
            mStream->requestStop();
            mIsRunning = false;
        }
    }

    void close() {
        stop();
        if (mStream) mStream->close();
    }

    void setDistortion(float v) { mDistortion = v; }
    void setReverb(float v) { mReverb = v; }
    void setDelay(float v) { mDelay = v; }
    void setVolume(float v) { mVolume = v; }
    void setPower(bool on) { mIsOn = on; }

    DataCallbackResult onAudioReady(AudioStream *stream, void *audioData, int32_t numFrames) override {
        float *output = static_cast<float*>(audioData);
        
        for (int i = 0; i < numFrames; i++) {
            float input = output[i];
            
            if (!mIsOn) {
                output[i] = input * mVolume;
                continue;
            }

            if (mDistortion > 0.01f) {
                float drive = 1.0f + mDistortion * 8.0f;
                input = std::tanh(input * drive) / std::tanh(drive);
            }

            if (mDelay > 0.01f && !mDelayBuffer.empty()) {
                float delayAmount = mDelay * 0.5f;
                float delayed = mDelayBuffer[mDelayIndex] * delayAmount;
                mDelayBuffer[mDelayIndex] = input;
                mDelayIndex = (mDelayIndex + 1) % mDelayBuffer.size();
                input = input + delayed;
            }

            if (mReverb > 0.01f) {
                mReverbPrev = mReverbPrev * mReverb + input * (1.0f - mReverb);
                input = input * (1.0f - mReverb * 0.5f) + mReverbPrev * mReverb * 0.5f;
            }

            output[i] = input * mVolume;
        }
        return DataCallbackResult::Continue;
    }

private:
    std::shared_ptr<AudioStream> mStream;
    int32_t mSampleRate = 48000;
    float mDistortion = 0.5f;
    float mReverb = 0.3f;
    float mDelay = 0.2f;
    float mVolume = 0.75f;
    float mReverbPrev = 0.0f;
    std::vector<float> mDelayBuffer;
    size_t mDelayIndex = 0;
    bool mIsOn = false;
    bool mIsRunning = false;
};

extern "C" {
    static GuitarAudioEngine *gEngine = nullptr;

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeInit(JNIEnv *, jobject) {
        if (!gEngine) gEngine = new GuitarAudioEngine();
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeStart(JNIEnv *, jobject) {
        if (gEngine) {
            gEngine->openStream();
            gEngine->start();
        }
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeStop(JNIEnv *, jobject) {
        if (gEngine) gEngine->stop();
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeSetPower(JNIEnv *, jobject, jboolean on) {
        if (gEngine) gEngine->setPower(on);
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeSetDistortion(JNIEnv *, jobject, jfloat v) {
        if (gEngine) gEngine->setDistortion(v);
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeSetReverb(JNIEnv *, jobject, jfloat v) {
        if (gEngine) gEngine->setReverb(v);
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeSetDelay(JNIEnv *, jobject, jfloat v) {
        if (gEngine) gEngine->setDelay(v);
    }

    JNIEXPORT void JNICALL
    Java_com_martodosko_guitarfx_AudioWrapper_nativeSetVolume(JNIEnv *, jobject, jfloat v) {
        if (gEngine) gEngine->setVolume(v);
    }
}

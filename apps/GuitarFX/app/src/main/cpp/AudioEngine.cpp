#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
using namespace oboe;

class Engine : public AudioStreamCallback {
public:
    bool mOn = false;
    float mDist = 0.5f, mRev = 0.3f, mDel = 0.2f, mVol = 0.75f;
    std::shared_ptr<AudioStream> mStream;
    DataCallbackResult onAudioReady(AudioStream*, void* d, int32_t n) override {
        float* buf = static_cast<float*>(d);
        for (int i=0; i<n; i++) {
            float x = buf[i];
            if (mOn && mDist > 0.01f) x = std::tanh(x * (1.0f + mDist*8.0f)) / std::tanh(1.0f + mDist*8.0f);
            buf[i] = x * mVol;
        }
        return DataCallbackResult::Continue;
    }
    Result open() {
        AudioStreamBuilder b;
        b.setDirection(Direction::InputOutput)->setSampleRate(48000)->setPerformanceMode(PerformanceMode::LowLatency);
        return b.openStream(&mStream);
    }
};
static Engine* gEng = nullptr;

extern "C" {
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_nativeInit(JNIEnv*, jobject) { if(!gEng) gEng=new Engine(); }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_nativeStart(JNIEnv*, jobject) { if(gEng)gEng->open(),gEng->mStream->requestStart(); }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setPower(JNIEnv*, jobject, jboolean o) { if(gEng)gEng->mOn=o; }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setDistortion(JNIEnv*, jobject, jfloat v) { if(gEng)gEng->mDist=v; }
    JNIEXPORT void JNICALL Java_com_martodosko_guitarfx_AudioWrapper_setVolume(JNIEnv*, jobject, jfloat v) { if(gEng)gEng->mVol=v; }
}

package com.martodosko.guitarfx

object AudioWrapper {
    init {
        System.loadLibrary("guitarfx")
    }
    external fun nativeInit()
    external fun nativeStart()
    external fun nativeStop()
    external fun nativeSetPower(on: Boolean)
    external fun nativeSetDistortion(value: Float)
    external fun nativeSetReverb(value: Float)
    external fun nativeSetDelay(value: Float)
    external fun nativeSetVolume(value: Float)
}

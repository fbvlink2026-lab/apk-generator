package com.martodosko.guitarfx
object AudioWrapper {
    init { System.loadLibrary("guitarfx") }
    external fun nativeInit()
    external fun nativeStart()
    external fun nativeStop()
    external fun setPower(on: Boolean)
    external fun setDistortion(value: Float)
    external fun setReverb(value: Float)
    external fun setDelay(value: Float)
    external fun setVolume(value: Float)
}

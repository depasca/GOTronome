package com.pdp.metronome_oboe

class NativeLib {

    /**
     * A native method that is implemented by the 'metronome_oboe' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'metronome_oboe' library on application startup.
        init {
            System.loadLibrary("metronome_oboe")
        }
    }
}
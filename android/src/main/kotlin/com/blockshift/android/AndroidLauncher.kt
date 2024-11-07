package com.blockshift.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.blockshift.Main

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val level = intent.getIntExtra("level",1)
        initialize(Main(level), AndroidApplicationConfiguration().apply {
            useAccelerometer = true
            useImmersiveMode = true // Recommended, but not required.
        })
    }
}

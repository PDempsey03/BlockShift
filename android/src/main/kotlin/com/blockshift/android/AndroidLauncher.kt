package com.blockshift.android

import android.content.Intent
import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.blockshift.Main
import com.blockshift.model.repositories.UserTableNames
import com.blockshift.ui.settings.SettingsActivity

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication(), Main.WinCallback {
    private lateinit var username: String
    private lateinit var displayName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        username = intent.getStringExtra(UserTableNames.USERNAME).toString()
        displayName = intent.getStringExtra(UserTableNames.DISPLAY_NAME).toString()

        val level = intent.getIntExtra("level",1)
        val main: Main = Main(level)

        main.setCallback(this)

        initialize(main, AndroidApplicationConfiguration().apply {
            useAccelerometer = true
            useImmersiveMode = true // Recommended, but not required.
        })
    }

    override fun returnToLevelSelect(moves: Int, level: Int) {
        val intent: Intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("moves", moves)
        intent.putExtra("time",Int.MAX_VALUE)
        intent.putExtra("distance",Int.MIN_VALUE)
        intent.putExtra("level",level)
        intent.putExtra(UserTableNames.USERNAME, username)
        intent.putExtra(UserTableNames.DISPLAY_NAME, displayName)
        startActivity(intent)
    }

}

package com.blockshift.android

import android.content.Intent
import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.blockshift.Main
import com.blockshift.model.repositories.HighScoreTableNames
import com.blockshift.model.repositories.UserTableNames
import com.blockshift.ui.settings.SettingsActivity

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication(), Main.ExitCallback {
    private lateinit var username: String
    private lateinit var displayName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        username = intent.getStringExtra(UserTableNames.USERNAME).toString()
        displayName = intent.getStringExtra(UserTableNames.DISPLAY_NAME).toString()

        val level = intent.getIntExtra(HighScoreTableNames.LEVEL_ID,1)
        val main: Main = Main(level)

        main.setCallback(this)

        initialize(main, AndroidApplicationConfiguration().apply {
            useAccelerometer = true
            useImmersiveMode = true // Recommended, but not required.
        })
    }

    override fun returnToLevelSelect(moves: Int?, time: Long?, distance: Int?, level: Int?) {
        val intent: Intent = Intent(this, SettingsActivity::class.java)
        if (moves != null) {
            intent.putExtra(HighScoreTableNames.MOVES, moves)
            intent.putExtra(HighScoreTableNames.TIME, time)
            intent.putExtra(HighScoreTableNames.DISTANCE, distance)
            intent.putExtra(HighScoreTableNames.LEVEL_ID, level)
        }
        intent.putExtra(UserTableNames.USERNAME, username)
        intent.putExtra(UserTableNames.DISPLAY_NAME, displayName)
        startActivity(intent)
    }
}

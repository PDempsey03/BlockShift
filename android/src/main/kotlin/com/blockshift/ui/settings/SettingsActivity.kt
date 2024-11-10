package com.blockshift.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blockshift.model.db.HighScore
import com.blockshift.model.db.OfflineHighScoreViewModel
import com.blockshift.model.db.OfflineUserViewModel
import com.blockshift.model.db.User
import com.blockshift.model.login.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.blockshift.ui.mainpage.HomePageFragment
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserRepository
import com.blockshift.model.repositories.UserTableNames
import com.blockshift.ui.mainpage.HighScorePageFragment
import com.blockshift.R
import kotlin.math.min

class SettingsActivity : AppCompatActivity() {

    private lateinit var offlineHighScoreModel: OfflineHighScoreViewModel
    private lateinit var offlineUserModel: OfflineUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val tabLayout = findViewById<TabLayout>(R.id.settings_tab_layout)

        val currentUsername:String = intent.getStringExtra(UserTableNames.USERNAME).toString()
        val currentDisplayName:String = intent.getStringExtra(UserTableNames.DISPLAY_NAME).toString()
        val userViewModel: UserViewModel by viewModels()
        userViewModel.currentUser.value = UserData(currentUsername,currentDisplayName)
        UserRepository.startListeningForUser(userViewModel)

        offlineHighScoreModel = ViewModelProvider(this).get(OfflineHighScoreViewModel::class.java)
        offlineUserModel = ViewModelProvider(this).get(OfflineUserViewModel::class.java)
        offlineHighScoreModel.readByUsername.observe(this, Observer {
            scores ->
            if(scores.isEmpty()) {
                offlineDBInit(offlineHighScoreModel,offlineUserModel)
            }
        })

        if (intent.hasExtra("moves") && intent.hasExtra("time") && intent.hasExtra("distance") && intent.hasExtra("level")) {
            val level: Int = intent.getIntExtra("level",-1)
            var time: Int = intent.getIntExtra("time",Int.MAX_VALUE)
            var distance: Int = intent.getIntExtra("distance",Int.MAX_VALUE)
            var moves: Int = intent.getIntExtra("score", Int.MAX_VALUE)
            Log.d("score", "$moves")
            // TODO: write score
            if(currentUsername == "lcl") {
                offlineHighScoreModel.getHighScoreByLevel(level).observe(this, Observer {
                    score ->
                    moves = min(moves,score.moves)
                    time = min(time,score.time)
                    distance = min(distance,score.distance)

                    offlineHighScoreModel.updateHighScore(HighScore("lcl",level,distance,time,moves))
                })
            } else {
                //TODO: Update Firebase
            }
        }


        // always load the account settings by default
        loadFragment(AccountSettingsFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when(tab?.position ?: 0) {
                    0 -> AccountSettingsFragment()
                    1 -> HomePageFragment()
                    2 -> HighScorePageFragment()
                    else -> null
                }
                if(fragment != null) {
                    loadFragment(fragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselected
            }
        })
    }

    fun getOfflineViewModel(): OfflineHighScoreViewModel {
        return offlineHighScoreModel
    }

    private fun offlineDBInit(highScoreVM:OfflineHighScoreViewModel,userVM:OfflineUserViewModel) {
        userVM.addUser(User("lcl","Guest"))
        for(i in 1..12) {
            highScoreVM.addHighScore(HighScore("lcl",i,Int.MAX_VALUE,Int.MAX_VALUE,Int.MAX_VALUE))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_frame_layout, fragment)
            .commit()
    }
}

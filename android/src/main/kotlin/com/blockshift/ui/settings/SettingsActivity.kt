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
import com.blockshift.model.repositories.HighScoreData
import com.blockshift.model.repositories.HighScoreRepository
import com.blockshift.model.repositories.HighScoreTableNames
import kotlin.math.min

open class SettingsActivity : AppCompatActivity() {

    private lateinit var offlineHighScoreModel: OfflineHighScoreViewModel
    private lateinit var offlineUserModel: OfflineUserViewModel
    private val TAG: String = javaClass.simpleName

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

        // if coming back from game and it was a win, update possible new highscores
        if (intent.hasExtra(HighScoreTableNames.MOVES)
            && intent.hasExtra(HighScoreTableNames.TIME)
            && intent.hasExtra(HighScoreTableNames.DISTANCE)
            && intent.hasExtra(HighScoreTableNames.LEVEL_ID)) {

            val level: Int = intent.getIntExtra(HighScoreTableNames.LEVEL_ID,-1)
            var time: Long = intent.getLongExtra(HighScoreTableNames.TIME, Long.MAX_VALUE)
            var distance: Int = intent.getIntExtra(HighScoreTableNames.DISTANCE,Int.MAX_VALUE)
            var moves: Int = intent.getIntExtra(HighScoreTableNames.MOVES, Int.MAX_VALUE)

            // store score for guest on offline local db
            if(currentUsername == "lcl") {
                offlineHighScoreModel.getHighScoreByLevel(level).observe(this, Observer {
                    score ->
                    moves = min(moves,score.moves)
                    time = min(time,score.time)
                    distance = min(distance,score.distance)

                    offlineHighScoreModel.updateHighScore(HighScore("lcl",level,distance,time,moves))
                })
            } else {
                // Store high score on firebase
                HighScoreRepository.updateHighScore(HighScoreData(
                    currentUsername, level.toString(), distance.toLong(), moves.toLong(), time),
                    /*
                     * TODO:
                     *  if for some reason high scores weren't / couldn't stored, consider storing locally
                     *  and attempting to sync next time the app starts which would need its own
                     *  implementation. i.e store any un-uploaded high scores in local room database
                     *  and when the app starts, run a new thread to sync firebase with local room
                     *  database (for now just printing some debug messages)
                     */
                    { success ->
                        Log.d(TAG, if(success) "Successfully updated high score" else "Failed to upload high score")
                    },
                    { exception ->
                        Log.e(TAG, "Failed to upload high score due to exception", exception)
                    })
            }
        }


        // always load the account settings by default
        //loadFragment(AccountSettingsFragment())
        loadFragment(HomePageFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when(tab?.position ?: 0) {
                    0 -> HomePageFragment()
                    1 -> AccountSettingsFragment()
                    //0 -> AccountSettingsFragment()
                    //1 -> HomePageFragment()
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

        savedInstanceState?.getInt("selectedTab")?.let { selectedTab ->
            tabLayout.getTabAt(selectedTab)?.select()
        }
    }

    fun getOfflineViewModel(): OfflineHighScoreViewModel {
        return offlineHighScoreModel
    }

    private fun offlineDBInit(highScoreVM:OfflineHighScoreViewModel,userVM:OfflineUserViewModel) {
        userVM.addUser(User("lcl","Guest"))
        for(i in 1..12) {
            highScoreVM.addHighScore(HighScore("lcl", i, Int.MAX_VALUE, Long.MAX_VALUE, Int.MAX_VALUE))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_frame_layout, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState:Bundle) {
        super.onSaveInstanceState(outState)
        val currentTab = findViewById<TabLayout>(R.id.settings_tab_layout)
        val tabPosition = currentTab.selectedTabPosition
        outState.putInt("selectedTab",tabPosition)
    }

}

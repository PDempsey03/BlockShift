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

        if (intent.hasExtra("score")) {
            val moves: Int = intent.getIntExtra("score", 0)
            Log.d("score", "$moves")

            // TODO: write score
        }

        offlineHighScoreModel = ViewModelProvider(this).get(OfflineHighScoreViewModel::class.java)
        offlineUserModel = ViewModelProvider(this).get(OfflineUserViewModel::class.java)
        offlineHighScoreModel.readByUsername.observe(this, Observer {
            scores ->
            if(scores.isEmpty()) {
                offlineDBInit(offlineHighScoreModel,offlineUserModel)
            }
        })


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
            highScoreVM.addHighScore(HighScore("lcl",i,0,0,0))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_frame_layout, fragment)
            .commit()
    }
}

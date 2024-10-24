package com.blockshift.ui.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.ui.login.CreateAccountFragment
import com.blockshift.ui.login.LoginFragment
import com.blockshift.model.login.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.blockshift.ui.mainpage.HomePageFragment
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserRepository
import com.blockshift.model.repositories.UserTableNames

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val tabLayout = findViewById<TabLayout>(R.id.settings_tab_layout)

        val currentUsername:String = intent.getStringExtra(UserTableNames.USERNAME).toString()
        val currentDisplayName:String = intent.getStringExtra(UserTableNames.DISPLAY_NAME).toString()
        val userViewModel: UserViewModel by viewModels()
        userViewModel.currentUser.value = UserData(currentUsername,currentDisplayName)
        UserRepository.startListeningForUser(userViewModel)

        // always load the account settings by default
        loadFragment(AccountSettingsFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when(tab?.position ?: 0) {
                    0 -> AccountSettingsFragment()
                    1 -> HomePageFragment()
                    2 -> LoginFragment()
                    3 -> CreateAccountFragment()
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_frame_layout, fragment)
            .commit()
    }
}
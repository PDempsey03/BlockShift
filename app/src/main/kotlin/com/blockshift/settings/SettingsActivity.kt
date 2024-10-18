package com.blockshift.settings

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.login.CreateAccountFragment
import com.blockshift.login.LoginFragment
import com.google.android.material.tabs.TabLayout

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val tabLayout = findViewById<TabLayout>(R.id.settings_tab_layout)

        // always load the account settings by default
        loadFragment(AccountSettingsFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when(tab?.position ?: 0) {
                    0 -> AccountSettingsFragment()
                    1 -> LoginFragment()
                    2 -> CreateAccountFragment()
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
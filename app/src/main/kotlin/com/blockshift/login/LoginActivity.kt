package com.blockshift.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.repositories.UserAuthenticationData
import com.blockshift.repositories.UserRepository
import com.blockshift.repositories.UserTableNames
import com.blockshift.settings.SettingsActivity
import com.blockshift.settings.SettingsDataStore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val TAG: String = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        val context = this

        CoroutineScope(Dispatchers.Main).launch {
            val settingsDataStore = SettingsDataStore.getInstance(context)

            // get the nullable stored auth username and token
            val authUsername = settingsDataStore.getString(UserTableNames.AUTH_USERNAME)
            val authToken = settingsDataStore.getString(UserTableNames.AUTH_TOKEN)

            if(authUsername != null && authToken != null) {
                // attempt to auto login as there were some stored values
                Log.d(TAG, "auth token and auth username found, attempting to auto login")
                LoginManager.tryAutoLogin(authUsername, authToken, {
                    success ->
                    if(success) {
                        Log.d(TAG, "Successfully auto logged in")
                        finishLogin()
                    } else {
                        // unregister auth token since it failed
                        LoginManager.unregisterLocalAuthToken(context)

                        Log.d(TAG, "Failed to auto log in")
                        loadFragment(LoginFragment())
                    }
                }, {
                    // don't unregister auth token since exception was thrown and it may still be valid
                    exception -> Log.e(TAG, "Error accessing firebase", exception)
                    loadFragment(LoginFragment())
                })
            } else {
                // no auth username and password so continue to login screen
                Log.d(TAG, "No auth token or auth username, going to login page")
                loadFragment(LoginFragment())
            }
        }
    }

    fun finishLogin() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
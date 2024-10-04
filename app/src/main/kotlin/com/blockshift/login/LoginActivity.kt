package com.blockshift.login

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.settings.SettingsDataStore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        val context = this

        CoroutineScope(Dispatchers.Main).launch {
            val settingsDataStore = SettingsDataStore.getInstance(context)

            // get the nullable stored auth username and token
            val authUsername = settingsDataStore.getString("authusername")
            val authToken = settingsDataStore.getString("authtoken")

            if(authUsername != null && authToken != null) {
                // attempt to auto login as there were some stored values
                Log.d("Login Activity", "auth token and auth username found, attempting to auto login")
                LoginManager.tryAutoLogin(authUsername, authToken, {
                    success ->
                    if(success) {
                        // if auto login,temporarily send to create account screen (TODO: launch whatever screen after successful login is)
                        Log.d("Login Activity", "Successfully auto logged in")
                        loadFragment(CreateAccountFragment())
                    } else {
                        // unregister auth token since it failed
                        CoroutineScope(Dispatchers.Main).launch { LoginManager.unregisterAuthToken(context) }

                        Log.d("Login Activity", "Failed to auto log in")
                        loadFragment(LoginFragment())
                    }
                }, {
                    // don't unregister auth token since exception was thrown and it may still be valid
                    exception -> Log.e("Login Activity", "Error accessing firebase", exception)
                    loadFragment(LoginFragment())
                })
            } else {
                // no auth username and password so continue to login screen
                Log.d("Login Activity", "No auth token or auth username, going to log in page")
                loadFragment(LoginFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
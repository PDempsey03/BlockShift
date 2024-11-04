package com.blockshift.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.blockshift.R
import com.blockshift.model.db.AppDatabase
import com.blockshift.model.db.HighScore
import com.blockshift.model.db.HighScoreDao
import com.blockshift.model.db.User
import com.blockshift.model.db.UserDao
import com.blockshift.utils.showBasicBanner
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.blockshift.model.repositories.UserData
import kotlinx.coroutines.GlobalScope


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private val TAG: String = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameText = view.findViewById<EditText>(R.id.usernameText)
        val passwordText = view.findViewById<EditText>(R.id.passwordText)
        val rememberMeCheckBox = view.findViewById<CheckBox>(R.id.rememberMe)

        val enterInfoButton = view.findViewById<Button>(R.id.enterLoginButton)
        enterInfoButton.setOnClickListener {
            Log.d(TAG,"Enter Login Info Button Clicked")

            // start coroutine to attempt to login
            CoroutineScope(Dispatchers.Main).launch {
                // get username and password from text boxes
                val username = usernameText.getText().toString()
                val password = passwordText.getText().toString()
                val rememberMe = rememberMeCheckBox.isChecked

                LoginManager.tryLogin(username, password, { userData ->
                    if (userData != null) {
                        Log.d(TAG, "Valid Login")
                        val context = activity?.applicationContext
                        if (rememberMe) {
                            // add auth token locally and possibly generate new one
                            if (context != null) LoginManager.registerAuthToken(username, context)
                        } else {
                            // explicitly remove the auth token from local store if the user unselects
                            if (context != null) LoginManager.unregisterLocalAuthToken(context)
                        }

                        // load into the main screen
                        (activity as LoginActivity).finishLogin(userData)
                    } else {
                        Log.e("Login Screen", "Invalid Login")
                        view.showBasicBanner(getString(R.string.username_password_invalid_error), getString(R.string.ok), Snackbar.LENGTH_SHORT)
                    }
                }, { exception ->
                    Log.e("Login Screen", "Error in connecting to the firebase database", exception)
                    view.showBasicBanner(getString(R.string.server_connection_error_message), getString(R.string.ok), Snackbar.LENGTH_LONG)
                })
            }
        }

        val newUserButton = view.findViewById<Button>(R.id.newUserButton)
        newUserButton.setOnClickListener {
            Log.d(TAG,"New User Button Clicked")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment()) // fragment_container is the container for fragments
                .addToBackStack(null)  // This ensures you can go back
                .commit()
        }

        val playGuestButton = view.findViewById<Button>(R.id.guestButton)
        playGuestButton.setOnClickListener {
            Log.d(TAG,"Play as Guest Button Clicked")
            val userData = UserData("lcl","Guest")

            (activity as LoginActivity).finishLogin(userData)
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LoginFragment.
         */
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
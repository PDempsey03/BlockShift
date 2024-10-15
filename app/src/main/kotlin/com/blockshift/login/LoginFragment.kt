package com.blockshift.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.blockshift.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

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
            Log.d("Login Screen","Enter Login Info Button Clicked")

            // start coroutine to attempt to login
            CoroutineScope(Dispatchers.Main).launch {
                // get username and password from text boxes
                val username = usernameText.getText().toString()
                val password = passwordText.getText().toString()
                val rememberMe = rememberMeCheckBox.isChecked

                LoginManager.tryLogin(username, password, {
                    success ->
                        if(success) {
                            Log.d("Login Screen", "Valid Login")
                            val context = activity?.applicationContext
                            if(rememberMe) {
                                // add auth token locally and possibly generate new one
                                //if(context != null) LoginManager.registerAuthToken(username, context)
                            } else {
                                // explicitly remove the auth token from local store if the user unselects
                                //if(context != null) CoroutineScope(Dispatchers.Main).launch { LoginManager.unregisterAuthToken(context) }
                            }

                            // load into the main screen
                            (activity as LoginActivity).finishLogin()
                        } else {
                            Log.e("Login Screen", "Invalid Login")
                            val eMessage = Snackbar.make(view,"Invalid Username or Password!", Snackbar.LENGTH_SHORT).setAction("OK") {}
                            eMessage.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                            eMessage.show()
                        }
                }, {
                    exception ->
                        Log.e("Login Screen", "Error in connecting to the firebase database", exception)
                        val eMessage = Snackbar.make(view,"Login Currently Unavailable", Snackbar.LENGTH_LONG).setAction("OK") {}
                        eMessage.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        eMessage.show()
                })
            }
        }

        val newUserButton = view.findViewById<Button>(R.id.newUserButton)
        newUserButton.setOnClickListener {
            Log.d("Login Screen","New User Button Clicked")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment()) // fragment_container is the container for fragments
                .addToBackStack(null)  // This ensures you can go back
                .commit()
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
package com.blockshift.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.blockshift.R
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameText = view.findViewById<EditText>(R.id.usernameText)
        val passwordText = view.findViewById<EditText>(R.id.passwordText)

        val enterInfoButton = view.findViewById<Button>(R.id.enterLoginButton)
        enterInfoButton.setOnClickListener {
            Log.d("Login Screen","Enter Login Info Button Clicked")
            val username = usernameText.getText().toString()
            val password = passwordText.getText().toString()
            val validLogin = this.verifyLogin(username,password)

            if(validLogin) {
                Log.d("Login Screen","Valid Login")
                parentFragmentManager.popBackStack()
            } else {
                Log.e("Login Screen", "Invalid Login")
                val eMessage = Snackbar.make(view,"Invalid Username or Password!", Snackbar.LENGTH_SHORT).setAction("OK") {}
                eMessage.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                eMessage.show()
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

    private fun verifyLogin(username:String,password:String) : Boolean{
        //TODO: Use Firebase to verify login
        Log.d("Login Screen","Username: $username")
        Log.d("Login Screen","Password: $password")

        val userNamesAndPasswords = mapOf(
            "Patrick" to "Dempsey1!",
            "Michael" to "Labib1!",
            "Jackson" to "Hoyt1!"
        )

        val validUsername = userNamesAndPasswords.containsKey(username)
        if(!validUsername) {
            return false
        }


        return (userNamesAndPasswords[username].equals(password))
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
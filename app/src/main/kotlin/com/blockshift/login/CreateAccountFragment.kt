package com.blockshift.login

import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
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
 * Use the [CreateAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateAccountFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_create_account, container, false)

        // set descriptions for username / password
        setDescriptions(view)

        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)

        // set action to be taken on create account button click
        createAccountButton.setOnClickListener {

            // get username and password from text input fields
            val desiredUsername = view.findViewById<EditText>(R.id.create_account_username).text.toString()
            val desiredPassword = view.findViewById<EditText>(R.id.create_account_password).text.toString()

            // Check for valid username
            val userNameTaken = LoginManager.doesUsernameExist(desiredUsername)
            val allowedUsername = !userNameTaken && LoginManager.isValidUsername(desiredUsername)

            // check for valid password
            val allowedPassword = LoginManager.isValidPassword(desiredPassword)

            if(allowedUsername && allowedPassword) {
                Log.d("Create Account", "Successfully created account")

                // add username to to login manager
                LoginManager.addUser(desiredUsername, desiredPassword)

                // go back to start screen
                parentFragmentManager.popBackStack()

                // Show a banner saying the account was successfully created
                val banner = Snackbar.make(view, "Account Successfully Created", Snackbar.LENGTH_SHORT).setAction("OK"){}
                banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                banner.show()
            }
            else {
                if(!allowedPassword) {
                    // password must have been invalid
                    val reason = "Password does not meet criteria"
                    view.findViewById<TextView>(R.id.create_account_password_error_message).text = reason
                    Log.d("Create Account", "Failed to create account ($reason)")
                } else {
                    view.findViewById<TextView>(R.id.create_account_password_error_message).text = ""
                }

                if(!allowedUsername){
                    val reason = if(userNameTaken) "Username is taken" else "Invalid username"
                    view.findViewById<TextView>(R.id.create_account_username_error_message).text = reason
                    Log.d("Create Account", "Failed to create account ($reason)")
                } else {
                    view.findViewById<TextView>(R.id.create_account_username_error_message).text = ""
                }
            }
        }

        val backButton = view.findViewById<Button>(R.id.create_account_back_button)
        backButton.setOnClickListener {
            Log.d("Create Account", "Back Button Clicked")
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun setDescriptions(view: View) {
        val passwordTextView = view.findViewById<TextView>(R.id.create_account_password_description)
        passwordTextView.text = Html.fromHtml("""
            Password must meet the following<br/>
            <ul>
                <li>At least ${LoginManager.MIN_PASSWORD_LENGTH} characters</li>
                <li>At least one uppercase letter</li>
                <li>At least one number</li>
            </ul>
            """.trimIndent(), Html.FROM_HTML_MODE_COMPACT)

        val userNameTextView = view.findViewById<TextView>(R.id.create_account_username_description)
        userNameTextView.text = Html.fromHtml("""
            Username must meet the following<br/>
            <ul>
                <li>At least ${LoginManager.MIN_USERNAME_LENGTH} characters</li>
                <li>Only alpha-numeric characters</li>
            </ul>
            """.trimIndent(), Html.FROM_HTML_MODE_COMPACT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment CreateAccountFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAccountFragment().apply {}
    }
}
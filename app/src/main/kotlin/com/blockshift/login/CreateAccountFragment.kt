package com.blockshift.login

import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.blockshift.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        // inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_create_account, container, false)

        // set descriptions for username / password
        setDescriptions(view)

        val usernameEditText = view.findViewById<EditText>(R.id.create_account_username)
        val passwordEditText = view.findViewById<EditText>(R.id.create_account_password)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.create_account_confirm_password)
        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)
        var confirmPasswordErrorText = view.findViewById<TextView>(R.id.create_account_confirm_password_error_message)
        val usernameErrorText = view.findViewById<TextView>(R.id.create_account_username_error_message)
        val passwordErrorText = view.findViewById<TextView>(R.id.create_account_password_error_message)

        // set action to be taken on create account button click
        createAccountButton.setOnClickListener {

            // launch a coroutine to attempt creating a new account
            CoroutineScope(Dispatchers.Main).launch {
                view.findViewById<EditText>(R.id.create_account_username)

                // get username and password from text input fields
                val desiredUsername = view.findViewById<EditText>(R.id.create_account_username).text.toString()
                val desiredPassword = passwordEditText.text.toString()
                val confirmedPassword = confirmPasswordEditText.text.toString()

                // get login manager to try adding the user
                LoginManager.tryAddUser(desiredUsername, desiredPassword, confirmedPassword, {
                    accountCreationResult ->
                    when(accountCreationResult) {
                        AccountCreationResult.SUCCESS -> {
                            // go back to start screen
                            parentFragmentManager.popBackStack()

                            Log.d("Create Account", "Account successfully created")
                            showBasicBanner(view, "Account Successfully Created", "OK", Snackbar.LENGTH_SHORT)
                        }
                        AccountCreationResult.INVALID_USERNAME -> {
                            Log.d("Create Account", "Failed to create account (${getString(R.string.username_invalid_error)})")
                        }
                        AccountCreationResult.INVALID_PASSWORD -> {
                            Log.d("Create Account", "Failed to create account (${getString(R.string.password_invalid_error)})")
                        }
                        AccountCreationResult.USERNAME_TAKEN -> {
                            usernameErrorText.text = getString(R.string.username_taken_error)
                            Log.d("Create Account", "Failed to create account (${getString(R.string.username_taken_error)})")
                        }
                        AccountCreationResult.PASSWORD_MISMATCH -> {
                            Log.d("Create Account", "Failed to create account (${getString(R.string.confirm_password_mismatch_error)})")
                        }
                    }
                }, {
                    exception ->
                    Log.e("Account Creation", "Exception was thrown during account creation", exception)
                    showBasicBanner(view, "Error Connecting to Server", "OK", Snackbar.LENGTH_LONG)
                })
            }
        }

        val backButton = view.findViewById<Button>(R.id.create_account_back_button)
        backButton.setOnClickListener {
            Log.d("Create Account", "Back Button Clicked")
            parentFragmentManager.popBackStack()
        }

        usernameEditText.addTextChangedListener { text ->
            // if username is invalid after text change, display error message
            if(LoginManager.isValidUsername(text.toString())) {
                usernameErrorText.text = ""
            } else {
                usernameErrorText.text = getString(R.string.username_invalid_error)
            }
        }

        passwordEditText.addTextChangedListener { text ->
            // if password is invalid after text change, display error message
            if(LoginManager.isValidPassword(text.toString())) {
                passwordErrorText.text = ""
            } else {
                passwordErrorText.text = getString(R.string.password_invalid_error)
            }

            // update confirmed password error text if the confirmed password isn't empty
            val confirmPasswordString = confirmPasswordEditText.text.toString()
            if(confirmPasswordString.isEmpty() || text.toString() == confirmPasswordString) {
                confirmPasswordErrorText.text = ""
            } else {
                confirmPasswordErrorText.text = getString(R.string.confirm_password_mismatch_error)
            }
        }

        confirmPasswordEditText.addTextChangedListener { text ->
            // if confirmed password doesn't match password after text change, display error message
            if(text.toString() == passwordEditText.text.toString()) {
                confirmPasswordErrorText.text = ""
            } else {
                confirmPasswordErrorText.text = getString(R.string.confirm_password_mismatch_error)
            }
        }

        return view
    }

    private fun showBasicBanner(view: View, text: String, actionText: String, length: Int) {
        val banner = Snackbar.make(view, text, length).setAction(actionText){}
        banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        banner.show()
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
        fun newInstance() = CreateAccountFragment()
    }
}
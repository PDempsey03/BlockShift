package com.blockshift.login

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.compose.ui.text.toUpperCase
import androidx.core.text.set
import androidx.core.widget.addTextChangedListener
import com.blockshift.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

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
        setInitialTextInputDescriptions(view)

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
                            val reason = getString(R.string.username_invalid_error)
                            Log.d("Create Account", "Failed to create account ($reason)")
                            usernameErrorText.text = reason.uppercase()
                            passwordErrorText.text = ""
                        }
                        AccountCreationResult.INVALID_PASSWORD -> {
                            val reason = getString(R.string.password_invalid_error)
                            Log.d("Create Account", "Failed to create account ($reason)")
                            passwordErrorText.text = reason.uppercase()
                            usernameErrorText.text = ""
                        }
                        AccountCreationResult.USERNAME_TAKEN -> {
                            val reason = getString(R.string.username_taken_error)
                            Log.d("Create Account", "Failed to create account ($reason)")
                            usernameErrorText.text = reason.uppercase()
                            passwordErrorText.text = ""
                        }
                        AccountCreationResult.PASSWORD_MISMATCH -> {
                            // message will already be there for password mismatch, no need put it there again
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
            val username = text.toString()
            updateUsernameDescription(view, LoginManager.usernameMeetsLength(username),
                LoginManager.usernameMeetsOnlyAlphaNumeric(username))

            if(usernameErrorText.text.isNotEmpty()) usernameErrorText.text = ""
        }

        passwordEditText.addTextChangedListener { text ->
            val password = text.toString()
            updatePasswordDescription(view, LoginManager.passwordMeetsLength(password),
                LoginManager.passwordMeetsUppercase(password),
                LoginManager.passwordMeetsDigit(password))

            if(passwordErrorText.text.isNotEmpty()) passwordErrorText.text = ""

            // update confirmed password error text if the confirmed password isn't empty
            val confirmPasswordString = confirmPasswordEditText.text.toString()
            if(confirmPasswordString.isEmpty() || password == confirmPasswordString) {
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

    private fun setInitialTextInputDescriptions(view: View) {
        // before user starts typing, default them to meeting rule, even though they technically don't
        val ruleMet = true
        updateUsernameDescription(view, ruleMet, ruleMet)
        updatePasswordDescription(view, ruleMet, ruleMet, ruleMet)
    }

    private fun updateUsernameDescription(view: View, rule1: Boolean, rule2: Boolean) {
        val userNameTextView = view.findViewById<TextView>(R.id.create_account_username_description)
        val usernameSpannableStringBuilder = SpannableStringBuilder()
        var start = 0
        var end = 0

        usernameSpannableStringBuilder.append("Username must meet the following:\n")

        start = usernameSpannableStringBuilder.length
        usernameSpannableStringBuilder.append("\t\u2022 Between ${LoginManager.MIN_USERNAME_LENGTH} and ${LoginManager.MAX_USERNAME_LENGTH} characters\n")
        end = usernameSpannableStringBuilder.length
        usernameSpannableStringBuilder.setSpan(getColorFromRule(rule1), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        start = usernameSpannableStringBuilder.length
        usernameSpannableStringBuilder.append("\t\u2022 Only alpha-numeric characters\n")
        end = usernameSpannableStringBuilder.length
        usernameSpannableStringBuilder.setSpan(getColorFromRule(rule2), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        userNameTextView.text = usernameSpannableStringBuilder
    }

    private fun updatePasswordDescription(view: View, rule1: Boolean, rule2: Boolean, rule3: Boolean) {
        val passwordTextView = view.findViewById<TextView>(R.id.create_account_password_description)
        val passwordSpannableStringBuilder = SpannableStringBuilder()
        var start = 0
        var end = 0

        passwordSpannableStringBuilder.append("Password must meet the following:\n")

        start = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.append("\t\u2022 Between ${LoginManager.MIN_PASSWORD_LENGTH} and ${LoginManager.MAX_PASSWORD_LENGTH} characters\n")
        end = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.setSpan(getColorFromRule(rule1), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        start = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.append("\t\u2022 At least one uppercase letter\n")
        end = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.setSpan(getColorFromRule(rule2), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        start = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.append("\t\u2022 At least one number")
        end = passwordSpannableStringBuilder.length
        passwordSpannableStringBuilder.setSpan(getColorFromRule(rule3), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        passwordTextView.text = passwordSpannableStringBuilder
    }

    private fun getColorFromRule(rule: Boolean): ForegroundColorSpan {
        return if (rule) ForegroundColorSpan(Color.BLACK) else ForegroundColorSpan(Color.RED)
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
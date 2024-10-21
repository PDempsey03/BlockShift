package com.blockshift.ui.login

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
import androidx.core.widget.addTextChangedListener
import com.blockshift.R
import com.blockshift.model.repositories.AccountCreationResult
import com.blockshift.model.repositories.UserRepository
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

    // state variables for editable text in fragment
    private var validUsername = false
    private var validPassword = false
    private var passwordsMatch = false

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

                // get login manager to try adding the user
                UserRepository.createUser(desiredUsername, desiredPassword, {
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
            updateUsernameState(view, LoginManager.usernameMeetsLength(username),
                LoginManager.usernameMeetsOnlyAlphaNumeric(username)
            )

            if(usernameErrorText.text.isNotEmpty()) usernameErrorText.text = ""

            // after all else is done, update whether create account button should be enabled
            updateCreateButtonActive(view)
        }

        passwordEditText.addTextChangedListener { text ->
            val password = text.toString()
            updatePasswordState(view, LoginManager.passwordMeetsLength(password),
                LoginManager.passwordMeetsUppercase(password),
                LoginManager.passwordMeetsDigit(password)
            )

            // reset any error message after start typing again
            if(passwordErrorText.text.isNotEmpty()) passwordErrorText.text = ""

            // update confirmed password error text if the confirmed password isn't empty
            val confirmPasswordString = confirmPasswordEditText.text.toString()
            updateConfirmPasswordState(view, password == confirmPasswordString)

            // after all else is done, update whether create account button should be enabled
            updateCreateButtonActive(view)
        }

        confirmPasswordEditText.addTextChangedListener { text ->
            // if confirmed password doesn't match password after text change, display error message
            updateConfirmPasswordState(view, text.toString() == passwordEditText.text.toString())

            // after all else is done, update whether create account button should be enabled
            updateCreateButtonActive(view)
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

    private fun updateUsernameState(view: View, rule1: Boolean, rule2: Boolean) {
        this.validUsername = rule1 && rule2
        updateUsernameDescription(view, rule1, rule2)
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

    private fun updatePasswordState(view: View, rule1: Boolean, rule2: Boolean, rule3: Boolean) {
        this.validPassword = rule1 && rule2 && rule3
        updatePasswordDescription(view, rule1, rule2, rule3)
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

    private fun updateConfirmPasswordState(view: View, rule1: Boolean) {
        passwordsMatch = rule1
        val confirmPasswordErrorText = view.findViewById<TextView>(R.id.create_account_confirm_password_error_message)
        if(rule1) {
            confirmPasswordErrorText.text = ""
        } else {
            confirmPasswordErrorText.text = getString(R.string.confirm_password_mismatch_error)
        }
    }

    private fun getColorFromRule(rule: Boolean): ForegroundColorSpan {
        return if (rule) ForegroundColorSpan(Color.BLACK) else ForegroundColorSpan(Color.RED)
    }

    private fun updateCreateButtonActive(view: View) {
        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)

        createAccountButton.isEnabled = validPassword && validPassword && passwordsMatch
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
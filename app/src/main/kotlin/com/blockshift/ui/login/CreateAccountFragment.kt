package com.blockshift.ui.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.blockshift.R
import com.blockshift.model.repositories.AccountCreationResult
import com.blockshift.model.repositories.UserRepository
import com.blockshift.utils.showBasicBanner
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

    private val TAG: String = javaClass.simpleName

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

        val usernameEditText = view.findViewById<EditText>(R.id.create_account_username)
        val passwordEditText = view.findViewById<EditText>(R.id.create_account_password)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.create_account_confirm_password)
        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)
        val usernameErrorText = view.findViewById<TextView>(R.id.create_account_username_error_message)
        val usernameAlert = view.findViewById<ImageView>(R.id.create_account_username_alert)
        val passwordAlert = view.findViewById<ImageView>(R.id.create_account_password_alert)
        val confirmPasswordAlert = view.findViewById<ImageView>(R.id.create_account_confirm_password_alert)
        val backButton = view.findViewById<Button>(R.id.create_account_back_button)

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
                    var failureReason: String? = null
                    when(accountCreationResult) {
                        AccountCreationResult.SUCCESS -> {
                            // go back to start screen
                            parentFragmentManager.popBackStack()

                            view.showBasicBanner(getString(R.string.account_creation_success), "OK", Snackbar.LENGTH_SHORT)
                        }
                        AccountCreationResult.INVALID_USERNAME ->
                            failureReason = getString(R.string.username_invalid_error)
                        AccountCreationResult.INVALID_PASSWORD ->
                            failureReason = getString(R.string.password_invalid_error)
                        AccountCreationResult.USERNAME_TAKEN -> {
                            failureReason = getString(R.string.username_taken_error)

                            // also put explicit message of username taken
                            usernameErrorText.text = failureReason.uppercase()
                            usernameErrorText.visibility = View.VISIBLE
                        }
                    }
                    if(failureReason != null) {
                        view.showBasicBanner(getString(R.string.account_creation_failure) + "($failureReason)", "OK", Snackbar.LENGTH_LONG)
                    }
                }, {  exception ->
                    Log.e(TAG, "Exception was thrown during account creation", exception)
                    view.showBasicBanner(getString(R.string.server_connection_error_message), "OK", Snackbar.LENGTH_LONG)
                })
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        usernameEditText.addTextChangedListener { text ->
            val username = text.toString()

            // update alert message if invalid
            validUsername = LoginManager.isValidUsername(username)

            // update whether alert should be there
            usernameAlert.visibility = if(validUsername) View.GONE else View.VISIBLE

            // update visibility of error message after the user updates username if it was taken
            usernameErrorText.visibility = View.GONE

            // after all else is done, update whether create account button should be enabled
            updateCreateButtonActive(view)
        }

        passwordEditText.addTextChangedListener { text ->
            val password = text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            updatePasswordsState(view, password, confirmPassword)
        }

        confirmPasswordEditText.addTextChangedListener { text ->
            val confirmPassword = text.toString()
            val password = passwordEditText.text.toString()

            updatePasswordsState(view, password, confirmPassword)
        }

        usernameAlert.setOnClickListener {
            buildAlertMessage(
                getString(R.string.username_criteria_title),
                        getString(R.string.username_criteria_one, LoginManager.MIN_USERNAME_LENGTH, LoginManager.MAX_USERNAME_LENGTH)
                        + "\n"
                        + getString(R.string.username_criteria_two)
            )
        }

        passwordAlert.setOnClickListener {
            buildAlertMessage(
                getString(R.string.password_criteria_title),
                getString(R.string.password_criteria_one)
                + "\n"
                + getString(R.string.password_criteria_two)
                + "\n"
                + getString(R.string.password_criteria_three)
            )
        }

        confirmPasswordAlert.setOnClickListener {
            buildAlertMessage(
                getString(R.string.confirm_password_criteria_title),
                getString(R.string.confirm_password_criteria_one)
            )
        }

        return view
    }

    private fun updatePasswordsState(view: View, password: String, confirmPassword: String) {
        // update whether the password is valid
        validPassword = LoginManager.isValidPassword(password)
        passwordsMatch = password == confirmPassword

        // update visibility of the alerts
        view.findViewById<ImageView>(R.id.create_account_password_alert).visibility = if(validPassword) View.GONE else View.VISIBLE
        view.findViewById<ImageView>(R.id.create_account_confirm_password_alert).visibility = if(passwordsMatch) View.GONE else View.VISIBLE

        // after all else is done, update whether create account button should be enabled
        updateCreateButtonActive(view)
    }

    private fun updateCreateButtonActive(view: View) {
        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)

        createAccountButton.isEnabled = validPassword && validPassword && passwordsMatch
    }

    private fun buildAlertMessage(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title )
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
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
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
import java.lang.reflect.Type
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val MIN_PASSWORD_LENGTH = 8
private const val MIN_USERNAME_LENGTH = 4

private val userNamesAndPasswords = mutableMapOf<String, String>()

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
        createAccountButton.setOnClickListener {
            Log.d("Create Account", "Create Account Button Clicked")

            // get username and password from text input fields
            val desiredUsername = view.findViewById<EditText>(R.id.create_account_username).text.toString()
            val desiredPassword = view.findViewById<EditText>(R.id.create_account_password).text.toString() //TODO: hash password

            // Check for valid username
            val userNameTaken = userNamesAndPasswords.containsKey(desiredUsername)
            val allowedUsername = !userNameTaken && isValidUsername(desiredUsername)

            // check for valid password
            val allowedPassword = isValidPassword(desiredPassword)

            if(allowedUsername && allowedPassword) {
                Log.d("Create Account", "Successfully created account")

                // hash password
                val hashedPassword = Base64.encodeToString(hashPassword(desiredPassword).encoded, Base64.NO_WRAP)

                // add username to dict (temporary)
                userNamesAndPasswords[desiredUsername] = hashedPassword

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
                <li>At least $MIN_PASSWORD_LENGTH characters</li>
                <li>At least one uppercase letter</li>
                <li>At least one number</li>
            </ul>
            """.trimIndent(), Html.FROM_HTML_MODE_COMPACT)

        val userNameTextView = view.findViewById<TextView>(R.id.create_account_username_description)
        userNameTextView.text = Html.fromHtml("""
            Username must meet the following<br/>
            <ul>
                <li>At least $MIN_USERNAME_LENGTH characters</li>
                <li>Only alpha-numeric characters</li>
            </ul>
            """.trimIndent(), Html.FROM_HTML_MODE_COMPACT)
    }

    private fun isValidUsername(username: String): Boolean{
        return username.length > MIN_USERNAME_LENGTH
                && username.all{it.isLetterOrDigit()}
    }

    private fun isValidPassword(password: String): Boolean {
        // check length
        if(password.length < MIN_PASSWORD_LENGTH) return false

        var containsDigit = false
        var containsUppercaseLetter = false

        // check contains at least one of each special character, digit, and capital letter
        password.forEach {
            if(it.isLetter() && it.isUpperCase()) containsUppercaseLetter = true
            if(it.isDigit()) containsDigit = true
        }

        return containsDigit && containsUppercaseLetter
    }

    // functionality from https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/
    private fun hashPassword(password: String): SecretKey {
        val hashFunction = "PBKDF2WithHmacSha1"
        val hashLength = 256
        val iterationCount = 2048
        val salt = "#B!S@h#i#f#t@*" // TODO: fix the salt
        val factory = SecretKeyFactory.getInstance(hashFunction)
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterationCount, hashLength)
        return factory.generateSecret(spec)
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
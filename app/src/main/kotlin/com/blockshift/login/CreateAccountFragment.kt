package com.blockshift.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.blockshift.R
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val temp1 = "param1"
private const val temp2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateAccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(temp1)
            param2 = it.getString(temp2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_create_account, container, false)

        val createAccountButton = view.findViewById<Button>(R.id.create_account_button)
        createAccountButton.setOnClickListener {
            Log.d("Create Account", "Create Account Button Clicked")

            // temp checking
            var userNamesAndPasswords = mutableMapOf(
                "Patrick" to "Dempsey1!",
                "Michael" to "Labib1!",
                "Jackson" to "Hoyt1!"
            )

            // get username and password from text input fields
            val desiredUsername = view.findViewById<EditText>(R.id.create_account_username).text.toString()
            val desiredPassword = view.findViewById<EditText>(R.id.create_account_password).text.toString() //TODO: hash password

            // perform logic to check against stored usernames / passwords
            var allowedUsername = !userNamesAndPasswords.containsKey(desiredUsername)
            var allowedPassword = isValidPassword(desiredPassword)

            if(allowedUsername && allowedPassword) {
                Log.d("Create Account", "Successfully created account")
                parentFragmentManager.popBackStack()

                // add username to dict (temporary)
                userNamesAndPasswords[desiredUsername] = desiredPassword

                // Show a banner saying the account was successfully created
                val banner = Snackbar.make(view, "Account Successfully Created", Snackbar.LENGTH_SHORT).setAction("OK"){}
                banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                banner.show()
            }
            else {
                val reason = if(allowedUsername) "Invalid Password" else "Invalid Username"

                Log.d("Create Account", "Failed to create account {$reason}")

                val banner = Snackbar.make(view, reason, Snackbar.LENGTH_LONG).setAction("OK"){}
                banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                banner.show()
            }
        }

        val backButton = view.findViewById<Button>(R.id.create_account_back_button)
        backButton.setOnClickListener {
            Log.d("Create Account", "Back Button Clicked")
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun isValidPassword(password: String): Boolean {
        //val specialCharacters = "!@#\$%^&*()_+\\-=;':\"\\\\|,.<>/?}][{"
        //val minLength = 8

        // pattern must be {minLength} long, containing 1 special character, uppercase letter, and number
        //val pattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[$specialCharacters]).{$minLength,}$"

        //val passwordRegex = Regex(pattern)

        return true//passwordRegex.matches(password)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateAccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(temp1, param1)
                    putString(temp2, param2)
                }
            }
    }
}
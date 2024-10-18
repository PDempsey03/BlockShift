package com.blockshift.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.login.LoginFragment
import com.blockshift.login.UserViewModel
import androidx.fragment.app.activityViewModels
import com.blockshift.repositories.UserData

/**
 * A simple [Fragment] subclass.
 * Use the [AccountSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountSettingsFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account_settings, container, false)

        // make sure to observe the user data for changes
        userViewModel.currentUser.observe(viewLifecycleOwner) { newUserData ->
            updateUserName(view, newUserData)
            updateDisplayName(view, newUserData)
        }

        val initialUserData = userViewModel.currentUser.value
        if (initialUserData != null) {
            updateUserName(view, initialUserData)
            updateDisplayName(view, initialUserData)
        } else {
            Log.d("TAG", "USER DATA IS SOMEHOW NULL on initial load")
        }

        return view
    }

    private fun updateUserName(view: View, userData: UserData?) {
        if(userData == null) return
        val displayNameEditText = view.findViewById<TextView>(R.id.account_settings_username)
        displayNameEditText.text = userData.username
    }

    private fun updateDisplayName(view: View, userData: UserData?) {
        if(userData == null) return
        val displayNameEditText = view.findViewById<EditText>(R.id.account_settings_displayname)
        displayNameEditText.setText(userData.displayname)
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
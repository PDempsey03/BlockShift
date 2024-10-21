package com.blockshift.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.blockshift.R
import com.blockshift.ui.login.LoginFragment
import com.blockshift.model.login.UserViewModel
import androidx.fragment.app.activityViewModels
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserRepository
import com.blockshift.ui.login.LoginManager
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass.
 * Use the [AccountSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountSettingsFragment : Fragment() {

    private var currentDisplayName = ""
    private var displayNameMatch = true
    private var displayNameValid = true
    private val userViewModel: UserViewModel by activityViewModels()
    private val TAG: String = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account_settings, container, false)

        // make sure to observe the user data for changes
        userViewModel.currentUser.observe(viewLifecycleOwner) { newUserData ->
            updateUserData(view, newUserData)
        }

        val initialUserData = userViewModel.currentUser.value
        if (initialUserData != null) {
            updateUserData(view, initialUserData)
        }

        val displayNameEditText = view.findViewById<EditText>(R.id.account_settings_displayname)
        displayNameEditText.addTextChangedListener { text ->
            val textString = text.toString()
            displayNameMatch = textString == currentDisplayName
            displayNameValid = LoginManager.isValidDisplayName(textString)
            updateValidDisplayName(view)
        }

        val displayNameErrorImage = view.findViewById<ImageView>(R.id.account_settings_Display_Name_Error)
        displayNameErrorImage.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Display Name Criteria")
                .setMessage("- Must be ${LoginManager.MIN_DISPLAY_NAME_LENGTH} - ${LoginManager.MAX_DISPLAY_NAME_LENGTH} characters long." +
                        "\n- Contain only alpha-numeric characters, or spaces.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        val updateDisplayNameButton = view.findViewById<Button>(R.id.account_settings_update_displayname_button)
        updateDisplayNameButton.setOnClickListener {
            // perform last minute check for valid display name update
            val userData = userViewModel.currentUser.value
            if(!displayNameMatch && displayNameValid && userData != null) {
                val newDisplayName = displayNameEditText.text.toString()
                val username = userData.username
                UserRepository.updateUserDisplayName(username, newDisplayName, { success ->
                    showBasicBanner(view,
                        if(success) "Display Name successfully updated"
                        else "Display Name could not be updated",
                        "OK", Snackbar.LENGTH_SHORT)
                }, { exception ->
                    Log.e(TAG, "Could not update display name, error connecting to server", exception)
                    showBasicBanner(view, "Error connecting to server, could not update display name", "OK", Snackbar.LENGTH_SHORT)
                })
            }
        }

        return view
    }

    private fun updateUserData(view: View, userData: UserData?) {
        if(userData == null) return

        currentDisplayName = userData.displayname

        // update display name
        val displayNameEditText = view.findViewById<EditText>(R.id.account_settings_displayname)
        displayNameEditText.setText(userData.displayname)

        // update username
        val userNameTextView= view.findViewById<TextView>(R.id.account_settings_username)
        userNameTextView.text = userData.username
    }

    private fun updateValidDisplayName(view: View) {
        val buttonDisplayNameButton = view.findViewById<Button>(R.id.account_settings_update_displayname_button)
        val displayNameErrorImage = view.findViewById<ImageView>(R.id.account_settings_Display_Name_Error)
        buttonDisplayNameButton.isEnabled = !displayNameMatch && displayNameValid
        displayNameErrorImage.visibility = if(displayNameValid) View.GONE else View.VISIBLE
    }

    private fun showBasicBanner(view: View, text: String, actionText: String, length: Int) {
        val banner = Snackbar.make(view, text, length).setAction(actionText){}
        banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        banner.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LoginFragment.
         */
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
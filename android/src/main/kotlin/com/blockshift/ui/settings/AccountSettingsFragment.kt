package com.blockshift.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.blockshift.ui.login.LoginFragment
import com.blockshift.model.login.UserViewModel
import androidx.fragment.app.activityViewModels
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserRepository
import com.blockshift.ui.login.LoginActivity
import com.blockshift.ui.login.LoginManager
import com.blockshift.utils.buildAlertMessage
import com.blockshift.utils.showBasicBanner
import com.gdxblkshkt.R
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
    private var validNewPassword = false
    private var confirmNewPasswordMatches = false
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

        val displayNameErrorImage = view.findViewById<ImageView>(R.id.account_settings_display_name_error)
        displayNameErrorImage.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.display_name_criteria_title))
                .setMessage(
                    "- " + getString(R.string.display_name_criteria_one, LoginManager.MIN_DISPLAY_NAME_LENGTH, LoginManager.MAX_DISPLAY_NAME_LENGTH)
                    + "\n- " + getString(R.string.display_name_criteria_two)
                )
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
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
                    view.showBasicBanner(if(success) getString(R.string.display_name_update_success)
                        else getString(R.string.display_name_update_failure),
                        getString(R.string.ok), Snackbar.LENGTH_SHORT)
                }, { exception ->
                    Log.e(TAG, "Could not update display name, error connecting to server", exception)
                    view.showBasicBanner(getString(R.string.server_connection_error_message), getString(R.string.ok), Snackbar.LENGTH_SHORT)
                })
            }
        }

        val changePasswordButton = view.findViewById<Button>(R.id.account_settings_change_password_button)
        val changePasswordCardView = view.findViewById<CardView>(R.id.change_password_card_view)
        changePasswordButton.setOnClickListener {
            // update the layout so the button is gone, and the change password layout is visible
            updatePasswordChangeView(true, changePasswordCardView, changePasswordButton)
        }

        val oldPasswordEditText = view.findViewById<EditText>(R.id.account_settings_old_password)
        val newPasswordEditText = view.findViewById<EditText>(R.id.account_settings_new_password)
        val confirmNewPasswordEditText = view.findViewById<EditText>(R.id.account_settings_confirm_new_password)

        oldPasswordEditText.addTextChangedListener { text ->
            val newPasswordString = newPasswordEditText.text.toString()
            val confirmNewPasswordString = confirmNewPasswordEditText.text.toString()
            val oldPasswordString = text.toString()

            // update error icons
            updatePasswordsState(view, oldPasswordString, newPasswordString, confirmNewPasswordString)
        }

        newPasswordEditText.addTextChangedListener { text ->
            val newPasswordString = text.toString()
            val confirmNewPasswordString = confirmNewPasswordEditText.text.toString()
            val oldPasswordString = oldPasswordEditText.text.toString()

            // update error icons
            updatePasswordsState(view, oldPasswordString, newPasswordString, confirmNewPasswordString)
        }

        confirmNewPasswordEditText.addTextChangedListener { text ->
            val newPasswordString = newPasswordEditText.text.toString()
            val confirmNewPasswordString = text.toString()
            val oldPasswordString = oldPasswordEditText.text.toString()

            // update error icons
            updatePasswordsState(view, oldPasswordString, newPasswordString, confirmNewPasswordString)
        }

        val newPasswordAlert = view.findViewById<ImageView>(R.id.account_settings_new_password_alert)
        newPasswordAlert.setOnClickListener {
            buildAlertMessage(
                getString(R.string.password_criteria_title),
                "- " + getString(R.string.password_criteria_one, LoginManager.MIN_PASSWORD_LENGTH, LoginManager.MAX_PASSWORD_LENGTH)
                        + "\n- "
                        + getString(R.string.password_criteria_two)
                        + "\n- "
                        + getString(R.string.password_criteria_three)
                        + "\n- "
                        + getString(R.string.password_criteria_four)
            )
        }

        val confirmNewPasswordAlert = view.findViewById<ImageView>(R.id.account_settings_confirm_new_password_alert)
        confirmNewPasswordAlert.setOnClickListener {
            buildAlertMessage(
                getString(R.string.confirm_password_criteria_title),
                "- " + getString(R.string.confirm_password_criteria_one)
            )
        }

        val cancelPasswordChangeButton = view.findViewById<Button>(R.id.account_settings_cancel_change_password)
        cancelPasswordChangeButton.setOnClickListener {
            // reverse changes of the original update password button
            updatePasswordChangeView(false, changePasswordCardView, changePasswordButton)
            clearPasswordChangeText(oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText)
        }

        val confirmPasswordChangeButton = view.findViewById<Button>(R.id.account_settings_confirm_change_password_button)
        confirmPasswordChangeButton.setOnClickListener {
            val userData = userViewModel.currentUser.value ?: return@setOnClickListener

            UserRepository.updateUserPassword(userData.username, oldPasswordEditText.text.toString(), newPasswordEditText.text.toString(),
                {success ->
                if(success) {
                    view.showBasicBanner(getString(R.string.password_update_success), getString(R.string.ok), Snackbar.LENGTH_LONG)
                    updatePasswordChangeView(false, changePasswordCardView, changePasswordButton)
                    clearPasswordChangeText(oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText)
                } else {
                    view.showBasicBanner(getString(R.string.password_update_fail), getString(R.string.ok), Snackbar.LENGTH_LONG)
                }
            }, {exception ->
                Log.e(TAG, "Error updating user password", exception)
                view.showBasicBanner(getString(R.string.server_connection_error_message), getString(R.string.ok), Snackbar.LENGTH_SHORT)
            })
        }

        val logoutButton = view.findViewById<Button>(R.id.account_settings_logout_button)
        logoutButton.setOnClickListener {

            // remove and potential remember me information
            LoginManager.unregisterLocalAuthToken(requireContext())

            // return to the login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }

        val deleteAccountButton = view.findViewById<Button>(R.id.account_settings_Delete_Account_button)
        deleteAccountButton.setOnClickListener {
            showConfirmDeleteDialog(view)
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
        val displayNameErrorImage = view.findViewById<ImageView>(R.id.account_settings_display_name_error)
        buttonDisplayNameButton.isEnabled = !displayNameMatch && displayNameValid
        displayNameErrorImage.visibility = if(displayNameValid) View.GONE else View.VISIBLE
    }

    private fun updatePasswordsState(view: View, oldPassword: String, newPassword: String, confirmNewPassword: String) {
        // update whether the password is valid
        validNewPassword = LoginManager.isValidPassword(newPassword) && oldPassword != newPassword
        confirmNewPasswordMatches = newPassword == confirmNewPassword

        // update visibility of the alerts
        view.findViewById<ImageView>(R.id.account_settings_new_password_alert).visibility = if(validNewPassword) View.GONE else View.VISIBLE
        view.findViewById<ImageView>(R.id.account_settings_confirm_new_password_alert).visibility = if(confirmNewPasswordMatches) View.GONE else View.VISIBLE

        // after all else is done, update whether the change password confirm button should be enabled
        updatePasswordUpdateButton(view)
    }

    private fun updatePasswordUpdateButton(view: View) {
        val updatePasswordButton = view.findViewById<Button>(R.id.account_settings_confirm_change_password_button)

        updatePasswordButton.isEnabled = validNewPassword && confirmNewPasswordMatches
    }

    private fun updatePasswordChangeView(showPasswordChangeView: Boolean, changePasswordCardView: CardView, changePasswordButton: Button) {
        changePasswordCardView.visibility = if(showPasswordChangeView) View.VISIBLE else View.GONE
        changePasswordButton.visibility = if(showPasswordChangeView) View.GONE else View.VISIBLE
    }

    private fun clearPasswordChangeText(oldPasswordEditText: EditText, newPasswordEditText: EditText, confirmNewPasswordEditText: EditText) {
        oldPasswordEditText.setText("")
        newPasswordEditText.setText("")
        confirmNewPasswordEditText.setText("")
    }

    private fun showConfirmDeleteDialog(view: View) {
        // inflate the account deletion dialogue box to new view
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialogue_confirm_delete_account, null)

        // put the dialogue alert in the view
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)

        // Create the dialog instance
        val alertDialog = dialogBuilder.create()

        // get references to the clickable elements
        val checkBox = dialogView.findViewById<CheckBox>(R.id.delete_account_checkbox)
        val confirmButton = dialogView.findViewById<Button>(R.id.delete_account_confirm_button)

        // only enable confirm button when the box is checked
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            confirmButton.isEnabled = isChecked
        }

        confirmButton.setOnClickListener {
            val userData = userViewModel.currentUser.value

            // handle case where user data is null
            if(userData == null) {
                Log.e(TAG, "Error deleting account, UserData is null")
                return@setOnClickListener
            }

            UserRepository.deleteUser(userData, { success ->
                if(success) {
                    // go back to login screen
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    view.showBasicBanner(getString(R.string.account_deletion_failure), getString(R.string.ok), Snackbar.LENGTH_SHORT)
                }
            }, { exception ->
                view.showBasicBanner(getString(R.string.server_connection_error_message), getString(R.string.ok), Snackbar.LENGTH_SHORT)
                Log.e(TAG, "Error deleting account", exception)
            })

            alertDialog.dismiss()
        }

        alertDialog.show()
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

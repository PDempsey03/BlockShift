package com.blockshift.utils

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gdxblkshkt.R

fun Fragment.buildAlertMessage(title: String, message: String) {
    AlertDialog.Builder(requireContext())
        .setTitle(title )
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}

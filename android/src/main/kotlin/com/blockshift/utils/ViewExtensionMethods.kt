package com.blockshift.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showBasicBanner(text: String, actionText: String, length: Int) {
    val banner = Snackbar.make(this, text, length).setAction(actionText){}
    banner.animationMode = Snackbar.ANIMATION_MODE_SLIDE
    banner.show()
}
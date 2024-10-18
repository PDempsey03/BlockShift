package com.blockshift.model.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.blockshift.model.repositories.UserData

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val currentUser: MutableLiveData<UserData> by lazy {
        MutableLiveData<UserData>()
    }
}
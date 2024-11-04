package com.blockshift.model.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OfflineUserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OfflineUserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = OfflineUserRepository(userDao)
    }

    fun addUser(user:User) {
        viewModelScope.launch {
            repository.addUser(user)
        }
    }
}
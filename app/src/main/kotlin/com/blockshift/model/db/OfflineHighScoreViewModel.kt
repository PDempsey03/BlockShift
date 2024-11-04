package com.blockshift.model.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineHighScoreViewModel(application: Application) : AndroidViewModel(application) {
    val readByUsername: LiveData<List<HighScore>>
    private val repository: OfflineHighScoreRepository

    init {
                val highScoreDao = AppDatabase.getDatabase(application).highScoreDao()
                repository = OfflineHighScoreRepository(highScoreDao,"lcl")
                readByUsername = repository.readByUsername
    }

    fun addHighScore(score: HighScore) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHighScore(score)
        }
    }
}
package com.blockshift.model.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class OfflineHighScoreViewModel(application: Application) : AndroidViewModel(application){
    val readByUsername: LiveData<List<HighScore>>
    private val repository: HighScoreRepository

    init {
        val highScoreDao = AppDatabase.getDatabase(application).highScoreDao()
        repository = HighScoreRepository(highScoreDao,"lcl")
        readByUsername = repository.readByUsername
    }


}

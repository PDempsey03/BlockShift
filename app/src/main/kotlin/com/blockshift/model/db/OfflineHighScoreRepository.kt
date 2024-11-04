package com.blockshift.model.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class OfflineHighScoreRepository(private val highScoreDao: HighScoreDao, private val username:String) {

    val readByUsername: LiveData<List<HighScore>> = highScoreDao.getHighScores(username)

    suspend fun addHighScore(score: HighScore) {
        highScoreDao.insert(score)
    }
}
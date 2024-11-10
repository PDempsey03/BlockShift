package com.blockshift.model.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class OfflineHighScoreRepository(private val highScoreDao: HighScoreDao, private val username:String) {

    val readByUsername: LiveData<List<HighScore>> = highScoreDao.getHighScores(username)

    fun getHighScoreByLevel(level: Int): LiveData<HighScore> {
        return highScoreDao.getHighScoreByLevel(username, level)
    }

    suspend fun addHighScore(score: HighScore) {
        highScoreDao.insert(score)
    }

    suspend fun updateHighScore(score: HighScore) {
        highScoreDao.update(score)
    }
}

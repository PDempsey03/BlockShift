package com.blockshift.model.db

import androidx.lifecycle.LiveData

class HighScoreRepository(private val highScoreDao: HighScoreDao, private val username:String) {

    val readByUsername: LiveData<List<HighScore>> = highScoreDao.getHighScores(username)
}
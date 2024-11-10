package com.blockshift.model.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM highScore WHERE username = :username")
    fun getHighScores(username: String): LiveData<List<HighScore>>

    @Query("SELECT * FROM highScore WHERE username = :username AND levelID = :levelID")
    fun getHighScoreByLevel(username: String, levelID: Int): LiveData<HighScore>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(highScore: HighScore)

    @Delete
    suspend fun delete(highScore: HighScore)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(highScore: HighScore)
}

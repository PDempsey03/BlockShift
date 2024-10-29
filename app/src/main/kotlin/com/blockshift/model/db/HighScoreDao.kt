package com.blockshift.model.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM user WHERE username = :username")
    fun getHighScores(username: String): LiveData<List<HighScore>>

    @Update
    suspend fun update(highScore: HighScore)

    @Delete
    suspend fun delete(highScore: HighScore)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highScore: HighScore)
}
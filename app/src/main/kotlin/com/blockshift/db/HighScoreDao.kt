package com.blockshift.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM user WHERE userID = :userID AND levelID = :levelID")
    fun getHighScore(userID: Int, levelID: Int): HighScore?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}
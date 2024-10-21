package com.blockshift.model.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Dao

@Dao
interface LevelDao {
    @Query("SELECT * FROM user WHERE levelID = :levelID")
    fun findByID(levelID: Int): Level

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}
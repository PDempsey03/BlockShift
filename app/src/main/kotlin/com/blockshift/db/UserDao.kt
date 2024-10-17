package com.blockshift.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE userID = :userID")
    fun findByID(userID: Int): User

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}
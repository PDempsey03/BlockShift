package com.blockshift.db
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "user")
data class User (
    @PrimaryKey(autoGenerate = false) val userID: Int,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "level1HS") val level1HS: Int,
    @ColumnInfo(name = "level2HS") val level2HS: Int,
    @ColumnInfo(name = "level3HS") val level3HS: Int,
    @ColumnInfo(name = "level4HS") val level4HS: Int,
    @ColumnInfo(name = "level5HS") val level5HS: Int,
    @ColumnInfo(name = "level6HS") val level6HS: Int,
    @ColumnInfo(name = "level7HS") val level7HS: Int,
    @ColumnInfo(name = "level8HS") val level8HS: Int,
    @ColumnInfo(name = "level9HS") val level9HS: Int,
    @ColumnInfo(name = "level10HS") val level10HS: Int,
    @ColumnInfo(name = "level11HS") val level11HS: Int,
    @ColumnInfo(name = "level12HS") val level12HS: Int
)
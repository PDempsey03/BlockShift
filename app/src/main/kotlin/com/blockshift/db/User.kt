package com.blockshift.db
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "user")
data class User (
    @PrimaryKey(autoGenerate = false) val username: String,
    @ColumnInfo(name = "displayName") val displayName: String
)
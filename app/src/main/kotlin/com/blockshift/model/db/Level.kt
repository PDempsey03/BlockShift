package com.blockshift.model.db

import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.ColumnInfo

@Entity(tableName = "level")
data class Level (
    @PrimaryKey(autoGenerate = true) val levelID: Int,
    @ColumnInfo(name = "level_name") val levelName: String
)
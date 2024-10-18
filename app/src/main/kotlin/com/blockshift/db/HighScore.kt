package com.blockshift.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ColumnInfo

@Entity(tableName = "highScore", foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = arrayOf("userID"),
    childColumns = arrayOf("userID"),
    onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
        entity = Level::class,
        parentColumns = arrayOf("levelID"),
        childColumns = arrayOf("levelID"),
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["userID","levelID"]
)
data class HighScore(
    val userID: Int,
    val levelID: Int,
    @ColumnInfo(name = "score") val score: Int
)
package com.blockshift.model.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ColumnInfo

@Entity(tableName = "highScore", foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = arrayOf("username"),
    childColumns = arrayOf("username"),
    onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["username","levelID"]
)
data class HighScore(
    val username: String,
    val levelID: Int,
    @ColumnInfo(name = "distance") val distance: Int,
    @ColumnInfo(name = "time") val time: Int,
    @ColumnInfo(name = "moves") val moves: Int
)

fun scoreIsZero(score:HighScore): Boolean {
    return score.distance == 0 && score.time == 0 && score.moves == 0
}
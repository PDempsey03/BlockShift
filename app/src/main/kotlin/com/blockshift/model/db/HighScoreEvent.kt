package com.blockshift.model.db

interface HighScoreEvent {
    object SaveHighScore: HighScoreEvent
    data class SetDistance(val distance:Int):HighScoreEvent
    data class SetTime(val time:Int):HighScoreEvent
    data class SetMoves(val moves:Int):HighScoreEvent
    data class DeleteScore(val score:HighScore):HighScoreEvent
}
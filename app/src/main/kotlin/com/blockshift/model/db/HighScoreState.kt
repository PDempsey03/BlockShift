package com.blockshift.model.db

data class HighScoreState (
    val scores: List<HighScore> = emptyList()
)
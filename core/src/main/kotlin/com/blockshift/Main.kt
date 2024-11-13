package com.blockshift

import com.badlogic.gdx.Game

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main(private val level: Int) : Game() {
    private lateinit var gameScreen: GameScreen
    private lateinit var exitCallback: ExitCallback

    // callback for android launcher
    interface ExitCallback {
        fun returnToLevelSelect(moves: Int? = null, time: Long? = null, distance: Int? = null, level: Int? = null)
    }

    fun setCallback(callback: ExitCallback) {
        exitCallback = callback
    }

    override fun create() {
        gameScreen = GameScreen(level)
        setScreen(gameScreen)
    }

    override fun resize(width: Int, height: Int) {
        gameScreen.resize(width, height)
    }

    override fun render() {
        if (gameScreen.isFinished == 1) {
            exitCallback.returnToLevelSelect(gameScreen.moves, gameScreen.time, gameScreen.distance, gameScreen.toLoad)
        } else if (gameScreen.isFinished == -1) {
            exitCallback.returnToLevelSelect()
        }
        super.render()
    }

    override fun dispose() {
        gameScreen.dispose()
    }
}

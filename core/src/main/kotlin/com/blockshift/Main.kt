package com.blockshift

import com.badlogic.gdx.Game

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main(private val level: Int) : Game() {
    private lateinit var gameScreen: GameScreen
    private lateinit var winCallback: WinCallback

    // callback for android launcher
    interface WinCallback {
        fun returnToLevelSelect(moves: Int, level: Int)
    }

    fun setCallback(callback: WinCallback) {
        winCallback = callback
    }

    override fun create() {
        gameScreen = GameScreen(level)
        setScreen(gameScreen)
    }

    override fun resize(width: Int, height: Int) {
        gameScreen.resize(width, height)
    }

    override fun render() {
        if (gameScreen.isFinished) {
            winCallback.returnToLevelSelect(gameScreen.moves, gameScreen.toLoad)
        }
        super.render()
    }

    override fun dispose() {
        gameScreen.dispose()
    }
}

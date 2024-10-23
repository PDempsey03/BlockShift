package com.blockshift

import com.badlogic.gdx.Game

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : Game() {
    lateinit var gameScreen: GameScreen

    override fun create() {
        gameScreen = GameScreen()
        setScreen(gameScreen)
    }

    override fun resize(width: Int, height: Int) {
        gameScreen.resize(width, height)
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        gameScreen.dispose()
    }
}

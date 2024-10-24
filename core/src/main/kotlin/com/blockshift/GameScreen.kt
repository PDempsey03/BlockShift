package com.blockshift

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.blockshift.BigBlock.Companion.DIR.*

class GameScreen : Screen {
    // screen
    private var camera: Camera
    private var viewport: Viewport

    // graphics
    private var batch: SpriteBatch
    private var background: Texture
    private var playerTexture: Texture
    private var blockTexture: Texture

    // timing

    // world paramters
    companion object {
        val blocksPerRow = 6
        val blocksPerCol = 8
        val blockWidth = 16f
        val screenWidth = (blockWidth * blocksPerRow).toFloat()
        val screenHeight = (blockWidth * blocksPerCol).toFloat()
    }

    // game objects
    private var level: Level
    private var player: Player
//    private var block: Block
    private var bigB: BigBlock
    private var bigB2: BigBlock
    private var bigBlocks: Set<BigBlock>

    init {
        camera = OrthographicCamera()
        viewport = StretchViewport(screenWidth, screenHeight, camera)

        // load textures
        background = Texture("flat_brown.png")
        playerTexture = Texture("player.png")
        blockTexture = Texture("block.png")

        // set up game objects
        player = Player(1, blockWidth, playerTexture)
        bigB = BigBlock(1, setOf(
            Block(3, blockWidth, blockTexture),
            Block(4, blockWidth, blockTexture),
            Block(5, blockWidth, blockTexture),
            ))
        bigB2 = BigBlock(2, setOf(
            Block(7, blockWidth, blockTexture),
            Block(8, blockWidth, blockTexture),

            ))

        bigBlocks = setOf(bigB, bigB2)

        level = Level(player, bigBlocks)

        batch = SpriteBatch()
    }

    override fun render(delta: Float) {
        batch.begin()

        detectInput()

        // static bg
        renderBg(delta)

        // blocks
        level.draw(batch)

        batch.end()
    }

    private fun detectInput() {
        // keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            level.slide(LEFT)
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            level.slide(RIGHT)
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            level.slide(UP)
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            level.slide(DOWN)
        }

        // touch input

    }

    private fun renderBg(delta: Float) {
        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        batch.setProjectionMatrix(camera.combined)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun dispose() {

    }
}

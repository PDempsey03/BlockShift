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
import com.blockshift.Block.Companion.DIR.*

class GameScreen : Screen {
    // world parameters
    companion object {
        const val TILES_PER_ROW = 5
        const val TILES_PER_COL = 5
        const val TILE_WIDTH = 16f
        const val SCREEN_WIDTH = (TILE_WIDTH * TILES_PER_ROW).toFloat()
        const val SCREEN_HEIGHT = (TILE_WIDTH * TILES_PER_COL).toFloat()
    }

    // screen
    private var camera: Camera = OrthographicCamera()
    private var viewport: Viewport = StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera)

    // graphics
    private var batch: SpriteBatch = SpriteBatch()

    // load textures
    private var background: Texture = Texture("flat_brown.png")
    private var playerTexture: Texture = Texture("player.png")
    private var blockTexture: Texture = Texture("Block.png")

    // timing

    // game objects
    private var player: Block = Block(0, setOf(Tile(11, TILE_WIDTH, false, playerTexture)))
    private var b1: Block = Block(1, setOf
        (
        Tile(6, TILE_WIDTH, false, blockTexture),
        Tile(7, TILE_WIDTH, false, blockTexture),
        Tile(15, TILE_WIDTH, false, blockTexture),
        Tile(16, TILE_WIDTH, false, blockTexture),
        Tile(17, TILE_WIDTH, false, blockTexture),
        Tile(12, TILE_WIDTH, false, blockTexture),
        ))
    private var blocks: Set<Block> = setOf(player, b1)
    private var level: Level = Level(blocks)

    override fun render(delta: Float) {
        batch.begin()

        resetMoveFlags()
        detectInput()

        // static bg
        renderBg(delta)

        // tiles
        level.draw(batch)

        batch.end()
    }

    private fun resetMoveFlags() {
        for (block in blocks) {
            block.ignoredIds = mutableSetOf(block.id)
            block.hasMoved = false
        }
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
//        TODO("holdable")
    }

    private fun renderBg(delta: Float) {
        batch.draw(background, 0f, 0f, SCREEN_WIDTH, SCREEN_HEIGHT)
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

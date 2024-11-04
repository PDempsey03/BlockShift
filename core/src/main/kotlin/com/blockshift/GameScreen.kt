package com.blockshift

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.blockshift.Block.Companion.DIR.*

class GameScreen : Screen {
    // world parameters
    companion object {
        const val TILES_PER_ROW = 4
        const val TILES_PER_COL = 9
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
    private var blockTexture: Texture = Texture("block.png")

    // tilt
    private val tiltDelay: Float = 0.8f
    private var delay: Float = 0f
    private val threshold: Float = 0f

    // game objects
    private var player: Block = Block(0, setOf(Tile(0, TILE_WIDTH, playerTexture)), false)
    private var b1: Block = Block(1, setOf
        (
        Tile(6, TILE_WIDTH, blockTexture),
        Tile(7, TILE_WIDTH, blockTexture),
        Tile(11, TILE_WIDTH, blockTexture),
        Tile(13, TILE_WIDTH, blockTexture),
        Tile(14, TILE_WIDTH, blockTexture),
        Tile(15, TILE_WIDTH, blockTexture),
        ))
    private var blocks: Set<Block> = setOf(player, b1)
    private var level: Level = Level(blocks)

    override fun render(delta: Float) {
        batch.begin()
        delay -= delta

        resetFlags()
        detectInput(delta)

        // static bg
        renderBg(delta)

        // tiles
        level.draw(batch)

        batch.end()
    }

    private fun resetFlags() {
        for (block in blocks) {
            block.ignoredIds = mutableSetOf(block.id)
            block.hasActions = true
            block.hasMoved = false
            block.isTouched = false
        }
    }

    private fun detectInput(delta: Float) {
        // touch input
        if (Gdx.input.isTouched) {
            val idx = getIdx(Gdx.input.getX(), Gdx.input.getY())
            setTouched(idx)
        }

        // tilt input
        if (delay < 0) {
            tilt(delta)
        }

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
    }

    private fun tilt(delta: Float) {
        val degX = Gdx.input.gyroscopeX * delta * MathUtils.radiansToDegrees
        val degY = Gdx.input.gyroscopeY * delta * MathUtils.radiansToDegrees

        if (degX > threshold) {
            delay = tiltDelay
            level.slide(DOWN)
        } else if (degX < -threshold) {
            delay = tiltDelay
            level.slide(UP)
        } else if (degY > threshold) {
            delay = tiltDelay
            level.slide(RIGHT)
        } else if (degY < -threshold) {
            delay = tiltDelay
            level.slide(LEFT)
        }
    }

    private fun setTouched(idx: Int) {
        for (block in blocks) {
            if (block.isHoldable) {
                for (tile in block.tiles) {
                    if (tile.idx == idx) {
                        block.isTouched = true
                        block.hasActions = false
                    }
                }
            }
        }
    }

    private fun getIdx(x: Int, y: Int): Int {
        // compute width based on resolution
        val tileHeight = Gdx.graphics.height / TILES_PER_COL
        val tileWidth = Gdx.graphics.width / TILES_PER_ROW

        val col = x / tileWidth
        val row = y / tileHeight

        val idx = col + (row * TILES_PER_ROW)

        return idx
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

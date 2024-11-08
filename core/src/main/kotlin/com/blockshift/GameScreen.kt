package com.blockshift

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.blockshift.Block.Companion.DIR.*
import kotlin.math.sqrt

class GameScreen(toLoad:Int) : Screen {
    // world parameters
    companion object {
        const val TILE_WIDTH: Float = 16f
        var TILES_PER_ROW: Int = 6 // width
        var TILES_PER_COL: Int = 9 // height
        var SCREEN_WIDTH: Float = 0f
        var SCREEN_HEIGHT: Float = 0f
    }

    // screen
    private lateinit var camera: Camera
    private lateinit var viewport: Viewport

    // graphics
    private var batch: SpriteBatch = SpriteBatch()

    // load textures
    private var background: Texture = Texture("flat_brown.png")
    private var playerTexture: Texture = Texture("player.png")
    private var blockTexture: Texture = Texture("block.png")

    // tilt
    private var basePitch: Float = 0f
    private var baseRoll: Float = 0f
    private val tiltDelay: Float = .5f
    private var delay: Float = 0f
    private val threshold: Float = 10f

    // score
    var isFinished: Boolean = false
    var moves: Int = 0

    private var level: Level = loadLevel(toLoad)

    private fun loadLevel(levelID: Int): Level {
        // update before loading any level
        updateBaseOrientation()

        val file = Gdx.files.internal("BlockShiftTiledProject/Levels/Level$levelID.tmx")
        val xmlReader = XmlReader()
        val rootElement = xmlReader.parse(file)

        // handle tmx file parsing
        val mapAttributes = rootElement.attributes

        TILES_PER_ROW = mapAttributes["width"].toInt()
        TILES_PER_COL = mapAttributes["height"].toInt()

        // now that dimensions of world are known, can load the camera/viewport settings
        loadCameraSettings()

        // gets all the entities in the map
        val layers = rootElement.getChildrenByName("layer")

        val blocks = mutableSetOf<Block>()

        // first parse the player
        val playerLayer = layers.single { it.attributes["name"] == "Player" }
        val playerLocation = playerLayer
            .getChildByName("data")
            .text
            .split(",")
            .map { it.trim().toInt() }
            .withIndex()
            .single { it.value != 0 }
            .index

        // add player to world
        blocks.add(Block(0, setOf(Tile(playerLocation, TILE_WIDTH, playerTexture)), Color.WHITE, false))

        // remove player from list so the rest are standard blocks
        layers.removeValue(playerLayer, true)

        // loop over the remaining layers which are block layers
        for(i in 0 until layers.size) {
            val layer = layers[i]

            val blockLocations = layer
                .getChildByName("data") // get the data inside the layer
                .text // convert the layer object to string format
                .split(",") // split the data based on csv format
                .map { it.trim().toInt() } // convert items to actual ints
                .withIndex() // get index with each value
                .filter { it.value != 0 } // keep positions with non-zero components as a tile is there
                .map { it.index } // only need to keep the index since its value is irrelevant

            // get the color of the block
            val colorString = layer.attributes["tintcolor"]
            val color = Color(Color.valueOf(colorString))

            // generate tiles for the block
            val tiles = mutableSetOf<Tile>()
            for(idx in blockLocations) {
                tiles.add(Tile(idx, TILE_WIDTH, blockTexture))
            }

            // handle edge case of possibly no tiles being part of the block
            if(tiles.isNotEmpty())
                blocks.add(Block(i + 1, tiles, color))
        }
        return Level(blocks)
    }

    private fun loadCameraSettings() {
        camera = OrthographicCamera()
        SCREEN_WIDTH = (TILE_WIDTH * TILES_PER_ROW)
        SCREEN_HEIGHT = (TILE_WIDTH * TILES_PER_COL)
        viewport = StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera)
    }

    override fun render(delta: Float) {
        batch.begin()

        updateDelay(delta)

        resetFlags()
        detectInput(delta)

        checkLevelComplete()

        // static bg
        renderBg(delta)

        // tiles
        level.draw(batch)

        batch.end()
    }

    private fun checkLevelComplete() {
        for (player in level.blockMap[0]!!.tiles) {
            if (level.bottomBoundary.contains(player.idx)) {
                moves = level.moves
                isFinished = true
            }
        }
    }

    private fun resetFlags() {
        for (block in level.blocks) {
            block.ignoredIds = mutableSetOf(block.id)
            block.hasActions = true
            block.hasMoved = false
            block.isTouched = false
        }
    }

    private fun updateDelay(delta: Float) {
        if(delay > 0) {
            delay -= delta
            if(delay <= 0) {
                Gdx.app.log("GAME SCREEN", "Allowing tilt input again")
                // once the delay is <= 0, moving will be allowed again so set the current orientation
                updateBaseOrientation()
            }
        }
    }

    private fun detectInput(delta: Float) {
        // touch input
        if (Gdx.input.isTouched) {
            val idx = getIdx(Gdx.input.getX(), Gdx.input.getY())
            setTouched(idx)
        }

        // tilt input
        if (delay <= 0) {
            checkTilt(delta)
        }

        // keyboard input
        when {
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> level.slide(LEFT)
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> level.slide(RIGHT)
            Gdx.input.isKeyPressed(Input.Keys.UP) -> level.slide(UP)
            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> level.slide(DOWN)
        }
    }

    private fun checkTilt(delta: Float) {
        val accelX = Gdx.input.accelerometerX
        val accelY = Gdx.input.accelerometerY
        val accelZ = Gdx.input.accelerometerZ

        // calculate pitch and roll of the phone
        val currentPitch = MathUtils.atan2(-accelX, sqrt(accelY * accelY + accelZ * accelZ)) * MathUtils.radiansToDegrees
        val currentRoll = MathUtils.atan2(accelY, accelZ) * MathUtils.radiansToDegrees

        val relativePitch = basePitch - currentPitch
        val relativeRoll = baseRoll - currentRoll

        when {
            (relativePitch > threshold) -> tilt(LEFT)
            (relativePitch < -threshold) -> tilt(RIGHT)
            (relativeRoll > threshold) -> tilt(UP)
            (relativeRoll < -threshold) -> tilt(DOWN)
        }
    }

    private fun tilt(direction: Block.Companion.DIR) {
        Gdx.app.log("GAME SCREEN", "MOVING ${direction.name}")
        delay = tiltDelay
        level.slide(direction)
    }

    // source: https://wiki.dfrobot.com/How_to_Use_a_Three-Axis_Accelerometer_for_Tilt_Sensing
    private fun updateBaseOrientation() {
        val accelX = Gdx.input.accelerometerX
        val accelY = Gdx.input.accelerometerY
        val accelZ = Gdx.input.accelerometerZ

        basePitch = MathUtils.atan2(-accelX, sqrt(accelY * accelY + accelZ * accelZ)) * MathUtils.radiansToDegrees
        baseRoll = MathUtils.atan2(accelY, accelZ) * MathUtils.radiansToDegrees

        Gdx.app.log("GAME SCREEN", "basePitch = $basePitch and baseRoll = $baseRoll")
    }

    private fun setTouched(idx: Int) {
        for (block in level.blocks) {
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
        updateBaseOrientation()
    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun dispose() {

    }
}

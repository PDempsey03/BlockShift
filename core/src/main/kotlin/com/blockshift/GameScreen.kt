package com.blockshift

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.blockshift.Block.Companion.DIR.*
import kotlin.math.sqrt

class GameScreen(val toLoad:Int) : Screen {
    // world parameters
    companion object {
        const val TILE_WIDTH: Float = 16f
        var TILES_PER_ROW: Int = 6 // width
        var TILES_PER_COL: Int = 9 // height
        var SCREEN_WIDTH: Float = 0f
        var SCREEN_HEIGHT: Float = 0f
        var graphicsNeedUpdated = true
    }

    // screen
    private lateinit var camera: Camera
    private lateinit var viewport: Viewport

    // graphics
    // size should roughly correspond to batch.maxSpritesInBatch
    private val batch: SpriteBatch = SpriteBatch(100)
    private val renderQueue = Queue<Sprite>()

    // gui
    val guiHeight = 8f // height of menu bar, win glow
    val guiPadding = 2f // padding around buttons
    val buttonWidth = 8f
    val buttonHeight = 4f

    // asset manager
    private val assetManager = AssetManager()
    private val assets = Assets(assetManager)

    // tilt
    private val threshold: Float = 10f
    private val tiltDelay: Float = .5f
    private var delay: Float = 0f
    private var basePitch: Float = 0f
    private var baseRoll: Float = 0f

    // score
    private var hasMadeFirstMove = false
    var isFinished: Int = 0
    var moves: Int = 0
    var time: Long = 0
    var distance: Int = 0

    val profiler: GLProfiler = GLProfiler(Gdx.graphics)

//    init { profiler.enable() }

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
        blocks.add(Block(0, setOf(Tile(playerLocation, TILE_WIDTH, assets.textures.playerTexture)), Color.WHITE, false))

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
                tiles.add(Tile(idx, TILE_WIDTH, assets.textures.blockTexture))
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
        SCREEN_HEIGHT = (TILE_WIDTH * TILES_PER_COL) + guiHeight
        viewport = StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(Color.BLACK)

        updateDelay(delta)
        detectInput()

        if (graphicsNeedUpdated) {
            queueBg(renderQueue) // static bg
            queueGui(renderQueue) // menu bar buttons, win glow
            level.queue(renderQueue) // tiles

            batch.begin()
            for (sprite in renderQueue) {
                sprite.draw(batch)
            }
            batch.end()
        }

        checkLevelComplete()
        resetFlags()
        renderQueue.clear()

        if (profiler.isEnabled) {
            Gdx.app.log("bindings", "${profiler.textureBindings}")
            profiler.reset()
        }
    }

    private fun queueGui(queue: Queue<Sprite>) {
        val menuBg = Sprite(assets.textures.menuTexture)
        val winGlow = Sprite(assets.textures.winGlow)
        val backButton = Sprite(assets.textures.backTexture)

        menuBg.setOrigin(0f, 0f)
        winGlow.setOrigin(0f, 0f)
        backButton.setOrigin(0f, 0f)

        backButton.setScale(buttonWidth / backButton.width, buttonHeight / backButton.height)
        menuBg.setScale(SCREEN_WIDTH / menuBg.width, guiHeight / menuBg.height)
        winGlow.setScale(SCREEN_WIDTH / winGlow.width, guiHeight / winGlow.height)

        menuBg.setPosition(0f, SCREEN_HEIGHT - guiHeight)
        backButton.setPosition(guiPadding, SCREEN_HEIGHT - guiHeight + guiPadding)

        menuBg.setColor(Color.valueOf("E89815"))
        winGlow.setColor(Color.valueOf("EFDB6A"))

        queue.addLast(menuBg)
        queue.addLast(winGlow)
        queue.addLast(backButton)
    }

    private fun checkLevelComplete() {
        for (player in level.blockMap[0]!!.tiles) {
            if (level.bottomBoundary.contains(player.idx)) {
                // update appropriate score values now that level is complete
                distance = level.blockMap[0]!!.distanceTraveled // distance traveled by player
                time = TimeUtils.timeSinceMillis(time) // time since level begun
                println("Moves = $moves\nDistance = $distance\nTime = $time")
                isFinished = 1
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

    private fun detectInput() {
        // touch input
        if (Gdx.input.isTouched) {
            val menuRegion = ((guiHeight.toFloat() / SCREEN_HEIGHT) * viewport.screenHeight).toInt()
            val backButtonRegion = ((buttonWidth + guiPadding) / SCREEN_WIDTH) * viewport.screenWidth

            val x = Gdx.input.x
            var y = Gdx.input.y

            if (y > menuRegion) {
                y -= menuRegion
                val idx = getIdx(menuRegion, x, y)
                setTouched(idx)
            } else if (y < menuRegion && x < backButtonRegion){
                isFinished = -1
            }
        }

        // tilt input
        if (delay <= 0) {
            checkTilt()
        }

        /*
         * keyboard input
         * isKeyPressed will send many events while held (wrong score)
         * isKeyJustPressed will send a single event, but requires an
         * InputProcessor and InputMultiplexer
         * https://stackoverflow.com/a/20048019
         */
        when {
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> slideLevel(LEFT)
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> slideLevel(RIGHT)
            Gdx.input.isKeyPressed(Input.Keys.UP) -> slideLevel(UP)
            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> slideLevel(DOWN)
        }
    }

    private fun checkTilt() {
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
        slideLevel(direction)
    }

    private fun slideLevel(direction: Block.Companion.DIR) {
        if(!hasMadeFirstMove) {
            hasMadeFirstMove = true
            time = TimeUtils.millis() // record starting time once first move is made
        }
        level.slide(direction)
        moves++
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

    private fun getIdx(menuRegion: Int, x: Int, y: Int): Int {
        // compute width based on resolution
        val tileHeight = (viewport.screenHeight - menuRegion) / TILES_PER_COL
        val tileWidth = viewport.screenWidth / TILES_PER_ROW

        val row = (y / tileHeight)
        val col = (x / tileWidth)

        return (col + (row * TILES_PER_ROW))
    }

    private fun queueBg(queue: Queue<Sprite>) {
        val bg = Sprite(assets.textures.background)
        bg.setScale( SCREEN_WIDTH / bg.width)
        bg.setOrigin(0f, 0f)
        queue.addLast(bg)
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
        batch.dispose()
        assetManager.dispose()
    }
}

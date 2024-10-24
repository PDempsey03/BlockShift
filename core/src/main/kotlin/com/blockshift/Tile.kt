package com.blockshift

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.blockshift.GameScreen.Companion.blocksPerCol
import com.blockshift.GameScreen.Companion.blocksPerRow

// Tile is a movable 1x1 square object
abstract class Tile (var idx: Int, val width: Float, val tileTexture: Texture) {
    var x: Float = 0f
    var y: Float = 0f
    var isMoving: Boolean = false
    var wasMoved: Boolean = false
    abstract val isHoldable: Boolean
    val id: Int = 0

    init {
        update(idx)
    }

    open fun update(idx: Int) {
        this.idx = idx
        y = (blocksPerCol - 1 - (idx / blocksPerRow)) * width
        x = (idx % blocksPerRow) * width
    }

    open fun draw(batch: Batch) {
        batch.draw(tileTexture, x, y, width, width)
    }
}


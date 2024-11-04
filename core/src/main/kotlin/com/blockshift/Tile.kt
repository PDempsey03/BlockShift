package com.blockshift

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.blockshift.GameScreen.Companion.TILES_PER_COL
import com.blockshift.GameScreen.Companion.TILES_PER_ROW

// Tile is a movable 1x1 square object
// idx is a value between 0 and TILES_PER_COL * TILES_PER_ROW - 1
// where 0 is the top-left and the max index is the bottom-right:
// 0 1 2
// 3 . .
// . . n
class Tile (var idx: Int, val width: Float, val tileTexture: Texture) {
    var x: Float = 0f
    var y: Float = 0f
    var id: Int = 0

    init {
        update(idx)
    }

    fun update(idx: Int) {
        this.idx = idx
        y = (TILES_PER_COL - 1 - (idx / TILES_PER_ROW)) * width
        x = (idx % TILES_PER_ROW) * width
    }

    fun draw(batch: Batch) {
        batch.draw(tileTexture, x, y, width, width)
    }
}


package com.blockshift

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Queue
import com.blockshift.GameScreen.Companion.TILES_PER_COL
import com.blockshift.GameScreen.Companion.TILES_PER_ROW

// Tile is a movable 1x1 square object
// idx is a value between 0 and TILES_PER_COL * TILES_PER_ROW - 1
// where 0 is the top-left and the max index is the bottom-right:
// 0 1 2
// 3 . .
// . . n
class Tile (var idx: Int, val width: Float, tileTexture: TextureRegion) {
    private var x = 0.0f
    private var y = 0.0f
    var id = 0
    val sprite = Sprite(tileTexture)

    init {
        update(idx)
        sprite.setOrigin(0f, 0f)
    }

    fun update(idx: Int) {
        this.idx = idx
        y = (TILES_PER_COL - 1 - (idx / TILES_PER_ROW)) * width
        x = (idx % TILES_PER_ROW) * width
    }

    fun queue(queue: Queue<Sprite>, color: Color) {
        sprite.setColor(color)
        sprite.setScale(width / sprite.width)
        sprite.setPosition(x, y)
        queue.addLast(sprite)
    }
}


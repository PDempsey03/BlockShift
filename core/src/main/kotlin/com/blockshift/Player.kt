package com.blockshift

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch

// Player is a Tile that is not holdable
class Player : Tile {
    constructor(idx: Int, width: Float, tileTexture: Texture) : super(idx, width, tileTexture)

    override val isHoldable = false
    var moves = 0

    override fun update(idx: Int) {
        super.update(idx)
        // blink
    }

    override fun draw(batch: Batch) {
        super.draw(batch)
        // draw eyes
    }
}

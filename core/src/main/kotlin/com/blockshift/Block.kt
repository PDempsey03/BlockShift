package com.blockshift

import com.badlogic.gdx.graphics.Texture

// Block is a Tile that is holdable
class Block : Tile {
    constructor(idx: Int, width: Float, tileTexture: Texture) : super(idx, width, tileTexture)

    override val isHoldable = true
}

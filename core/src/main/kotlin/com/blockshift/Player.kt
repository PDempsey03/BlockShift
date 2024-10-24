package com.blockshift

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.blockshift.BigBlock.Companion.DIR
import com.blockshift.BigBlock.Companion.offset
import com.blockshift.GameScreen.Companion.blocksPerCol
import com.blockshift.GameScreen.Companion.blocksPerRow

// Player is a Tile that is not holdable
class Player : Tile {
    constructor(idx: Int, width: Float, tileTexture: Texture) : super(idx, width, tileTexture)

    override val isHoldable = false
    var moves = 0

    // return true if each Block element will stay in bounds after moving one tile toward dir
    fun canMove(dir: DIR, level: Level): Boolean {
        if (level.willCauseCollision(id, idx + offset(dir))) {
            return false
        }

        // compute indices of row and col which block lies on
        val colOffset = idx % blocksPerRow
        val rowOffset = idx - (idx % blocksPerRow)
        val colInBounds = List(blocksPerCol) { it * blocksPerRow + colOffset }
        val rowInBounds = List(blocksPerRow) { it + rowOffset }

        // bounds form a cross
        val bounds = setOf(colInBounds, rowInBounds).flatten()

        // if moving block one tile will not stay in bounds then Player cannot move
        return idx + offset(dir) in bounds
    }

    // move Player until collision occurs
    fun slide(dir: DIR, level: Level) {
        while (canMove(dir, level)) {
            this.move(dir)
        }
    }

    // move Player by one tile
    fun move(dir: DIR) {
        this.update(offset(dir))
    }

    override fun update(offset: Int) {
        super.update(offset + idx)
        // blink
    }

    override fun draw(batch: Batch) {
        super.draw(batch)
        // draw eyes
    }
}

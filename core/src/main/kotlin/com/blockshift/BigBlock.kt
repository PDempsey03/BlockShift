package com.blockshift

import com.badlogic.gdx.graphics.g2d.Batch
import com.blockshift.GameScreen.Companion.blocksPerCol
import com.blockshift.GameScreen.Companion.blocksPerRow

// BigBlock is a collection of one or more Blocks
class BigBlock(val blocks: Set<Block>) {
    companion object {
        enum class DIR {LEFT, RIGHT, UP, DOWN}
    }

    // return offset corresponding to a move by one tile
    fun offset(dir: DIR): Int {
        return when (dir) {
            DIR.LEFT -> -1
            DIR.RIGHT -> 1
            DIR.UP -> -blocksPerRow
            DIR.DOWN -> blocksPerRow
        }
    }

    // return true if each Block element will stay in bounds after moving one tile toward dir
    fun canMove(dir: DIR): Boolean {
        for (block in blocks) {
            // compute indices of row and col which block lies on
            val colOffset = block.idx % blocksPerRow
            val rowOffset = block.idx - (block.idx % blocksPerRow)
            val colInBounds = List(blocksPerCol) { it * blocksPerRow + colOffset }
            val rowInBounds = List(blocksPerRow) { it + rowOffset }

            // bounds forms a cross
            val bounds = setOf(colInBounds, rowInBounds).flatten()

            // if moving block one tile will not stay in bounds then BigBlock cannot move
            if (block.idx + offset(dir) !in bounds) {
                return false
            }
        }
        return true
    }

    // move a BigBlock until collision occurs
    fun slide(dir: DIR, level: Level) {
        while (!level.hasCollision() && canMove(dir)) {
            this.move(dir)
        }
    }

    // move a BigBlock by one tile
    fun move(dir: DIR) {
        this.update(offset(dir))
    }

    // update each Block of BigBlock such that block.idx = block.idx + offset
    fun update(offset: Int) {
        for (block in blocks) {
            block.update(block.idx + offset)
        }
    }

    fun draw(batch: Batch) {
        for (block in blocks) {
            block.draw(batch)
        }
    }
}

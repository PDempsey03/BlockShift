package com.blockshift

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Queue
import com.blockshift.Block.Companion.DIR
import com.blockshift.Block.Companion.DIR.*
import com.blockshift.GameScreen.Companion.TILES_PER_COL
import com.blockshift.GameScreen.Companion.TILES_PER_ROW

class Level(val blocks: Set<Block>) {
    // map of id to Tiles with that id
    val blockMap = blocks.map{ it -> Pair(it.id, it)}.toMap()

    // compute indices of screen boundary for collision checking
    val leftBoundary = ((0 .. TILES_PER_COL).map{ it -> it * TILES_PER_ROW}).toSet()
    val rightBoundary = ((1 .. TILES_PER_COL).map{ it -> it * TILES_PER_ROW - 1}).toSet()
    val topBoundary: Set<Int> = (0 .. TILES_PER_ROW - 1).toSet()
    val bottomBoundary = ((TILES_PER_ROW * TILES_PER_COL) - TILES_PER_ROW .. TILES_PER_ROW * TILES_PER_COL).toSet()

    // call move on each Block while a Block has actions remaining
    fun slide(dir: DIR) {
        GameScreen.graphicsNeedUpdated = true
        while (actionsRemaining()) {
            for (block in blocks) {
                block.move(dir, this)
            }
        }
    }

    // check whether every Block took its turn
    fun actionsRemaining(): Boolean {
        for (block in blocks) {
            if (block.hasActions) {
                return true
            }
        }
        return false
    }

    // check whether indices overlap any Block in the level
    // returns id of Block which causes collision, -1 otherwise
    fun willCauseCollision(indices: Set<Int>, ignoredIds: Set<Int>): Int {
        for (otherBlock in blocks) {
            if (!ignoredIds.contains(otherBlock.id)) {
                val otherIndices = otherBlock.tiles.map { it -> it.idx }.toSet()
                if (!indices.intersect(otherIndices).isEmpty()) {
                    return otherBlock.id
                }
            }
        }
        return -1
    }

    // return true if no index lies on a boundary
    // when dir is in the direction of the boundary
    fun inBounds(indices: Set<Int>, dir: DIR): Boolean {
        return when (dir) {
            LEFT -> leftBoundary.intersect(indices).isEmpty()
            RIGHT -> rightBoundary.intersect(indices).isEmpty()
            UP -> topBoundary.intersect(indices).isEmpty()
            DOWN -> bottomBoundary.intersect(indices).isEmpty()
        }
    }

    fun queue(queue: Queue<Sprite>) {
        for (block in blocks) {
            block.queue(queue)
        }
    }
}

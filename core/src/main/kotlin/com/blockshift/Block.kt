package com.blockshift

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Queue
import com.blockshift.GameScreen.Companion.TILES_PER_ROW

// Block is a collection of one or more Tiles
class Block(val id: Int, val tiles: Set<Tile>, val color: Color, val isHoldable: Boolean = true, var hasMoved: Boolean = false) {
    var hasActions: Boolean = true
    var isTouched: Boolean = false
    var distanceTraveled: Int = 0

    // collision checks will treat ids in ignoredIds as part of the Block
    var ignoredIds = mutableSetOf(id)

    init {
        for (tile in tiles) {
            tile.id = id
        }
    }

    // expose DIR to classes like Level, GameScreen
    companion object {
        enum class DIR {LEFT, RIGHT, UP, DOWN}

        // return offset corresponding to a move by one tile
        fun offset(dir: DIR): Int {
            return when (dir) {
                DIR.LEFT -> -1
                DIR.RIGHT -> 1
                DIR.UP -> -TILES_PER_ROW
                DIR.DOWN -> TILES_PER_ROW
            }
        }
    }

    // return true if each Tile in Block will not collide other
    // Tiles or exceed bounds after moving one tile toward dir
    fun canMove(dir: DIR, level: Level): Boolean {
        var indices = mutableSetOf<Int>()
        var extendedTransformedIndices = mutableSetOf<Int>()
        var baseTransformedIndices = level.blockMap[id]!!.tiles
            .map{tile -> tile.idx + offset(dir)}
            .toMutableSet()

        for (id in ignoredIds) {
            indices = indices
                .plus(level.blockMap[id]!!.tiles
                    .map{tile -> tile.idx}
                    .toMutableSet()) as MutableSet<Int>
            extendedTransformedIndices = extendedTransformedIndices
                .plus(level.blockMap[id]!!.tiles
                    .map{tile -> tile.idx + offset(dir)}
                    .toMutableSet()) as MutableSet<Int>
        }

        // Block on boundary collision
        if (!level.inBounds(indices, dir)) {
            hasMoved = true
            hasActions = false
            return false
        }

        // Block on Block collision
        var id = level.willCauseCollision(baseTransformedIndices, mutableSetOf(id))

        if (id > -1) {
            id = level.willCauseCollision(extendedTransformedIndices, ignoredIds)
        }

        if (id > -1) {
            val obstacle = level.blockMap[id]
            if (obstacle!!.hasMoved && !obstacle.isTouched) {
                ignoredIds.add(id)
                return false
            } else {
                hasMoved = true
                hasActions = false
                return false
            }
        }

        // no collision
        hasMoved = true
        return true
    }

    // move a Block by one tile if no collisions detected
    fun move(dir: DIR, level: Level) {
        if (!isTouched && canMove(dir, level)) {
            this.update(offset(dir))
            distanceTraveled++
        }
    }

    // add offset to each Tile in Block
    fun update(offset: Int) {
        for (tile in tiles) {
            tile.update(offset + tile.idx)
        }
    }

    fun queue(queue: Queue<Sprite>) {
        for (tile in tiles) {
            tile.queue(queue, color)
        }
    }
}

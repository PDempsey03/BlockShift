package com.blockshift

import com.badlogic.gdx.graphics.g2d.Batch

class Level(val player: Player, val bigBlocks: Set<BigBlock>) {
    var tileMap: Map<Int, Collection<Tile>> = mapOf(player.id to listOf(player))

    init {
        val blockMap =  bigBlocks
            .map { it -> it.id to it.blocks }
        tileMap = tileMap.plus(blockMap)
    }

    fun slide(dir: BigBlock.Companion.DIR) {
        player.slide(dir, this)
        for (bigBlock in bigBlocks) {
            bigBlock.slide(dir, this)
        }
    }

    // check for overlapping tiles
    fun willCauseCollision(id: Int, i: Int): Boolean {
        for (tileGroup in tileMap) {
            if (id != tileGroup.key) {
                val tiles = tileGroup.value
                for (tile in tiles) {
                    if (tile.idx == i) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun draw(batch: Batch) {
        player.draw(batch)
        for (bigBlock in bigBlocks) {
            bigBlock.draw(batch)
        }
    }
}

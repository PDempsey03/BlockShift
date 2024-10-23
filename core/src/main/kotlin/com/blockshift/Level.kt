package com.blockshift

import com.badlogic.gdx.graphics.g2d.Batch

class Level(val player: Player, val bigBlocks: Set<BigBlock>) {
    val blockIndices =  bigBlocks
        .flatMap { it -> it.blocks }
        .map { it -> it.idx }

    val tileIndices = blockIndices.plus(player.idx)

    fun slide(dir: BigBlock.Companion.DIR) {
//        player.slide(dir, this)
        for (bigBlock in bigBlocks) {
            bigBlock.slide(dir, this)
        }
    }

    // check for overlapping tiles
    fun hasCollision(): Boolean {
        return (tileIndices.size != tileIndices.distinct().size)
    }

    fun draw(batch: Batch) {
        player.draw(batch)
        for (bigBlock in bigBlocks) {
            bigBlock.draw(batch)
        }
    }
}

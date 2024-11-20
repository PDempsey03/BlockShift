package com.blockshift

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureAssets (atlas: TextureAtlas){
    val background: TextureRegion = atlas.findRegion("flat_brown")
    val playerTexture: TextureRegion = atlas.findRegion("player")
    val blockTexture: TextureRegion = atlas.findRegion("block")
    val menuTexture: TextureRegion = atlas.findRegion("menu")
    val winGlow: TextureRegion = atlas.findRegion("glow")
    val backTexture: TextureRegion = atlas.findRegion("back")
}

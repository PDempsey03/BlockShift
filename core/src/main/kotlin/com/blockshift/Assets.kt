package com.blockshift

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas

class Assets (assetManager: AssetManager){
    val textureAtlas = AssetDescriptor<TextureAtlas>("textures.atlas", TextureAtlas::class.java)
    val textures: TextureAssets

    init {
        assetManager.load(textureAtlas)
        assetManager.finishLoading()
        textures = TextureAssets(assetManager.get(textureAtlas))
    }
}

package com.mygdx.mariobros.sprites.tile_objects

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.math.Rectangle
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.scenes.Hud
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario

class Brick(screen: PlayScreen, mapObject: MapObject) : InteractiveTileObject(screen, mapObject) {
    override val tile: TiledMapTile
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    init {
        fixture.userData = this
        setCategoryFiler(MarioBros.BRICK_BIT)
    }

    override fun onHeadHit(mario: Mario) {
        if(mario.marioIsBig) {
            setCategoryFiler(MarioBros.DESTROYED_BIT)
            screen.game.assetManager.get("audio/sounds/breakblock.wav", Sound::class.java).play()
            getCell().tile = null
            Hud.addScore(100)
        } else {
            screen.game.assetManager.get("audio/sounds/bump.wav", Sound::class.java).play()
        }
    }
}
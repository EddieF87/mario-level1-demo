package com.mygdx.mariobros.sprites.tile_objects

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileSet
import com.badlogic.gdx.math.Vector2
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.scenes.Hud
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario
import com.mygdx.mariobros.sprites.items.ItemDef
import com.mygdx.mariobros.sprites.items.Mushroom

class Coin(screen: PlayScreen, val mapObject: MapObject) : InteractiveTileObject(screen, mapObject) {
    override val tile: TiledMapTile
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private val tileSet: TiledMapTileSet = screen.map.tileSets.getTileSet("tileset_gutter")

    init {
        fixture.userData = this
        setCategoryFiler(MarioBros.COIN_BIT)
    }

    override fun onHeadHit(mario: Mario) {
        if(getCell().tile.id != BLANK_COIN) {
            getCell().tile = tileSet.getTile(BLANK_COIN)
            if(mapObject.properties.containsKey("mushroom")) {
                screen.spawnItem(ItemDef(Vector2(body.position.x, body.position.y + 16 / MarioBros.PPM),
                        Mushroom::class.java))
                screen.game.assetManager.get("audio/sounds/powerup_spawn.wav", Sound::class.java).play()
            } else {
                Hud.addScore(200)
                screen.game.assetManager.get("audio/sounds/coin.wav", Sound::class.java).play()
            }
        } else {
            screen.game.assetManager.get("audio/sounds/bump.wav", Sound::class.java).play()
        }
    }

    companion object {
        private const val BLANK_COIN = 28
    }
}
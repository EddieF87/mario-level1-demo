package com.mygdx.mariobros.sprites.tile_objects

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario

abstract class InteractiveTileObject (val screen: PlayScreen, mapObject: MapObject) {

    abstract val tile : TiledMapTile
    val body : Body

    private val bounds = (mapObject as RectangleMapObject).rectangle
    private val bodyDef = BodyDef()
    private val fixtureDef = FixtureDef()
    private val shape = PolygonShape()
    var fixture : Fixture

    init {
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position.set((bounds.x + bounds.width / 2) / MarioBros.PPM,
                (bounds.y + bounds.height / 2) / MarioBros.PPM)
        body = screen.world.createBody(bodyDef)
        shape.setAsBox((bounds.width / 2) / MarioBros.PPM, (bounds.height / 2) / MarioBros.PPM)
        fixtureDef.shape = shape
        fixture = body.createFixture(fixtureDef)
    }

    abstract fun onHeadHit(mario: Mario)

    fun setCategoryFiler(filterBit : Short) {
        val filter = Filter()
        filter.categoryBits = filterBit
        fixture.filterData = filter
    }

    fun getCell() : TiledMapTileLayer.Cell {
        val layer : TiledMapTileLayer = screen.map.layers.get(1) as TiledMapTileLayer
        return layer.getCell((body.position.x * MarioBros.PPM/16).toInt(),
                (body.position.y * MarioBros.PPM/16).toInt())
    }
}
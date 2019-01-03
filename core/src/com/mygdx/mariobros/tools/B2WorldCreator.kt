package com.mygdx.mariobros.tools

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.tile_objects.Brick
import com.mygdx.mariobros.sprites.tile_objects.Coin
import com.mygdx.mariobros.sprites.enemies.Goomba
import com.badlogic.gdx.utils.Array
import com.mygdx.mariobros.sprites.enemies.Enemy
import com.mygdx.mariobros.sprites.enemies.Koopa

class B2WorldCreator(playScreen: PlayScreen) {

    private val world = playScreen.world
    private val map = playScreen.map
    val enemies = Array<Enemy>()

    init {
        val bodyDef = BodyDef()
        val shape = PolygonShape()
        val fixtureDef = FixtureDef()
        var body: Body

        map.layers.get(2).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            val rectangle = mapObject.rectangle
            bodyDef.type = BodyDef.BodyType.StaticBody
            bodyDef.position.set((rectangle.x + rectangle.width / 2) / MarioBros.PPM,
                    (rectangle.y + rectangle.height / 2) / MarioBros.PPM)
            body = world.createBody(bodyDef)
            shape.setAsBox((rectangle.width / 2) / MarioBros.PPM, (rectangle.height / 2) / MarioBros.PPM)
            fixtureDef.shape = shape
            body.createFixture(fixtureDef)
        }
        map.layers.get(3).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            val rectangle = mapObject.rectangle
            bodyDef.type = BodyDef.BodyType.StaticBody
            bodyDef.position.set((rectangle.x + rectangle.width / 2) / MarioBros.PPM,
                    (rectangle.y + rectangle.height / 2) / MarioBros.PPM)
            body = world.createBody(bodyDef)
            shape.setAsBox((rectangle.width / 2) / MarioBros.PPM, (rectangle.height / 2) / MarioBros.PPM)
            fixtureDef.shape = shape
            fixtureDef.filter.categoryBits = MarioBros.OBJECT_BIT
            body.createFixture(fixtureDef)
        }
        map.layers.get(4).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            val rectangle = mapObject.rectangle
            Coin(playScreen, mapObject)
        }
        map.layers.get(5).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            Brick(playScreen, mapObject)
        }
        map.layers.get(6).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            val rectangle = mapObject.rectangle
            enemies.add(Goomba(playScreen, rectangle.x / MarioBros.PPM, rectangle.y / MarioBros.PPM))
        }
        map.layers.get(7).objects.getByType(RectangleMapObject::class.java).forEach { mapObject ->
            val rectangle = mapObject.rectangle
            enemies.add(Koopa(playScreen, rectangle.x / MarioBros.PPM, rectangle.y / MarioBros.PPM))
        }
    }
}

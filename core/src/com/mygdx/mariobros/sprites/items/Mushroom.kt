package com.mygdx.mariobros.sprites.items

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario
import kotlin.experimental.or

class Mushroom(val playScreen: PlayScreen, private val posX: Float, private val posY: Float)
    : Item(playScreen, posX, posY) {


    init {
        setPositionAndBounds()
        defineItem()
        setRegion(playScreen.textureAtlas.findRegion("mushroom"), 0,0,16,16)
    }

    override fun use(mario: Mario) {
        body.linearVelocity.x = 0F
        mario.grow()
        destroy()
    }
    override fun setPositionAndBounds() {
        setPosition(posX, posY)
        setBounds(posX, posY, 16/MarioBros.PPM, 16/MarioBros.PPM)
    }
    override fun defineItem() {
        val bdef = BodyDef()
        bdef.position.set(x, y)
        bdef.type = BodyDef.BodyType.DynamicBody
        body = world.createBody(bdef)

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 4 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.ITEM_BIT

        println("mushroom ${MarioBros.ITEM_BIT} fdef.filter.categoryBits = ${fdef.filter.categoryBits}" )

        fdef.filter.maskBits = MarioBros.MARIO_BIT or MarioBros.OBJECT_BIT or
                MarioBros.GROUND_BIT or MarioBros.COIN_BIT or MarioBros.BRICK_BIT

        fdef.shape = shape
        body.createFixture(fdef).userData = this
        body.linearVelocity = velocity
    }

    override fun update(dt: Float) {
        super.update(dt)
        if (!destroyed) {
            setPosition(body.position.x - width / 2, body.position.y - height / 2)
            velocity.y = body.linearVelocity.y
            body.linearVelocity = velocity
        }
    }
}
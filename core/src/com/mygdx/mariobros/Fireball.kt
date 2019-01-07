package com.mygdx.mariobros

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array
import com.mygdx.mariobros.screens.PlayScreen
import kotlin.experimental.or

class Fireball(val screen: PlayScreen, val posX: Float, val posY: Float, val goRight : Boolean) : Sprite() {

    private var stateTime: Float = 0F
    private val animation : Animation<TextureRegion>
    private val frames = Array<TextureRegion>()
    private lateinit var b2body : Body
    private val world = screen.world
    val fireballMomentum = if(goRight) 1F else -1F
    val velocity = Vector2(fireballMomentum, 0F)
    var destroyed = false

    init {
        val firePos = if(goRight) 16/MarioBros.PPM else -16/MarioBros.PPM
        setPosition(posX + firePos, posY)
        defineFireball()

        for (i in 0..3) {
            frames.add(TextureRegion(screen.textureAtlas.findRegion("fireball"), i * 8, 0, 8, 8))
        }
        animation = Animation(.1f, frames)
        stateTime = 0F
        setBounds(x, y, 16 / MarioBros.PPM, 16 / MarioBros.PPM)
    }

    fun update(dt: Float) {
        stateTime += dt
        if (stateTime > 4) {
            destroyed = true
            world.destroyBody(b2body)
            return
        }

        b2body.linearVelocity = velocity
        setPosition(b2body.position.x - width / 2, b2body.position.y - height / 2)
        setRegion(animation.getKeyFrame(stateTime, true))
    }

    fun defineFireball() {
        val bdef = BodyDef()
        bdef.position.set(x, y)
        bdef.type = BodyDef.BodyType.DynamicBody
        b2body = world.createBody(bdef)

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 3 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.FIREBALL_BIT
        fdef.filter.maskBits = (MarioBros.GROUND_BIT or
                MarioBros.COIN_BIT or
                MarioBros.BRICK_BIT or
                MarioBros.ENEMY_BIT or
                MarioBros.OBJECT_BIT or
                MarioBros.MARIO_BIT)

        fdef.restitution = 1F;
        fdef.friction = 0F;
        fdef.shape = shape
        b2body.createFixture(fdef).userData = this
    }

    fun reverseXVelocity() {
        velocity.x = -velocity.x
    }

    fun bounce() {
        b2body.applyLinearImpulse(Vector2(0F, 6F), b2body.worldCenter, true)
    }

    override fun draw(batch: Batch) {
        if(!destroyed) {
            super.draw(batch)
        }
    }
}
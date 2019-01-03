package com.mygdx.mariobros.sprites.enemies

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario
import kotlin.experimental.or

class Goomba(val screen: PlayScreen, posX: Float, posY: Float) : Enemy(screen, posX, posY) {
    private var stateTime: Float = 0.toFloat()
    private val walkAnimation: Animation<TextureRegion>
    private val frames = Array<TextureRegion>()
    private var setToDestroy: Boolean = false
    private var destroyed: Boolean = false
    private val world = screen.world
    private lateinit var b2body : Body


    init {
        println("goomba init")
        setPosition(posX, posY)
        defineEnemy()

        for (i in 0..1) {
            frames.add(TextureRegion(screen.textureAtlas.findRegion("goomba"), i * 16, 0, 16, 16))
        }
        walkAnimation = Animation(0.4f, frames)
        stateTime = 0F
        setBounds(x, y, 16 / MarioBros.PPM, 16 / MarioBros.PPM)
        setToDestroy = false
        destroyed = false
    }

    override fun update(dt: Float) {
        if(destroyed) return
        stateTime += dt
        if (setToDestroy) {
            world.destroyBody(b2body)
            destroyed = true
            setRegion(TextureRegion(screen.textureAtlas.findRegion("goomba"), 32, 0, 16, 16))
            stateTime = 0f
        } else {
            b2body.linearVelocity = velocity
            setPosition(b2body.position.x - width / 2, b2body.position.y - height / 2)
            setRegion(walkAnimation.getKeyFrame(stateTime, true))
        }
    }

    override fun defineEnemy() {
        val bdef = BodyDef()
        bdef.position.set(x, y)
        bdef.type = BodyDef.BodyType.DynamicBody
        b2body = world.createBody(bdef)
        b2body.isActive = false

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 6 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT
        fdef.filter.maskBits = (MarioBros.GROUND_BIT or
                MarioBros.COIN_BIT or
                MarioBros.BRICK_BIT or
                MarioBros.ENEMY_BIT or
                MarioBros.OBJECT_BIT or
                MarioBros.MARIO_BIT)

        fdef.shape = shape
        b2body.createFixture(fdef).userData = this

        //Create the Head here:
        val head = PolygonShape()
        val vertice = arrayOfNulls<Vector2>(4)
        vertice[0] = Vector2(-5f, 8f).scl(1 / MarioBros.PPM)
        vertice[1] = Vector2(5f, 8f).scl(1 / MarioBros.PPM)
        vertice[2] = Vector2(-3f, 3f).scl(1 / MarioBros.PPM)
        vertice[3] = Vector2(3f, 3f).scl(1 / MarioBros.PPM)
        head.set(vertice)

        fdef.shape = head
        fdef.restitution = 0.5f
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT
        b2body.createFixture(fdef).userData = this
    }

    override fun draw(batch: Batch) {
        if(!destroyed || stateTime < 1) {
            super.draw(batch)
        }
    }

    override fun hitOnHead(mario: Mario) {
        destroyGoomba()
    }

    override fun setActive(isActive : Boolean) : Boolean  {
        if(destroyed) {
            return false
        }
        b2body.isActive = isActive
        return true
    }

    override fun onEnemyHit(enemy: Enemy) {
        if(enemy is Koopa && enemy.currentState == Koopa.State.MOVING_SHELL) {
            println("OUCHIEMAMA!!!")
            destroyGoomba()
        } else {
            reverseVelocity(true, false)
        }
    }

    fun destroyGoomba() {
        setToDestroy = true
        screen.game.assetManager.get("audio/sounds/stomp.wav", Sound::class.java).play()
    }
}
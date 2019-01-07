package com.mygdx.mariobros.sprites.enemies

import com.badlogic.gdx.Input.Keys.K
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.scenes.Hud
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario
import jdk.nashorn.internal.objects.NativeArray.forEach
import kotlin.experimental.or

class Koopa(val screen: PlayScreen, posX: Float, posY: Float) : Enemy(screen, posX, posY) {

    enum class State { WALKING, STANDING_SHELL, MOVING_SHELL, DEAD }
    companion object {
        const val KICK_LEFT_SPEED = -2F
        const val KICK_RIGHT_SPEED = 2F
    }

    private var stateTime: Float = 0F
    var currentState = State.WALKING
    private var previousState = State.WALKING
    private val walkAnimation: Animation<TextureRegion>
    private val frames = Array<TextureRegion>()
    private var setToDestroy: Boolean = false
    private var destroyed: Boolean = false
    private val world = screen.world
    private val shell = TextureRegion(screen.textureAtlas.findRegion("turtle"), 64, 0, 16, 24)
    private lateinit var b2body: Body
    private var deadRotationDegrees = 0F

    init {
        setPosition(posX, posY)
        defineEnemy()

        for (i in 0..1) {
            frames.add(TextureRegion(screen.textureAtlas.findRegion("turtle"), i * 16, 0, 16, 24))
        }
        walkAnimation = Animation(0.2f, frames)

        stateTime = 0F
        setBounds(x, y, 16 / MarioBros.PPM, 24 / MarioBros.PPM)
        setToDestroy = false
        destroyed = false
    }

    override fun update(dt: Float) {
        setRegion(getFrame(dt))
        if (currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING
            velocity.x = 1F
        }
        setPosition(b2body.position.x - width / 2, b2body.position.y - 8 / MarioBros.PPM)
        if (currentState == State.DEAD) {
            deadRotationDegrees += 3
            rotate(deadRotationDegrees)
            if (stateTime > 5 && !destroyed) {
                world.destroyBody(b2body)
                destroyed = true
            }
            return
        }
        b2body.linearVelocity = velocity
    }

    fun getFrame(dt: Float): TextureRegion {
        val region = when (currentState) {
            State.STANDING_SHELL, State.MOVING_SHELL, State.DEAD -> shell
            State.WALKING -> walkAnimation.getKeyFrame(stateTime, true)
        }
        if (velocity.x > 0 && !region.isFlipX) {
            region.flip(true, false)
        }
        if (velocity.x < 0 && region.isFlipX) {
            region.flip(true, false)
        }
        stateTime = if (currentState == previousState) stateTime + dt else 0F
        previousState = currentState
        return region
    }

    override fun hitOnHead(mario: Mario) {
        if (currentState != State.STANDING_SHELL) {
            makeKoopaIntoStandingShell()
        } else {
            val speed = if (mario.x <= this.x) KICK_RIGHT_SPEED else KICK_LEFT_SPEED
            kick(speed)
        }
    }

    fun makeKoopaIntoStandingShell() {
        currentState = State.STANDING_SHELL
        velocity.x = 0F
        screen.game.assetManager.get("audio/sounds/stomp.wav", Sound::class.java).play()
    }

    fun destroyKoopa() {
        currentState = State.DEAD
        val filter = Filter()
        filter.maskBits = MarioBros.NOTHING_BIT

        b2body.fixtureList.forEach { fixture ->
            fixture.filterData = filter
        }
        b2body.applyLinearImpulse(Vector2(0F, 5F), b2body.worldCenter, true)
        Hud.addScore(200)
        screen.game.assetManager.get("audio/sounds/stomp.wav", Sound::class.java).play()
    }

    fun kick(speed: Float) {
        velocity.x = speed
        currentState = State.MOVING_SHELL
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
                MarioBros.MARIO_BIT or
                MarioBros.FIREBALL_BIT)

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

    override fun setActive(isActive: Boolean): Boolean {
        if (destroyed) {
            return false
        }
        b2body.isActive = isActive
        return true
    }

    override fun draw(batch: Batch) {
        if (!destroyed || stateTime < 1) {
            super.draw(batch)
        }
    }

    override fun onEnemyHitByEnemy(enemy: Enemy) {
        if (enemy is Koopa && enemy.currentState == Koopa.State.MOVING_SHELL && currentState != Koopa.State.MOVING_SHELL) {
            destroyKoopa()
        } else {
            reverseVelocity(true, false)
        }
    }

    override fun onEnemyHitByFire() {
        destroyKoopa()
    }
}
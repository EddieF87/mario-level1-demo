package com.mygdx.mariobros.sprites

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.screens.PlayScreen
import kotlin.experimental.or
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.BodyDef
import com.mygdx.mariobros.sprites.enemies.Enemy
import com.mygdx.mariobros.sprites.enemies.Koopa


class Mario(var world: World, val playScreen: PlayScreen)
    : Sprite(playScreen.textureAtlas.findRegion("little_mario")) {

    enum class State { FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD, WIN }

    var currentState = State.STANDING
    var previousState = State.STANDING
    private var runningRight = false
    var marioIsBig = false
    var marioIsDead = false
    var marioWins = false
    var timeToRedefineMario = false
    private var runGrowAnimation = false
    private var timeToDefineBigMario = false
    var stateTimer: Float = 0F
    lateinit var b2Body: Body
    private var marioRun: Animation<TextureRegion>
    private var bigMarioRun: Animation<TextureRegion>
    private var growMario: Animation<TextureRegion>

    private var marioStand = TextureRegion(playScreen.textureAtlas.findRegion("little_mario"),
            0, 0, 16, 16)
    private var marioJump = TextureRegion(playScreen.textureAtlas.findRegion("little_mario"),
            80, 0, 16, 16)
    private var marioDead = TextureRegion(playScreen.textureAtlas.findRegion("little_mario"),
            96, 0, 16, 16)
    private var bigMarioStand = TextureRegion(playScreen.textureAtlas.findRegion("big_mario"),
            0, 0, 16, 32)
    private var bigMarioJump = TextureRegion(playScreen.textureAtlas.findRegion("big_mario"),
            80, 0, 16, 32)

    init {
        val frames = Array<TextureRegion>()
        for (i in 1..3) {
            frames.add(TextureRegion(playScreen.textureAtlas.findRegion("little_mario"), i * 16, 0, 16, 16))
        }
        marioRun = Animation(.1f, frames)
        frames.clear()

        for (i in 1..3) {
            frames.add(TextureRegion(playScreen.textureAtlas.findRegion("big_mario"), i * 16, 0, 16, 32))
        }
        bigMarioRun = Animation(.1f, frames)
        frames.clear()

        frames.add(TextureRegion(playScreen.textureAtlas.findRegion("big_mario"), 240, 0, 16, 32))
        frames.add(TextureRegion(playScreen.textureAtlas.findRegion("big_mario"), 0, 0, 16, 32))
        frames.add(TextureRegion(playScreen.textureAtlas.findRegion("big_mario"), 240, 0, 16, 32))
        frames.add(TextureRegion(playScreen.textureAtlas.findRegion("big_mario"), 0, 0, 16, 32))

        growMario = Animation(.2f, frames)
        frames.clear()
    }


    init {
        defineMario()
        setBounds(0F, 0F, 16 / MarioBros.PPM, 16 / MarioBros.PPM)
        setRegion(marioStand)
    }

    fun update(dt: Float) {
        if(y < 0 && !marioIsDead) {
            killMario()
        }
        if(x>31.9 && !marioWins) {
            playScreen.music.stop()
            marioWins = true
            b2Body.setTransform(31.9F, b2Body.position.y, b2Body.angle)
        }
        if(marioWins) {
            b2Body.setTransform(31.9F, b2Body.position.y, b2Body.angle)
        }
        if(b2Body.position.x < width / 2) {
            b2Body.setTransform(width / 2, b2Body.position.y, b2Body.angle)
        }
        if (marioIsBig) {
            setPosition(b2Body.position.x - width / 2, b2Body.position.y - height / 2 - 6 / MarioBros.PPM)
        } else {
            setPosition(b2Body.position.x - width / 2, b2Body.position.y - height / 2)
        }
        setRegion(getFrame(dt))
        if (timeToDefineBigMario) defineBigMario()
        if (timeToRedefineMario) redefineMario()
    }

    fun getState(): State {

        return when {
            marioWins -> State.WIN
            marioIsDead -> State.DEAD
            runGrowAnimation -> State.GROWING
            b2Body.linearVelocity.y > 0 || (b2Body.linearVelocity.y < 0 && previousState == State.JUMPING) -> State.JUMPING
            b2Body.linearVelocity.y < 0 -> State.FALLING
            b2Body.linearVelocity.x != 0F -> State.RUNNING
            else -> State.STANDING
        }
    }

    fun getFrame(dt: Float): TextureRegion {
        currentState = getState()
        val region = when (currentState) {
            State.DEAD, State.WIN -> marioDead
            State.GROWING -> {
                if (growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnimation = false
                }
                growMario.getKeyFrame(stateTimer)
            }
            State.JUMPING -> if (marioIsBig) bigMarioJump else marioJump
            State.RUNNING -> {
                if (marioIsBig) bigMarioRun.getKeyFrame(stateTimer, true) else marioRun.getKeyFrame(stateTimer, true)
            }
            State.STANDING, State.FALLING -> if (marioIsBig) bigMarioStand else marioStand
        }
        if ((b2Body.linearVelocity.x < 0 || !runningRight) && !region.isFlipX) {
            region.flip(true, false)
            runningRight = false
        } else if ((b2Body.linearVelocity.x > 0 || runningRight) && region.isFlipX) {
            region.flip(true, false)
            runningRight = true
        }
        stateTimer = if (currentState == previousState) stateTimer + dt else 0F
        previousState = currentState
        return region
    }

    fun grow() {
        timeToDefineBigMario = true
        runGrowAnimation = true
        marioIsBig = true
        setBounds(x, y, width, height * 2)
        playScreen.game.assetManager.get("audio/sounds/powerup.wav", Sound::class.java).play()
    }

    fun hit(enemy: Enemy) {

        if(enemy is Koopa && enemy.currentState == Koopa.State.STANDING_SHELL) {
            val speed : Float = if (this.x < enemy.x) Koopa.KICK_RIGHT_SPEED else Koopa.KICK_LEFT_SPEED
            enemy.kick(speed)
            return
        }
        if (marioIsBig) {
            marioIsBig = false
            timeToRedefineMario = true
            setBounds(x, y, width, height / 2)
            playScreen.game.assetManager.get("audio/sounds/powerdown.wav", Sound::class.java).play()
        } else {
            killMario()
            b2Body.applyLinearImpulse(Vector2(0F, 4F), b2Body.worldCenter, true)
        }
    }

    fun killMario() {
        marioIsDead = true
        playScreen.music.stop()
        playScreen.game.assetManager.get("audio/sounds/mariodie.wav", Sound::class.java).play()
        val filter = Filter()
        filter.maskBits = MarioBros.NOTHING_BIT
        b2Body.fixtureList.forEach { fixture -> fixture.filterData = filter }
    }

    fun defineMario() {
        val bdef = BodyDef()
        bdef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM)
        bdef.type = BodyDef.BodyType.DynamicBody
        b2Body = world.createBody(bdef)

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 6 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.MARIO_BIT
        fdef.filter.maskBits = (MarioBros.GROUND_BIT or
                MarioBros.COIN_BIT or
                MarioBros.BRICK_BIT or
                MarioBros.ENEMY_BIT or
                MarioBros.OBJECT_BIT or
                MarioBros.ITEM_BIT or
                MarioBros.ENEMY_HEAD_BIT)

        fdef.shape = shape
        b2Body.createFixture(fdef).userData = this

        val head = EdgeShape()
        head.set(Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM))
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT
        fdef.shape = head
        fdef.isSensor = true

        b2Body.createFixture(fdef).userData = this
    }


    fun defineBigMario() {
        val currentPosition = b2Body.position
        world.destroyBody(b2Body)

        val bdef = BodyDef()
        bdef.position.set(currentPosition.add(0F, 10F / MarioBros.PPM))
        bdef.type = BodyDef.BodyType.DynamicBody
        b2Body = world.createBody(bdef)

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 6 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.MARIO_BIT
        fdef.filter.maskBits = (MarioBros.GROUND_BIT or
                MarioBros.COIN_BIT or
                MarioBros.BRICK_BIT or
                MarioBros.ENEMY_BIT or
                MarioBros.OBJECT_BIT or
                MarioBros.ITEM_BIT or
                MarioBros.ENEMY_HEAD_BIT)

        fdef.shape = shape
        b2Body.createFixture(fdef).userData = this
        shape.position = Vector2(0F, -14F / MarioBros.PPM)
        b2Body.createFixture(fdef).userData = this

        val head = EdgeShape()
        head.set(Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM))
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT
        fdef.shape = head
        fdef.isSensor = true

        b2Body.createFixture(fdef).userData = this
        timeToDefineBigMario = false
    }

    fun redefineMario() {
        val position = b2Body.position
        world.destroyBody(b2Body)

        val bdef = BodyDef()
        bdef.position.set(position)
        bdef.type = BodyDef.BodyType.DynamicBody
        b2Body = world.createBody(bdef)

        val fdef = FixtureDef()
        val shape = CircleShape()
        shape.radius = 6 / MarioBros.PPM
        fdef.filter.categoryBits = MarioBros.MARIO_BIT
        fdef.filter.maskBits = (MarioBros.GROUND_BIT or
                MarioBros.COIN_BIT or
                MarioBros.BRICK_BIT or
                MarioBros.ENEMY_BIT or
                MarioBros.OBJECT_BIT or
                MarioBros.ITEM_BIT or
                MarioBros.ENEMY_HEAD_BIT)

        fdef.shape = shape
        b2Body.createFixture(fdef).userData = this

        val head = EdgeShape()
        head.set(Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM))
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT
        fdef.shape = head
        fdef.isSensor = true

        b2Body.createFixture(fdef).userData = this

        timeToRedefineMario = false
    }
}
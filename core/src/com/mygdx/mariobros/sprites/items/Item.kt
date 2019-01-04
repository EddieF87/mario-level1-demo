package com.mygdx.mariobros.sprites.items

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario

abstract class Item(playScreen: PlayScreen, posX: Float, posY: Float) : Sprite() {

    var toDestroy = false
    var destroyed = false
    val world = playScreen.world
    lateinit var body : Body
    var velocity = Vector2(.7F, 0F)

    abstract fun defineItem()
    abstract fun use(mario: Mario)
    abstract fun setPositionAndBounds()

    open fun update(dt: Float) {
        if(toDestroy && !destroyed) {
            world.destroyBody(body)
            destroyed = true
        }
    }

    fun destroy() {
        toDestroy = true
    }

    override fun draw(batch: Batch) {
        if(!destroyed) {
            super.draw(batch)
        }
    }

    fun reverseVelocity(x: Boolean, y: Boolean) {
        if(x) {
            velocity.x = -velocity.x
        }
        if (y) {
            velocity.y = -velocity.y
        }
    }
}
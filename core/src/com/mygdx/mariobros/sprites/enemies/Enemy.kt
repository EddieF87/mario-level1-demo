package com.mygdx.mariobros.sprites.enemies

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.mygdx.mariobros.screens.PlayScreen
import com.mygdx.mariobros.sprites.Mario


abstract class Enemy(screen: PlayScreen, posX: Float, posY: Float) : Sprite() {

    val velocity = Vector2(1F, 0F)
    init {
        println("Enemy init")
    }
    abstract fun update(dt: Float)
    abstract fun hitOnHead(mario: Mario)
    abstract fun defineEnemy()
    abstract fun setActive(isActive : Boolean): Boolean
    abstract fun onEnemyHit(enemy: Enemy)

    fun reverseVelocity(x: Boolean, y: Boolean) {
        if(x) {
            velocity.x = -velocity.x
        }
        if (y) {
            velocity.y = -velocity.y
        }
    }
}
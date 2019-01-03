package com.mygdx.mariobros.tools

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.sprites.Mario
import com.mygdx.mariobros.sprites.enemies.Enemy
import com.mygdx.mariobros.sprites.items.Item
import com.mygdx.mariobros.sprites.tile_objects.InteractiveTileObject
import kotlin.experimental.or


class WorldContactListener : ContactListener {


    override fun beginContact(contact: Contact) {
        val fixA = contact.fixtureA
        val fixB = contact.fixtureB
        val cDef = fixA.filterData.categoryBits or fixB.filterData.categoryBits

        when (cDef) {
            MarioBros.ENEMY_HEAD_BIT or MarioBros.MARIO_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.ENEMY_HEAD_BIT)
                    (fixA.userData as Enemy).hitOnHead(fixB.userData as Mario)
                else (fixB.userData as Enemy).hitOnHead(fixA.userData as Mario)

            MarioBros.ENEMY_BIT or MarioBros.OBJECT_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.ENEMY_BIT)
                    (fixA.userData as Enemy).reverseVelocity(true, false)
                else (fixB.userData as Enemy).reverseVelocity(true, false)

            MarioBros.ENEMY_BIT or MarioBros.MARIO_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.MARIO_BIT)
                    (fixA.userData as Mario).hit(fixB.userData as Enemy)
                else (fixB.userData as Mario).hit(fixA.userData as Enemy)

            MarioBros.ENEMY_BIT or MarioBros.ENEMY_BIT -> {
                (fixA.userData as Enemy).onEnemyHit(fixB.userData as Enemy)
                (fixB.userData as Enemy).onEnemyHit(fixA.userData as Enemy)
            }

            MarioBros.ITEM_BIT or MarioBros.OBJECT_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.ITEM_BIT)
                    (fixA.userData as Item).reverseVelocity(true, false)
                else (fixB.userData as Item).reverseVelocity(true, false)

            MarioBros.ITEM_BIT or MarioBros.MARIO_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.ITEM_BIT)
                    (fixA.userData as Item).use((fixB.userData as Mario))
                else (fixB.userData as Item).use((fixA.userData as Mario))

            MarioBros.MARIO_HEAD_BIT or MarioBros.BRICK_BIT ->
                if (fixA.filterData.categoryBits == MarioBros.MARIO_HEAD_BIT)
                    (fixB.userData as InteractiveTileObject).onHeadHit((fixA.userData as Mario))
                else (fixA.userData as InteractiveTileObject).onHeadHit((fixB.userData as Mario))

            MarioBros.MARIO_HEAD_BIT or MarioBros.COIN_BIT -> {
                if (fixA.filterData.categoryBits == MarioBros.MARIO_HEAD_BIT)
                    (fixB.userData as InteractiveTileObject).onHeadHit((fixA.userData as Mario))
                else (fixA.userData as InteractiveTileObject).onHeadHit((fixB.userData as Mario))
            }
        }
    }

    override fun endContact(contact: Contact?) {}

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
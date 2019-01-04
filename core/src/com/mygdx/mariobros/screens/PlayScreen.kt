package com.mygdx.mariobros.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.mariobros.MarioBros
import com.mygdx.mariobros.scenes.Hud
import com.mygdx.mariobros.sprites.Mario
import com.mygdx.mariobros.sprites.items.Item
import com.mygdx.mariobros.sprites.items.ItemDef
import com.mygdx.mariobros.sprites.items.Mushroom
import com.mygdx.mariobros.tools.B2WorldCreator
import com.mygdx.mariobros.tools.WorldContactListener
import java.util.concurrent.LinkedBlockingQueue

class PlayScreen(val game: MarioBros) : Screen {

    val textureAtlas = TextureAtlas("Mario_and_Enemies.pack.txt")

    private val gameCamera = OrthographicCamera()
    private val viewport = FitViewport(MarioBros.V_WIDTH / MarioBros.PPM,
            MarioBros.V_HEIGHT / MarioBros.PPM, gameCamera)
    private val hud = Hud(game.batch)

    private val mapLoader = TmxMapLoader()
    val map = mapLoader.load("level1.tmx")
    private val renderer = OrthogonalTiledMapRenderer(map, (1 / MarioBros.PPM).toFloat())
    val music = game.assetManager.get("audio/music/mario_music.ogg", Music::class.java)

    val world = World(Vector2(0F, -10F), true)
    private val b2dr = Box2DDebugRenderer()
    private val worldCreator = B2WorldCreator(this)

    private val player = Mario(world, this)
    val items = Array<Item>()
    val itemsToSpawn = LinkedBlockingQueue<ItemDef>()

    init {
        gameCamera.position.set((viewport.worldWidth / 2), (viewport.worldHeight / 2), 0F)
        world.setContactListener(WorldContactListener())
        music.isLooping = true
        music.volume = .2F
        music.play()
    }

    fun spawnItem(itemDef: ItemDef) {
        println("spawnItem")
        itemsToSpawn.add(itemDef)
    }

    fun handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()) {
            val itemDef = itemsToSpawn.poll()
            if(itemDef.type == Mushroom::class.java) {
                items.add(Mushroom(this, itemDef.position.x, itemDef.position.y))
            }
        }
    }

    override fun show() {

    }

    fun handleInput(dt: Float) {
        if (player.currentState == Mario.State.DEAD || player.currentState == Mario.State.WIN) return
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.b2Body.applyLinearImpulse(Vector2(0F, 4F), player.b2Body.worldCenter, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2Body.linearVelocity.x <= 2) {
            player.b2Body.applyLinearImpulse(Vector2(0.1F, 0F), player.b2Body.worldCenter, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2Body.linearVelocity.x >= -2) {
            player.b2Body.applyLinearImpulse(Vector2(-0.1F, 0F), player.b2Body.worldCenter, true)
        }
    }

    fun update(dt: Float) {
        handleInput(dt)
        handleSpawningItems()
        world.step(1/60F, 6, 2)
        player.update(dt)
        for ((index, enemy) in worldCreator.enemies.withIndex()) {
            enemy.update(dt)
            if(enemy.x < player.x + 224 / MarioBros.PPM) {
                if(!enemy.setActive(true)) worldCreator.enemies.removeIndex(index)
            }
        }
        items.forEach { item -> item.update(dt) }
        hud.update(dt)

        if(player.currentState != Mario.State.DEAD) {
            gameCamera.position.x = player.b2Body.position.x
        }
        if(hud.worldTimer <= 0) {
            if(!player.marioIsDead) {
                player.killMario()
            }
        }
        gameCamera.update()
        renderer.setView(gameCamera)
    }

    override fun render(delta: Float) {
        //update render data
        update(delta)

        //clear game screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        //render map
        renderer.render()

        //render debug lines
        b2dr.render(world, gameCamera.combined)


        game.batch.projectionMatrix = gameCamera.combined
        game.batch.begin()
        player.draw(game.batch)
        worldCreator.enemies.forEach { enemy -> enemy.draw(game.batch) }
        items.forEach { item -> item.draw(game.batch) }
        game.batch.end()

        game.batch.projectionMatrix = hud.stage.camera.combined
        hud.stage.draw()

        if(gameOver()) {
            game.screen = GameOverScreen(game)
            hud.setScoreNil()
            dispose()
        }
        if(gameWon()) {
            game.screen = GameWinScreen(game)
            dispose()
        }
    }

    fun gameOver() : Boolean {
        return player.currentState == Mario.State.DEAD && player.stateTimer > 3
    }
    fun gameWon() : Boolean {
        return player.currentState == Mario.State.WIN
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        map.dispose()
        renderer.dispose()
        world.dispose()
        b2dr.dispose()
        hud.dispose()
    }
}

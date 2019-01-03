package com.mygdx.mariobros.scenes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.mariobros.MarioBros

class Hud (sb : SpriteBatch) : Disposable {


    private var worldTimer : Int = 300
    private var timeCount : Float = 0F
    private var camera : OrthographicCamera = OrthographicCamera()
    private var viewport: Viewport = FitViewport(
            MarioBros.V_WIDTH.toFloat(), MarioBros.V_HEIGHT.toFloat(), camera)
    var stage : Stage = Stage(viewport, sb)

    val countDownLabel = Label(String.format("%03d", worldTimer), Label.LabelStyle(BitmapFont(), Color.WHITE))
    val timeLabel = Label("TIME", Label.LabelStyle(BitmapFont(), Color.WHITE))
    val levelLabel = Label("1-1", Label.LabelStyle(BitmapFont(), Color.WHITE))
    val worldLabel = Label("WORLD", Label.LabelStyle(BitmapFont(), Color.WHITE))
    val marioLabel = Label("MARIO", Label.LabelStyle(BitmapFont(), Color.WHITE))

    private val table = Table()

    init {
        table.top()
        table.setFillParent(true)
        table.add(marioLabel).expandX().padTop(10F)
        table.add(worldLabel).expandX().padTop(10F)
        table.add(timeLabel).expandX().padTop(10F)
        table.row()
        table.add(scoreLabel).expandX()
        table.add(levelLabel).expandX()
        table.add(countDownLabel).expandX()
        stage.addActor(table)
    }

    override fun dispose() {
        stage.dispose()
    }

    fun update(dt:Float) {
        timeCount+=dt
        if(timeCount >= 1) {
            worldTimer--
            countDownLabel.setText(String.format("%03d", worldTimer))
            timeCount--
        }
    }

    companion object {
        private var score : Int = 0
        val scoreLabel = Label(String.format("%06d", 0), Label.LabelStyle(BitmapFont(), Color.WHITE))
        fun addScore(value : Int) {
            score+=value
            scoreLabel.setText(String.format("%06d", score))
        }
    }
}
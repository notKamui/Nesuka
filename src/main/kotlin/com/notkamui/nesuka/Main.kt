package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.render.Bitmap
import com.notkamui.nesuka.render.drawPixel
import com.notkamui.nesuka.render.handleUserInput
import com.notkamui.nesuka.render.readScreenState
import com.notkamui.nesuka.utils.TEST_ROM_SNAKE
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import kotlin.random.Random

const val WINDOW_WIDTH = 32
const val WINDOW_HEIGHT = 32

class Main : Application() {
    override fun start(stage: Stage) {
        val window = Canvas(320.0, 320.0)
        val root = Group(window)
        val scene = Scene(root)
        scene.fill = Color.BLACK
        stage.title = "Nesuka"
        stage.scene = scene

        val cpu = CPU()
        handleUserInput(cpu, stage.scene)
        cpu.load(TEST_ROM_SNAKE)
        cpu.reset()

        val bitmap = Bitmap(WINDOW_WIDTH, WINDOW_HEIGHT)
        stage.show()

        val timeline = Timeline()
        timeline.cycleCount = Timeline.INDEFINITE
        val call: CPU.() -> Unit = {
            val gc: GraphicsContext = window.graphicsContext2D
            memWrite(0xFE.u16, Random.nextInt(1, 16).u8)
            if (bitmap.readScreenState(this)) { // if change -> render
                for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                    gc.drawPixel(x, y, bitmap[x, y])
                }
            }
        }
        val kf = KeyFrame(Duration.millis(.088), { cpu.step(call) })
        timeline.keyFrames += kf
        timeline.play()
    }
}

fun main() {
    Application.launch(Main::class.java)
}
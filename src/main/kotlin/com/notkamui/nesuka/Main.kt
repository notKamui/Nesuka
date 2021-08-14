package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.render.Bitmap
import com.notkamui.nesuka.render.handleUserInput
import com.notkamui.nesuka.utils.TEST_ROM_SNAKE
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage) {
        val window = Canvas(320.0, 320.0)
        val gc: GraphicsContext = window.graphicsContext2D
        val root = Group(window)
        val scene = Scene(root)
        stage.title = "Nesuka"
        stage.scene = scene
        stage.show()


        val bitmap = Bitmap(32, 32)
        val cpu = CPU()
        handleUserInput(cpu, stage.scene)
        cpu.load(TEST_ROM_SNAKE)
        cpu.reset()
        cpu.run {
            TODO(
                """
                read user input and write it to memory[0xFF]
                update memory[0xFE] with new random number
                read memory mapped screen state
                render screen state
            """.trimIndent()
            )
        }
    }
}

fun main() {
    Application.launch(Main::class.java)
}
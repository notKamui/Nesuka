package com.notkamui.nesuka

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val canvas = Canvas(320.0, 320.0)
        val gc = canvas.graphicsContext2D
        gc.pixelWriter.setColor(50, 50, Color.BLACK)
        val root = VBox(5.0, canvas)
        val scene = Scene(root)
        stage.title = "Nesuka"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}
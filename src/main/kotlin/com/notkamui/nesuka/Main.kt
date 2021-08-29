package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.render.InputFeed
import com.notkamui.nesuka.utils.TEST_ROM_SNAKE
import java.awt.Color
import javax.swing.JFrame

fun main() {
    val cpu = CPU()
    cpu.load(TEST_ROM_SNAKE)
    cpu.reset()

    JFrame("Nesuka").run {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        addKeyListener(InputFeed(this, cpu))
        setSize(320, 320)
        isResizable = false
        isVisible = true

        graphics.run {
            color = Color.BLACK
            for (i in 0..99) for (j in 0..99)
                drawLine(i, j, i, j)
        }
    }
}
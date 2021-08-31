package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.render.GamepadListener
import com.notkamui.nesuka.render.Renderer
import com.notkamui.nesuka.utils.Logger
import com.notkamui.nesuka.utils.PrintLogger
import com.notkamui.nesuka.utils.TEST_ROM_SNAKE
import java.awt.Canvas
import java.awt.Color
import java.awt.Dimension
import java.awt.Frame

private const val TITLE = "Nesuka"
private const val NANOS_PER_MILLISECOND = 1000000
private const val NANOS_PER_FRAME = 16666666

val logger: Logger = PrintLogger()

class Application : Canvas(), Runnable {
    private val thread = Thread(this)
    private var isRunning = false
    private val window = Frame(TITLE)
    private val renderer: Renderer
    private val cpu = CPU()

    init {
        createBufferStrategy(1)
        renderer = Renderer(bufferStrategy)
        cpu.load(TEST_ROM_SNAKE)
        cpu.reset()
        initWindow()
        thread.start()
    }

    override fun run() {
        isRunning = true
        var now = System.nanoTime()
        var nextRender = now

        while (isRunning) {
            now = System.nanoTime()
            update()
            if (now - nextRender >= 0) {
                do {
                    nextRender += NANOS_PER_FRAME
                } while (now - nextRender >= 0)
            }

            val delay = nextRender - System.nanoTime()
            if (delay > 0) {
                Thread.sleep((delay + NANOS_PER_MILLISECOND) / NANOS_PER_MILLISECOND)
            }
        }
    }

    private fun initWindow() {
        size = Dimension(320, 320)
        addKeyListener(GamepadListener(cpu))
        background = Color.BLACK

        window.run {
            add(this@Application)
            pack()
            setLocationRelativeTo(null)
            isResizable = false
            isVisible = true
            addKeyListener(GamepadListener(cpu))
        }
    }

    private fun update() {
        repeat(1000) {
            isRunning = cpu.step(renderer.render)
        }
    }
}

fun main() {
    Application()
}

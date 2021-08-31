package com.notkamui.nesuka.render

import com.notkamui.nesuka.Global
import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferStrategy
import kotlin.random.Random

private const val WINDOW_WIDTH = 32
private const val WINDOW_HEIGHT = 32

class Renderer(private val bufferStrategy: BufferStrategy) {
    private val bitmap = Bitmap(WINDOW_WIDTH, WINDOW_HEIGHT)

    val render: CPU.() -> Unit = {
        memWrite(0xFE.u16, Random.nextInt(1, 16).u8)
        if (bitmap.readScreenState(this)) { // if change -> render
            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                bufferStrategy.drawGraphics.drawPixel(x, y, bitmap[x, y])
            }
        }
        bufferStrategy.show()
        Global.logger?.log("RENDERED")
    }
}

fun Graphics.drawPixel(x: Int, y: Int, color: Color) {
    this.color = color
    fillRect(x * 10, y * 10, 10, 10)
}

class Bitmap(val width: Int, val height: Int) {
    private val bitmap = Array(width * height * 3) { 0.u8 }

    operator fun get(frameIndex: Int) =
        bitmap[frameIndex]

    operator fun get(x: Int, y: Int): Color {
        val frameIndex = (y * width + x) * 3
        return Color(
            bitmap[frameIndex].toInt(),
            bitmap[frameIndex + 1].toInt(),
            bitmap[frameIndex + 2].toInt()
        )
    }

    operator fun set(frameIndex: Int, byte: UByte) {
        bitmap[frameIndex] = byte
    }

    operator fun set(x: Int, y: Int, color: Color) {
        val frameIndex = (y * width + x) * 3
        val (r, g, b) = color.rgb()
        bitmap[frameIndex] = r
        bitmap[frameIndex + 1] = g
        bitmap[frameIndex + 2] = b
    }
}

fun Bitmap.readScreenState(cpu: CPU): Boolean {
    var frameIndex = 0
    var update = false
    for (i in 0x0200 until 0x600) {
        val colorIndex = cpu.memRead(i.u16)
        val (r, g, b) = colorIndex.colorFromByte().rgb()
        if (this[frameIndex] != r || this[frameIndex + 1] != g || this[frameIndex + 2] != b) {
            this[frameIndex] = r
            this[frameIndex + 1] = g
            this[frameIndex + 2] = b
            update = true
        }
        frameIndex += 3
    }
    return update
}

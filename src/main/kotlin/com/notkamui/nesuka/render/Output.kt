package com.notkamui.nesuka.render
/*
import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

fun GraphicsContext.drawPixel(x: Int, y: Int, color: Color) {
    fill = color
    fillRect(x * 10.0, y * 10.0, 10.0, 10.0)
}

class Bitmap(val width: Int, val height: Int) {
    private val bitmap = Array(width * height * 3) { 0.u8 }

    operator fun get(frameIndex: Int) =
        bitmap[frameIndex]

    operator fun get(x: Int, y: Int): Color {
        val frameIndex = (y * width + x) * 3
        return Color.rgb(
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
}*/

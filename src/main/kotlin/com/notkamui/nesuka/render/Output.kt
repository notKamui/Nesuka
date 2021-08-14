package com.notkamui.nesuka.render

import com.notkamui.nesuka.utils.u8
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

private fun GraphicsContext.drawPixel(x: Int, y: Int, color: Color) {
    fill = color
    fillRect(x * 10.0, y * 10.0, 10.0, 10.0)
}

class Bitmap(val width: Int, val height: Int) {
    private val bitmap =
        Array(32) { Array(32) { Triple(0.u8, 0.u8, 0.u8) } }

    operator fun get(x: Int, y: Int) = bitmap[y][x]

    operator fun set(x: Int, y: Int, rgb: Triple<UByte, UByte, UByte>) {
        bitmap[y][x] = rgb
    }

    operator fun set(x: Int, y: Int, color: Color) {
        bitmap[y][x] = color.rgb()
    }
}
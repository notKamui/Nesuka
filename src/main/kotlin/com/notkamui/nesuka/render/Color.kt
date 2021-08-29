package com.notkamui.nesuka.render

import com.notkamui.nesuka.utils.u8
import java.awt.Color

fun Color.rgb() = Triple(
    red.u8,
    green.u8,
    blue.u8
)

fun UByte.colorFromByte(): Color = when (toInt()) {
    0 -> Color.BLACK
    1 -> Color.WHITE
    2, 9 -> Color.GRAY
    3, 10 -> Color.RED
    4, 11 -> Color.GREEN
    5, 12 -> Color.BLUE
    6, 13 -> Color.MAGENTA
    7, 14 -> Color.YELLOW
    else -> Color.CYAN
}
package com.notkamui.nesuka

import java.awt.Color
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.JPanel

class Example : JPanel() {
    override fun paint(g: Graphics) {
        g.color = Color(0, 0, 0)
        for (i in 0..99) for (j in 0..99)
            g.drawLine(i, j, i, j)
    }
}

fun main() {
    JFrame("Nesuka").run {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(Example())
        setSize(200, 200)
        setLocation(350, 25)
        isVisible = true
    }
}
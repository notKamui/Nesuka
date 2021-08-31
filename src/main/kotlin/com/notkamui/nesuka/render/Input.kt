package com.notkamui.nesuka.render

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import kotlin.system.exitProcess

class GamepadListener(private val cpu: CPU) : KeyAdapter() {
    override fun keyPressed(event: KeyEvent) {
        when (event.keyCode) {
            KeyEvent.VK_ESCAPE -> exitProcess(0)
            KeyEvent.VK_UP -> cpu.memWrite(0xFF.u16, 0x77.u8)
            KeyEvent.VK_DOWN -> cpu.memWrite(0xFF.u16, 0x73.u8)
            KeyEvent.VK_LEFT -> cpu.memWrite(0xFF.u16, 0x61.u8)
            KeyEvent.VK_RIGHT -> cpu.memWrite(0xFF.u16, 0x64.u8)
        }
    }

    override fun keyTyped(event: KeyEvent) {
        /*nothing*/
    }

    override fun keyReleased(event: KeyEvent) {
        /*nothing*/
    }
}

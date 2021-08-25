package com.notkamui.nesuka.render

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import kotlin.system.exitProcess

fun handleUserInput(cpu: CPU, scene: Scene) {
    scene.setOnKeyPressed { event: KeyEvent ->
        when (event.code) {
            KeyCode.ESCAPE -> exitProcess(0)
            KeyCode.Z -> cpu.memWrite(0xFF.u16, 0x77.u8)
            KeyCode.S -> cpu.memWrite(0xFF.u16, 0x73.u8)
            KeyCode.Q -> cpu.memWrite(0xFF.u16, 0x61.u8)
            KeyCode.D -> cpu.memWrite(0xFF.u16, 0x64.u8)
            else -> { /* nothing */
            }
        }
    }
}
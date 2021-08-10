package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CPUTests {
    @Test
    fun `test 0xA9 LDA immediate load data`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9u.toUByte(), 0x05u.toUByte(), 0x00u.toUByte()))

        assertEquals(cpu.registerA, 0x05u.toUByte())
        assertTrue(cpu.status and 0b0000_0010u == 0b00u.toUByte())
        assertTrue(cpu.status and 0b1000_0000u == 0x00u.toUByte())
    }

    @Test
    fun `test 0xA9 LDA zero flag`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9.toUByte(), 0x00.toUByte(), 0x00.toUByte()))

        assertTrue(cpu.status and 0b0000_0010.toUByte() == 0b10.toUByte())
    }
}
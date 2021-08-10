package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CPUTests {
    @Test
    fun `test 0xA9 LDA immediate load data`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9u, 0x05u, 0x00u))

        assertEquals(cpu.registerA, 0x05u.toUByte())
        assertEquals(cpu.status and 0b0000_0010u, 0b00u.toUByte())
        assertEquals(cpu.status and 0b1000_0000u, 0x00u.toUByte())
    }

    @Test
    fun `test 0xA9 LDA zero flag`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9u, 0x00u, 0x00u))

        assertEquals(cpu.status and 0b0000_0010.toUByte(), 0b10.toUByte())
    }

    @Test
    fun `test 0xAA TAX move A to X`() {
        val cpu = CPU()
        cpu.registerA = 10u
        cpu.interpret(listOf(0xAAu, 0x00u))

        assertEquals(cpu.registerX, 10u.toUByte())
    }
}
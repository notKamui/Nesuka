package com.notkamui.nesuka

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.u8
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CPUTests {
    @Test
    fun `test 0xA9 LDA immediate load data`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9.u8, 0x05.u8, 0x00.u8))

        assertEquals(cpu.registerA, 0x05.u8)
        assertEquals(cpu.status and 0b0000_0010.u8, 0b00.u8)
        assertEquals(cpu.status and 0b1000_0000.u8, 0x00.u8)
    }

    @Test
    fun `test 0xA9 LDA zero flag`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9.u8, 0x00.u8, 0x00.u8))

        assertEquals(cpu.status and 0b0000_0010.u8, 0b10.u8)
    }

    @Test
    fun `test 0xAA TAX move A to X`() {
        val cpu = CPU()
        cpu.registerA = 10.u8
        cpu.interpret(listOf(0xAA.u8, 0x00.u8))

        assertEquals(cpu.registerX, 10.u8)
    }

    @Test
    fun `test 5 ops`() {
        val cpu = CPU()
        cpu.interpret(listOf(0xA9.u8, 0xC0.u8, 0xAA.u8, 0xE8.u8, 0x00.u8))

        assertEquals(cpu.registerX, 0xC1.u8)
    }

    @Test
    fun `test INX overflow`() {
        val cpu = CPU()
        cpu.registerX = 0xFF.u8
        cpu.interpret(listOf(0xE8.u8, 0xE8.u8, 0x00.u8))

        assertEquals(cpu.registerX, 1.u8)
    }
}
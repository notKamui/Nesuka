package com.notkamui.nesuka.core

import com.notkamui.nesuka.utils.shl
import com.notkamui.nesuka.utils.shr
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8

class CPU {
    var registerA = 0.u8
        private set

    var registerX = 0.u8
        private set

    var status = 0.u8
        private set

    private var programCounter = 0.u16

    private val memory = Array(0xFFFF) { 0.u8 }

    /**
     * Sets the zero and negative flags according to a given register status
     */
    private fun updateZeroNegFlags(value: UByte) {
        status = if (value == 0.u8) {
            status or 0b0000_0010.u8
        } else {
            status and 0b1111_1101.u8
        }

        status = if (value and 0b1000_0000.u8 != 0.u8) {
            status or 0b1000_0000.u8
        } else {
            status and 0b0111_1111.u8
        }
    }

    /**
     * 0xA9 ; LoaD Accumulator
     *
     * Takes one argument of one byte
     *
     * Loads a byte of memory into the accumulator
     * setting the zero and negative flags as appropriate.
     */
    private fun lda(param: UByte) {
        registerA = param
        updateZeroNegFlags(registerA)
    }

    /**
     * 0xAA ; Transfer Accumulator to X
     *
     * Copies the current contents of the accumulator
     * into the X register and sets the zero and negative
     * flags as appropriate.
     */
    private fun tax() {
        registerX = registerA
        updateZeroNegFlags(registerX)
    }

    /**
     * 0xE8 ; INcrement X register
     *
     * Adds one to the X register
     * setting the zero and negative flags as appropriate.
     */
    private fun inx() {
        registerX++
        updateZeroNegFlags(registerX)
    }

    /**
     * Reads the memory at a specific [addr]ess.
     */
    private fun memRead(addr: UShort): UByte =
        memory[addr.toInt()]

    /**
     * Sets the memory at a specific [addr]ess to the value of [data].
     */
    private fun memWrite(addr: UShort, data: UByte) {
        memory[addr.toInt()] = data
    }

    /**
     * Reads the memory at a specific [addr]ess for a u16 ([Short]).
     */
    private fun memReadShort(addr: UShort): UShort {
        val lo = memRead(addr).toUShort()
        val hi = memRead((addr + 1u).toUShort()).toUShort()
        return ((hi shl 8) or lo.toInt()).toUShort()
    }

    /**
     * Sets the memory at a specific [addr]ess to the value of [data] for a u16 ([Short]).
     */
    private fun memWriteShort(addr: UShort, data: UShort) {
        val hi = (data shr 8).toUByte()
        val lo = (data and 0xFF.u16).toUByte()
        memWrite(addr, lo)
        memWrite((addr + 1u).toUShort(), hi)
    }

    private fun reset() {
        registerA = 0.u8
        registerX = 0.u8
        status = 0.u8

        programCounter = memReadShort(0xFFFC.u16)
    }

    /**
     * Loads a [program] into memory.
     */
    private fun load(program: List<UByte>) {
        program.forEachIndexed { index, opcode ->
            memWrite((0x8000 + index).u16, opcode)
        }
        memWriteShort(0xFFFC.u16, 0x8000.u16)
    }

    /**
     * Runs a program loaded into the memory.
     */
    private fun run() {
        while (true) {
            val opcode = memRead(programCounter)
            programCounter++

            when (opcode) {
                0xA9.u8 -> {
                    lda(memRead(programCounter))
                    programCounter++
                }
                0xAA.u8 -> tax()
                0xE8.u8 -> inx()
                0x00.u8 -> return // brk
                else -> TODO()
            }
        }
    }

    /**
     * Loads a [program] into memeory and runs it.
     */
    fun loadAndRun(program: List<UByte>) {
        load(program)
        reset()
        run()
    }
}

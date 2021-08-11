package com.notkamui.nesuka.core

import com.notkamui.nesuka.utils.shl
import com.notkamui.nesuka.utils.shr
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8

enum class AddressingMode {
    Immediate,
    ZeroPage,
    Absolute,
    ZeroPageX,
    ZeroPageY,
    AbsoluteX,
    AbsoluteY,
    IndirectX,
    IndirectY,
    NoneAddressing,

}

private interface Memory {
    val memory: Array<UByte>

    /**
     * Reads the memory at a specific [addr]ess.
     */
    fun memRead(addr: UShort): UByte =
        memory[addr.toInt()]

    /**
     * Sets the memory at a specific [addr]ess to the value of [data].
     */
    fun memWrite(addr: UShort, data: UByte) {
        memory[addr.toInt()] = data
    }

    /**
     * Reads the memory at a specific [addr]ess for an u16 ([Short]).
     */
    fun memReadShort(addr: UShort): UShort {
        val lo = memRead(addr).toUShort()
        val hi = memRead((addr + 1u).toUShort()).toUShort()
        return ((hi shl 8) or lo.toInt()).toUShort()
    }

    /**
     * Sets the memory at a specific [addr]ess to the value of [data] for an u16 ([Short]).
     */
    fun memWriteShort(addr: UShort, data: UShort) {
        val hi = (data shr 8).toUByte()
        val lo = (data and 0xFF.u16).toUByte()
        memWrite(addr, lo)
        memWrite((addr + 1u).toUShort(), hi)
    }
}

class CPU : Memory {
    var registerA = 0.u8
        private set

    var registerX = 0.u8
        private set

    var registerY = 0.u8
        private set

    var status = 0.u8
        private set

    private var programCounter = 0.u16

    override val memory = Array(0xFFFF) { 0.u8 }

    fun getOperandAddress(mode: AddressingMode): UShort = when (mode) {
        AddressingMode.Immediate -> programCounter
        AddressingMode.ZeroPage -> memRead(programCounter).toUShort()
        AddressingMode.Absolute -> memReadShort(programCounter)
        AddressingMode.ZeroPageX -> {
            val pos = memRead(programCounter)
            (pos + registerX).toUShort()
        }
        AddressingMode.ZeroPageY -> {
            val pos = memRead(programCounter)
            (pos + registerY).toUShort()
        }
        AddressingMode.AbsoluteX -> {
            val base = memReadShort(programCounter)
            (base + registerX).toUShort()
        }
        AddressingMode.AbsoluteY -> {
            val base = memReadShort(programCounter)
            (base + registerY).toUShort()
        }
        AddressingMode.IndirectX -> {
            val base = memReadShort(programCounter)
            val ptr = (base.toUByte() + registerX).toUByte()
            val lo = memRead(ptr.toUShort())
            val hi = memRead((ptr + 1u).toUShort())
            ((hi.toUShort() shl 8) or lo.toInt()).toUShort()
        }
        AddressingMode.IndirectY -> {
            val base = memRead(programCounter)
            val lo = memRead(base.toUShort())
            val hi = memRead((base + 1u).toUShort())
            val derefBase = ((hi.toUShort() shl 8) or lo.toInt()).toUShort()
            (derefBase + registerY.toUShort()).toUShort()
        }
        AddressingMode.NoneAddressing -> {
            throw IllegalStateException("Mode $mode is not supported")
        }
    }

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
     * according to the addressing [mode] given,
     * setting the zero and negative flags as appropriate.
     */
    private fun lda(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val value = memRead(addr)

        registerA = value
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

    private fun reset() {
        registerA = 0.u8
        registerX = 0.u8
        registerY = 0.u8
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
                    lda(AddressingMode.Immediate)
                    programCounter++
                }
                0xA5.u8 -> {
                    lda(AddressingMode.ZeroPage)
                    programCounter++
                }
                0xAD.u8 -> {
                    lda(AddressingMode.Absolute)
                    programCounter++
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
     * Loads a [program] into memory and runs it.
     */
    fun loadAndRun(program: List<UByte>) {
        load(program)
        reset()
        run()
    }
}

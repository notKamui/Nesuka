package com.notkamui.nesuka.core

import com.notkamui.nesuka.utils.AddressingMode
import com.notkamui.nesuka.utils.AddressingMode.Absolute
import com.notkamui.nesuka.utils.AddressingMode.AbsoluteX
import com.notkamui.nesuka.utils.AddressingMode.AbsoluteY
import com.notkamui.nesuka.utils.AddressingMode.Immediate
import com.notkamui.nesuka.utils.AddressingMode.IndirectX
import com.notkamui.nesuka.utils.AddressingMode.IndirectY
import com.notkamui.nesuka.utils.AddressingMode.NoneAddressing
import com.notkamui.nesuka.utils.AddressingMode.ZeroPage
import com.notkamui.nesuka.utils.AddressingMode.ZeroPageX
import com.notkamui.nesuka.utils.AddressingMode.ZeroPageY
import com.notkamui.nesuka.utils.CPUFlags
import com.notkamui.nesuka.utils.OpCode.Companion.OPCODES_MAP
import com.notkamui.nesuka.utils.STACK
import com.notkamui.nesuka.utils.STACK_RESET
import com.notkamui.nesuka.utils.shl
import com.notkamui.nesuka.utils.shr
import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8

private interface Memory {
    val memory: Array<UByte>

    /**
     * Reads the memory at a specific [addr]ess.
     */
    fun memRead(addr: UShort): UByte

    /**
     * Sets the memory at a specific [addr]ess to the value of [data].
     */
    fun memWrite(addr: UShort, data: UByte)

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
    var registerX = 0.u8
    var registerY = 0.u8
    var stackPointer = STACK_RESET
    var programCounter = 0.u16
    var status = CPUFlags.INTERRUPT_DISABLE or CPUFlags.BREAK_2
    override var memory = Array(0xFFFF) { 0.u8 }

    override fun memRead(addr: UShort): UByte =
        memory[addr.toInt()]

    override fun memWrite(addr: UShort, data: UByte) {
        memory[addr.toInt()] = data
    }

    /**
     * Gets the address for the next operand given an addressing [mode]
     */
    fun getOperandAddress(mode: AddressingMode): UShort = when (mode) {
        Immediate -> programCounter
        ZeroPage -> memRead(programCounter).toUShort()
        Absolute -> memReadShort(programCounter)
        ZeroPageX -> {
            val pos = memRead(programCounter)
            (pos + registerX).toUShort()
        }
        ZeroPageY -> {
            val pos = memRead(programCounter)
            (pos + registerY).toUShort()
        }
        AbsoluteX -> {
            val base = memReadShort(programCounter)
            (base + registerX).toUShort()
        }
        AbsoluteY -> {
            val base = memReadShort(programCounter)
            (base + registerY).toUShort()
        }
        IndirectX -> {
            val base = memReadShort(programCounter)
            val ptr = (base.toUByte() + registerX).toUByte()
            val lo = memRead(ptr.toUShort())
            val hi = memRead((ptr + 1u).toUShort())
            ((hi.toUShort() shl 8) or lo.toInt()).toUShort()
        }
        IndirectY -> {
            val base = memRead(programCounter)
            val lo = memRead(base.toUShort())
            val hi = memRead((base + 1u).toUShort())
            val derefBase = ((hi.toUShort() shl 8) or lo.toInt()).toUShort()
            (derefBase + registerY.toUShort()).toUShort()
        }
        NoneAddressing -> {
            throw IllegalStateException("Mode $mode is not supported")
        }
    }

    fun registerASet(value: UByte) {
        registerA = value
        updateZeroNegFlags(registerA)
    }

    fun insertFlag(flag: UByte) {
        status = status or flag
    }

    fun removeFlag(flag: UByte) {
        status = status and flag.inv()
    }

    fun hasFlag(flag: UByte): Boolean =
        status and flag == flag

    fun stackPop(): UByte {
        stackPointer++
        return memRead((STACK + stackPointer).toUShort())
    }

    fun stackPush(data: UByte) {
        memWrite((STACK + stackPointer).toUShort(), data)
        stackPointer--
    }

    fun stackPopShort(): UShort {
        val lo = stackPop().toUShort()
        val hi = stackPop().toUShort()
        return ((hi shl 8) or lo.toInt()).u16
    }

    fun stackPushShort(data: UShort) {
        val hi = (data shl 8).toUByte()
        val lo = (data and 0xFF.u16).toUByte()
        stackPush(hi)
        stackPush(lo)
    }

    fun addToRegisterA(data: UByte) {
        val sum =
            (registerA.toUShort() + data.toUShort() + (if (hasFlag(CPUFlags.CARRY)) 1 else 0).toUShort()).toUShort()
        if (sum > 0xFF.u16) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }

        val result = sum.toUByte()
        if ((data xor result) and (result xor registerA) and 0x80.u8 != 0.u8) {
            insertFlag(CPUFlags.OVERFLOW)
        } else {
            removeFlag(CPUFlags.OVERFLOW)
        }

        registerASet(result)
    }

    /**
     * Sets the zero and negative flags according to a given register status.
     */
    fun updateZeroNegFlags(value: UByte) {
        if (value == 0.u8) {
            insertFlag(CPUFlags.ZERO)
        } else {
            removeFlag(CPUFlags.ZERO)
        }

        if (value and 0b1000_0000.u8 != 0.u8) {
            insertFlag(CPUFlags.NEGATIVE)
        } else {
            removeFlag(CPUFlags.NEGATIVE)
        }
    }

    /**
     * Resets the state of the CPU.
     */
    fun reset() {
        registerA = 0.u8
        registerX = 0.u8
        registerY = 0.u8
        stackPointer = STACK_RESET
        status = CPUFlags.INTERRUPT_DISABLE or CPUFlags.BREAK_2

        programCounter = memReadShort(0xFFFC.u16)
    }

    /**
     * Loads a [program] into memory.
     */
    fun load(program: List<UByte>) {
        //memory = Array(0xFFFF) { 0.u8 }
        program.forEachIndexed { index, opcode ->
            memWrite((0x8000 + index).u16, opcode)
        }
        memWriteShort(0xFFFC.u16, 0x8000.u16)
    }

    /**
     * Runs a program loaded into the memory.
     */
    fun run(interrupter: CPU.() -> Unit = {}) {
        while (true) {
            interrupter()

            val code = memRead(programCounter)
            programCounter++
            val programCounterState = programCounter

            val opcode = OPCODES_MAP[code]
                ?: throw IllegalStateException("OpCode $code is not recognized")

            if (code == 0x00.u8) return // BRK

            opcode.action(this)

            if (programCounterState == programCounter) {
                programCounter = (programCounter + (opcode.len - 1u).toUShort()).toUShort()
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

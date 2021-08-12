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

@Suppress("DuplicatedCode")
class CPU : Memory {
    var registerA = 0.u8
        private set

    var registerX = 0.u8
        private set

    var registerY = 0.u8
        private set

    private var stackPointer = STACK_RESET

    private var programCounter = 0.u16

    var status = CPUFlags.INTERRUPT_DISABLE or CPUFlags.BREAK_2
        private set

    override var memory = Array(0xFFFF) { 0.u8 }

    override fun memRead(addr: UShort): UByte =
        memory[addr.toInt()]

    override fun memWrite(addr: UShort, data: UByte) {
        memory[addr.toInt()] = data
    }

    /**
     * Gets the address for the next operand given an addressing [mode]
     */
    private fun getOperandAddress(mode: AddressingMode): UShort = when (mode) {
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

    private fun setRegisterA(value: UByte) {
        registerA = value
        updateZeroNegFlags(registerA)
    }

    private fun insertFlag(flag: UByte) {
        status = status or flag
    }

    private fun removeFlag(flag: UByte) {
        status = status and flag.inv()
    }

    private fun hasFlag(flag: UByte): Boolean =
        status and flag == flag

    private fun stackPop(): UByte {
        stackPointer++
        return memRead((STACK + stackPointer).toUShort())
    }

    private fun stackPush(data: UByte) {
        memWrite((STACK + stackPointer).toUShort(), data)
        stackPointer--
    }

    private fun stackPopShort(): UShort {
        val lo = stackPop().toUShort()
        val hi = stackPop().toUShort()
        return ((hi shl 8) or lo.toInt()).u16
    }

    private fun stackPushShort(data: UShort) {
        val hi = (data shl 8).toUByte()
        val lo = (data and 0xFF.u16).toUByte()
        stackPush(hi)
        stackPush(lo)
    }

    private fun addToRegisterA(data: UByte) {
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

        setRegisterA(result)
    }

    /**
     * Sets the zero and negative flags according to a given register status.
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
     * LoaD Y register
     *
     * Loads a byte of memory into the Y register
     * setting the zero and negative flags as appropriate.
     */
    private fun ldy(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        registerY = data
        updateZeroNegFlags(registerY)
    }

    /**
     * LoaD X register
     *
     * Loads a byte of memory into the X register
     * setting the zero and negative flags as appropriate.
     */
    private fun ldx(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        registerX = data
        updateZeroNegFlags(registerX)
    }

    /**
     * LoaD Accumulator
     *
     * Loads a byte of memory into the accumulator
     * setting the zero and negative flags as appropriate.
     */
    private fun lda(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val value = memRead(addr)
        setRegisterA(value)
    }

    /**
     * STore Accumulator
     *
     * Stores the content of the accumulator into memory.
     */
    private fun sta(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        memWrite(addr, registerA)
    }

    /**
     * logical AND
     *
     * A logical AND is performed, bit by bit,
     * on the accumulator contents using the contents
     * of a byte of memory.
     */
    private fun and(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        setRegisterA(data and registerA)
    }

    /**
     * Exclusive OR
     *
     * An exclusive OR is performed, bit by bit,
     * on the accumulator contents using the contents
     * of a byte of memory.
     */
    private fun eor(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        setRegisterA(data xor registerA)
    }

    /**
     * logical inclusive OR (on A)
     *
     * An inclusive OR is performed, bit by bit,
     * on the accumulator contents using the contents
     * of a byte of memory.
     */
    private fun ora(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        setRegisterA(data or registerA)
    }

    /**
     * Transfer Accumulator to X
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
     * INcrement X register
     *
     * Adds one to the X register
     * setting the zero and negative flags as appropriate.
     */
    private fun inx() {
        registerX++
        updateZeroNegFlags(registerX)
    }

    /**
     * INcrement Y register
     *
     * Adds one to the Y register
     * setting the zero and negative flags as appropriate.
     */
    private fun iny() {
        registerY++
        updateZeroNegFlags(registerY)
    }

    /**
     * SuBtract with Carry
     *
     * This instruction subtracts the contents
     * of a memory location to the accumulator
     * together with the not of the carry bit.
     * If overflow occurs the carry bit is clear,
     * this enables multiple byte subtractions
     * to be performed.
     */
    private fun sbc(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        addToRegisterA((data.toByte() - 1).u8)
    }

    /**
     * ADd with Carry
     *
     * This instruction adds the contents
     * of a memory location to the accumulator
     * together with carry bit.
     * If overflow occurs the carry bit is set,
     * this enables multiple byte additions
     * to be performed.
     */
    private fun adc(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val value = memRead(addr)
        addToRegisterA(value)
    }

    private fun aslAccumulator() {
        var data = registerA
        if ((data shr 7).u8 == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shl 1).u8
        setRegisterA(data)
    }

    /**
     * Arithmetic Shift Left
     *
     * This operation shifts all the bits of the accumulator
     * or memory contents one bit left.
     * Bit 0 is set to 0 and bit 7 is placed in the carry flag.
     * The effect of this operation is to multiply
     * the memory contents by 2 (ignoring 2's complement considerations),
     * setting the carry if the result will not fit in 8 bits.
     */
    private fun asl(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        if ((data shr 7).u8 == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shl 1).u8
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
    }

    private fun lsrAccumulator() {
        var data = registerA
        if (data and 1u == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shr 1).u8
        setRegisterA(data)
    }

    /**
     * Logical Shift Right
     *
     * Each of the bits in A or M is shift one place to the right.
     * The bit that was in bit 0 is shifted into the carry flag.
     * Bit 7 is set to zero.
     */
    private fun lsr(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        if (data and 1u == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shr 1).u8
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
    }

    private fun rolAccumulator() {
        var data = registerA
        val oldCarry = hasFlag(CPUFlags.CARRY)
        if ((data shr 7).u8 == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shl 1).u8
        if (oldCarry) {
            data = data or 1.u8
        }
        setRegisterA(data)
    }

    /**
     * ROtate Left
     *
     * Move each of the bits in either A or M one place to the left.
     * Bit 0 is filled with the current value of the carry flag
     * whilst the old bit 7 becomes the new carry flag value.
     */
    private fun rol(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        val oldCarry = hasFlag(CPUFlags.CARRY)
        if ((data shr 7).u8 == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shl 1).u8
        if (oldCarry) {
            data = data or 1.u8
        }
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
    }

    private fun rorAccumulator() {
        var data = registerA
        val oldCarry = hasFlag(CPUFlags.CARRY)
        if (data and 1u == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shr 1).u8
        if (oldCarry) {
            data = data or 0b1000_0000.u8
        }
        setRegisterA(data)
    }

    /**
     * ROtate Right
     *
     * Move each of the bits in either A or M one place to the right.
     * Bit 7 is filled with the current value of the carry flag
     * whilst the old bit 0 becomes the new carry flag value.
     */
    private fun ror(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        val oldCarry = hasFlag(CPUFlags.CARRY)
        if (data and 1u == 1.u8) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        data = (data shr 1).u8
        if (oldCarry) {
            data = data or 0b1000_0000.u8
        }
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
    }

    /**
     * Resets the state of the CPU.
     */
    private fun reset() {
        registerA = 0.u8
        registerX = 0.u8
        registerY = 0.u8
        stackPointer = STACK_RESET
        status = CPUFlags.INTERRUPT_DISABLE or CPUFlags.BREAK_2
        memory = Array(0xFFFF) { 0.u8 }

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
            val code = memRead(programCounter)
            programCounter++
            val programCounterState = programCounter

            val opcode = OPCODES_MAP[code]
                ?: throw IllegalStateException("OpCode $code is not recognized")

            when (code) {
                0xA9.u8, 0xA5.u8, 0xB5.u8, 0xAD.u8,
                0xBD.u8, 0xB9.u8, 0xA1.u8, 0xB1.u8 ->
                    lda(opcode.mode)

                0x85.u8, 0x95.u8, 0x8D.u8, 0x9D.u8,
                0x99.u8, 0x81.u8, 0x91.u8 ->
                    sta(opcode.mode)

                0xAA.u8 -> tax()
                0xE8.u8 -> inx()
                0x00.u8 -> return // brk
                else -> TODO()
            }

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

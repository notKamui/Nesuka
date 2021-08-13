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
     * INCrement memory
     *
     * Adds one to the value held at a specified memory location
     * setting the zero and negative flags as appropriate.
     */
    private fun inc(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        data++
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
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
     * DECrement memory
     *
     * Subtracts one from the value held at a specified memory location
     * setting the zero and negative flags as appropriate.
     */
    private fun dec(mode: AddressingMode): UByte {
        val addr = getOperandAddress(mode)
        var data = memRead(addr)
        data--
        memWrite(addr, data)
        updateZeroNegFlags(data)
        return data
    }

    /**
     * DEcrement X register
     *
     * Subtracts one from the X register
     * setting the zero and negative flags as appropriate.
     */
    private fun dex() {
        registerX--
        updateZeroNegFlags(registerX)
    }

    /**
     * DEcrement Y register
     *
     * Subtracts one from the Y register
     * setting the zero and negative flags as appropriate.
     */
    private fun dey() {
        registerY--
        updateZeroNegFlags(registerY)
    }

    /**
     * PuLl Accumulator
     *
     * Pulls an 8 bit value from the stack and into the accumulator.
     * The zero and negative flags are set as appropriate.
     */
    private fun pla() {
        val data = stackPop()
        setRegisterA(data)
    }

    /**
     * PuLl Processor status
     *
     * Pulls an 8 bit value from the stack and into the processor flags.
     * The flags will take on new states as determined by the value pulled.
     */
    private fun plp() {
        status = stackPop()
        removeFlag(CPUFlags.BREAK)
        insertFlag(CPUFlags.BREAK_2)
    }

    /**
     * PusH Processor status
     *
     * Pushes a copy of the status flags on to the stack.
     */
    private fun php() {
        var flags = status
        flags = flags or CPUFlags.BREAK
        flags = flags or CPUFlags.BREAK_2
        stackPush(flags)
    }

    /**
     * BIt Test
     *
     * This instruction is used to test if one or more bits are set in a target memory location.
     * The mask pattern in A is ANDed with the value in memory to set or clear the zero flag,
     * but the result is not kept.
     * Bits 7 and 6 of the value from memory are copied into the N and V flags.
     */
    private fun bit(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        val and = registerA and data
        if (and == 0.u8) {
            insertFlag(CPUFlags.ZERO)
        } else {
            removeFlag(CPUFlags.ZERO)
        }
        if (data and 0b1000_0000.u8 > 0.u8) {
            insertFlag(CPUFlags.NEGATIVE)
        } else {
            removeFlag(CPUFlags.NEGATIVE)
        }
        if (data and 0b0100_0000.u8 > 0.u8) {
            insertFlag(CPUFlags.OVERFLOW)
        } else {
            removeFlag(CPUFlags.OVERFLOW)
        }
    }

    private fun compare(mode: AddressingMode, compareWith: UByte) {
        val addr = getOperandAddress(mode)
        val data = memRead(addr)
        if (data <= compareWith) {
            insertFlag(CPUFlags.CARRY)
        } else {
            removeFlag(CPUFlags.CARRY)
        }
        updateZeroNegFlags((compareWith - data).toUByte())
    }

    private fun branch(condition: Boolean) {
        if (condition) {
            val jump = memRead(programCounter).toByte()
            val jumpAddr = (programCounter + 1u + jump.toUShort()).toUShort()
            programCounter = jumpAddr
        }
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

        programCounter = memReadShort(0xFFFC.u16)
    }

    /**
     * Loads a [program] into memory.
     */
    private fun load(program: List<UByte>) {
        //memory = Array(0xFFFF) { 0.u8 }
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

            when (code.toInt()) {
                0xA9, 0xA5, 0xB5, 0xAD, 0xBD, 0xB9, 0xA1, 0xB1 -> lda(opcode.mode)

                0x85, 0x95, 0x8D, 0x9D, 0x99, 0x81, 0x91 -> sta(opcode.mode)

                0xAA -> tax()

                /*CLC*/0x18 -> removeFlag(CPUFlags.CARRY)
                /*SEC*/0x38 -> insertFlag(CPUFlags.CARRY)
                /*CLD*/0xD8 -> removeFlag(CPUFlags.DECIMAL_MODE)
                /*SED*/0xF8 -> insertFlag(CPUFlags.DECIMAL_MODE)
                /*CLI*/0x58 -> removeFlag(CPUFlags.INTERRUPT_DISABLE)
                /*SEI*/0x78 -> insertFlag(CPUFlags.INTERRUPT_DISABLE)
                /*CLV*/0xB8 -> removeFlag(CPUFlags.OVERFLOW)

                /*PHA*/0x48 -> stackPush(registerA)
                0x68 -> pla()
                0x08 -> php()
                0x28 -> plp()

                0x69, 0x65, 0x75, 0x6D, 0x7D, 0x79, 0x61, 0x71 -> adc(opcode.mode)
                0xE9, 0xE5, 0xF5, 0xED, 0xFD, 0xF9, 0xE1, 0xF1 -> sbc(opcode.mode)

                0x29, 0x25, 0x35, 0x2D, 0x3D, 0x39, 0x21, 0x31 -> and(opcode.mode)
                0x49, 0x45, 0x55, 0x4D, 0x5D, 0x59, 0x41, 0x51 -> eor(opcode.mode)
                0x09, 0x05, 0x15, 0x0D, 0x1D, 0x19, 0x01, 0x11 -> ora(opcode.mode)

                0x4A -> lsrAccumulator()
                0x46, 0x56, 0x4E, 0x5E -> lsr(opcode.mode)
                0x0A -> aslAccumulator()
                0x06, 0x16, 0x0E, 0x1E -> asl(opcode.mode)
                0x2A -> rolAccumulator()
                0x26, 0x36, 0x2E, 0x3E -> rol(opcode.mode)
                0x6A -> rorAccumulator()
                0x66, 0x76, 0x6E, 0x7E -> ror(opcode.mode)

                0xE6, 0xF6, 0xEE, 0xFE -> inc(opcode.mode)
                0xC6, 0xD6, 0xCE, 0xDE -> dec(opcode.mode)
                0xE8 -> inx()
                0xCA -> dex()
                0xC8 -> iny()
                0x88 -> dey()


                0x00 -> return // brk
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

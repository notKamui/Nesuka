package com.notkamui.nesuka.utils

import com.notkamui.nesuka.core.CPU
import com.notkamui.nesuka.utils.AddressingMode.Absolute
import com.notkamui.nesuka.utils.AddressingMode.AbsoluteX
import com.notkamui.nesuka.utils.AddressingMode.AbsoluteY
import com.notkamui.nesuka.utils.AddressingMode.Immediate
import com.notkamui.nesuka.utils.AddressingMode.IndirectX
import com.notkamui.nesuka.utils.AddressingMode.IndirectY
import com.notkamui.nesuka.utils.AddressingMode.ZeroPage
import com.notkamui.nesuka.utils.AddressingMode.ZeroPageX
import com.notkamui.nesuka.utils.AddressingMode.ZeroPageY

/**
 * LoaD Y register
 *
 * Loads a byte of memory into the Y register
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.ldy(mode: AddressingMode) {
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
private fun CPU.ldx(mode: AddressingMode) {
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
private fun CPU.lda(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    val value = memRead(addr)
    registerASet(value)
}

/**
 * STore Accumulator
 *
 * Stores the content of the accumulator into memory.
 */
private fun CPU.sta(mode: AddressingMode) {
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
private fun CPU.and(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    val data = memRead(addr)
    registerASet(data and registerA)
}

/**
 * Exclusive OR
 *
 * An exclusive OR is performed, bit by bit,
 * on the accumulator contents using the contents
 * of a byte of memory.
 */
private fun CPU.eor(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    val data = memRead(addr)
    registerASet(data xor registerA)
}

/**
 * logical inclusive OR (on A)
 *
 * An inclusive OR is performed, bit by bit,
 * on the accumulator contents using the contents
 * of a byte of memory.
 */
private fun CPU.ora(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    val data = memRead(addr)
    registerASet(data or registerA)
}

/**
 * Transfer Accumulator to X
 *
 * Copies the current contents of the accumulator
 * into the X register and sets the zero and negative
 * flags as appropriate.
 */
private fun CPU.tax() {
    registerX = registerA
    updateZeroNegFlags(registerX)
}

/**
 * Transfer Accumulator to Y
 *
 * Copies the current contents of the accumulator
 * into the Y register and sets the zero and negative
 * flags as appropriate.
 */
private fun CPU.tay() {
    registerY = registerA
    updateZeroNegFlags(registerY)
}

/**
 * Transfer Stack pointer to X
 *
 * Copies the current contents of the stack register into the X register
 * and sets the zero and negative flags as appropriate.
 */
private fun CPU.tsx() {
    registerX = stackPointer
    updateZeroNegFlags(registerX)
}

/**
 * Transfer X to Accumulator
 *
 * Copies the current contents of the X register into the accumulator
 * and sets the zero and negative flags as appropriate.
 */
private fun CPU.txa() {
    registerA = registerX
    updateZeroNegFlags(registerA)
}

/**
 * Transfer X to Stack pointer
 *
 * Copies the current contents of the X register into the stack register.
 */
private fun CPU.txs() {
    stackPointer = registerX
}


/**
 * Transfer Y to Accumulator
 *
 * Copies the current contents of the Y register into the accumulator
 * and sets the zero and negative flags as appropriate.
 */
private fun CPU.tya() {
    registerA = registerY
    updateZeroNegFlags(registerA)
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
private fun CPU.sbc(mode: AddressingMode) {
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
private fun CPU.adc(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    val value = memRead(addr)
    addToRegisterA(value)
}

private fun CPU.aslAccumulator() {
    var data = registerA
    if ((data shr 7).u8 == 1.u8) {
        insertFlag(CPUFlags.CARRY)
    } else {
        removeFlag(CPUFlags.CARRY)
    }
    data = (data shl 1).u8
    registerASet(data)
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
private fun CPU.asl(mode: AddressingMode) {
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
}

private fun CPU.lsrAccumulator() {
    var data = registerA
    if (data and 1u == 1.u8) {
        insertFlag(CPUFlags.CARRY)
    } else {
        removeFlag(CPUFlags.CARRY)
    }
    data = (data shr 1).u8
    registerASet(data)
}

/**
 * Logical Shift Right
 *
 * Each of the bits in A or M is shift one place to the right.
 * The bit that was in bit 0 is shifted into the carry flag.
 * Bit 7 is set to zero.
 */
private fun CPU.lsr(mode: AddressingMode) {
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
}

private fun CPU.rolAccumulator() {
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
    registerASet(data)
}

/**
 * ROtate Left
 *
 * Move each of the bits in either A or M one place to the left.
 * Bit 0 is filled with the current value of the carry flag
 * whilst the old bit 7 becomes the new carry flag value.
 */
private fun CPU.rol(mode: AddressingMode) {
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
}

private fun CPU.rorAccumulator() {
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
    registerASet(data)
}

/**
 * ROtate Right
 *
 * Move each of the bits in either A or M one place to the right.
 * Bit 7 is filled with the current value of the carry flag
 * whilst the old bit 0 becomes the new carry flag value.
 */
private fun CPU.ror(mode: AddressingMode) {
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
}

/**
 * INCrement memory
 *
 * Adds one to the value held at a specified memory location
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.inc(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    var data = memRead(addr)
    data++
    memWrite(addr, data)
    updateZeroNegFlags(data)
}

/**
 * INcrement X register
 *
 * Adds one to the X register
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.inx() {
    registerX++
    updateZeroNegFlags(registerX)
}

/**
 * INcrement Y register
 *
 * Adds one to the Y register
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.iny() {
    registerY++
    updateZeroNegFlags(registerY)
}

/**
 * DECrement memory
 *
 * Subtracts one from the value held at a specified memory location
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.dec(mode: AddressingMode) {
    val addr = getOperandAddress(mode)
    var data = memRead(addr)
    data--
    memWrite(addr, data)
    updateZeroNegFlags(data)
}

/**
 * DEcrement X register
 *
 * Subtracts one from the X register
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.dex() {
    registerX--
    updateZeroNegFlags(registerX)
}

/**
 * DEcrement Y register
 *
 * Subtracts one from the Y register
 * setting the zero and negative flags as appropriate.
 */
private fun CPU.dey() {
    registerY--
    updateZeroNegFlags(registerY)
}

/**
 * PuLl Accumulator
 *
 * Pulls an 8 bit value from the stack and into the accumulator.
 * The zero and negative flags are set as appropriate.
 */
private fun CPU.pla() {
    val data = stackPop()
    registerASet(data)
}

/**
 * PuLl Processor status
 *
 * Pulls an 8 bit value from the stack and into the processor flags.
 * The flags will take on new states as determined by the value pulled.
 */
private fun CPU.plp() {
    status = stackPop()
    removeFlag(CPUFlags.BREAK)
    insertFlag(CPUFlags.BREAK_2)
}

/**
 * PusH Processor status
 *
 * Pushes a copy of the status flags on to the stack.
 */
private fun CPU.php() {
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
private fun CPU.bit(mode: AddressingMode) {
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

fun CPU.compare(mode: AddressingMode, compareWith: UByte) {
    val addr = getOperandAddress(mode)
    val data = memRead(addr)
    if (data <= compareWith) {
        insertFlag(CPUFlags.CARRY)
    } else {
        removeFlag(CPUFlags.CARRY)
    }
    updateZeroNegFlags((compareWith - data).toUByte())
}

fun CPU.branch(condition: Boolean) {
    if (condition) {
        val jump = memRead(programCounter).toByte()
        val jumpAddr = (programCounter + 1u + jump.toUShort()).toUShort()
        programCounter = jumpAddr
    }
}

class OpCode(
    code: Int,
    val mnemonic: String,
    len: Int,
    cycles: Int,
    val action: CPU.() -> Unit
) {
    val code = code.u8
    val len = len.u8
    val cycles = cycles.u8

    companion object {
        private val CPU_OPS_CODES = listOf(
            OpCode(0x00, "BRK", 1, 7) { return@OpCode },
            OpCode(0xEA, "NOP", 1, 2) {},

            // Arithmetic
            OpCode(0x69, "ADC", 2, 2) { adc(Immediate) },
            OpCode(0x65, "ADC", 2, 3) { adc(ZeroPage) },
            OpCode(0x75, "ADC", 2, 4) { adc(ZeroPageX) },
            OpCode(0x6D, "ADC", 3, 4) { adc(Absolute) },
            OpCode(0x7D, "ADC", 3, 4/*+1 if page crossed*/) { adc(AbsoluteX) },
            OpCode(0x79, "ADC", 3, 4/*+1 if page crossed*/) { adc(AbsoluteY) },
            OpCode(0x61, "ADC", 2, 6) { adc(IndirectX) },
            OpCode(0x71, "ADC", 2, 5/*+1 if page crossed*/) { adc(IndirectY) },

            OpCode(0xE9, "SBC", 2, 2) { sbc(Immediate) },
            OpCode(0xE5, "SBC", 2, 3) { sbc(ZeroPage) },
            OpCode(0xF5, "SBC", 2, 4) { sbc(ZeroPageX) },
            OpCode(0xED, "SBC", 3, 4) { sbc(Absolute) },
            OpCode(0xFD, "SBC", 3, 4/*+1 if page crossed*/) { sbc(AbsoluteX) },
            OpCode(0xF9, "SBC", 3, 4/*+1 if page crossed*/) { sbc(AbsoluteY) },
            OpCode(0xE1, "SBC", 2, 6) { sbc(IndirectX) },
            OpCode(0xF1, "SBC", 2, 5/*+1 if page crossed*/) { sbc(IndirectY) },

            OpCode(0x29, "AND", 2, 2) { and(Immediate) },
            OpCode(0x25, "AND", 2, 3) { and(ZeroPage) },
            OpCode(0x35, "AND", 2, 4) { and(ZeroPageX) },
            OpCode(0x2D, "AND", 3, 4) { and(Absolute) },
            OpCode(0x3D, "AND", 3, 4/*+1 if page crossed*/) { and(AbsoluteX) },
            OpCode(0x39, "AND", 3, 4/*+1 if page crossed*/) { and(AbsoluteY) },
            OpCode(0x21, "AND", 2, 6) { and(IndirectX) },
            OpCode(0x31, "AND", 2, 5/*+1 if page crossed*/) { and(IndirectY) },

            OpCode(0x49, "EOR", 2, 2) { eor(Immediate) },
            OpCode(0x45, "EOR", 2, 3) { eor(ZeroPage) },
            OpCode(0x55, "EOR", 2, 4) { eor(ZeroPageX) },
            OpCode(0x4D, "EOR", 3, 4) { eor(Absolute) },
            OpCode(0x5D, "EOR", 3, 4/*+1 if page crossed*/) { eor(AbsoluteX) },
            OpCode(0x59, "EOR", 3, 4/*+1 if page crossed*/) { eor(AbsoluteY) },
            OpCode(0x41, "EOR", 2, 6) { eor(IndirectX) },
            OpCode(0x51, "EOR", 2, 5/*+1 if page crossed*/) { eor(IndirectY) },

            OpCode(0x09, "ORA", 2, 2) { ora(Immediate) },
            OpCode(0x05, "ORA", 2, 3) { ora(ZeroPage) },
            OpCode(0x15, "ORA", 2, 4) { ora(ZeroPageX) },
            OpCode(0x0D, "ORA", 3, 4) { ora(Absolute) },
            OpCode(0x1D, "ORA", 3, 4/*+1 if page crossed*/) { ora(AbsoluteX) },
            OpCode(0x19, "ORA", 3, 4/*+1 if page crossed*/) { ora(AbsoluteY) },
            OpCode(0x01, "ORA", 2, 6) { ora(IndirectX) },
            OpCode(0x11, "ORA", 2, 5/*+1 if page crossed*/) { ora(IndirectY) },

            // Shifts
            OpCode(0x0A, "ASL", 1, 2) { aslAccumulator() },
            OpCode(0x06, "ASL", 2, 5) { asl(ZeroPage) },
            OpCode(0x16, "ASL", 2, 6) { asl(ZeroPageX) },
            OpCode(0x0E, "ASL", 3, 6) { asl(Absolute) },
            OpCode(0x1E, "ASL", 3, 7) { asl(AbsoluteX) },

            OpCode(0x4A, "LSR", 1, 2) { lsrAccumulator() },
            OpCode(0x46, "LSR", 2, 5) { lsr(ZeroPage) },
            OpCode(0x56, "LSR", 2, 6) { lsr(ZeroPageX) },
            OpCode(0x4E, "LSR", 3, 6) { lsr(Absolute) },
            OpCode(0x5E, "LSR", 3, 7) { lsr(AbsoluteX) },

            OpCode(0x2A, "ROL", 1, 2) { rolAccumulator() },
            OpCode(0x26, "ROL", 2, 5) { rol(ZeroPage) },
            OpCode(0x36, "ROL", 2, 6) { rol(ZeroPageX) },
            OpCode(0x2E, "ROL", 3, 6) { rol(Absolute) },
            OpCode(0x3E, "ROL", 3, 7) { rol(AbsoluteX) },

            OpCode(0x6A, "ROR", 1, 2) { rorAccumulator() },
            OpCode(0x66, "ROR", 2, 5) { ror(ZeroPage) },
            OpCode(0x76, "ROR", 2, 6) { ror(ZeroPageX) },
            OpCode(0x6E, "ROR", 3, 6) { ror(Absolute) },
            OpCode(0x7E, "ROR", 3, 7) { ror(AbsoluteX) },

            OpCode(0xE6, "INC", 2, 5) { inc(ZeroPage) },
            OpCode(0xF6, "INC", 2, 6) { inc(ZeroPageX) },
            OpCode(0xEE, "INC", 3, 6) { inc(Absolute) },
            OpCode(0xFE, "INC", 3, 7) { inc(AbsoluteX) },

            OpCode(0xE8, "INX", 1, 2) { inx() },
            OpCode(0xC8, "INY", 1, 2) { iny() },

            OpCode(0xC6, "DEC", 2, 5) { dec(ZeroPage) },
            OpCode(0xD6, "DEC", 2, 6) { dec(ZeroPageX) },
            OpCode(0xCE, "DEC", 3, 6) { dec(Absolute) },
            OpCode(0xDE, "DEC", 3, 7) { dec(AbsoluteX) },

            OpCode(0xCA, "DEX", 1, 2) { dex() },
            OpCode(0x88, "DEY", 1, 2) { dey() },

            OpCode(0xC9, "CMP", 2, 2) { compare(Immediate, registerA) },
            OpCode(0xC5, "CMP", 2, 3) { compare(ZeroPage, registerA) },
            OpCode(0xD5, "CMP", 2, 4) { compare(ZeroPageX, registerA) },
            OpCode(0xCD, "CMP", 3, 4) { compare(Absolute, registerA) },
            OpCode(0xDD, "CMP", 3, 4/*+1 if page crossed*/) { compare(AbsoluteX, registerA) },
            OpCode(0xD9, "CMP", 3, 4/*+1 if page crossed*/) { compare(AbsoluteY, registerA) },
            OpCode(0xC1, "CMP", 2, 6) { compare(IndirectX, registerA) },
            OpCode(0xD1, "CMP", 2, 5/*+1 if page crossed*/) { compare(IndirectY, registerA) },

            OpCode(0xE0, "CPX", 2, 2) { compare(Immediate, registerX) },
            OpCode(0xE4, "CPX", 2, 3) { compare(ZeroPage, registerX) },
            OpCode(0xEC, "CPX", 3, 4) { compare(Absolute, registerX) },

            OpCode(0xC0, "CPY", 2, 2) { compare(Immediate, registerY) },
            OpCode(0xC4, "CPY", 2, 3) { compare(ZeroPage, registerY) },
            OpCode(0xCC, "CPY", 3, 4) { compare(Absolute, registerY) },

            // Branching
            OpCode(0x4C, "JMP", 3, 3) {
                val memAddr = memReadShort(programCounter)
                programCounter = memAddr
            }, //AddressingMode that acts as Immediate
            OpCode(0x6C, "JMP", 3, 5) {
                val memAddr = memReadShort(programCounter)
                // val indirectRef = memReadShort(memAddr)
                /*
                6502 bug mode with page boundary:
                if address $3000 contains $40, $30FF contains $80, and $3100 contains $50,
                the result of JMP ($30FF) will be a transfer of control to $4080 rather than $5080 as intended
                i.e. the 6502 took the low byte of the address from $30FF and the high byte from $3000
                 */
                val indirectRef = if (memAddr and 0x00FF.u16 == 0x00FF.u16) {
                    val lo = memRead(memAddr)
                    val hi = memRead(memAddr and 0xFF00.u16)
                    ((hi.toUShort() shl 8) or lo.toInt()).u16
                } else {
                    memReadShort(memAddr)
                }
                programCounter = indirectRef
            }, //AddressingMode.Indirect with 6502 bug

            OpCode(0x20, "JSR", 3, 6) {
                stackPushShort((programCounter + 2u - 1u).toUShort())
                val targetAddr = memReadShort(programCounter)
                programCounter = targetAddr
            },
            OpCode(0x60, "RTS", 1, 6) {
                programCounter = (stackPopShort() + 1u).toUShort()
            },
            OpCode(0x40, "RTI", 1, 6) {
                status = stackPop()
                removeFlag(CPUFlags.BREAK)
                insertFlag(CPUFlags.BREAK_2)
                programCounter = stackPopShort()
            },

            OpCode(0xF0, "BEQ", 2, 2/*+1 if branch succeeds, +2 if to a new page*/) { branch(hasFlag(CPUFlags.ZERO)) },
            OpCode(0xD0, "BNE", 2, 2/*+1 if branch succeeds, +2 if to a new page*/) { branch(!hasFlag(CPUFlags.ZERO)) },
            OpCode(
                0x70,
                "BVS",
                2,
                2/*+1 if branch succeeds, +2 if to a new page*/
            ) { branch(hasFlag(CPUFlags.OVERFLOW)) },
            OpCode(
                0x50,
                "BVC",
                2,
                2/*+1 if branch succeeds, +2 if to a new page*/
            ) { branch(!hasFlag(CPUFlags.OVERFLOW)) },
            OpCode(
                0x30,
                "BMI",
                2,
                2/*+1 if branch succeeds, +2 if to a new page*/
            ) { branch(hasFlag(CPUFlags.NEGATIVE)) },
            OpCode(
                0x10,
                "BPL",
                2,
                2/*+1 if branch succeeds, +2 if to a new page*/
            ) { branch(!hasFlag(CPUFlags.NEGATIVE)) },
            OpCode(0xB0, "BCS", 2, 2/*+1 if branch succeeds, +2 if to a new page*/) { branch(hasFlag(CPUFlags.CARRY)) },
            OpCode(
                0x90,
                "BCC",
                2,
                2/*+1 if branch succeeds, +2 if to a new page*/
            ) { branch(!hasFlag(CPUFlags.CARRY)) },

            OpCode(0x24, "BIT", 2, 3) { bit(ZeroPage) },
            OpCode(0x2C, "BIT", 3, 4) { bit(Absolute) },

            // Stores, Loads
            OpCode(0xA9, "LDA", 2, 2) { lda(Immediate) },
            OpCode(0xA5, "LDA", 2, 3) { lda(ZeroPage) },
            OpCode(0xB5, "LDA", 2, 4) { lda(ZeroPageX) },
            OpCode(0xAD, "LDA", 3, 4) { lda(Absolute) },
            OpCode(0xBD, "LDA", 3, 4/*+1 if page crossed*/) { lda(AbsoluteX) },
            OpCode(0xB9, "LDA", 3, 4/*+1 if page crossed*/) { lda(AbsoluteY) },
            OpCode(0xA1, "LDA", 2, 6) { lda(IndirectX) },
            OpCode(0xB1, "LDA", 2, 5/*+1 if page crossed*/) { lda(IndirectY) },

            OpCode(0xA2, "LDX", 2, 2) { ldx(Immediate) },
            OpCode(0xA6, "LDX", 2, 3) { ldx(ZeroPage) },
            OpCode(0xB6, "LDX", 2, 4) { ldx(ZeroPageY) },
            OpCode(0xAE, "LDX", 3, 4) { ldx(Absolute) },
            OpCode(0xBE, "LDX", 3, 4/*+1 if page crossed*/) { ldx(AbsoluteY) },

            OpCode(0xA0, "LDY", 2, 2) { ldy(Immediate) },
            OpCode(0xA4, "LDY", 2, 3) { ldy(ZeroPage) },
            OpCode(0xB4, "LDY", 2, 4) { ldy(ZeroPageX) },
            OpCode(0xAC, "LDY", 3, 4) { ldy(Absolute) },
            OpCode(0xBC, "LDY", 3, 4/*+1 if page crossed*/) { ldy(AbsoluteX) },

            OpCode(0x85, "STA", 2, 3) { sta(ZeroPage) },
            OpCode(0x95, "STA", 2, 4) { sta(ZeroPageX) },
            OpCode(0x8D, "STA", 3, 4) { sta(Absolute) },
            OpCode(0x9D, "STA", 3, 5) { sta(AbsoluteX) },
            OpCode(0x99, "STA", 3, 5) { sta(AbsoluteY) },
            OpCode(0x81, "STA", 2, 6) { sta(IndirectX) },
            OpCode(0x91, "STA", 2, 6) { sta(IndirectY) },

            OpCode(0x86, "STX", 2, 3) {
                val addr = getOperandAddress(ZeroPage)
                memWrite(addr, registerX)
            },
            OpCode(0x96, "STX", 2, 4) {
                val addr = getOperandAddress(ZeroPageY)
                memWrite(addr, registerX)
            },
            OpCode(0x8E, "STX", 3, 4) {
                val addr = getOperandAddress(Absolute)
                memWrite(addr, registerX)
            },

            OpCode(0x84, "STY", 2, 3) {
                val addr = getOperandAddress(ZeroPage)
                memWrite(addr, registerY)
            },
            OpCode(0x94, "STY", 2, 4) {
                val addr = getOperandAddress(ZeroPageX)
                memWrite(addr, registerY)
            },
            OpCode(0x8C, "STY", 3, 4) {
                val addr = getOperandAddress(Absolute)
                memWrite(addr, registerY)
            },

            // Flags clear
            OpCode(0xD8, "CLD", 1, 2) { removeFlag(CPUFlags.DECIMAL_MODE) },
            OpCode(0xF8, "SED", 1, 2) { insertFlag(CPUFlags.DECIMAL_MODE) },
            OpCode(0x58, "CLI", 1, 2) { removeFlag(CPUFlags.INTERRUPT_DISABLE) },
            OpCode(0x78, "SEI", 1, 2) { insertFlag(CPUFlags.INTERRUPT_DISABLE) },
            OpCode(0x18, "CLC", 1, 2) { removeFlag(CPUFlags.CARRY) },
            OpCode(0x38, "SEC", 1, 2) { insertFlag(CPUFlags.CARRY) },
            OpCode(0xB8, "CLV", 1, 2) { removeFlag(CPUFlags.OVERFLOW) },

            OpCode(0xAA, "TAX", 1, 2) { tax() },
            OpCode(0xA8, "TAY", 1, 2) { tay() },
            OpCode(0xBA, "TSX", 1, 2) { tsx() },
            OpCode(0x8A, "TXA", 1, 2) { txa() },
            OpCode(0x9A, "TXS", 1, 2) { txs() },
            OpCode(0x98, "TYA", 1, 2) { tya() },

            // Stack
            OpCode(0x48, "PHA", 1, 3) { stackPush(registerA) },
            OpCode(0x68, "PLA", 1, 4) { pla() },
            OpCode(0x08, "PHP", 1, 3) { php() },
            OpCode(0x28, "PLP", 1, 4) { plp() },
        )
            get() = field.toList()

        val OPCODES_MAP = CPU_OPS_CODES.associateBy { it.code }
            get() = field.toMap()
    }
}
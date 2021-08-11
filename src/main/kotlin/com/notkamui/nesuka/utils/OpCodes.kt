package com.notkamui.nesuka.utils

import com.notkamui.nesuka.core.AddressingMode
import com.notkamui.nesuka.core.AddressingMode.Absolute
import com.notkamui.nesuka.core.AddressingMode.AbsoluteX
import com.notkamui.nesuka.core.AddressingMode.AbsoluteY
import com.notkamui.nesuka.core.AddressingMode.Immediate
import com.notkamui.nesuka.core.AddressingMode.IndirectX
import com.notkamui.nesuka.core.AddressingMode.IndirectY
import com.notkamui.nesuka.core.AddressingMode.NoneAddressing
import com.notkamui.nesuka.core.AddressingMode.ZeroPage
import com.notkamui.nesuka.core.AddressingMode.ZeroPageX

class OpCode(
    code: Int,
    val mnemonic: String,
    len: Int,
    cycles: Int,
    val mode: AddressingMode,
) {
    val code = code.u8
    val len = len.u8
    val cycles = cycles.u8

    companion object {
        val CPU_OPS_CODES = listOf(
            OpCode(0x00, "BRK", 1, 7, NoneAddressing),
            OpCode(0xAA, "TAX", 1, 2, NoneAddressing),
            OpCode(0xE8, "INX", 1, 2, NoneAddressing),

            OpCode(0xA9, "LDA", 2, 2, Immediate),
            OpCode(0xA5, "LDA", 2, 3, ZeroPage),
            OpCode(0xB5, "LDA", 2, 4, ZeroPageX),
            OpCode(0xAD, "LDA", 3, 4, Absolute),
            OpCode(0xBD, "LDA", 3, 4/*+1 if page crossed*/, AbsoluteX),
            OpCode(0xB9, "LDA", 3, 4/*+1 if page crossed*/, AbsoluteY),
            OpCode(0xA1, "LDA", 2, 6, IndirectX),
            OpCode(0xB1, "LDA", 2, 5/*+1 if page crossed*/, IndirectY),

            OpCode(0x85, "STA", 2, 3, ZeroPage),
            OpCode(0x95, "STA", 2, 4, ZeroPageX),
            OpCode(0x8D, "STA", 3, 4, Absolute),
            OpCode(0x9D, "STA", 3, 5, AbsoluteX),
            OpCode(0x99, "STA", 3, 5, AbsoluteY),
            OpCode(0x81, "STA", 2, 6, IndirectX),
            OpCode(0x91, "STA", 2, 6, IndirectY),
        )
            get() = field.toList()

        val OPCODES_MAP = CPU_OPS_CODES.associateBy { it.code }
            get() = field.toMap()
    }
}
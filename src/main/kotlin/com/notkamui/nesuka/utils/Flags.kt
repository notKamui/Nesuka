package com.notkamui.nesuka.utils

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

object CPUFlags {
    val CARRY = 0b0000_0001.u8
    val ZERO = 0b0000_0010.u8
    val INTERRUPT_DISABLE = 0b0000_0100.u8
    val DECIMAL_MODE = 0b0000_1000.u8
    val BREAK = 0b0001_0000.u8
    val BREAK_2 = 0b0010_0000.u8
    val OVERFLOW = 0b0100_0000.u8
    val NEGATIVE = 0b1000_0000.u8
}

val STACK = 0x0100.u16
val STACK_RESET = 0xFD.u8
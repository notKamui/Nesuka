package com.notkamui.nesuka.utils

val Int.u8: UByte
    get() = toUByte()

val Int.u16: UShort
    get() = toUShort()

infix fun UShort.shl(bitshift: Int) = this.toInt() shl bitshift

infix fun UShort.shr(bitshift: Int) = this.toInt() shr bitshift
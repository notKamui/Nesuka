package com.notkamui.nesuka.utils

val Int.u8: UByte
    get() = toUByte()

val Int.u16: UShort
    get() = toUShort()

infix fun UShort.shl(bitshift: Int): Int = this.toInt() shl bitshift
infix fun UByte.shl(bitshift: Int): Int = this.toInt() shl bitshift

infix fun UShort.shr(bitshift: Int): Int = this.toInt() ushr bitshift
infix fun UByte.shr(bitshift: Int): Int = this.toInt() ushr bitshift
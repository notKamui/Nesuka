package com.notkamui.nesuka.core

class CPU {
    var registerA: UByte = 0u
        private set

    var status: UByte = 0u
        private set

    private var programCounter: UShort = 0u

    fun interpret(program: List<UByte>) {
        programCounter = 0u

        while (true) {
            val opcode = program[programCounter.toInt()]
            programCounter++

            when (opcode) {
                0xA9u.toUByte() -> lda(program[programCounter.toInt()])
                0x00u.toUByte() -> return // brk
                else -> TODO()
            }
        }
    }

    private fun lda(param: UByte) {
        programCounter++
        registerA = param

        status = if (registerA == 0u.toUByte()) {
            status or 0b0000_0010u
        } else {
            status and 0b1111_1101u
        }

        status = if (registerA and 0b1000_0000u != 0u.toUByte()) {
            status or 0b1000_0000u
        } else {
            status and 0b0111_1111u
        }
    }
}
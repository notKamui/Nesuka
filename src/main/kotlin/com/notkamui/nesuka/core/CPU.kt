package com.notkamui.nesuka.core

class CPU {
    var registerA: UByte = 0u
    var registerX: UByte = 0u
    var status: UByte = 0u
    var programCounter: UShort = 0u

    /**
     * Interprets a [program] (which is a list of [UByte]s and runs it
     */
    fun interpret(program: List<UByte>) {
        programCounter = 0u

        while (true) {
            val opcode = program[programCounter.toInt()]
            programCounter++

            when (opcode) {
                0xA9u.toUByte() -> {
                    programCounter++
                    lda(program[programCounter.toInt()])
                }
                0xAAu.toUByte() -> tax()
                0x00u.toUByte() -> return // brk
                else -> TODO()
            }
        }
    }

    /**
     * Sets the zero and negative flags according to a given register status
     */
    private fun updateZeroNegFlags(value: UByte) {
        status = if (value == 0u.toUByte()) {
            status or 0b0000_0010u
        } else {
            status and 0b1111_1101u
        }

        status = if (value and 0b1000_0000u != 0u.toUByte()) {
            status or 0b1000_0000u
        } else {
            status and 0b0111_1111u
        }
    }

    /**
     * 0xA9 ; LoaD Accumulator
     *
     * Takes one argument of one byte
     *
     * Loads a byte of memory into the accumulator
     * setting the zero and negative flags as appropriate.
     */
    private fun lda(param: UByte) {
        registerA = param
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
}
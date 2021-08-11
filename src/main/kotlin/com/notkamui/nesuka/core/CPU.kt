package com.notkamui.nesuka.core

import com.notkamui.nesuka.utils.u16
import com.notkamui.nesuka.utils.u8

class CPU {
    var registerA = 0.u8
    var registerX = 0.u8
    var status = 0.u8
    private var programCounter = 0.u16

    /**
     * Interprets a [program] (which is a list of [UByte]s and runs it
     */
    fun interpret(program: List<UByte>) {
        programCounter = 0.u16

        while (true) {
            val opcode = program[programCounter.toInt()]
            programCounter++

            when (opcode) {
                0xA9.u8 -> {
                    lda(program[programCounter.toInt()])
                    programCounter++
                }
                0xAA.u8 -> tax()
                0xE8.u8 -> inx()
                0x00.u8 -> return // brk
                else -> TODO()
            }
        }
    }

    /**
     * Sets the zero and negative flags according to a given register status
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

    /**
     * 0xE8 ; INcrement X register
     *
     * Adds one to the X register
     * setting the zero and negative flags as appropriate.
     */
    private fun inx() {
        registerX++
        updateZeroNegFlags(registerX)
    }
}
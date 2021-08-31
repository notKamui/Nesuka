package com.notkamui.nesuka.utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logger {
    companion object {
        fun nowAsString(showMS: Boolean = true, timeSep: Char = ':'): String =
            with(LocalDateTime.now()) {
                val fmt = DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy_HH${timeSep}mm${timeSep}ss${
                        if (showMS) ".SS"
                        else ""
                    }"
                )
                format(fmt)
            }
    }

    fun log(message: String)
}

class FileLogger : Logger {
    private val file = run {
        val dir = File("logs")
        if ((dir.exists() && dir.isDirectory) || !dir.exists()) {
            dir.mkdir()
        }

        val date = Logger.nowAsString(showMS = false, timeSep = '.')

        val file = File("logs", "$date-log.txt")
        file.createNewFile()
        file.appendText("Nesuka - NES Emulator - Log $date\n")
        file
    }

    override fun log(message: String) {
        file.appendText("$message\n")
    }
}

class PrintLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}
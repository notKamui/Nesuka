package com.notkamui.nesuka.utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logger {
    companion object {
        fun nowAsString(showMS: Boolean = true, timeSep: Char = ':'): String =
            LocalDateTime.now().run {
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

        val file = File("logs", "${Logger.nowAsString(showMS = false, timeSep = '.')}-log.txt")
        file.createNewFile()
        file
    }

    override fun log(message: String) {
        file.appendText("${Logger.nowAsString()}\t$message\n")
    }
}

class PrintLogger : Logger {
    override fun log(message: String) {
        println("${Logger.nowAsString()}\t$message")
    }
}
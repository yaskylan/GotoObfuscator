package org.g0to.utils

import java.io.*
import java.nio.charset.StandardCharsets

class TextWriter(filename: String) : Flushable, Closeable {
    private val writer = FileOutputStream(filename).bufferedWriter(StandardCharsets.UTF_8)

    fun text(s: String) {
        writer.write(s)
    }

    @JvmOverloads
    fun space(count: Int = 1) {
        text(" ".repeat(count))
    }

    fun newline() {
        writer.newLine()
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }
}

package org.g0to.test

import org.apache.commons.io.IOUtils
import org.g0to.test.target.Main
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

fun main() {
    writeTestJar()
    org.g0to.main(arrayOf("-c", "conf.json"))
}

fun writeTestJar() {
    JarOutputStream(BufferedOutputStream(FileOutputStream("testme.jar")), Manifest().apply {
        mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        mainAttributes[Attributes.Name.MAIN_CLASS] = "org.g0to.test.target.Main"
    }).use { jos ->
        fun getResourceAsStream(name: String): InputStream? {
            return Main::class.java.getResourceAsStream(name)
        }

        val basePath = "/org/g0to/test/target"

        getResourceAsStream(basePath).use {
            for (name in IOUtils.readLines(it, StandardCharsets.UTF_8)) {
                val absolutePath = "$basePath/$name"
                val classStream = requireNotNull(getResourceAsStream(absolutePath))

                jos.putNextEntry(JarEntry(absolutePath.substring(1)))
                classStream.transferTo(jos)
                jos.closeEntry()
            }
        }
    }
}
package org.g0to.test

import org.apache.commons.io.IOUtils
import org.g0to.test.target.Main
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

fun main(args: Array<String>) {
    writeTestJar()
    org.g0to.main(arrayOf("-c", args[0]))
}

fun writeTestJar() {
    JarOutputStream(BufferedOutputStream(FileOutputStream("input.jar")), Manifest().apply {
        mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        mainAttributes[Attributes.Name.MAIN_CLASS] = "org.g0to.test.target.Main"
    }).use { jos ->
        fun getResourceAsStream(name: String): InputStream? {
            return Main::class.java.getResourceAsStream(name)
        }

        fun transfer(src: Path, dst: JarOutputStream) {
            JarFile(src.toFile()).use {
                for (entry in it.entries()) {
                    if (entry.name == "META-INF/"
                        || entry.name == "META-INF/MANIFEST.MF") {
                        continue
                    }

                    dst.putNextEntry(entry)
                    it.getInputStream(entry).transferTo(dst)
                    dst.closeEntry()
                }
            }
        }

        // Transfer kotlin-stdlib.jar
        transfer(Path.of(KotlinVersion::class.java.protectionDomain.codeSource.location.toURI()), jos)
        // Transfer jetbrains_annotation.jar
        transfer(Path.of(org.jetbrains.annotations.Nullable::class.java.protectionDomain.codeSource.location.toURI()), jos)

        fun transferDirectory(path: String) {
            getResourceAsStream(path).use {
                for (name in IOUtils.readLines(it, StandardCharsets.UTF_8)) {
                    val absolutePath = "$path/$name"

                    jos.putNextEntry(JarEntry(absolutePath.substring(1)))
                    getResourceAsStream(absolutePath)!!.use { classStream -> classStream.transferTo(jos) }
                    jos.closeEntry()
                }
            }
        }

        transferDirectory("/org/g0to/test/target")
        transferDirectory("/org/g0to/test/target/nameobftest")
    }
}
package org.g0to.classloaders

import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.g0to.core.Core
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.walk

class ExtLoader(private val core: Core) : Closeable, ASMClassLoader {
    companion object {
        private val logger = LogManager.getLogger("ExtLoader")
    }

    private val extFiles = ArrayList<JarFile>()
    private val classMap = HashMap<String, ClassWrapper>()
    private var jrtLoader: JrtLoader? = null

    fun init() {
        if (core.conf.javaVersion > 8) {
            jrtLoader = JrtLoader(core)
        } else {
            for (dir in arrayOf("lib", "jre/lib")) {
                Files.walk(Path.of(core.conf.jdkPath, dir))
                    .filter { it.toString().endsWith(".jar") }
                    .forEach { addJar(it) }
            }
        }
    }

    fun addJar(path: Path) {
        if (extFiles.none { it.toString() == path.toString() }) {
            logger.trace("Adding library jar: {}", path)
            extFiles.add(JarFile(path.toFile()))
        } else {
            logger.warn("Duplicate lib: {}", path.toString())
        }
    }

    override fun getClassWrapper(name: String): ClassWrapper? {
        var classWrapper = classMap[name]

        if (classWrapper == null) {
            classWrapper = loadClass("$name.class")
        }

        if (classWrapper == null && jrtLoader != null) {
            classWrapper = loadClassFromJrt("$name.class")
        }

        if (classWrapper == null) {
            logger.trace("Class not found {}", name)
        } else {
            classMap[name] = classWrapper
        }

        return classWrapper
    }

    private fun loadClass(name: String): ClassWrapper? {
        for (extFile in extFiles) {
            val entry = extFile.getJarEntry(name)

            if (entry != null) {
                extFile.getInputStream(entry).use { eis ->
                    logger.trace("Loading class {} from jar {}", name, extFile.name)

                    return toClassWrapper(IOUtils.toByteArray(eis))
                }
            }
        }

        return null
    }

    private fun loadClassFromJrt(name: String): ClassWrapper? {
        val classBuffer = jrtLoader!!.findClass(name) ?: return null

        return toClassWrapper(classBuffer)
    }

    private fun toClassWrapper(classBuffer: ByteArray): ClassWrapper {
        val classReader = ClassReader(classBuffer)
        val classNode = ClassNode()

        classReader.accept(classNode, ClassReader.SKIP_FRAMES)

        return ClassWrapper(core, classNode, this)
    }

    fun addLibraryClass(classBuffer: ByteArray) {
        val classWrapper = toClassWrapper(classBuffer)

        classMap[classWrapper.getClassName()] = classWrapper
    }

    override fun close() {
        extFiles.forEach {
            IOUtils.closeQuietly(it)
        }
    }
}
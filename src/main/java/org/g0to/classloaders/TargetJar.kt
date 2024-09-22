package org.g0to.classloaders

import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.g0to.core.Core
import org.g0to.utils.ValueProcessor
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipException
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.io.path.name

class TargetJar(private val core: Core) : ASMClassLoader {
    companion object {
        private val logger = LogManager.getLogger("TargetJar")
    }

    private val processFunctions = EnumMap<JarProcessFunctionId, MutableList<ValueProcessor<*>>>(JarProcessFunctionId::class.java)
    private val classes = HashMap<String, ClassWrapper>()
    private val resources = ArrayList<String>()
    private val extractJars = ArrayList<Path>()

    init {
        JarProcessFunctionId.entries.forEach {
            processFunctions[it] = ArrayList()
        }
    }

    fun init() {
        JarFile(core.conf.inputPath).use { jarFile ->
            for (entry in jarFile.entries()) {
                parseEntry(jarFile, entry)
            }
        }

        for (s in core.conf.extractJars) {
            val path = Path.of(s).toAbsolutePath()

            if (Files.isDirectory(path)) {
                Files.walk(path)
                    .filter {
                        it != path && !it.endsWith(".jar")
                    }
                    .forEach {
                        extractJars.add(it.toAbsolutePath())
                    }
            } else {
                extractJars.add(path)
            }
        }
    }

    private fun parseEntry(jarFile: JarFile, entry: JarEntry) {
        val entryName = entry.name

        when {
            entryName.endsWith(".class") && isEligibleClass(entryName) -> {
                jarFile.getInputStream(entry).use { entryInputStream ->
                    addClass(entryName, IOUtils.toByteArray(entryInputStream))
                }
            }
            else -> {
                if (!entryName.endsWith('/')) {
                    resources.add(entryName)
                }
            }
        }
    }

    private fun isEligibleClass(resName: String): Boolean {
        val classFullName = resName.substring(0, resName.length - ".class".length)

        return !core.skipClasses.isExcludedClass(classFullName)
                && !classFullName.endsWith("module-info")
    }

    private fun addClass(entryName: String, classBuffer: ByteArray) {
        if (core.libraryClasses.isExcludedClass(entryName.substring(0, entryName.length - ".class".length))) {
            core.extLoader.addLibraryClass(classBuffer)
            resources.add(entryName)
            return
        }

        val classWrapper = ClassWrapper(core, ClassNode().apply {
            ClassReader(classBuffer).accept(this, ClassReader.SKIP_FRAMES)
        }, this)

        logger.trace("Loading class {}", classWrapper.getClassName())
        classes[classWrapper.getClassName()] = classWrapper
    }

    fun writeModified(out: OutputStream) {
        logger.info("Writing jar")

        val rawJar = JarFile(core.conf.inputPath)

        JarOutputStream(out).use { jos ->
            resources.forEach {
                val entry = JarEntry(processFunc(JarProcessFunctionId.RESOURCE_NAME, it))

                jos.putNextEntry(entry)
                rawJar.getInputStream(rawJar.getEntry(it)).transferTo(jos)
                jos.closeEntry()
            }

            classes.forEach {
                val entry = JarEntry(processFunc(JarProcessFunctionId.CLASS_NAME, it.value.classNode.name + ".class"))

                jos.putNextEntry(entry)
                jos.write(it.value.toByteArray())
                jos.closeEntry()
            }

            extractJars(jos)
        }
    }

    private fun extractJars(out: JarOutputStream) {
        for (path in extractJars) {
            logger.info("Extracting jar: ${path.name}")

            try {
                val jarFile = JarFile(path.toFile())

                for (entry in jarFile.entries()) {
                    try {
                        out.putNextEntry(JarEntry(entry))
                    } catch (e: ZipException) {
                        if (e.message!!.startsWith("duplicate entry:")) {
                            logger.warn(e.message)
                            continue
                        } else {
                            throw e
                        }
                    }

                    jarFile.getInputStream(entry).transferTo(out)
                    out.closeEntry()
                }
            } catch (e: Throwable) {
                logger.error("Can't extract jar ${path.name}", e)
            }
        }
    }

    fun registerProcessFunction(id: JarProcessFunctionId, func: ValueProcessor<*>) {
        processFunctions[id]!!.add(func)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> processFunc(id: JarProcessFunctionId, value: T): T {
        val functionList = processFunctions[id]!!

        if (functionList.isEmpty())
            return value

        var ret = value

        functionList.forEach {
            ret = (it as ValueProcessor<T>).process(ret)
        }

        return ret
    }

    fun updateClassKey(oldKey: String, newKey: String) {
        if (classes.containsKey(oldKey)) {
            classes[newKey] = classes.remove(oldKey)!!
        }
    }

    override fun getClassWrapper(name: String): ClassWrapper? {
        return classes[name]
    }

    fun getClasses(): Collection<ClassWrapper> {
        return classes.values
    }

    enum class JarProcessFunctionId {
        RESOURCE_NAME,
        CLASS_NAME
    }
}
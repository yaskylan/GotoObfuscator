package org.g0to.core

import com.google.gson.annotations.SerializedName
import org.apache.logging.log4j.LogManager
import org.g0to.classloaders.ExtLoader
import org.g0to.classloaders.GlobalClassManager
import org.g0to.classloaders.SyntheticClasses
import org.g0to.classloaders.TargetJar
import org.g0to.conf.Configuration
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.exclusion.ExclusionManager
import org.g0to.transformer.Transformer
import org.g0to.transformer.TransformerRegistry
import org.g0to.utils.AnalyzerExceptionDumper
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicVerifier
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path

class Core(
    val conf: Configuration
) {
    companion object {
        private val logger = LogManager.getLogger("Core")
    }

    val transformers = ArrayList<Transformer<*>>()
    val skipClasses = ExclusionManager(ExclusionManager.ExcludeSetting(conf.skipClasses, null, null))
    val libraryClasses = ExclusionManager(ExclusionManager.ExcludeSetting(conf.libraryClasses, null, null))
    val targetJar = TargetJar(this)
    val extLoader = ExtLoader(this)
    val syntheticClasses = SyntheticClasses(this)
    val globalClassManager = GlobalClassManager(this)

    fun init() {
        logger.info("Init")

        targetJar.init()
        extLoader.init()

        conf.libraries.forEach {
            addLibrary(Path.of(it).toAbsolutePath())
        }

        addTransformers()
    }

    private fun addLibrary(path: Path) {
        if (Files.isDirectory(path)) {
            Files.walk(path)
                .filter { it.toString().endsWith(".jar") }
                .forEach { extLoader.addJar(it) }
        } else {
            extLoader.addJar(path)
        }
    }

    private fun addTransformers() {
        for (field in conf.transformers::class.java.fields) {
            val annotation = field.getAnnotation(SerializedName::class.java)

            if (annotation != null && TransformerBaseSetting::class.java.isAssignableFrom(field.type)) {
                val serializedName = annotation.value
                val setting = field.get(conf.transformers)

                if (setting != null) {
                    val transformer =
                        TransformerRegistry.newTransformerByName(serializedName, setting as TransformerBaseSetting)

                    if (transformer == null) {
                        logger.warn("Transformer '$serializedName' not found")
                    } else {
                        transformers.add(transformer)
                    }
                }
            }
        }
    }

    fun run() {
        logger.info("Run transformers")

        for (transformer in transformers) {
            if (transformer.setting.disable) {
                continue
            }

            transformer.preRun()
            transformer.run(this)
        }

        if (conf.analyze) {
            logger.info("Analyze classes")

            foreachTargetMethods { classWrapper, methodNode ->
                try {
                    Analyzer(BasicVerifier()).analyzeAndComputeMaxs(classWrapper.getClassName(), methodNode)
                } catch (e: AnalyzerException) {
                    val dumper = AnalyzerExceptionDumper(e, classWrapper.classNode, methodNode)
                    dumper.parse()
                    dumper.print()

                    throw RuntimeException(e)
                }
            }
        }
    }

    inline fun foreachTargetMethods(block: (ClassWrapper, MethodNode) -> Unit) {
        foreachTargetClasses { classWrapper ->
            classWrapper.getMethods().forEach { method ->
                block(classWrapper, method)
            }
        }
    }

    inline fun foreachTargetClasses(block: (ClassWrapper) -> Unit) {
        for (classWrapper in targetJar.getClasses()) {
            block(classWrapper)
        }
    }

    fun done() {
        logger.info("Done")

        File(conf.outputPath).parentFile?.mkdirs()
        targetJar.writeModified(BufferedOutputStream(FileOutputStream(conf.outputPath)))
    }
}
package org.g0to.core

import com.google.gson.annotations.SerializedName
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.BiConsumer
import org.g0to.classloaders.ExtLoader
import org.g0to.classloaders.GlobalClassManager
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
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.name

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
            Files.list(path).forEach {
                if (it.name.endsWith(".jar")) {
                    addLibrary(it)
                }
            }
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

        logger.info("Analyzer classes")

        val analyzer = Analyzer(BasicVerifier())
        foreachTargetMethods { classWrapper, methodNode ->
            try {
                analyzer.analyzeAndComputeMaxs(classWrapper.getClassName(), methodNode)
            } catch (e: AnalyzerException) {
                val dumper = AnalyzerExceptionDumper(e, classWrapper.classNode, methodNode)
                dumper.parse()
                dumper.print()

                throw RuntimeException(e)
            }
        }
    }

    fun foreachTargetMethods(consumer: BiConsumer<ClassWrapper, MethodNode>) {
        for (classWrapper in targetJar.getClasses()) {
            for (method in classWrapper.classNode.methods) {
                consumer.accept(classWrapper, method)
            }
        }
    }

    fun foreachTargetClasses(consumer: Consumer<ClassWrapper>) {
        for (classWrapper in targetJar.getClasses()) {
            consumer.accept(classWrapper)
        }
    }

    fun done() {
        logger.info("Done")

        val output = BufferedOutputStream(FileOutputStream(conf.outputPath))
        targetJar.writeModified(output)
    }
}
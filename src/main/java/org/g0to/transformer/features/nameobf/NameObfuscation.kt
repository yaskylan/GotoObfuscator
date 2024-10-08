package org.g0to.transformer.features.nameobf

import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import org.g0to.classloaders.TargetJar
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.dictionary.Dictionary
import org.g0to.exclusion.ExclusionManager
import org.g0to.transformer.Transformer
import org.g0to.utils.TextWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NameObfuscation(
    setting: TransformerBaseSetting
) : Transformer<NameObfuscation.Setting>("NameObfuscation", setting as Setting) {
    class Setting(
        @SerializedName("exclude")
        val exclude: ExclusionManager.ExcludeSetting? = null,
        @SerializedName("mappingPath")
        val mappingPath: String = "mapping.txt",
        @SerializedName("mappingType")
        val mappingType: String = "Default",
        @SerializedName("package")
        val packageName: String = "",
        @SerializedName("renameClass")
        val renameClass: Boolean = true,
        @SerializedName("renameField")
        val renameField: Boolean = true,
        @SerializedName("renameMethod")
        val renameMethod: Boolean = true,
        @SerializedName("renameMain")
        val renameMain: Boolean = false,
        @SerializedName("multithreading")
        val multithreading: Boolean = true,
        @SerializedName("threadPoolSize")
        val threadPoolSize: Int = Runtime.getRuntime().availableProcessors()
    ) : TransformerBaseSetting()

    private val exclusionManager = ExclusionManager((setting as Setting).exclude)
    private val classTree = ClassTree()
    private val packageName = run {
        val settingPackage = (setting as Setting).packageName

        if (settingPackage.isEmpty() || settingPackage.last() == '/') {
            settingPackage
        } else {
            "$settingPackage/"
        }
    }

    override fun run(core: Core) {
        logger.info("Package: $packageName")
        logger.info("Initializing class tree")
        classTree.init(core)

        rename(core)
        writeMapping()
        remap()
    }

    private fun rename(core: Core) {
        logger.info("Picking name")

        val classDictionary = core.conf.dictionary.newDictionary()
        val fieldDictionary = core.conf.dictionary.newDictionary()
        val methodDictionary = core.conf.dictionary.newDictionary()
        val relativeMethodsMap = ConcurrentHashMap<MethodStruct, HashSet<MethodStruct>>()

        if (setting.renameMethod && setting.multithreading) {
            logger.info("Using multithreading, thread pool size is ${setting.threadPoolSize}")
            logger.info("Building relative methods")

            val executor = Executors.newFixedThreadPool(setting.threadPoolSize)

            for (classStruct in classTree.classes.values) {
                if (classStruct.isExternal()
                    || classStruct.isModule()) {
                    continue
                }

                for (method in classStruct.getMethods()) {
                    if (!method.shouldRename()
                        || exclusionManager.isExcludedMethod(method.owner.getClassName(), method.name(), method.desc())) {
                        continue
                    }

                    executor.execute {
                        logger.trace("[Multithreading] Build relative methods map for {}", method.owner.getClassName() + "." + method.name() + method.desc())

                        relativeMethodsMap[method] = HashSet<MethodStruct>().apply {
                            searchRelativeMethods(
                                method.owner,
                                method.name(),
                                method.desc(),
                                this,
                                HashSet()
                            )
                        }
                    }
                }
            }

            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        }

        for (classStruct in classTree.classes.values) {
            if (classStruct.isExternal()
                || classStruct.isModule()) {
                continue
            }

            if (setting.renameClass) {
                renameClass(classStruct, classDictionary)
            }

            if (setting.renameField) {
                for (field in classStruct.getFields()) {
                    renameField(field, fieldDictionary)
                }
            }

            if (setting.renameMethod) {
                for (method in classStruct.getMethods()) {
                    renameMethod(method, methodDictionary, relativeMethodsMap)
                }
            }
        }
    }

    private fun renameClass(classStruct: ClassStruct, dictionary: Dictionary) {
        if (!classStruct.shouldRename(setting) || classStruct.hasMappedName() || exclusionManager.isExcludedClass(classStruct.getClassName())) {
            return
        }

        val rawName = classStruct.getClassName()
        val symbolIndex = rawName.indexOf('$')

        if (symbolIndex == -1) {
            classStruct.mappedName = packageName + dictionary.randString()
        } else {
            val outerName = rawName.substring(0, symbolIndex)
            val innerName = rawName.substring(symbolIndex + 1)
            val outerStruct = classTree.classes[outerName]!!

            renameClass(outerStruct, dictionary)

            classStruct.mappedName =
                outerStruct.getFinalName() + '$' + (if (StringUtils.isNumeric(innerName)) innerName else dictionary.randString())
        }
    }

    private fun renameField(field: FieldStruct, dictionary: Dictionary) {
        if (!field.shouldRename() || exclusionManager.isExcludedField(field.owner.getClassName(), field.name(), field.desc())) {
            return
        }

        field.mappedName = dictionary.randString()
    }

    private fun renameMethod(method: MethodStruct, dictionary: Dictionary, relativesMethodsMap: ConcurrentHashMap<MethodStruct, HashSet<MethodStruct>>) {
        if (method.hasMappedName()
            || !method.shouldRename()
            || exclusionManager.isExcludedMethod(method.owner.getClassName(), method.name(), method.desc())) {
            return
        }

        if (method.isStatic()) {
            method.mappedName = dictionary.randString()
            return
        }

        val relativeMethods = if (relativesMethodsMap.isEmpty()) {
            logger.trace("Build relative methods map for {}", method.owner.getClassName() + "." + method.name() + method.desc())

            HashSet<MethodStruct>().apply {
                searchRelativeMethods(
                    method.owner,
                    method.name(),
                    method.desc(),
                    this,
                    HashSet()
                )
            }
        } else {
            relativesMethodsMap[method]!!
        }

        for (relativesMethod in relativeMethods) {
            if (relativesMethod.owner.isExternal()
                || relativesMethod.isNative()
                || exclusionManager.isExcludedMethod(relativesMethod.owner.getClassName(), relativesMethod.name(), relativesMethod.desc())) {
                return
            }
        }

        val mappedName = dictionary.randString()

        for (relativeMethod in relativeMethods) {
            relativeMethod.mappedName = mappedName
        }
    }

    private fun searchRelativeMethods(classStruct: ClassStruct,
                                      name: String,
                                      desc: String,
                                      targetSet: HashSet<MethodStruct>,
                                      visitedSet: HashSet<ClassStruct>) {
        if (visitedSet.contains(classStruct)) {
            return
        }
        visitedSet.add(classStruct)

        classStruct.getMethod(name, desc).also {
            if (it != null) {
                targetSet.add(it)
            }
        }

        if (classStruct.superClass != null) {
            searchRelativeMethods(classStruct.superClass, name, desc, targetSet, visitedSet)
        }

        for (iface in classStruct.interfaces) {
            searchRelativeMethods(iface, name, desc, targetSet, visitedSet)
        }

        for (subClass in classStruct.subClasses) {
            searchRelativeMethods(subClass, name, desc, targetSet, visitedSet)
        }
    }

    private fun remap() {
        logger.info("Remap")

        val treeRemapper = TreeRemapper(classTree)

        for (classStruct in classTree.classes.values) {
            if (classStruct.isExternal()) {
                continue
            }

            val newClassNode = ClassNode()
            val classRemapper = ClassRemapper(newClassNode, treeRemapper)
            val oldName = classStruct.getClassName()

            classStruct.classWrapper.classNode.accept(classRemapper)
            classStruct.classWrapper.reloadClassNode(newClassNode)

            newClassNode.sourceFile = newClassNode.name.split('/').last() + ".java"
            newClassNode.visibleAnnotations?.removeIf {
                it.desc == "Lkotlin/Metadata;"
            }
            newClassNode.invisibleAnnotations?.removeIf {
                it.desc == "Lkotlin/jvm/internal/SourceDebugExtension;"
            }

            if (classStruct.hasMappedName()) {
                (classStruct.classWrapper.classLoader as TargetJar).updateClassKey(oldName, classStruct.getFinalName())
            }
        }
    }

    private fun writeMapping() {
        logger.info("Writing mapping")

        val remapper = TreeRemapper(classTree)

        try {
            TextWriter(setting.mappingPath).use { tw ->
                for (c in classTree.classes.values) {
                    if (c.isExternal()) {
                        continue
                    }

                    tw.text(c.getClassName())
                    tw.text(" ---> ")
                    tw.text(c.getFinalName())
                    tw.newline()

                    if (!c.getFields().isEmpty()) {
                        tw.space(2)
                        tw.text("Fields:")
                        tw.newline()

                        for (f in c.getFields()) {
                            tw.space(4)
                            tw.text(f.desc())
                            tw.space()
                            tw.text(f.name())
                            tw.text(" ---> ")
                            tw.text(remapper.mapDesc(f.desc()))
                            tw.space()
                            tw.text(f.getFinalName())
                            tw.newline()
                        }
                    }

                    if (!c.getMethods().isEmpty()) {
                        tw.space(2)
                        tw.text("Methods:")
                        tw.newline()

                        for (m in c.getMethods()) {
                            tw.space(4)
                            tw.text(m.desc())
                            tw.space()
                            tw.text(m.name())
                            tw.text(" ---> ")
                            tw.text(remapper.mapMethodDesc(m.desc()))
                            tw.space()
                            tw.text(m.getFinalName())
                            tw.newline()
                        }
                    }

                    tw.newline()
                }
            }
        } catch (e: IOException) {
            logger.error("Unable to write mapping.", e)
        }
    }
}
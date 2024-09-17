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
import org.g0to.utils.Utils
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import java.io.IOException

class NameObfuscation(
    setting: TransformerBaseSetting
) : Transformer<NameObfuscation.Setting>("NameObfuscation", setting as Setting) {
    class Setting(
        @SerializedName("exclude")
        val exclude: ExclusionManager.ExcludeSetting? = null,
        @SerializedName("mappingPath")
        val mappingPath: String = "mapping.txt",
        @SerializedName("renameClass")
        val renameClass: Boolean = true,
        @SerializedName("renameField")
        val renameField: Boolean = true,
        @SerializedName("renameMethod")
        val renameMethod: Boolean = true,
        @SerializedName("renameMain")
        val renameMain: Boolean = false,
    ) : TransformerBaseSetting()

    private val exclusionManager = ExclusionManager((setting as Setting).exclude)
    private val classTree = ClassTree()

    override fun run(core: Core) {
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

        for (classStruct in classTree.classes.values) {
            if (classStruct.isExternal()) {
                continue
            }

            if (setting.renameClass) {
                renameClass(classStruct, classDictionary)
            }

            if (setting.renameField) {
                for (field in classStruct.fields.values) {
                    renameField(field, fieldDictionary)
                }
            }

            if (setting.renameMethod) {
                for (method in classStruct.methods.values) {
                    renameMethod(method, methodDictionary)
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
            classStruct.mappedName = dictionary.randString()
        } else {
            val outerName = rawName.substring(0, symbolIndex)
            val innerName = rawName.substring(symbolIndex + 1)
            val outerStruct = classTree.classes[outerName]!!

            renameClass(outerStruct, dictionary)

            classStruct.mappedName =
                outerStruct.getFinalName() + '$' + (if (StringUtils.isNumeric(innerName)) innerName else dictionary.randString())

            Utils.breakpoint()
        }
    }

    private fun renameField(field: FieldStruct, dictionary: Dictionary) {
        if (isEnumField(field) || exclusionManager.isExcludedField(field.owner.getClassName(), field.name(), field.desc())) {
            return
        }

        field.mappedName = dictionary.randString()
    }

    private fun isEnumField(field: FieldStruct): Boolean {
        if (field.owner.isEnum() && field.isStatic() && field.isFinal()) {
            if (field.isPublic()) {
                return field.desc().equals('L' + field.owner.getClassName() + ';')
            } else if (field.isPrivate()) {
                return field.name().equals("\$VALUES")
                        && field.desc().equals("[L" + field.owner.getClassName() + ';')
            }
        }

        return false
    }

    private fun renameMethod(method: MethodStruct, dictionary: Dictionary) {
        if (method.hasMappedName()
            || !method.shouldRename()
            || isEnumMethod(method)
            || exclusionManager.isExcludedMethod(method.owner.getClassName(), method.name(), method.desc())) {
            return
        }

        if (method.isStatic()) {
            method.mappedName = dictionary.randString()
            return
        }

        val relativesMethods = HashSet<MethodStruct>().apply {
            searchRelativesMethods(
                method.owner,
                method.name(),
                method.desc(),
                this,
                HashSet()
            )
        }

        for (relativesMethod in relativesMethods) {
            if (relativesMethod.owner.isExternal()
                || relativesMethod.isNative()
                || exclusionManager.isExcludedMethod(relativesMethod.owner.getClassName(), relativesMethod.name(), relativesMethod.desc())) {
                return
            }
        }

        val mappedName = dictionary.randString()

        for (relativesMethod in relativesMethods) {
            relativesMethod.mappedName = mappedName
        }
    }

    private fun searchRelativesMethods(classStruct: ClassStruct,
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
            searchRelativesMethods(classStruct.superClass, name, desc, targetSet, visitedSet)
        }

        for (iface in classStruct.interfaces) {
            searchRelativesMethods(iface, name, desc, targetSet, visitedSet)
        }

        for (subClass in classStruct.subClasses) {
            searchRelativesMethods(subClass, name, desc, targetSet, visitedSet)
        }
    }

    private fun isEnumMethod(method: MethodStruct): Boolean {
        if (method.owner.isEnum()) {
            if (method.isPublic() && method.isStatic()) {
                val ownerName = method.owner.getClassName()
                val methodName = method.name()
                val methodDesc = method.desc()

                return ((methodName == "values" || methodName == "\$values") && methodDesc == "()[L$ownerName;")
                        || (methodName == "valueOf" && methodDesc == "(Ljava/lang/String;)L$ownerName;")
            }
        }

        return false
    }

    private fun remap() {
        logger.info("Remap")

        val treeRemapper = TreeRemapper(classTree)

        for (classStruct in classTree.classes.values) {
            if (classStruct.isExternal()) {
                continue
            }

            val cacheNode = ClassNode()
            val classRemapper = ClassRemapper(cacheNode, treeRemapper)
            val oldName = classStruct.getClassName()

            classStruct.classWrapper.classNode.accept(classRemapper)
            classStruct.classWrapper.reloadClassNode(cacheNode)

            if (classStruct.hasMappedName()) {
                (classStruct.classWrapper.classLoader as TargetJar).updateClassKey(oldName, classStruct.getFinalName())
            }

            Utils.breakpoint()
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
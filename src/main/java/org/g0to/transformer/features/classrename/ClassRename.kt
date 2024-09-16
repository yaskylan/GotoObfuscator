package org.g0to.transformer.features.classrename

import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import org.g0to.classloaders.TargetJar
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.dictionary.Dictionary
import org.g0to.exclude.ExcludeManager
import org.g0to.transformer.Transformer
import org.g0to.utils.TextWriter
import org.g0to.utils.Utils
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import java.io.IOException

class ClassRename(
    setting: TransformerBaseSetting
) : Transformer<ClassRename.Setting>("ClassRename", setting as Setting) {
    class Setting(
        @SerializedName("exclude")
        val exclude: ExcludeManager.ExcludeSetting? = null,
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

    private val excludeManager = ExcludeManager((setting as Setting).exclude)
    private val classTree = ClassTree()

    override fun run(core: Core) {
        logger.info("Breakpoint start")
        classTree.init(core)
        rename(core)
        remap()
        writeMapping()
        logger.info("Breakpoint end")
    }

    private fun rename(core: Core) {
        val classDictionary = core.conf.dictionary.newDictionary()
        val fieldDictionary = core.conf.dictionary.newDictionary()

        for (classStruct in classTree.classes.values) {
            if (classStruct.isExternal()) {
                continue
            }

            renameClass(classStruct, classDictionary)

            for (field in classStruct.fields.values) {
                renameField(field, fieldDictionary)
            }
        }
    }

    private fun renameClass(classStruct: ClassStruct, dictionary: Dictionary) {
        // TODO Add exclude support

        if (!classStruct.shouldRename(setting) || classStruct.hasMappedName()) {
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
        if (isEnumField(field)) {
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

    private fun renameMethod() {
        // TODO
    }

    private fun remap() {
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
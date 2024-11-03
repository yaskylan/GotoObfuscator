package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.transformer.features.nameobf.ClassTree
import org.g0to.transformer.features.nameobf.NameObfuscation
import org.g0to.transformer.features.nameobf.TreeRemapper
import org.g0to.utils.ClassBuilder
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.extensions.*
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class Debug(
    setting: TransformerBaseSetting
) : Transformer<Debug.Setting>("Debug", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var classTree: ClassTree? = null

        core.transformers.find { it is NameObfuscation }.also { nameObf ->
            if (nameObf != null) {
                val classTreeField = NameObfuscation::class.java.getDeclaredField("classTree")
                classTreeField.isAccessible = true

                classTree = classTreeField.get(nameObf) as ClassTree
            }
        }

        core.foreachTargetClasses { cw ->
            cw.getMethods().asSequence()
                .filterNot { it.isAbstract() || it.isNative() }
                .forEach { mt ->
                    generateDebugInfoForMethod(classTree, cw, mt)
                }
        }

        core.syntheticClasses.addClassNode(generateDebugClass())
    }

    private fun generateDebugInfoForMethod(classTree: ClassTree?, cw: ClassWrapper, mt: MethodNode) {
        if (!mt.isInitializer()) {
            mt.visitAnnotation("LGotoAnnotation;", false).also { annotation ->
                var renameInfo: String? = null

                if (classTree == null) {
                    renameInfo = cw.getClassName() + "." + mt.name + mt.desc
                } else {
                    val classStruct = classTree.classes.values.find { it.mappedName == cw.getClassName() }

                    if (classStruct != null) {
                        val remapper = TreeRemapper(classTree)
                        val methodStruct = classStruct.methods.values.find {
                            it.mappedName == mt.name && remapper.mapMethodDesc(it.desc()) == mt.desc
                        }

                        if (methodStruct != null) {
                            renameInfo = cw.getClassName() + "." + mt.name + mt.desc + " ---> " + classStruct.rawName + "." + methodStruct.name() + methodStruct.desc()
                        }
                    }
                }

                if (renameInfo != null) {
                    annotation.visit("renameInfo", renameInfo)
                }
            }
        }

        mt.modify { buffer ->
            mt.instructions.asSequence()
                .filter { it is LineNumberNode }
                .forEach { instruction ->
                    buffer.insert(instruction, InstructionBuilder.buildInsnList {
                        ldc("Line " + (instruction as LineNumberNode).line)
                        invokestatic("GotoDebug", "___MARK___", "(Ljava/lang/String;)V")
                    })
                }
        }
    }

    private fun generateDebugClass(): ClassNode {
        val classBuilder = ClassBuilder().visit {
            version = Opcodes.V1_8
            access = Opcodes.ACC_PUBLIC
            name = "GotoDebug"
            superName = "java/lang/Object"
            sourceFile = "GotoDebug.java"
        }

        fun generatePrintln(varDesc: String, isArray: Boolean) {
            classBuilder.method {
                visit {
                    access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
                    name = "println"
                    desc = "(" + (if (isArray) "[" else "") + varDesc + ")V"
                }
                instructions {
                    label(LabelNode())

                    getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")

                    if (isArray) {
                        aload(0)
                        invokestatic("java/util/Arrays", "toString", "([$varDesc)Ljava/lang/String;")
                        invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V")
                    } else {
                        xload(Type.getType(varDesc), 0)
                        invokevirtual("java/io/PrintStream", "println", "($varDesc)V")
                    }

                    vreturn()
                }
            }
        }

        for (varDesc in arrayOf("Z", "C", "B", "S", "I", "F", "J", "D", "Ljava/lang/Object;")) {
            generatePrintln(varDesc, false)
            generatePrintln(varDesc, true)
        }

        classBuilder.method {
            visit {
                access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
                name = "___MARK___"
                desc = "(Ljava/lang/String;)V"
            }
            instructions {
                vreturn()
            }
        }

        return classBuilder.build()
    }
}
package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.transformer.features.nameobf.ClassTree
import org.g0to.transformer.features.nameobf.NameObfuscation
import org.g0to.transformer.features.nameobf.TreeRemapper
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier

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
            for (mt in cw.getMethods()) {
                if (Modifier.isAbstract(mt.access)
                    || Modifier.isNative(mt.access)) {
                    continue
                }

                val buffer = InstructionBuffer(mt)

                buffer.insertFirst(InstructionBuilder.buildInsnList {
                    if (classTree == null) {
                        ldc(cw.getClassName() + "." + mt.name + mt.desc)
                        invokeStatic("GotoDebug", "___MARK___", "(Ljava/lang/String;)V")
                    } else {
                        val classStruct = classTree!!.classes.values.find { it.mappedName == cw.getClassName() }

                        if (classStruct != null) {
                            val remapper = TreeRemapper(classTree!!)

                            when (mt.name) {
                                "<clinit>" -> {}
                                "<init>" -> {
                                    ldc(cw.getClassName()
                                            + "."
                                            + mt.name
                                            + mt.desc
                                            + " ---> "
                                            + classStruct.rawName
                                            + ".<init>"
                                            + remapper.mapMethodDesc(mt.desc))
                                    invokeStatic("GotoDebug", "___MARK___", "(Ljava/lang/String;)V")
                                }
                                else -> {
                                    val methodStruct = classStruct.methods.values.find { it.mappedName == mt.name && remapper.mapMethodDesc(it.desc()) == mt.desc }

                                    if (methodStruct != null) {
                                        ldc(cw.getClassName()
                                                + "."
                                                + mt.name
                                                + mt.desc
                                                + " ---> "
                                                + classStruct.rawName
                                                + "."
                                                + methodStruct.name()
                                                + methodStruct.desc())
                                        invokeStatic("GotoDebug", "___MARK___", "(Ljava/lang/String;)V")
                                    }
                                }
                            }
                        }
                    }
                })

                for (instruction in mt.instructions) {
                    if (instruction is LineNumberNode) {
                        buffer.insert(instruction, InstructionBuilder.buildInsnList {
                            ldc("Line " + instruction.line)
                            invokeStatic("GotoDebug", "___MARK___", "(Ljava/lang/String;)V")
                        })
                    }
                }

                buffer.apply()
            }
        }

        core.syntheticClasses.addClassNode(generateDebugClass())
    }

    private fun generateDebugClass(): ClassNode {
        val classNode = ClassNode().apply {
            visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "GotoDebug", null, "java/lang/Object", null)
            visitSource("GotoDebug.java", null)
        }

        fun generatePrintln(varDesc: String, isArray: Boolean): MethodNode {
            val methodNode = MethodNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "println", "(" + (if (isArray) "[" else "") + varDesc + ")V", null, null)

            methodNode.instructions.add(InstructionBuilder.buildInsnList {
                label(LabelNode())

                getStatic("java/lang/System", "out", "Ljava/io/PrintStream;")

                if (isArray) {
                    aload(0)
                    invokeStatic("java/util/Arrays", "toString", "([$varDesc)Ljava/lang/String;")
                    invokeVirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V")
                } else {
                    xload(Type.getType(varDesc), 0)
                    invokeVirtual("java/io/PrintStream", "println", "($varDesc)V")
                }

                vreturn()
            })

            return methodNode
        }

        for (varDesc in arrayOf("Z", "C", "B", "S", "I", "F", "J", "D", "Ljava/lang/Object;")) {
            classNode.methods.add(generatePrintln(varDesc, false))
            classNode.methods.add(generatePrintln(varDesc, true))
        }

        classNode.methods.add(MethodNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "___MARK___", "(Ljava/lang/String;)V", null, null).apply {
            instructions.insert(InsnNode(Opcodes.RETURN))
        })

        return classNode
    }
}
package org.g0to.transformer.features

import com.google.gson.annotations.SerializedName
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.dictionary.ClassDictionary
import org.g0to.transformer.Transformer
import org.g0to.utils.ASMUtils
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.Utils
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.concurrent.ThreadLocalRandom

class InvokeProxy(
    setting: TransformerBaseSetting
) : Transformer<InvokeProxy.Setting>("InvokeProxy", setting as Setting) {
    class Setting(
        @SerializedName("invoke")
        val invoke: Boolean = true,
        @SerializedName("field")
        val field: Boolean = true,
        @SerializedName("random")
        val random: Boolean = false
    ) : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulatedInvoke = 0
        var accumulatedField = 0

        core.foreachTargetClasses { classWrapper ->
            val dictionary = core.conf.dictionary.newClassDictionary(classWrapper)
            val methodMap = HashMap<String, MethodNode>()

            classWrapper.getMethods().forEach { method ->
                val buffer = InstructionBuffer(method)

                for (instruction in method.instructions) {
                    when (instruction) {
                        is MethodInsnNode -> {
                            if (!setting.invoke) {
                                continue
                            }

                            if (setting.random && ThreadLocalRandom.current().nextBoolean()) {
                                continue
                            }

                            if (Utils.isOneOfThese(
                                instruction.opcode,
                                Opcodes.INVOKESTATIC,
                                Opcodes.INVOKEVIRTUAL,
                                Opcodes.INVOKEINTERFACE
                            )) {
                                val isStatic = instruction.opcode == Opcodes.INVOKESTATIC
                                val desc = if (isStatic) {
                                    instruction.desc
                                } else {
                                    StringBuilder(instruction.desc).insert(1, Type.getObjectType(instruction.owner).descriptor).toString()
                                }
                                val mixName = mixName(instruction, desc)
                                val proxyMethod = methodMap.getOrPut("${if (isStatic) "invokestatic" else "invokevirtual"}_$mixName") {
                                    newProxyMethod(
                                        dictionary, desc,
                                        MethodInsnNode(
                                            instruction.opcode,
                                            instruction.owner,
                                            instruction.name,
                                            instruction.desc,
                                            isInterface(core, instruction)
                                        )
                                    )
                                }

                                buffer.replace(instruction, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    classWrapper.getClassName(),
                                    proxyMethod.name,
                                    proxyMethod.desc,
                                    classWrapper.isInterface()
                                ))

                                accumulatedInvoke++
                            }
                        }
                        is FieldInsnNode -> {
                            if (!setting.field) {
                                continue
                            }

                            if (setting.random && ThreadLocalRandom.current().nextBoolean()) {
                                continue
                            }

                            val isGet = Utils.isOneOfThese(instruction.opcode, Opcodes.GETSTATIC, Opcodes.GETFIELD)
                            val isStatic = Utils.isOneOfThese(instruction.opcode, Opcodes.GETSTATIC, Opcodes.PUTSTATIC)

                            if (!isGet && Utils.isOneOfThese(method.name, "<init>", "<clinit>")) {
                                continue
                            }

                            val desc = if (isGet) {
                                if (isStatic) {
                                    Type.getMethodDescriptor(Type.getType(instruction.desc))
                                } else {
                                    Type.getMethodDescriptor(Type.getType(instruction.desc), Type.getObjectType(instruction.owner))
                                }
                            } else {
                                if (isStatic) {
                                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(instruction.desc))
                                } else {
                                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(instruction.owner), Type.getType(instruction.desc))
                                }
                            }
                            val mixName = mixName(instruction, desc)
                            val operationName = if (isGet) {
                                if (isStatic) "getstatic" else "getfield"
                            } else {
                                if (isStatic) "putstatic" else "putfield"
                            }
                            val proxyMethod = methodMap.getOrPut("${operationName}_$mixName") {
                                newProxyMethod(dictionary, desc,
                                    FieldInsnNode(
                                        instruction.opcode,
                                        instruction.owner,
                                        instruction.name,
                                        instruction.desc
                                    )
                                )
                            }

                            buffer.replace(instruction, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                classWrapper.getClassName(),
                                proxyMethod.name,
                                proxyMethod.desc,
                                classWrapper.isInterface()
                            ))

                            accumulatedField++
                        }
                    }
                }

                buffer.apply()
            }

            for (proxyMethod in methodMap.values) {
                classWrapper.addMethod(proxyMethod)
            }
        }

        logger.info("$accumulatedInvoke invoke is proxied")
        logger.info("$accumulatedField field is proxied")
    }

    companion object {
        @JvmStatic
        private fun isInterface(core: Core, instruction: MethodInsnNode): Boolean {
            if (instruction.opcode == Opcodes.INVOKEINTERFACE) {
                return true
            }

            if (instruction.owner[0] == '[') {
                return false
            }

            return core.globalClassManager.getClassWrapperNonNull(instruction.owner).isInterface()
        }

        @JvmStatic
        private fun newProxyMethod(dictionary: ClassDictionary,
                                   desc: String,
                                   dest: AbstractInsnNode): MethodNode {
            val proxyMethod = MethodNode(ASMUtils.generateModifier(
                Opcodes.ACC_PRIVATE,
                Opcodes.ACC_STATIC
            ), dictionary.randMethodName(desc), desc, null, null)
            val builder = InstructionBuilder()

            setupArgument(proxyMethod, builder, desc)
            builder.ainsn(dest)
            setupReturn(builder, desc)

            proxyMethod.instructions.add(builder.build())

            return proxyMethod
        }

        @JvmStatic
        private fun setupArgument(method: MethodNode,
                                  builder: InstructionBuilder,
                                  desc: String) {
            var varIndex = 0

            for (t in Type.getArgumentTypes(desc)) {
                when (t.sort) {
                    Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                        builder.iload(varIndex)
                    }
                    Type.FLOAT -> {
                        builder.fload(varIndex)
                    }
                    Type.LONG -> {
                        builder.lload(varIndex)
                    }
                    Type.DOUBLE -> {
                        builder.dload(varIndex)
                    }
                    Type.ARRAY, Type.OBJECT -> {
                        builder.aload(varIndex)
                    }
                }

                varIndex += t.size
            }

            method.maxLocals = varIndex
        }

        @JvmStatic
        private fun setupReturn(builder: InstructionBuilder,
                                desc: String) {
            when (Type.getReturnType(desc).sort) {
                Type.VOID -> {
                    builder.returnn()
                }
                Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                    builder.ireturn()
                }
                Type.FLOAT -> {
                    builder.freturn()
                }
                Type.LONG -> {
                    builder.lreturn()
                }
                Type.DOUBLE -> {
                    builder.dreturn()
                }
                Type.ARRAY, Type.OBJECT -> {
                    builder.areturn()
                }
            }
        }

        @JvmStatic
        private fun mixName(n: MethodInsnNode, desc: String): String {
            return n.owner + ':' + n.name + ':' + desc
        }

        @JvmStatic
        private fun mixName(n: FieldInsnNode, desc: String): String {
            return n.owner + ':' + n.name + ':' + desc
        }
    }
}
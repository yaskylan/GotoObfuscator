package org.g0to.transformer.features

import com.google.gson.annotations.SerializedName
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.utils.MethodBuilder
import org.g0to.utils.extensions.modify
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.lang.invoke.CallSite
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

// Unstable
class IndyTransformer(
    setting: TransformerBaseSetting
) : Transformer<IndyTransformer.Setting>("IndyTransformer", setting as Setting) {
    class Setting(
        @SerializedName("bootstrapName")
        val bootstrapName: String = "gb"
    ) : TransformerBaseSetting()

    private class InvokeData(
        val index: Int,
        val owner: String,
        val name: String
    )

    private val invokeTypeMap = mapOf(
        Opcodes.INVOKESTATIC to 0x1FFFFFF2,
        Opcodes.INVOKEVIRTUAL to 0x1FFFFFF3
    )

    private val bootstrapDesc = MethodType.methodType(
        CallSite::class.java,
        MethodHandles.Lookup::class.java,
        String::class.java,
        MethodType::class.java,
        Long::class.java // data
    ).toMethodDescriptorString()

    override fun run(core: Core) {
        var accumulatedInvoke = 0

        core.foreachTargetClasses { classWrapper ->
            val bootstrapName = classWrapper.allocMethodName(setting.bootstrapName, bootstrapDesc)
            val invokeMap = LinkedHashMap<String, InvokeData>()

            classWrapper.getMethods().forEach { method -> method.modify { buffer ->
                fun processMethodInstruction(instruction: MethodInsnNode) {
                    if (instruction.opcode == Opcodes.INVOKESPECIAL
                        || instruction.opcode == Opcodes.INVOKEINTERFACE
                    ) {
                        return
                    }

                    val descriptor = if (instruction.opcode == Opcodes.INVOKESTATIC) {
                        instruction.desc
                    } else {
                        StringBuilder(instruction.desc).insert(1, Type.getObjectType(instruction.owner).descriptor)
                            .toString()
                    }
                    val invokeData = invokeMap.computeIfAbsent(instruction.owner + '.' + instruction.name) {
                        InvokeData(invokeMap.size, instruction.owner, instruction.name)
                    }

                    buffer.replace(
                        instruction, InvokeDynamicInsnNode(
                            "a",
                            descriptor,
                            Handle(
                                Opcodes.H_INVOKESTATIC,
                                classWrapper.getClassName(),
                                bootstrapName,
                                bootstrapDesc,
                                classWrapper.isInterface()
                            ),
                            (invokeData.index.toLong() shl 32) or invokeTypeMap[instruction.opcode]!!.toLong()
                        )
                    )

                    accumulatedInvoke++
                }

                method.instructions.forEach { instruction ->
                    when (instruction) {
                        is MethodInsnNode -> {
                            processMethodInstruction(instruction)
                        }
                    }
                }
            }}

            if (invokeMap.isNotEmpty()) {
                classWrapper.addMethod(
                    createBootstrap(
                        bootstrapName,
                        ArrayList(invokeMap.sequencedValues())
                    )
                )
            }
        }

        logger.info("Transformed $accumulatedInvoke invoke to invokedynamic")
    }

    private fun createBootstrap(bootstrapName: String,
                                invokeList: ArrayList<InvokeData>): MethodNode {
        val bootstrapMethod = MethodBuilder().visit {
            access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
            name = bootstrapName
            desc = bootstrapDesc
        }

        val varCaller = bootstrapMethod.allocSlot()
        bootstrapMethod.allocSlot()
        val varMethodType = bootstrapMethod.allocSlot()
        val varData = bootstrapMethod.allocSlot64()
        val varOwner = bootstrapMethod.allocSlot()
        val varName = bootstrapMethod.allocSlot()
        val varRefc = bootstrapMethod.allocSlot()
        val varMethodHandle = bootstrapMethod.allocSlot()

        val labelSwitchIndexJump = LabelNode()
        val labelSwitchInvokeTypeJump = LabelNode()

        bootstrapMethod.instructions {
            label(LabelNode())

            lload(varData)
            number(32)
            lushr()
            l2i()
            tableswitch(0, invokeList.size - 1, {
                anew("java/lang/IllegalStateException")
                dup()
                invokespecial("java/lang/IllegalStateException", "<init>", "()V")
                athrow()
            }) { index ->
                val invokeData = invokeList[index]

                ldc(invokeData.owner.replace('/', '.'))
                astore(varOwner)
                ldc(invokeData.name)
                astore(varName)
                agoto(labelSwitchIndexJump)
            }

            label(labelSwitchIndexJump)
            aload(varCaller)
            aload(varOwner)
            invokevirtual("java/lang/invoke/MethodHandles\$Lookup", "findClass", "(Ljava/lang/String;)Ljava/lang/Class;")
            astore(varRefc)

            lload(varData)
            number(0xFFFFFFFFL)
            land()
            l2i()
            tableswitch(0x1FFFFFF2, 0x1FFFFFF3, {
                anew("java/lang/IllegalStateException")
                dup()
                invokespecial("java/lang/IllegalStateException", "<init>", "()V")
                athrow()
            }) { index ->
                aload(varCaller)
                aload(varRefc)

                when (index) {
                    0 -> {
                        aload(varName)
                        aload(varMethodType)
                        invokevirtual("java/lang/invoke/MethodHandles\$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                    }
                    1 -> {
                        aload(varName)
                        aload(varMethodType)
                        number(0)
                        number(1)
                        invokevirtual("java/lang/invoke/MethodType", "dropParameterTypes", "(II)Ljava/lang/invoke/MethodType;")
                        invokevirtual("java/lang/invoke/MethodHandles\$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                    }
                }

                astore(varMethodHandle)
                agoto(labelSwitchInvokeTypeJump)
            }

            label(labelSwitchInvokeTypeJump)
            anew("java/lang/invoke/ConstantCallSite")
            dup()
            aload(varMethodHandle)
            invokespecial("java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V")
            areturn()
        }

        return bootstrapMethod.build()
    }
}
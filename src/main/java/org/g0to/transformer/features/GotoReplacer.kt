package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import java.util.concurrent.ThreadLocalRandom

class GotoReplacer(
    setting: TransformerBaseSetting
) : Transformer<GotoReplacer.Setting>("GotoReplacer", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetMethods { _, methodNode ->
            val buffer = InstructionBuffer(methodNode)
            var varIndex = -1
            var randNumber = 0

            for (instruction in methodNode.instructions) {
                if (instruction.opcode == Opcodes.GOTO) {
                    val n = instruction as JumpInsnNode

                    buffer.replace(n, InstructionBuilder.buildInsnList {
                        if (varIndex == -1) {
                            varIndex = methodNode.maxLocals++
                            randNumber = ThreadLocalRandom.current().nextInt()
                        }

                        val l1 = LabelNode()
                        val l2 = LabelNode()
                        val l3 = LabelNode()

                        label(l1)
                        iload(varIndex)
                        number(randNumber)
                        if_icmpeq(l3)
                        number(1)
                        ifeq(l1)

                        label(l2)
                        iload(varIndex)
                        number(randNumber)
                        if_icmpeq(l3)
                        number(0)
                        ifeq(instruction.label)
                        agoto(l3)

                        label(l3)
                        number(0)
                        ifeq(l2)
                        agoto(l1)
                    })

                    accumulated++
                }
            }

            if (varIndex != -1) {
                val initVar = InstructionBuilder.buildInsnList {
                    number(randNumber)
                    istore(varIndex)
                }

                if (methodNode.instructions.first is LabelNode) {
                    buffer.insert(methodNode.instructions.first, initVar)
                } else {
                    initVar.insertBefore(initVar.first, LabelNode())

                    buffer.insertBefore(methodNode.instructions.first, initVar)
                }
            }

            buffer.apply()
        }

        logger.info("Replaced $accumulated GOTO instructions")
    }
}
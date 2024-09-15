package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode

class GotoReplacer(
    setting: TransformerBaseSetting
) : Transformer<GotoReplacer.Setting>("GotoReplacer", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetMethods { _, methodNode ->
            val buffer = InstructionBuffer(methodNode)

            for (instruction in methodNode.instructions) {
                if (instruction.opcode == Opcodes.GOTO) {
                    val n = instruction as JumpInsnNode
                    val b = InstructionBuilder()
                    val l0 = LabelNode()
                    val l1 = LabelNode()
                    val l2 = LabelNode()

                    b.number(1).tableSwitch(0, 1, l2, l0, l1)
                    b.lable(l0).aconstNull().ifNull(l1).agoto(l2)
                    b.lable(l1).agoto(n.label)
                    b.lable(l2).aconstNull().athrow()

                    buffer.replace(n, b.build())

                    accumulated++
                }
            }

            buffer.apply()
        }

        logger.info("Replaced $accumulated GOTO instructions")
    }
}
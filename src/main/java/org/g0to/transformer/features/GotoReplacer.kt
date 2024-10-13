package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.extensions.modify
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode

class GotoReplacer(
    setting: TransformerBaseSetting
) : Transformer<GotoReplacer.Setting>("GotoReplacer", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetMethods { _, methodNode -> methodNode.modify { buffer ->
            for (instruction in methodNode.instructions) {
                if (instruction.opcode == Opcodes.GOTO) {
                    instruction as JumpInsnNode

                    buffer.replace(instruction, InstructionBuilder.buildInsnList {
                        number(0)
                        ifeq(instruction.label)
                        label(LabelNode()) {
                            aconst_null()
                            athrow()
                        }
                    })

                    accumulated++
                }
            }
        } }

        logger.info("Replaced $accumulated GOTO instructions")
    }
}
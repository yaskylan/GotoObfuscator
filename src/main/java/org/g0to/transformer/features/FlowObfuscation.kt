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
import kotlin.random.asKotlinRandom

class FlowObfuscation(
    setting: TransformerBaseSetting
) : Transformer<FlowObfuscation.Setting>("FlowObfuscation", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetMethods { _, method ->
            val buffer = InstructionBuffer(method)

            for (instruction in method.instructions) {
                when (instruction) {
                    is JumpInsnNode -> {
                        if (instruction.opcode == Opcodes.GOTO
                            || instruction.opcode == Opcodes.JSR) {
                            continue
                        }

                        val nextLabel = if (instruction.getNext() is LabelNode) {
                            instruction.getNext() as LabelNode
                        } else {
                            LabelNode()
                        }

                        val builder = InstructionBuilder()
                        val trapLabels = Array(ThreadLocalRandom.current().nextInt(5, 10)) { LabelNode() }
                        val insertPosition = ThreadLocalRandom.current().nextInt(trapLabels.size)

                        builder.block {
                            number(insertPosition)
                            tableSwitch(0, trapLabels.size - 1, trapLabels[insertPosition], *Array(trapLabels.size) {
                                if (it == insertPosition) {
                                    nextLabel
                                } else {
                                    trapLabels[it]
                                }
                            })
                        }

                        for (trapLabel in trapLabels) {
                            builder.label(trapLabel) {
                                getStatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                                number(ThreadLocalRandom.current().nextInt())
                                invokeVirtual("java/io/PrintStream", "println", "(I)V")
                                number(0)
                                ifeq(instruction.label)
                                agoto(trapLabels.random(ThreadLocalRandom.current().asKotlinRandom()))
                            }
                        }

                        if (nextLabel != instruction.next) {
                            builder.label(nextLabel)
                        }

                        buffer.insert(instruction, builder.build())

                        accumulated++
                    }
                }
            }

            buffer.apply()
        }

        logger.info("Processed $accumulated if instruction")
    }
}
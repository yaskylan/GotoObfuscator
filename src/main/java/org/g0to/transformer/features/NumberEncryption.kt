package org.g0to.transformer.features

import com.google.gson.annotations.SerializedName
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.ValueWrapper
import org.g0to.utils.extensions.modify
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.util.concurrent.ThreadLocalRandom

class NumberEncryption(
    setting: TransformerBaseSetting
) : Transformer<NumberEncryption.Setting>("NumberEncryption", setting as Setting) {
    class Setting(
        @SerializedName("doInt")
        val doInt: Boolean = true,
        @SerializedName("doLong")
        val doLong: Boolean = true,
        @SerializedName("doFloat")
        val doFloat: Boolean = true,
        @SerializedName("doDouble")
        val doDouble: Boolean = true
    ) : TransformerBaseSetting()

    override fun run(core: Core) {
        // TODO Harder encryption
        val accumulatedInt = ValueWrapper(0)
        val accumulatedLong = ValueWrapper(0)
        val accumulatedFloat = ValueWrapper(0)
        val accumulatedDouble = ValueWrapper(0)

        core.foreachTargetMethods { _, methodNode -> methodNode.modify { buffer ->
            for (instruction in methodNode.instructions) {
                when (instruction) {
                    is InsnNode -> {
                        when (instruction.opcode) {
                            in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> {
                                encryptInt(accumulatedInt, buffer, instruction, instruction.opcode - Opcodes.ICONST_0)
                            }
                            in Opcodes.LCONST_0..Opcodes.LCONST_1 -> {
                                encryptLong(accumulatedLong, buffer, instruction, (instruction.opcode - Opcodes.LCONST_0).toLong())
                            }
                            in Opcodes.FCONST_0..Opcodes.FCONST_2 -> {
                                encryptFloat(accumulatedFloat, buffer, instruction, (instruction.opcode - Opcodes.FCONST_0).toFloat())
                            }
                            in Opcodes.DCONST_0..Opcodes.DCONST_1 -> {
                                encryptDouble(accumulatedDouble, buffer, instruction, (instruction.opcode - Opcodes.DCONST_0).toDouble())
                            }
                        }
                    }
                    is IntInsnNode -> {
                        if (instruction.opcode in Opcodes.BIPUSH..Opcodes.SIPUSH) {
                            encryptInt(accumulatedInt, buffer, instruction, instruction.operand)
                        }
                    }
                    is LdcInsnNode -> {
                        when (val cst = instruction.cst) {
                            is Int -> {
                                encryptInt(accumulatedInt, buffer, instruction, cst)
                            }
                            is Long -> {
                                encryptLong(accumulatedLong, buffer, instruction, cst)
                            }
                            is Float -> {
                                encryptFloat(accumulatedFloat, buffer, instruction, cst)
                            }
                            is Double -> {
                                encryptDouble(accumulatedDouble, buffer, instruction, cst)
                            }
                        }
                    }
                }
            }
        } }

        logger.info("Encrypted ${accumulatedInt.value} Int, ${accumulatedLong.value} Long, ${accumulatedFloat.value} Float, ${accumulatedDouble.value} Double")
        logger.info("Total ${accumulatedInt.value + accumulatedLong.value + accumulatedFloat.value + accumulatedDouble.value}")
    }

    private fun encryptInt(accumulated: ValueWrapper<Int>, buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Int) {
        if (!setting.doInt) {
            return
        }

        encrypt32(buffer, instruction, value, false)
        accumulated.value++
    }

    private fun encryptLong(accumulated: ValueWrapper<Int>, buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Long) {
        if (!setting.doLong) {
            return
        }

        encrypt64(buffer, instruction, value, false)
        accumulated.value++
    }

    private fun encryptFloat(accumulated: ValueWrapper<Int>, buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Float) {
        if (!setting.doFloat) {
            return
        }

        encrypt32(buffer, instruction, java.lang.Float.floatToIntBits(value), true)
        accumulated.value++
    }

    private fun encryptDouble(accumulated: ValueWrapper<Int>, buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Double) {
        if (!setting.doDouble) {
            return
        }

        encrypt64(buffer, instruction, java.lang.Double.doubleToLongBits(value), true)
        accumulated.value++
    }

    private fun encrypt32(buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Int, isFloat: Boolean) {
        buffer.replace(instruction, InstructionBuilder.buildInsnList {
            val key = ThreadLocalRandom.current().nextInt()

            number(value xor key)
            number(key)
            ixor()
            /*
            val shiftBits = ThreadLocalRandom.current().nextInt(1, 31)

            number(value ushr shiftBits)
            number(shiftBits)
            ishl()
            number(value or ((1 shl shiftBits) - 1))
            ior()
            */

            if (isFloat) {
                invokestatic("java/lang/Float", "intBitsToFloat", "(I)F")
            }
        })
    }

    private fun encrypt64(buffer: InstructionBuffer, instruction: AbstractInsnNode, value: Long, isFloat: Boolean) {
        buffer.replace(instruction, InstructionBuilder.buildInsnList {
            val key = ThreadLocalRandom.current().nextLong()

            number(value xor key)
            number(key)
            lxor()

            if (isFloat) {
                invokestatic("java/lang/Double", "longBitsToDouble", "(J)D")
            }
        })
    }
}
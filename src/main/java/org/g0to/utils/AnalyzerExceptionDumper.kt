package org.g0to.utils

import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.AnalyzerException
import java.lang.reflect.Modifier

class AnalyzerExceptionDumper(
    private val error: AnalyzerException,
    private val classNode: ClassNode,
    private val methodNode: MethodNode
) {
    companion object {
        private val logger = LogManager.getLogger("AnalyzerDumper")
        private val opcodeNameMap = HashMap<Int, String>()

        init {
            opcodeNameMap[Opcodes.NOP] = "nop"
            opcodeNameMap[Opcodes.ACONST_NULL] = "aconst_null"
            opcodeNameMap[Opcodes.ICONST_M1] = "iconst_m1"
            opcodeNameMap[Opcodes.ICONST_0] = "iconst_0"
            opcodeNameMap[Opcodes.ICONST_1] = "iconst_1"
            opcodeNameMap[Opcodes.ICONST_2] = "iconst_2"
            opcodeNameMap[Opcodes.ICONST_3] = "iconst_3"
            opcodeNameMap[Opcodes.ICONST_4] = "iconst_4"
            opcodeNameMap[Opcodes.ICONST_5] = "iconst_5"
            opcodeNameMap[Opcodes.LCONST_0] = "lconst_0"
            opcodeNameMap[Opcodes.LCONST_1] = "lconst_1"
            opcodeNameMap[Opcodes.FCONST_0] = "fconst_0"
            opcodeNameMap[Opcodes.FCONST_1] = "fconst_1"
            opcodeNameMap[Opcodes.FCONST_2] = "fconst_2"
            opcodeNameMap[Opcodes.DCONST_0] = "dconst_0"
            opcodeNameMap[Opcodes.DCONST_1] = "dconst_1"
            opcodeNameMap[Opcodes.BIPUSH] = "bipush"
            opcodeNameMap[Opcodes.SIPUSH] = "sipush"
            opcodeNameMap[Opcodes.LDC] = "ldc"
            opcodeNameMap[Opcodes.ILOAD] = "iload"
            opcodeNameMap[Opcodes.LLOAD] = "lload"
            opcodeNameMap[Opcodes.FLOAD] = "fload"
            opcodeNameMap[Opcodes.DLOAD] = "dload"
            opcodeNameMap[Opcodes.ALOAD] = "aload"
            opcodeNameMap[Opcodes.IALOAD] = "iaload"
            opcodeNameMap[Opcodes.LALOAD] = "laload"
            opcodeNameMap[Opcodes.FALOAD] = "faload"
            opcodeNameMap[Opcodes.DALOAD] = "daload"
            opcodeNameMap[Opcodes.AALOAD] = "aaload"
            opcodeNameMap[Opcodes.BALOAD] = "baload"
            opcodeNameMap[Opcodes.CALOAD] = "caload"
            opcodeNameMap[Opcodes.SALOAD] = "saload"
            opcodeNameMap[Opcodes.ISTORE] = "istore"
            opcodeNameMap[Opcodes.LSTORE] = "lstore"
            opcodeNameMap[Opcodes.FSTORE] = "fstore"
            opcodeNameMap[Opcodes.DSTORE] = "dstore"
            opcodeNameMap[Opcodes.ASTORE] = "astore"
            opcodeNameMap[Opcodes.IASTORE] = "iastore"
            opcodeNameMap[Opcodes.LASTORE] = "lastore"
            opcodeNameMap[Opcodes.FASTORE] = "fastore"
            opcodeNameMap[Opcodes.DASTORE] = "dastore"
            opcodeNameMap[Opcodes.AASTORE] = "aastore"
            opcodeNameMap[Opcodes.BASTORE] = "bastore"
            opcodeNameMap[Opcodes.CASTORE] = "castore"
            opcodeNameMap[Opcodes.SASTORE] = "sastore"
            opcodeNameMap[Opcodes.POP] = "pop"
            opcodeNameMap[Opcodes.POP2] = "pop2"
            opcodeNameMap[Opcodes.DUP] = "dup"
            opcodeNameMap[Opcodes.DUP_X1] = "dup_x1"
            opcodeNameMap[Opcodes.DUP_X2] = "dup_x2"
            opcodeNameMap[Opcodes.DUP2] = "dup2"
            opcodeNameMap[Opcodes.DUP2_X1] = "dup2_x1"
            opcodeNameMap[Opcodes.DUP2_X2] = "dup2_x2"
            opcodeNameMap[Opcodes.SWAP] = "swap"
            opcodeNameMap[Opcodes.IADD] = "iadd"
            opcodeNameMap[Opcodes.LADD] = "ladd"
            opcodeNameMap[Opcodes.FADD] = "fadd"
            opcodeNameMap[Opcodes.DADD] = "dadd"
            opcodeNameMap[Opcodes.ISUB] = "isub"
            opcodeNameMap[Opcodes.LSUB] = "lsub"
            opcodeNameMap[Opcodes.FSUB] = "fsub"
            opcodeNameMap[Opcodes.DSUB] = "dsub"
            opcodeNameMap[Opcodes.IMUL] = "imul"
            opcodeNameMap[Opcodes.LMUL] = "lmul"
            opcodeNameMap[Opcodes.FMUL] = "fmul"
            opcodeNameMap[Opcodes.DMUL] = "dmul"
            opcodeNameMap[Opcodes.IDIV] = "idiv"
            opcodeNameMap[Opcodes.LDIV] = "ldiv"
            opcodeNameMap[Opcodes.FDIV] = "fdiv"
            opcodeNameMap[Opcodes.DDIV] = "ddiv"
            opcodeNameMap[Opcodes.IREM] = "irem"
            opcodeNameMap[Opcodes.LREM] = "lrem"
            opcodeNameMap[Opcodes.FREM] = "frem"
            opcodeNameMap[Opcodes.DREM] = "drem"
            opcodeNameMap[Opcodes.INEG] = "ineg"
            opcodeNameMap[Opcodes.LNEG] = "lneg"
            opcodeNameMap[Opcodes.FNEG] = "fneg"
            opcodeNameMap[Opcodes.DNEG] = "dneg"
            opcodeNameMap[Opcodes.ISHL] = "ishl"
            opcodeNameMap[Opcodes.LSHL] = "lshl"
            opcodeNameMap[Opcodes.ISHR] = "ishr"
            opcodeNameMap[Opcodes.LSHR] = "lshr"
            opcodeNameMap[Opcodes.IUSHR] = "iushr"
            opcodeNameMap[Opcodes.LUSHR] = "lushr"
            opcodeNameMap[Opcodes.IAND] = "iand"
            opcodeNameMap[Opcodes.LAND] = "land"
            opcodeNameMap[Opcodes.IOR] = "ior"
            opcodeNameMap[Opcodes.LOR] = "lor"
            opcodeNameMap[Opcodes.IXOR] = "ixor"
            opcodeNameMap[Opcodes.LXOR] = "lxor"
            opcodeNameMap[Opcodes.IINC] = "iinc"
            opcodeNameMap[Opcodes.I2L] = "i2l"
            opcodeNameMap[Opcodes.I2F] = "i2f"
            opcodeNameMap[Opcodes.I2D] = "i2d"
            opcodeNameMap[Opcodes.L2I] = "l2i"
            opcodeNameMap[Opcodes.L2F] = "l2f"
            opcodeNameMap[Opcodes.L2D] = "l2d"
            opcodeNameMap[Opcodes.F2I] = "f2i"
            opcodeNameMap[Opcodes.F2L] = "f2l"
            opcodeNameMap[Opcodes.F2D] = "f2d"
            opcodeNameMap[Opcodes.D2I] = "d2i"
            opcodeNameMap[Opcodes.D2L] = "d2l"
            opcodeNameMap[Opcodes.D2F] = "d2f"
            opcodeNameMap[Opcodes.I2B] = "i2b"
            opcodeNameMap[Opcodes.I2C] = "i2c"
            opcodeNameMap[Opcodes.I2S] = "i2s"
            opcodeNameMap[Opcodes.LCMP] = "lcmp"
            opcodeNameMap[Opcodes.FCMPL] = "fcmpl"
            opcodeNameMap[Opcodes.FCMPG] = "fcmpg"
            opcodeNameMap[Opcodes.DCMPL] = "dcmpl"
            opcodeNameMap[Opcodes.DCMPG] = "dcmpg"
            opcodeNameMap[Opcodes.IFEQ] = "ifeq"
            opcodeNameMap[Opcodes.IFNE] = "ifne"
            opcodeNameMap[Opcodes.IFLT] = "iflt"
            opcodeNameMap[Opcodes.IFGE] = "ifge"
            opcodeNameMap[Opcodes.IFGT] = "ifgt"
            opcodeNameMap[Opcodes.IFLE] = "ifle"
            opcodeNameMap[Opcodes.IF_ICMPEQ] = "if_icmpeq"
            opcodeNameMap[Opcodes.IF_ICMPNE] = "if_icmpne"
            opcodeNameMap[Opcodes.IF_ICMPLT] = "if_icmplt"
            opcodeNameMap[Opcodes.IF_ICMPGE] = "if_icmpge"
            opcodeNameMap[Opcodes.IF_ICMPGT] = "if_icmpgt"
            opcodeNameMap[Opcodes.IF_ICMPLE] = "if_icmple"
            opcodeNameMap[Opcodes.IF_ACMPEQ] = "if_acmpeq"
            opcodeNameMap[Opcodes.IF_ACMPNE] = "if_acmpne"
            opcodeNameMap[Opcodes.GOTO] = "goto"
            opcodeNameMap[Opcodes.JSR] = "jsr"
            opcodeNameMap[Opcodes.RET] = "ret"
            opcodeNameMap[Opcodes.TABLESWITCH] = "tableswitch"
            opcodeNameMap[Opcodes.LOOKUPSWITCH] = "lookupswitch"
            opcodeNameMap[Opcodes.IRETURN] = "ireturn"
            opcodeNameMap[Opcodes.LRETURN] = "lreturn"
            opcodeNameMap[Opcodes.FRETURN] = "freturn"
            opcodeNameMap[Opcodes.DRETURN] = "dreturn"
            opcodeNameMap[Opcodes.ARETURN] = "areturn"
            opcodeNameMap[Opcodes.RETURN] = "return"
            opcodeNameMap[Opcodes.GETSTATIC] = "getstatic"
            opcodeNameMap[Opcodes.PUTSTATIC] = "putstatic"
            opcodeNameMap[Opcodes.GETFIELD] = "getfield"
            opcodeNameMap[Opcodes.PUTFIELD] = "putfield"
            opcodeNameMap[Opcodes.INVOKEVIRTUAL] = "invokevirtual"
            opcodeNameMap[Opcodes.INVOKESPECIAL] = "invokespecial"
            opcodeNameMap[Opcodes.INVOKESTATIC] = "invokestatic"
            opcodeNameMap[Opcodes.INVOKEINTERFACE] = "invokeinterface"
            opcodeNameMap[Opcodes.INVOKEDYNAMIC] = "invokedynamic"
            opcodeNameMap[Opcodes.NEW] = "new"
            opcodeNameMap[Opcodes.NEWARRAY] = "newarray"
            opcodeNameMap[Opcodes.ANEWARRAY] = "anewarray"
            opcodeNameMap[Opcodes.ARRAYLENGTH] = "arraylength"
            opcodeNameMap[Opcodes.ATHROW] = "athrow"
            opcodeNameMap[Opcodes.CHECKCAST] = "checkcast"
            opcodeNameMap[Opcodes.INSTANCEOF] = "instanceof"
            opcodeNameMap[Opcodes.MONITORENTER] = "monitorenter"
            opcodeNameMap[Opcodes.MONITOREXIT] = "monitorexit"
            opcodeNameMap[Opcodes.MULTIANEWARRAY] = "multianewarray"
            opcodeNameMap[Opcodes.IFNULL] = "ifnull"
            opcodeNameMap[Opcodes.IFNONNULL] = "ifnonnull"
        }
    }

    private val messageBuffer = ArrayList<String>()
    private val labelIndexMap = HashMap<LabelNode, Int>()

    fun parse() {
        parseLabels()

        messageBuffer.add("==============Instruction Dump==============")
        messageBuffer.add("MethodInfo:")
        messageBuffer.add("Modifier: " + Modifier.toString(methodNode.access))
        messageBuffer.add("ID: ${classNode.name}.${methodNode.name}${methodNode.desc}")
        messageBuffer.add("")

        for ((index, instruction) in methodNode.instructions.withIndex()) {
            if (instruction == error.node) {
                messageBuffer.add("↓↓↓↓↓↓Error↓↓↓↓↓↓")
            }

            messageBuffer.add(covert(index, instruction))
        }

        messageBuffer.add("============================================")
    }

    fun print() {
        for (s in messageBuffer) {
            logger.error(s)
        }
    }

    private fun parseLabels() {
        var index = 0

        for (instruction in methodNode.instructions) {
            if (instruction is LabelNode) {
                labelIndexMap[instruction] = index++
            }
        }
    }

    private fun covert(index: Int, instruction: AbstractInsnNode): String {
        val builder = StringBuilder()

        builder.append('[').append(index).append("] ")

        if (instruction.opcode != -1) {
            builder.append(opcodeNameMap.getOrDefault(instruction.opcode, instruction.opcode.toString()))
        }

        when (instruction) {
            is IntInsnNode -> {
                builder.append(" Operand: ").append(instruction.operand)
            }
            is VarInsnNode -> {
                builder.append(" VarIndex: ").append(instruction.`var`)
            }
            is TypeInsnNode -> {
                builder.append(" Type: ").append(instruction.desc)
            }
            is FieldInsnNode -> {
                builder.append(" Owner: ").append(instruction.owner)
                    .append(" Name: ").append(instruction.name)
                    .append(" Descriptor: ").append(instruction.desc)
            }
            is MethodInsnNode -> {
                builder.append(" Owner: ").append(instruction.owner)
                    .append(" Name: ").append(instruction.name)
                    .append(" Descriptor: ").append(instruction.desc)
                    .append(" IsInterface: ").append(instruction.itf)
            }
            is InvokeDynamicInsnNode -> {
                builder.append(" Name: ").append(instruction.name)
                    .append(" Descriptor: ").append(instruction.desc)
                    .append(" BootstrapMethod: ").append(instruction.bsm.toString())
                    .append(" BootstrapArguments: ").append(instruction.bsmArgs.contentToString())
            }
            is JumpInsnNode -> {
                builder.append(" Label: ").append(labelIndexMap[instruction.label])
            }
            is LabelNode -> {
                builder.append("Label").append(labelIndexMap[instruction]).append(':')
            }
            is LdcInsnNode -> {
                builder.append(" Value: ").append(instruction.cst)
            }
            is IincInsnNode -> {
                builder.append(" Increment: ").append(instruction.incr)
            }
            is TableSwitchInsnNode -> {
                builder.append(" Min: ").append(instruction.min)
                    .append(" Max: ").append(instruction.max)
                    .append(" DefaultLabel: ").append(labelIndexMap[instruction.dflt])

                builder.append(" Labels: [")
                for (i in 0 until instruction.labels.size) {
                    builder.append(labelIndexMap[instruction.labels[i]])

                    if (i != instruction.labels.lastIndex) {
                        builder.append(", ")
                    }
                }
                builder.append(']')
            }
            is LookupSwitchInsnNode -> {
                builder.append(" DefaultLabel: ").append(labelIndexMap[instruction.dflt])
                    .append(" Keys: ").append(instruction.keys)

                builder.append(" Labels: [")
                for (i in 0 until instruction.labels.size) {
                    builder.append(labelIndexMap[instruction.labels[i]])

                    if (i != instruction.labels.lastIndex) {
                        builder.append(", ")
                    }
                }
                builder.append(']')
            }
            is MultiANewArrayInsnNode -> {
                builder.append(" Descriptor: ").append(instruction.desc)
                    .append(" Dimension: ").append(instruction.dims)
            }
            is FrameNode -> {
                builder.append(" Type: ").append(instruction.type)
                    .append(" Stack: ").append(instruction.stack)
                    .append(" Local: ").append(instruction.local)
            }
            is LineNumberNode -> {
                builder.append("LineNumberNode")
                    .append(" StartLabel: ").append(labelIndexMap[instruction.start])
                    .append(" Line: ").append(instruction.line)
            }
        }

        return builder.toString()
    }
}
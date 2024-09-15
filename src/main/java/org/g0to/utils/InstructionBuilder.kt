package org.g0to.utils

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class InstructionBuilder {
    private val list = InsnList()

    fun build(): InsnList {
        return list
    }

    fun <T : Any> array(type: String, list: List<T>): InstructionBuilder {
        number(list.size)
            .anewArray(type)
            .dup()

        for (i in list.indices) {
            number(i).ldc(list[i]).aastore()

            if (i != list.size - 1) {
                dup()
            }
        }

        return this
    }

    fun <T : Any> array(type: String, array: Array<T>): InstructionBuilder {
        val len = array.size

        number(len)
            .anewArray(type)
            .dup()

        for (i in 0 until len) {
            number(i).ldc(array[i]).aastore()

            if (i != len - 1) {
                dup()
            }
        }

        return this
    }

    fun number(v: Int): InstructionBuilder {
        if (v in -1..5) {
            return insn(Opcodes.ICONST_0 + v)
        }

        if (v in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            return bipush(v)
        }

        if (v in Short.MIN_VALUE..Short.MAX_VALUE) {
            return sipush(v)
        }

        return ldc(v)
    }

    fun number(v: Long): InstructionBuilder {
        if (v == 0L) {
            return insn(Opcodes.LCONST_0)
        }

        if (v == 1L) {
            return insn(Opcodes.LCONST_1)
        }

        return ldc(v)
    }

    fun number(v: Float): InstructionBuilder {
        if (v == 0.0F) {
            return insn(Opcodes.FCONST_0)
        }

        if (v == 1.0F) {
            return insn(Opcodes.FCONST_1)
        }

        if (v == 2.0F) {
            return insn(Opcodes.FCONST_2)
        }

        return ldc(v)
    }

    fun number(v: Double): InstructionBuilder {
        if (v == 0.0) {
            return insn(Opcodes.DCONST_0)
        }

        if (v == 1.0) {
            return insn(Opcodes.DCONST_1)
        }

        return ldc(v)
    }

    fun aconstNull(): InstructionBuilder {
        insn(Opcodes.ACONST_NULL)

        return this
    }

    fun insn(opcode: Int): InstructionBuilder {
        list.add(InsnNode(opcode))

        return this
    }

    fun nop(): InstructionBuilder {
        return insn(Opcodes.NOP)
    }

    fun iaload(): InstructionBuilder {
        return insn(Opcodes.IALOAD)
    }

    fun laload(): InstructionBuilder {
        return insn(Opcodes.LALOAD)
    }

    fun faload(): InstructionBuilder {
        return insn(Opcodes.FALOAD)
    }

    fun daload(): InstructionBuilder {
        return insn(Opcodes.DALOAD)
    }

    fun aaload(): InstructionBuilder {
        return insn(Opcodes.AALOAD)
    }

    fun baload(): InstructionBuilder {
        return insn(Opcodes.BALOAD)
    }

    fun caload(): InstructionBuilder {
        return insn(Opcodes.CALOAD)
    }

    fun saload(): InstructionBuilder {
        return insn(Opcodes.SALOAD)
    }

    fun iastore(): InstructionBuilder {
        return insn(Opcodes.IASTORE)
    }

    fun lastore(): InstructionBuilder {
        return insn(Opcodes.LASTORE)
    }

    fun fastore(): InstructionBuilder {
        return insn(Opcodes.FASTORE)
    }

    fun dastore(): InstructionBuilder {
        return insn(Opcodes.DASTORE)
    }

    fun aastore(): InstructionBuilder {
        return insn(Opcodes.AASTORE)
    }

    fun bastore(): InstructionBuilder {
        return insn(Opcodes.BASTORE)
    }

    fun castore(): InstructionBuilder {
        return insn(Opcodes.CASTORE)
    }

    fun sastore(): InstructionBuilder {
        return insn(Opcodes.SASTORE)
    }

    fun pop(): InstructionBuilder {
        return insn(Opcodes.POP)
    }

    fun pop2(): InstructionBuilder {
        return insn(Opcodes.POP2)
    }

    fun dup(): InstructionBuilder {
        return insn(Opcodes.DUP)
    }

    fun dupx1(): InstructionBuilder {
        return insn(Opcodes.DUP_X1)
    }

    fun dupx2(): InstructionBuilder {
        return insn(Opcodes.DUP_X2)
    }

    fun dup2(): InstructionBuilder {
        return insn(Opcodes.DUP2)
    }

    fun dup2x1(): InstructionBuilder {
        return insn(Opcodes.DUP2_X1)
    }

    fun dup2x2(): InstructionBuilder {
        return insn(Opcodes.DUP2_X2)
    }

    fun swap(): InstructionBuilder {
        return insn(Opcodes.SWAP)
    }

    fun iadd(): InstructionBuilder {
        return insn(Opcodes.IADD)
    }

    fun ladd(): InstructionBuilder {
        return insn(Opcodes.LADD)
    }

    fun fadd(): InstructionBuilder {
        return insn(Opcodes.FADD)
    }

    fun dadd(): InstructionBuilder {
        return insn(Opcodes.DADD)
    }

    fun isub(): InstructionBuilder {
        return insn(Opcodes.ISUB)
    }

    fun lsub(): InstructionBuilder {
        return insn(Opcodes.LSUB)
    }

    fun fsub(): InstructionBuilder {
        return insn(Opcodes.FSUB)
    }

    fun dsub(): InstructionBuilder {
        return insn(Opcodes.DSUB)
    }

    fun imul(): InstructionBuilder {
        return insn(Opcodes.IMUL)
    }

    fun lmul(): InstructionBuilder {
        return insn(Opcodes.LMUL)
    }

    fun fmul(): InstructionBuilder {
        return insn(Opcodes.FMUL)
    }

    fun dmul(): InstructionBuilder {
        return insn(Opcodes.DMUL)
    }

    fun idiv(): InstructionBuilder {
        return insn(Opcodes.IDIV)
    }

    fun ldiv(): InstructionBuilder {
        return insn(Opcodes.LDIV)
    }

    fun fdiv(): InstructionBuilder {
        return insn(Opcodes.FDIV)
    }

    fun ddiv(): InstructionBuilder {
        return insn(Opcodes.DDIV)
    }

    fun irem(): InstructionBuilder {
        return insn(Opcodes.IREM)
    }

    fun lrem(): InstructionBuilder {
        return insn(Opcodes.LREM)
    }

    fun frem(): InstructionBuilder {
        return insn(Opcodes.FREM)
    }

    fun drem(): InstructionBuilder {
        return insn(Opcodes.DREM)
    }

    fun ineg(): InstructionBuilder {
        return insn(Opcodes.INEG)
    }

    fun lneg(): InstructionBuilder {
        return insn(Opcodes.LNEG)
    }

    fun fneg(): InstructionBuilder {
        return insn(Opcodes.FNEG)
    }

    fun dneg(): InstructionBuilder {
        return insn(Opcodes.DNEG)
    }

    fun ishl(): InstructionBuilder {
        return insn(Opcodes.ISHL)
    }

    fun lshl(): InstructionBuilder {
        return insn(Opcodes.LSHL)
    }

    fun ishr(): InstructionBuilder {
        return insn(Opcodes.ISHR)
    }

    fun lshr(): InstructionBuilder {
        return insn(Opcodes.LSHR)
    }

    fun iushr(): InstructionBuilder {
        return insn(Opcodes.IUSHR)
    }

    fun lushr(): InstructionBuilder {
        return insn(Opcodes.LUSHR)
    }

    fun iand(): InstructionBuilder {
        return insn(Opcodes.IAND)
    }

    fun land(): InstructionBuilder {
        return insn(Opcodes.LAND)
    }

    fun ior(): InstructionBuilder {
        return insn(Opcodes.IOR)
    }

    fun lor(): InstructionBuilder {
        return insn(Opcodes.LOR)
    }

    fun ixor(): InstructionBuilder {
        return insn(Opcodes.IXOR)
    }

    fun lxor(): InstructionBuilder {
        return insn(Opcodes.LXOR)
    }

    fun i2l(): InstructionBuilder {
        return insn(Opcodes.I2L)
    }

    fun i2f(): InstructionBuilder {
        return insn(Opcodes.I2F)
    }

    fun i2d(): InstructionBuilder {
        return insn(Opcodes.I2D)
    }

    fun l2i(): InstructionBuilder {
        return insn(Opcodes.L2I)
    }

    fun l2f(): InstructionBuilder {
        return insn(Opcodes.L2F)
    }

    fun l2d(): InstructionBuilder {
        return insn(Opcodes.L2D)
    }

    fun f2i(): InstructionBuilder {
        return insn(Opcodes.F2I)
    }

    fun f2l(): InstructionBuilder {
        return insn(Opcodes.F2L)
    }

    fun f2d(): InstructionBuilder {
        return insn(Opcodes.F2D)
    }

    fun d2i(): InstructionBuilder {
        return insn(Opcodes.D2I)
    }

    fun d2l(): InstructionBuilder {
        return insn(Opcodes.D2L)
    }

    fun d2f(): InstructionBuilder {
        return insn(Opcodes.D2F)
    }

    fun i2b(): InstructionBuilder {
        return insn(Opcodes.I2B)
    }

    fun i2c(): InstructionBuilder {
        return insn(Opcodes.I2C)
    }

    fun i2s(): InstructionBuilder {
        return insn(Opcodes.I2S)
    }

    fun lcmp(): InstructionBuilder {
        return insn(Opcodes.LCMP)
    }

    fun fcmpl(): InstructionBuilder {
        return insn(Opcodes.FCMPL)
    }

    fun fcmpg(): InstructionBuilder {
        return insn(Opcodes.FCMPG)
    }

    fun dcmpl(): InstructionBuilder {
        return insn(Opcodes.DCMPL)
    }

    fun dcmpg(): InstructionBuilder {
        return insn(Opcodes.DCMPG)
    }

    fun ireturn(): InstructionBuilder {
        return insn(Opcodes.IRETURN)
    }

    fun lreturn(): InstructionBuilder {
        return insn(Opcodes.LRETURN)
    }

    fun freturn(): InstructionBuilder {
        return insn(Opcodes.FRETURN)
    }

    fun dreturn(): InstructionBuilder {
        return insn(Opcodes.DRETURN)
    }

    fun areturn(): InstructionBuilder {
        return insn(Opcodes.ARETURN)
    }

    fun returnn(): InstructionBuilder {
        return insn(Opcodes.RETURN)
    }

    fun arraylength(): InstructionBuilder {
        return insn(Opcodes.ARRAYLENGTH)
    }

    fun athrow(): InstructionBuilder {
        return insn(Opcodes.ATHROW)
    }

    fun monitorEnter(): InstructionBuilder {
        return insn(Opcodes.MONITORENTER)
    }

    fun monitorExit(): InstructionBuilder {
        return insn(Opcodes.MONITOREXIT)
    }

    fun intInsn(opcode: Int, operand: Int): InstructionBuilder {
        list.add(IntInsnNode(opcode, operand))

        return this
    }

    fun bipush(operand: Int): InstructionBuilder {
        return intInsn(Opcodes.BIPUSH, operand)
    }

    fun sipush(operand: Int): InstructionBuilder {
        return intInsn(Opcodes.SIPUSH, operand)
    }

    fun newArray(type: Int): InstructionBuilder {
        return intInsn(Opcodes.NEWARRAY, type)
    }

    fun varInsn(opcode: Int, index: Int): InstructionBuilder {
        list.add(VarInsnNode(opcode, index))

        return this
    }

    fun iload(index: Int): InstructionBuilder {
        return varInsn(Opcodes.ILOAD, index)
    }

    fun lload(index: Int): InstructionBuilder {
        return varInsn(Opcodes.LLOAD, index)
    }

    fun fload(index: Int): InstructionBuilder {
        return varInsn(Opcodes.FLOAD, index)
    }

    fun dload(index: Int): InstructionBuilder {
        return varInsn(Opcodes.DLOAD, index)
    }

    fun aload(index: Int): InstructionBuilder {
        return varInsn(Opcodes.ALOAD, index)
    }

    fun istore(index: Int): InstructionBuilder {
        return varInsn(Opcodes.ISTORE, index)
    }

    fun lstore(index: Int): InstructionBuilder {
        return varInsn(Opcodes.LSTORE, index)
    }

    fun fstore(index: Int): InstructionBuilder {
        return varInsn(Opcodes.FSTORE, index)
    }

    fun dstore(index: Int): InstructionBuilder {
        return varInsn(Opcodes.DSTORE, index)
    }

    fun astore(index: Int): InstructionBuilder {
        return varInsn(Opcodes.ASTORE, index)
    }

    fun ret(index: Int): InstructionBuilder {
        return varInsn(Opcodes.RET, index)
    }

    fun typeInsn(opcode: Int, desc: String): InstructionBuilder {
        list.add(TypeInsnNode(opcode, desc))

        return this
    }

    fun anew(desc: String): InstructionBuilder {
        return typeInsn(Opcodes.NEW, desc)
    }

    fun anewArray(desc: String): InstructionBuilder {
        return typeInsn(Opcodes.ANEWARRAY, desc)
    }

    fun checkCast(desc: String): InstructionBuilder {
        return typeInsn(Opcodes.CHECKCAST, desc)
    }

    fun instanceOf(desc: String): InstructionBuilder {
        return typeInsn(Opcodes.INSTANCEOF, desc)
    }

    fun fieldInsn(opcode: Int, owner: String, name: String, desc: String): InstructionBuilder {
        list.add(FieldInsnNode(opcode, owner, name, desc))

        return this
    }

    fun getStatic(owner: String, name: String, desc: String): InstructionBuilder {
        return fieldInsn(Opcodes.GETSTATIC, owner, name, desc)
    }

    fun putStatic(owner: String, name: String, desc: String): InstructionBuilder {
        return fieldInsn(Opcodes.PUTSTATIC, owner, name, desc)
    }

    fun getField(owner: String, name: String, desc: String): InstructionBuilder {
        return fieldInsn(Opcodes.GETFIELD, owner, name, desc)
    }

    fun putField(owner: String, name: String, desc: String): InstructionBuilder {
        return fieldInsn(Opcodes.PUTFIELD, owner, name, desc)
    }

    @JvmOverloads
    fun methodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String,
        isInterface: Boolean = opcode == Opcodes.INVOKEINTERFACE
    ): InstructionBuilder {
        list.add(MethodInsnNode(opcode, owner, name, desc, isInterface))

        return this
    }

    fun invokeVirtual(owner: String, name: String, desc: String): InstructionBuilder {
        return methodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, false)
    }

    fun invokeSpecial(owner: String, name: String, desc: String): InstructionBuilder {
        return methodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false)
    }

    fun invokeStatic(owner: String, name: String, desc: String): InstructionBuilder {
        return methodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false)
    }

    fun invokeInterface(owner: String, name: String, desc: String): InstructionBuilder {
        return methodInsn(Opcodes.INVOKEINTERFACE, owner, name, desc, true)
    }

    fun invokeDynamic(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any): InstructionBuilder {
        list.add(InvokeDynamicInsnNode(name, desc, bsm, *bsmArgs))

        return this
    }

    fun jumpInsn(opcode: Int, label: LabelNode): InstructionBuilder {
        list.add(JumpInsnNode(opcode, label))

        return this
    }

    fun ifeq(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFEQ, label)
    }

    fun ifne(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFNE, label)
    }

    fun iflt(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFLT, label)
    }

    fun ifge(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFGE, label)
    }

    fun ifgt(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFGT, label)
    }

    fun ifle(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFLE, label)
    }

    fun if_icmpeq(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPEQ, label)
    }

    fun if_icmpne(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPNE, label)
    }

    fun if_icmplt(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPLT, label)
    }

    fun if_icmpge(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPGE, label)
    }

    fun if_icmpgt(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPGT, label)
    }

    fun if_icmple(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ICMPLE, label)
    }

    fun if_acmpeq(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ACMPEQ, label)
    }

    fun if_acmpne(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IF_ACMPNE, label)
    }

    fun ifNull(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFNULL, label)
    }

    fun ifNonNull(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.IFNONNULL, label)
    }

    fun agoto(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.GOTO, label)
    }

    fun jsr(label: LabelNode): InstructionBuilder {
        return jumpInsn(Opcodes.JSR, label)
    }

    fun lable(label: LabelNode): InstructionBuilder {
        list.add(label)

        return this
    }

    fun ldc(value: Any): InstructionBuilder {
        list.add(LdcInsnNode(value))

        return this
    }

    fun iinc(varIndex: Int, increment: Int): InstructionBuilder {
        list.add(IincInsnNode(varIndex, increment))

        return this
    }

    fun tableSwitch(min: Int, max: Int, defaultLabel: LabelNode, vararg labels: LabelNode): InstructionBuilder {
        list.add(TableSwitchInsnNode(min, max, defaultLabel, *labels))

        return this
    }

    fun lookupSwitch(defaultLabel: LabelNode, keys: IntArray, labels: Array<LabelNode>): InstructionBuilder {
        list.add(LookupSwitchInsnNode(defaultLabel, keys, labels))

        return this
    }

    fun multiANewArray(desc: String, numDimensions: Int): InstructionBuilder {
        list.add(MultiANewArrayInsnNode(desc, numDimensions))

        return this
    }

    fun frame(type: Int, numLocal: Int, local: Array<Any>, numStack: Int, stack: Array<Any>): InstructionBuilder {
        list.add(FrameNode(type, numLocal, local, numStack, stack))

        return this
    }

    fun line(line: Int, start: LabelNode): InstructionBuilder {
        list.add(LineNumberNode(line, start))

        return this
    }

    fun ainsn(insn: AbstractInsnNode): InstructionBuilder {
        list.add(insn)

        return this
    }
}

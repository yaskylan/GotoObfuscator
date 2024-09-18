package org.g0to.utils

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class InstructionBuilder {
    companion object {
        @JvmStatic
        fun buildInsnList(builder: InstructionBuilder.() -> Unit): InsnList {
            return InstructionBuilder().apply(builder).build()
        }
    }

    private val list = InsnList()

    fun build(): InsnList {
        return list
    }

    fun block(build: InstructionBuilder.() -> Unit) {
        build(this)
    }

    fun <T : Any> array(newArray: () -> Unit,
                        store: T.() -> Unit,
                        array: Array<T>) {
        val len = array.size

        number(len)
        newArray()
        dup()

        for (i in 0 until len) {
            number(i)
            store(array[i])

            if (i != len - 1) {
                dup()
            }
        }
    }

    fun number(v: Int) {
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

    fun number(v: Long) {
        if (v == 0L) {
            return insn(Opcodes.LCONST_0)
        }

        if (v == 1L) {
            return insn(Opcodes.LCONST_1)
        }

        return ldc(v)
    }

    fun number(v: Float) {
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

    fun number(v: Double) {
        if (v == 0.0) {
            return insn(Opcodes.DCONST_0)
        }

        if (v == 1.0) {
            return insn(Opcodes.DCONST_1)
        }

        return ldc(v)
    }

    fun aconstNull() {
        insn(Opcodes.ACONST_NULL)

    }

    fun insn(opcode: Int) {
        list.add(InsnNode(opcode))

    }

    fun nop() {
        return insn(Opcodes.NOP)
    }

    fun iaload() {
        return insn(Opcodes.IALOAD)
    }

    fun laload() {
        return insn(Opcodes.LALOAD)
    }

    fun faload() {
        return insn(Opcodes.FALOAD)
    }

    fun daload() {
        return insn(Opcodes.DALOAD)
    }

    fun aaload() {
        return insn(Opcodes.AALOAD)
    }

    fun baload() {
        return insn(Opcodes.BALOAD)
    }

    fun caload() {
        return insn(Opcodes.CALOAD)
    }

    fun saload() {
        return insn(Opcodes.SALOAD)
    }

    fun iastore() {
        return insn(Opcodes.IASTORE)
    }

    fun lastore() {
        return insn(Opcodes.LASTORE)
    }

    fun fastore() {
        return insn(Opcodes.FASTORE)
    }

    fun dastore() {
        return insn(Opcodes.DASTORE)
    }

    fun aastore() {
        return insn(Opcodes.AASTORE)
    }

    fun bastore() {
        return insn(Opcodes.BASTORE)
    }

    fun castore() {
        return insn(Opcodes.CASTORE)
    }

    fun sastore() {
        return insn(Opcodes.SASTORE)
    }

    fun pop() {
        return insn(Opcodes.POP)
    }

    fun pop2() {
        return insn(Opcodes.POP2)
    }

    fun dup() {
        return insn(Opcodes.DUP)
    }

    fun dupx1() {
        return insn(Opcodes.DUP_X1)
    }

    fun dupx2() {
        return insn(Opcodes.DUP_X2)
    }

    fun dup2() {
        return insn(Opcodes.DUP2)
    }

    fun dup2x1() {
        return insn(Opcodes.DUP2_X1)
    }

    fun dup2x2() {
        return insn(Opcodes.DUP2_X2)
    }

    fun swap() {
        return insn(Opcodes.SWAP)
    }

    fun iadd() {
        return insn(Opcodes.IADD)
    }

    fun ladd() {
        return insn(Opcodes.LADD)
    }

    fun fadd() {
        return insn(Opcodes.FADD)
    }

    fun dadd() {
        return insn(Opcodes.DADD)
    }

    fun isub() {
        return insn(Opcodes.ISUB)
    }

    fun lsub() {
        return insn(Opcodes.LSUB)
    }

    fun fsub() {
        return insn(Opcodes.FSUB)
    }

    fun dsub() {
        return insn(Opcodes.DSUB)
    }

    fun imul() {
        return insn(Opcodes.IMUL)
    }

    fun lmul() {
        return insn(Opcodes.LMUL)
    }

    fun fmul() {
        return insn(Opcodes.FMUL)
    }

    fun dmul() {
        return insn(Opcodes.DMUL)
    }

    fun idiv() {
        return insn(Opcodes.IDIV)
    }

    fun ldiv() {
        return insn(Opcodes.LDIV)
    }

    fun fdiv() {
        return insn(Opcodes.FDIV)
    }

    fun ddiv() {
        return insn(Opcodes.DDIV)
    }

    fun irem() {
        return insn(Opcodes.IREM)
    }

    fun lrem() {
        return insn(Opcodes.LREM)
    }

    fun frem() {
        return insn(Opcodes.FREM)
    }

    fun drem() {
        return insn(Opcodes.DREM)
    }

    fun ineg() {
        return insn(Opcodes.INEG)
    }

    fun lneg() {
        return insn(Opcodes.LNEG)
    }

    fun fneg() {
        return insn(Opcodes.FNEG)
    }

    fun dneg() {
        return insn(Opcodes.DNEG)
    }

    fun ishl() {
        return insn(Opcodes.ISHL)
    }

    fun lshl() {
        return insn(Opcodes.LSHL)
    }

    fun ishr() {
        return insn(Opcodes.ISHR)
    }

    fun lshr() {
        return insn(Opcodes.LSHR)
    }

    fun iushr() {
        return insn(Opcodes.IUSHR)
    }

    fun lushr() {
        return insn(Opcodes.LUSHR)
    }

    fun iand() {
        return insn(Opcodes.IAND)
    }

    fun land() {
        return insn(Opcodes.LAND)
    }

    fun ior() {
        return insn(Opcodes.IOR)
    }

    fun lor() {
        return insn(Opcodes.LOR)
    }

    fun ixor() {
        return insn(Opcodes.IXOR)
    }

    fun lxor() {
        return insn(Opcodes.LXOR)
    }

    fun i2l() {
        return insn(Opcodes.I2L)
    }

    fun i2f() {
        return insn(Opcodes.I2F)
    }

    fun i2d() {
        return insn(Opcodes.I2D)
    }

    fun l2i() {
        return insn(Opcodes.L2I)
    }

    fun l2f() {
        return insn(Opcodes.L2F)
    }

    fun l2d() {
        return insn(Opcodes.L2D)
    }

    fun f2i() {
        return insn(Opcodes.F2I)
    }

    fun f2l() {
        return insn(Opcodes.F2L)
    }

    fun f2d() {
        return insn(Opcodes.F2D)
    }

    fun d2i() {
        return insn(Opcodes.D2I)
    }

    fun d2l() {
        return insn(Opcodes.D2L)
    }

    fun d2f() {
        return insn(Opcodes.D2F)
    }

    fun i2b() {
        return insn(Opcodes.I2B)
    }

    fun i2c() {
        return insn(Opcodes.I2C)
    }

    fun i2s() {
        return insn(Opcodes.I2S)
    }

    fun lcmp() {
        return insn(Opcodes.LCMP)
    }

    fun fcmpl() {
        return insn(Opcodes.FCMPL)
    }

    fun fcmpg() {
        return insn(Opcodes.FCMPG)
    }

    fun dcmpl() {
        return insn(Opcodes.DCMPL)
    }

    fun dcmpg() {
        return insn(Opcodes.DCMPG)
    }

    fun ireturn() {
        return insn(Opcodes.IRETURN)
    }

    fun lreturn() {
        return insn(Opcodes.LRETURN)
    }

    fun freturn() {
        return insn(Opcodes.FRETURN)
    }

    fun dreturn() {
        return insn(Opcodes.DRETURN)
    }

    fun areturn() {
        return insn(Opcodes.ARETURN)
    }

    fun returnn() {
        return insn(Opcodes.RETURN)
    }

    fun arraylength() {
        return insn(Opcodes.ARRAYLENGTH)
    }

    fun athrow() {
        return insn(Opcodes.ATHROW)
    }

    fun monitorEnter() {
        return insn(Opcodes.MONITORENTER)
    }

    fun monitorExit() {
        return insn(Opcodes.MONITOREXIT)
    }

    fun intInsn(opcode: Int, operand: Int) {
        list.add(IntInsnNode(opcode, operand))
    }

    fun bipush(operand: Int) {
        return intInsn(Opcodes.BIPUSH, operand)
    }

    fun sipush(operand: Int) {
        return intInsn(Opcodes.SIPUSH, operand)
    }

    fun newArray(type: Int) {
        return intInsn(Opcodes.NEWARRAY, type)
    }

    fun varInsn(opcode: Int, index: Int) {
        list.add(VarInsnNode(opcode, index))

    }

    fun iload(index: Int) {
        return varInsn(Opcodes.ILOAD, index)
    }

    fun lload(index: Int) {
        return varInsn(Opcodes.LLOAD, index)
    }

    fun fload(index: Int) {
        return varInsn(Opcodes.FLOAD, index)
    }

    fun dload(index: Int) {
        return varInsn(Opcodes.DLOAD, index)
    }

    fun aload(index: Int) {
        return varInsn(Opcodes.ALOAD, index)
    }

    fun istore(index: Int) {
        return varInsn(Opcodes.ISTORE, index)
    }

    fun lstore(index: Int) {
        return varInsn(Opcodes.LSTORE, index)
    }

    fun fstore(index: Int) {
        return varInsn(Opcodes.FSTORE, index)
    }

    fun dstore(index: Int) {
        return varInsn(Opcodes.DSTORE, index)
    }

    fun astore(index: Int) {
        return varInsn(Opcodes.ASTORE, index)
    }

    fun ret(index: Int) {
        return varInsn(Opcodes.RET, index)
    }

    fun typeInsn(opcode: Int, desc: String) {
        list.add(TypeInsnNode(opcode, desc))
    }

    fun anew(desc: String) {
        return typeInsn(Opcodes.NEW, desc)
    }

    fun anewArray(desc: String) {
        return typeInsn(Opcodes.ANEWARRAY, desc)
    }

    fun checkCast(desc: String) {
        return typeInsn(Opcodes.CHECKCAST, desc)
    }

    fun instanceOf(desc: String) {
        return typeInsn(Opcodes.INSTANCEOF, desc)
    }

    fun fieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        list.add(FieldInsnNode(opcode, owner, name, desc))
    }

    fun getStatic(owner: String, name: String, desc: String) {
        return fieldInsn(Opcodes.GETSTATIC, owner, name, desc)
    }

    fun putStatic(owner: String, name: String, desc: String) {
        return fieldInsn(Opcodes.PUTSTATIC, owner, name, desc)
    }

    fun getField(owner: String, name: String, desc: String) {
        return fieldInsn(Opcodes.GETFIELD, owner, name, desc)
    }

    fun putField(owner: String, name: String, desc: String) {
        return fieldInsn(Opcodes.PUTFIELD, owner, name, desc)
    }

    @JvmOverloads
    fun methodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String,
        isInterface: Boolean = opcode == Opcodes.INVOKEINTERFACE
    ) {
        list.add(MethodInsnNode(opcode, owner, name, desc, isInterface))
    }

    fun invokeVirtual(owner: String, name: String, desc: String) {
        return methodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, false)
    }

    fun invokeSpecial(owner: String, name: String, desc: String) {
        return methodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false)
    }

    fun invokeStatic(owner: String, name: String, desc: String) {
        return methodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false)
    }

    fun invokeInterface(owner: String, name: String, desc: String) {
        return methodInsn(Opcodes.INVOKEINTERFACE, owner, name, desc, true)
    }

    fun invokeDynamic(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any) {
        list.add(InvokeDynamicInsnNode(name, desc, bsm, *bsmArgs))
    }

    fun jumpInsn(opcode: Int, label: LabelNode) {
        list.add(JumpInsnNode(opcode, label))
    }

    fun ifeq(label: LabelNode) {
        return jumpInsn(Opcodes.IFEQ, label)
    }

    fun ifne(label: LabelNode) {
        return jumpInsn(Opcodes.IFNE, label)
    }

    fun iflt(label: LabelNode) {
        return jumpInsn(Opcodes.IFLT, label)
    }

    fun ifge(label: LabelNode) {
        return jumpInsn(Opcodes.IFGE, label)
    }

    fun ifgt(label: LabelNode) {
        return jumpInsn(Opcodes.IFGT, label)
    }

    fun ifle(label: LabelNode) {
        return jumpInsn(Opcodes.IFLE, label)
    }

    fun if_icmpeq(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPEQ, label)
    }

    fun if_icmpne(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPNE, label)
    }

    fun if_icmplt(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPLT, label)
    }

    fun if_icmpge(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPGE, label)
    }

    fun if_icmpgt(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPGT, label)
    }

    fun if_icmple(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ICMPLE, label)
    }

    fun if_acmpeq(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ACMPEQ, label)
    }

    fun if_acmpne(label: LabelNode) {
        return jumpInsn(Opcodes.IF_ACMPNE, label)
    }

    fun ifNull(label: LabelNode) {
        return jumpInsn(Opcodes.IFNULL, label)
    }

    fun ifNonNull(label: LabelNode) {
        return jumpInsn(Opcodes.IFNONNULL, label)
    }

    fun agoto(label: LabelNode) {
        return jumpInsn(Opcodes.GOTO, label)
    }

    fun jsr(label: LabelNode) {
        return jumpInsn(Opcodes.JSR, label)
    }

    fun label(label: LabelNode) {
        list.add(label)
    }

    fun label(label: LabelNode, addInstructions: InstructionBuilder.() -> Unit) {
        label(label)
        addInstructions(this)
    }

    fun ldc(value: Any) {
        list.add(LdcInsnNode(value))
    }

    fun iinc(varIndex: Int, increment: Int) {
        list.add(IincInsnNode(varIndex, increment))
    }

    fun tableSwitch(min: Int, max: Int, defaultLabel: LabelNode, vararg labels: LabelNode) {
        list.add(TableSwitchInsnNode(min, max, defaultLabel, *labels))
    }

    fun lookupSwitch(defaultLabel: LabelNode, keys: IntArray, labels: Array<LabelNode>) {
        list.add(LookupSwitchInsnNode(defaultLabel, keys, labels))
    }

    fun multiANewArray(desc: String, numDimensions: Int) {
        list.add(MultiANewArrayInsnNode(desc, numDimensions))
    }

    fun frame(type: Int, numLocal: Int, local: Array<Any>, numStack: Int, stack: Array<Any>) {
        list.add(FrameNode(type, numLocal, local, numStack, stack))
    }

    fun line(line: Int, start: LabelNode) {
        list.add(LineNumberNode(line, start))
    }

    fun ainsn(insn: AbstractInsnNode) {
        list.add(insn)
    }
}

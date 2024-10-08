package org.g0to.utils

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
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

    fun size(): Int {
        return list.size()
    }

    fun clear() {
        list.clear()
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
        insn(Opcodes.NOP)
    }

    fun xaload(type: Type) {
        when (type.sort) {
            Type.BOOLEAN -> {
                iaload()
            }
            Type.CHAR -> {
                caload()
            }
            Type.BYTE -> {
                baload()
            }
            Type.SHORT -> {
                saload()
            }
            Type.INT -> {
                iaload()
            }
            Type.FLOAT -> {
                faload()
            }
            Type.LONG -> {
                laload()
            }
            Type.DOUBLE -> {
                daload()
            }
            Type.OBJECT, Type.ARRAY -> {
                aaload()
            }
            else -> {
                throw UnsupportedOperationException(type.toString())
            }
        }
    }

    fun iaload() {
        insn(Opcodes.IALOAD)
    }

    fun laload() {
        insn(Opcodes.LALOAD)
    }

    fun faload() {
        insn(Opcodes.FALOAD)
    }

    fun daload() {
        insn(Opcodes.DALOAD)
    }

    fun aaload() {
        insn(Opcodes.AALOAD)
    }

    fun baload() {
        insn(Opcodes.BALOAD)
    }

    fun caload() {
        insn(Opcodes.CALOAD)
    }

    fun saload() {
        insn(Opcodes.SALOAD)
    }

    fun xastore(type: Type) {
        when (type.sort) {
            Type.BOOLEAN -> {
                iastore()
            }
            Type.CHAR -> {
                castore()
            }
            Type.BYTE -> {
                bastore()
            }
            Type.SHORT -> {
                sastore()
            }
            Type.INT -> {
                iastore()
            }
            Type.FLOAT -> {
                fastore()
            }
            Type.LONG -> {
                lastore()
            }
            Type.DOUBLE -> {
                dastore()
            }
            Type.OBJECT, Type.ARRAY -> {
                aastore()
            }
            else -> {
                throw UnsupportedOperationException(type.toString())
            }
        }
    }

    fun iastore() {
        insn(Opcodes.IASTORE)
    }

    fun lastore() {
        insn(Opcodes.LASTORE)
    }

    fun fastore() {
        insn(Opcodes.FASTORE)
    }

    fun dastore() {
        insn(Opcodes.DASTORE)
    }

    fun aastore() {
        insn(Opcodes.AASTORE)
    }

    fun bastore() {
        insn(Opcodes.BASTORE)
    }

    fun castore() {
        insn(Opcodes.CASTORE)
    }

    fun sastore() {
        insn(Opcodes.SASTORE)
    }

    fun pop() {
        insn(Opcodes.POP)
    }

    fun pop2() {
        insn(Opcodes.POP2)
    }

    fun dup() {
        insn(Opcodes.DUP)
    }

    fun dupx1() {
        insn(Opcodes.DUP_X1)
    }

    fun dupx2() {
        insn(Opcodes.DUP_X2)
    }

    fun dup2() {
        insn(Opcodes.DUP2)
    }

    fun dup2x1() {
        insn(Opcodes.DUP2_X1)
    }

    fun dup2x2() {
        insn(Opcodes.DUP2_X2)
    }

    fun swap() {
        insn(Opcodes.SWAP)
    }

    fun iadd() {
        insn(Opcodes.IADD)
    }

    fun ladd() {
        insn(Opcodes.LADD)
    }

    fun fadd() {
        insn(Opcodes.FADD)
    }

    fun dadd() {
        insn(Opcodes.DADD)
    }

    fun isub() {
        insn(Opcodes.ISUB)
    }

    fun lsub() {
        insn(Opcodes.LSUB)
    }

    fun fsub() {
        insn(Opcodes.FSUB)
    }

    fun dsub() {
        insn(Opcodes.DSUB)
    }

    fun imul() {
        insn(Opcodes.IMUL)
    }

    fun lmul() {
        insn(Opcodes.LMUL)
    }

    fun fmul() {
        insn(Opcodes.FMUL)
    }

    fun dmul() {
        insn(Opcodes.DMUL)
    }

    fun idiv() {
        insn(Opcodes.IDIV)
    }

    fun ldiv() {
        insn(Opcodes.LDIV)
    }

    fun fdiv() {
        insn(Opcodes.FDIV)
    }

    fun ddiv() {
        insn(Opcodes.DDIV)
    }

    fun irem() {
        insn(Opcodes.IREM)
    }

    fun lrem() {
        insn(Opcodes.LREM)
    }

    fun frem() {
        insn(Opcodes.FREM)
    }

    fun drem() {
        insn(Opcodes.DREM)
    }

    fun ineg() {
        insn(Opcodes.INEG)
    }

    fun lneg() {
        insn(Opcodes.LNEG)
    }

    fun fneg() {
        insn(Opcodes.FNEG)
    }

    fun dneg() {
        insn(Opcodes.DNEG)
    }

    fun ishl() {
        insn(Opcodes.ISHL)
    }

    fun lshl() {
        insn(Opcodes.LSHL)
    }

    fun ishr() {
        insn(Opcodes.ISHR)
    }

    fun lshr() {
        insn(Opcodes.LSHR)
    }

    fun iushr() {
        insn(Opcodes.IUSHR)
    }

    fun lushr() {
        insn(Opcodes.LUSHR)
    }

    fun iand() {
        insn(Opcodes.IAND)
    }

    fun land() {
        insn(Opcodes.LAND)
    }

    fun ior() {
        insn(Opcodes.IOR)
    }

    fun lor() {
        insn(Opcodes.LOR)
    }

    fun ixor() {
        insn(Opcodes.IXOR)
    }

    fun lxor() {
        insn(Opcodes.LXOR)
    }

    fun i2l() {
        insn(Opcodes.I2L)
    }

    fun i2f() {
        insn(Opcodes.I2F)
    }

    fun i2d() {
        insn(Opcodes.I2D)
    }

    fun l2i() {
        insn(Opcodes.L2I)
    }

    fun l2f() {
        insn(Opcodes.L2F)
    }

    fun l2d() {
        insn(Opcodes.L2D)
    }

    fun f2i() {
        insn(Opcodes.F2I)
    }

    fun f2l() {
        insn(Opcodes.F2L)
    }

    fun f2d() {
        insn(Opcodes.F2D)
    }

    fun d2i() {
        insn(Opcodes.D2I)
    }

    fun d2l() {
        insn(Opcodes.D2L)
    }

    fun d2f() {
        insn(Opcodes.D2F)
    }

    fun i2b() {
        insn(Opcodes.I2B)
    }

    fun i2c() {
        insn(Opcodes.I2C)
    }

    fun i2s() {
        insn(Opcodes.I2S)
    }

    fun lcmp() {
        insn(Opcodes.LCMP)
    }

    fun fcmpl() {
        insn(Opcodes.FCMPL)
    }

    fun fcmpg() {
        insn(Opcodes.FCMPG)
    }

    fun dcmpl() {
        insn(Opcodes.DCMPL)
    }

    fun dcmpg() {
        insn(Opcodes.DCMPG)
    }

    fun xreturn(type: Type) {
        when (type.sort) {
            Type.VOID -> {
                vreturn()
            }
            Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                ireturn()
            }
            Type.FLOAT -> {
                freturn()
            }
            Type.LONG -> {
                lreturn()
            }
            Type.DOUBLE -> {
                dreturn()
            }
            Type.OBJECT, Type.ARRAY -> {
                areturn()
            }
            else -> {
                throw UnsupportedOperationException(type.toString())
            }
        }
    }

    fun ireturn() {
        insn(Opcodes.IRETURN)
    }

    fun lreturn() {
        insn(Opcodes.LRETURN)
    }

    fun freturn() {
        insn(Opcodes.FRETURN)
    }

    fun dreturn() {
        insn(Opcodes.DRETURN)
    }

    fun areturn() {
        insn(Opcodes.ARETURN)
    }

    fun vreturn() {
        insn(Opcodes.RETURN)
    }

    fun arraylength() {
        insn(Opcodes.ARRAYLENGTH)
    }

    fun athrow() {
        insn(Opcodes.ATHROW)
    }

    fun monitorEnter() {
        insn(Opcodes.MONITORENTER)
    }

    fun monitorExit() {
        insn(Opcodes.MONITOREXIT)
    }

    fun intInsn(opcode: Int, operand: Int) {
        list.add(IntInsnNode(opcode, operand))
    }

    fun bipush(operand: Int) {
        intInsn(Opcodes.BIPUSH, operand)
    }

    fun sipush(operand: Int) {
        intInsn(Opcodes.SIPUSH, operand)
    }

    fun newArray(type: Int) {
        intInsn(Opcodes.NEWARRAY, type)
    }

    fun varInsn(opcode: Int, index: Int) {
        list.add(VarInsnNode(opcode, index))
    }

    fun xload(type: Type, index: Int) {
        when (type.sort) {
            Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                iload(index)
            }
            Type.FLOAT -> {
                fload(index)
            }
            Type.LONG -> {
                lload(index)
            }
            Type.DOUBLE -> {
                dload(index)
            }
            Type.OBJECT, Type.ARRAY -> {
                aload(index)
            }
            else -> {
                throw UnsupportedOperationException(type.toString())
            }
        }
    }

    fun iload(index: Int) {
        varInsn(Opcodes.ILOAD, index)
    }

    fun lload(index: Int) {
        varInsn(Opcodes.LLOAD, index)
    }

    fun fload(index: Int) {
        varInsn(Opcodes.FLOAD, index)
    }

    fun dload(index: Int) {
        varInsn(Opcodes.DLOAD, index)
    }

    fun aload(index: Int) {
        varInsn(Opcodes.ALOAD, index)
    }

    fun xstore(type: Type, index: Int) {
        when (type.sort) {
            Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                istore(index)
            }
            Type.FLOAT -> {
                fstore(index)
            }
            Type.LONG -> {
                lstore(index)
            }
            Type.DOUBLE -> {
                dstore(index)
            }
            Type.OBJECT, Type.ARRAY -> {
                astore(index)
            }
            else -> {
                throw UnsupportedOperationException(type.toString())
            }
        }
    }

    fun istore(index: Int) {
        varInsn(Opcodes.ISTORE, index)
    }

    fun lstore(index: Int) {
        varInsn(Opcodes.LSTORE, index)
    }

    fun fstore(index: Int) {
        varInsn(Opcodes.FSTORE, index)
    }

    fun dstore(index: Int) {
        varInsn(Opcodes.DSTORE, index)
    }

    fun astore(index: Int) {
        varInsn(Opcodes.ASTORE, index)
    }

    fun ret(index: Int) {
        varInsn(Opcodes.RET, index)
    }

    fun typeInsn(opcode: Int, desc: String) {
        list.add(TypeInsnNode(opcode, desc))
    }

    fun anew(desc: String) {
        typeInsn(Opcodes.NEW, desc)
    }

    fun anewArray(desc: String) {
        typeInsn(Opcodes.ANEWARRAY, desc)
    }

    fun checkCast(desc: String) {
        typeInsn(Opcodes.CHECKCAST, desc)
    }

    fun instanceOf(desc: String) {
        typeInsn(Opcodes.INSTANCEOF, desc)
    }

    fun fieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        list.add(FieldInsnNode(opcode, owner, name, desc))
    }

    fun getStatic(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.GETSTATIC, owner, name, desc)
    }

    fun putStatic(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.PUTSTATIC, owner, name, desc)
    }

    fun getField(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.GETFIELD, owner, name, desc)
    }

    fun putField(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.PUTFIELD, owner, name, desc)
    }

    fun methodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String,
        isInterface: Boolean
    ) {
        list.add(MethodInsnNode(opcode, owner, name, desc, isInterface))
    }

    @JvmOverloads
    fun invokeVirtual(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, isInterface)
    }

    @JvmOverloads
    fun invokeSpecial(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, isInterface)
    }

    @JvmOverloads
    fun invokeStatic(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKESTATIC, owner, name, desc, isInterface)
    }

    fun invokeInterface(owner: String, name: String, desc: String) {
        methodInsn(Opcodes.INVOKEINTERFACE, owner, name, desc, true)
    }

    fun invokeDynamic(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any) {
        list.add(InvokeDynamicInsnNode(name, desc, bsm, *bsmArgs))
    }

    fun jumpInsn(opcode: Int, label: LabelNode) {
        list.add(JumpInsnNode(opcode, label))
    }

    fun ifeq(label: LabelNode) {
        jumpInsn(Opcodes.IFEQ, label)
    }

    fun ifne(label: LabelNode) {
        jumpInsn(Opcodes.IFNE, label)
    }

    fun iflt(label: LabelNode) {
        jumpInsn(Opcodes.IFLT, label)
    }

    fun ifge(label: LabelNode) {
        jumpInsn(Opcodes.IFGE, label)
    }

    fun ifgt(label: LabelNode) {
        jumpInsn(Opcodes.IFGT, label)
    }

    fun ifle(label: LabelNode) {
        jumpInsn(Opcodes.IFLE, label)
    }

    fun if_icmpeq(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPEQ, label)
    }

    fun if_icmpne(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPNE, label)
    }

    fun if_icmplt(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPLT, label)
    }

    fun if_icmpge(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPGE, label)
    }

    fun if_icmpgt(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPGT, label)
    }

    fun if_icmple(label: LabelNode) {
        jumpInsn(Opcodes.IF_ICMPLE, label)
    }

    fun if_acmpeq(label: LabelNode) {
        jumpInsn(Opcodes.IF_ACMPEQ, label)
    }

    fun if_acmpne(label: LabelNode) {
        jumpInsn(Opcodes.IF_ACMPNE, label)
    }

    fun ifNull(label: LabelNode) {
        jumpInsn(Opcodes.IFNULL, label)
    }

    fun ifNonNull(label: LabelNode) {
        jumpInsn(Opcodes.IFNONNULL, label)
    }

    fun agoto(label: LabelNode) {
        jumpInsn(Opcodes.GOTO, label)
    }

    fun jsr(label: LabelNode) {
        jumpInsn(Opcodes.JSR, label)
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

    fun tableSwitch(min: Int, max: Int, defaultCase: InstructionBuilder.() -> Unit, cases: InstructionBuilder.(Int) -> Unit) {
        val caseLabels = ASMUtils.newLabels((max + 1) - min)
        val defaultLabel = LabelNode()

        tableSwitch(min, max, defaultLabel, *caseLabels)

        caseLabels.withIndex().forEach { (index, label) ->
            label(label)
            cases(this, index)
        }

        label(defaultLabel) {
            defaultCase(this)
        }
    }

    fun tableSwitch(min: Int, max: Int, defaultLabel: LabelNode, vararg labels: LabelNode) {
        list.add(TableSwitchInsnNode(min, max, defaultLabel, *labels))
    }

    fun lookupSwitch(keys: IntArray, defaultCase: InstructionBuilder.() -> Unit, cases: InstructionBuilder.(Int) -> Unit) {
        val caseLabels = ASMUtils.newLabels(keys.size)
        val defaultLabel = LabelNode()

        lookupSwitch(defaultLabel, keys, caseLabels)

        caseLabels.withIndex().forEach { (index, label) ->
            label(label)
            cases(this, index)
        }

        label(defaultLabel) {
            defaultCase(this)
        }
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

    fun alist(insnList: InsnList) {
        list.add(insnList)
    }
}

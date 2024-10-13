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

    fun <T : Any> array(
        newArray: () -> Unit,
        store: T.() -> Unit,
        array: Array<T>
    ) {
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
            return insnNode(Opcodes.ICONST_0 + v)
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
            return insnNode(Opcodes.LCONST_0)
        }

        if (v == 1L) {
            return insnNode(Opcodes.LCONST_1)
        }

        return ldc(v)
    }

    fun number(v: Float) {
        if (v == 0.0F) {
            return insnNode(Opcodes.FCONST_0)
        }

        if (v == 1.0F) {
            return insnNode(Opcodes.FCONST_1)
        }

        if (v == 2.0F) {
            return insnNode(Opcodes.FCONST_2)
        }

        return ldc(v)
    }

    fun number(v: Double) {
        if (v == 0.0) {
            return insnNode(Opcodes.DCONST_0)
        }

        if (v == 1.0) {
            return insnNode(Opcodes.DCONST_1)
        }

        return ldc(v)
    }

    fun insnNode(opcode: Int) {
        list.add(InsnNode(opcode))
    }

    fun aconst_null() {
        insnNode(Opcodes.ACONST_NULL)
    }

    fun nop() {
        insnNode(Opcodes.NOP)
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
        insnNode(Opcodes.IALOAD)
    }

    fun laload() {
        insnNode(Opcodes.LALOAD)
    }

    fun faload() {
        insnNode(Opcodes.FALOAD)
    }

    fun daload() {
        insnNode(Opcodes.DALOAD)
    }

    fun aaload() {
        insnNode(Opcodes.AALOAD)
    }

    fun baload() {
        insnNode(Opcodes.BALOAD)
    }

    fun caload() {
        insnNode(Opcodes.CALOAD)
    }

    fun saload() {
        insnNode(Opcodes.SALOAD)
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
        insnNode(Opcodes.IASTORE)
    }

    fun lastore() {
        insnNode(Opcodes.LASTORE)
    }

    fun fastore() {
        insnNode(Opcodes.FASTORE)
    }

    fun dastore() {
        insnNode(Opcodes.DASTORE)
    }

    fun aastore() {
        insnNode(Opcodes.AASTORE)
    }

    fun bastore() {
        insnNode(Opcodes.BASTORE)
    }

    fun castore() {
        insnNode(Opcodes.CASTORE)
    }

    fun sastore() {
        insnNode(Opcodes.SASTORE)
    }

    fun pop() {
        insnNode(Opcodes.POP)
    }

    fun pop2() {
        insnNode(Opcodes.POP2)
    }

    fun dup() {
        insnNode(Opcodes.DUP)
    }

    fun dupx1() {
        insnNode(Opcodes.DUP_X1)
    }

    fun dupx2() {
        insnNode(Opcodes.DUP_X2)
    }

    fun dup2() {
        insnNode(Opcodes.DUP2)
    }

    fun dup2x1() {
        insnNode(Opcodes.DUP2_X1)
    }

    fun dup2x2() {
        insnNode(Opcodes.DUP2_X2)
    }

    fun swap() {
        insnNode(Opcodes.SWAP)
    }

    fun iadd() {
        insnNode(Opcodes.IADD)
    }

    fun ladd() {
        insnNode(Opcodes.LADD)
    }

    fun fadd() {
        insnNode(Opcodes.FADD)
    }

    fun dadd() {
        insnNode(Opcodes.DADD)
    }

    fun isub() {
        insnNode(Opcodes.ISUB)
    }

    fun lsub() {
        insnNode(Opcodes.LSUB)
    }

    fun fsub() {
        insnNode(Opcodes.FSUB)
    }

    fun dsub() {
        insnNode(Opcodes.DSUB)
    }

    fun imul() {
        insnNode(Opcodes.IMUL)
    }

    fun lmul() {
        insnNode(Opcodes.LMUL)
    }

    fun fmul() {
        insnNode(Opcodes.FMUL)
    }

    fun dmul() {
        insnNode(Opcodes.DMUL)
    }

    fun idiv() {
        insnNode(Opcodes.IDIV)
    }

    fun ldiv() {
        insnNode(Opcodes.LDIV)
    }

    fun fdiv() {
        insnNode(Opcodes.FDIV)
    }

    fun ddiv() {
        insnNode(Opcodes.DDIV)
    }

    fun irem() {
        insnNode(Opcodes.IREM)
    }

    fun lrem() {
        insnNode(Opcodes.LREM)
    }

    fun frem() {
        insnNode(Opcodes.FREM)
    }

    fun drem() {
        insnNode(Opcodes.DREM)
    }

    fun ineg() {
        insnNode(Opcodes.INEG)
    }

    fun lneg() {
        insnNode(Opcodes.LNEG)
    }

    fun fneg() {
        insnNode(Opcodes.FNEG)
    }

    fun dneg() {
        insnNode(Opcodes.DNEG)
    }

    fun ishl() {
        insnNode(Opcodes.ISHL)
    }

    fun lshl() {
        insnNode(Opcodes.LSHL)
    }

    fun ishr() {
        insnNode(Opcodes.ISHR)
    }

    fun lshr() {
        insnNode(Opcodes.LSHR)
    }

    fun iushr() {
        insnNode(Opcodes.IUSHR)
    }

    fun lushr() {
        insnNode(Opcodes.LUSHR)
    }

    fun iand() {
        insnNode(Opcodes.IAND)
    }

    fun land() {
        insnNode(Opcodes.LAND)
    }

    fun ior() {
        insnNode(Opcodes.IOR)
    }

    fun lor() {
        insnNode(Opcodes.LOR)
    }

    fun ixor() {
        insnNode(Opcodes.IXOR)
    }

    fun lxor() {
        insnNode(Opcodes.LXOR)
    }

    fun i2l() {
        insnNode(Opcodes.I2L)
    }

    fun i2f() {
        insnNode(Opcodes.I2F)
    }

    fun i2d() {
        insnNode(Opcodes.I2D)
    }

    fun l2i() {
        insnNode(Opcodes.L2I)
    }

    fun l2f() {
        insnNode(Opcodes.L2F)
    }

    fun l2d() {
        insnNode(Opcodes.L2D)
    }

    fun f2i() {
        insnNode(Opcodes.F2I)
    }

    fun f2l() {
        insnNode(Opcodes.F2L)
    }

    fun f2d() {
        insnNode(Opcodes.F2D)
    }

    fun d2i() {
        insnNode(Opcodes.D2I)
    }

    fun d2l() {
        insnNode(Opcodes.D2L)
    }

    fun d2f() {
        insnNode(Opcodes.D2F)
    }

    fun i2b() {
        insnNode(Opcodes.I2B)
    }

    fun i2c() {
        insnNode(Opcodes.I2C)
    }

    fun i2s() {
        insnNode(Opcodes.I2S)
    }

    fun lcmp() {
        insnNode(Opcodes.LCMP)
    }

    fun fcmpl() {
        insnNode(Opcodes.FCMPL)
    }

    fun fcmpg() {
        insnNode(Opcodes.FCMPG)
    }

    fun dcmpl() {
        insnNode(Opcodes.DCMPL)
    }

    fun dcmpg() {
        insnNode(Opcodes.DCMPG)
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
        insnNode(Opcodes.IRETURN)
    }

    fun lreturn() {
        insnNode(Opcodes.LRETURN)
    }

    fun freturn() {
        insnNode(Opcodes.FRETURN)
    }

    fun dreturn() {
        insnNode(Opcodes.DRETURN)
    }

    fun areturn() {
        insnNode(Opcodes.ARETURN)
    }

    fun vreturn() {
        insnNode(Opcodes.RETURN)
    }

    fun arraylength() {
        insnNode(Opcodes.ARRAYLENGTH)
    }

    fun athrow() {
        insnNode(Opcodes.ATHROW)
    }

    fun monitor_enter() {
        insnNode(Opcodes.MONITORENTER)
    }

    fun monitor_exit() {
        insnNode(Opcodes.MONITOREXIT)
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

    fun anewarray(desc: String) {
        typeInsn(Opcodes.ANEWARRAY, desc)
    }

    fun checkcast(desc: String) {
        typeInsn(Opcodes.CHECKCAST, desc)
    }

    fun instanceof(desc: String) {
        typeInsn(Opcodes.INSTANCEOF, desc)
    }

    fun fieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        list.add(FieldInsnNode(opcode, owner, name, desc))
    }

    fun getstatic(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.GETSTATIC, owner, name, desc)
    }

     fun putstatic(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.PUTSTATIC, owner, name, desc)
    }

    fun getfield(owner: String, name: String, desc: String) {
        fieldInsn(Opcodes.GETFIELD, owner, name, desc)
    }

    fun putfield(owner: String, name: String, desc: String) {
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
    fun invokevirtual(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, isInterface)
    }

    @JvmOverloads
    fun invokespecial(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, isInterface)
    }

    @JvmOverloads
    fun invokestatic(owner: String, name: String, desc: String, isInterface: Boolean = false) {
        methodInsn(Opcodes.INVOKESTATIC, owner, name, desc, isInterface)
    }

    fun invokeinterface(owner: String, name: String, desc: String) {
        methodInsn(Opcodes.INVOKEINTERFACE, owner, name, desc, true)
    }

    fun invokedynamic(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any) {
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

    fun tableswitch(min: Int, max: Int, defaultCase: InstructionBuilder.() -> Unit, cases: InstructionBuilder.(Int) -> Unit) {
        val caseLabels = ASMUtils.newLabels((max + 1) - min)
        val defaultLabel = LabelNode()

        tableswitch(min, max, defaultLabel, *caseLabels)

        caseLabels.withIndex().forEach { (index, label) ->
            label(label)
            cases(this, index)
        }

        label(defaultLabel) {
            defaultCase(this)
        }
    }

    fun tableswitch(min: Int, max: Int, defaultLabel: LabelNode, vararg labels: LabelNode) {
        list.add(TableSwitchInsnNode(min, max, defaultLabel, *labels))
    }

    fun lookupswitch(keys: IntArray, defaultCase: InstructionBuilder.() -> Unit, cases: InstructionBuilder.(Int) -> Unit) {
        val caseLabels = ASMUtils.newLabels(keys.size)
        val defaultLabel = LabelNode()

        lookupswitch(defaultLabel, keys, caseLabels)

        caseLabels.withIndex().forEach { (index, label) ->
            label(label)
            cases(this, index)
        }

        label(defaultLabel) {
            defaultCase(this)
        }
    }

    fun lookupswitch(defaultLabel: LabelNode, keys: IntArray, labels: Array<LabelNode>) {
        list.add(LookupSwitchInsnNode(defaultLabel, keys, labels))
    }

    fun multianewarray(desc: String, numDimensions: Int) {
        list.add(MultiANewArrayInsnNode(desc, numDimensions))
    }

    fun frame(type: Int, numLocal: Int, local: Array<Any>, numStack: Int, stack: Array<Any>) {
        list.add(FrameNode(type, numLocal, local, numStack, stack))
    }

    fun line(line: Int, start: LabelNode) {
        list.add(LineNumberNode(line, start))
    }

    fun addInstruction(insn: AbstractInsnNode) {
        list.add(insn)
    }

    fun addInstructionList(insnList: InsnList) {
        list.add(insnList)
    }
}

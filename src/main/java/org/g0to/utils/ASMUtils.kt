package org.g0to.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

object ASMUtils {
    private val primitiveToWrapperMap = mapOf(
        'Z' to "Ljava/lang/Boolean;",
        'C' to "Ljava/lang/Character;",
        'B' to "Ljava/lang/Byte;",
        'S' to "Ljava/lang/Short;",
        'I' to "Ljava/lang/Integer;",
        'F' to "Ljava/lang/Float;",
        'J' to "Ljava/lang/Long;",
        'D' to "Ljava/lang/Double;"
    )

    fun generateModifier(vararg attrs: Int): Int {
        var modifier = 0

        for (attr in attrs) {
            modifier = modifier or attr
        }

        return modifier
    }

    fun getClinit(n: ClassNode): MethodNode {
        for (method in n.methods) {
            if (method.name == "<clinit>") {
                return method
            }
        }

        return MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).apply {
            instructions.add(LabelNode())
            instructions.add(InsnNode(Opcodes.RETURN))

            n.methods.add(this)
        }
    }

    fun allocVariable(node: MethodNode): Int {
        return node.maxLocals++
    }

    fun asInsnList(vararg instructions: AbstractInsnNode): InsnList {
        return InsnList().apply {
            for (instruction in instructions) {
                add(instruction)
            }
        }
    }

    fun isString(n: LdcInsnNode): Boolean {
        return n.cst is String
    }

    fun isString(n: AbstractInsnNode): Boolean {
        if (n.opcode == Opcodes.LDC) {
            return isString(n as LdcInsnNode)
        }

        return false
    }

    fun getString(n: LdcInsnNode): String {
        return n.cst.toString()
    }

    fun getString(n: AbstractInsnNode): String {
        return getString(n as LdcInsnNode)
    }

    fun toWrappedPrimaryType(desc: String): String? {
        return primitiveToWrapperMap[desc[0]]
    }
}
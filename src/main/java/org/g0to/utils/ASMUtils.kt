package org.g0to.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode

object ASMUtils {
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
}
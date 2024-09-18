package org.g0to.utils

import org.objectweb.asm.tree.MethodNode

class MethodBuilder(
    access: Int,
    name: String,
    desc: String,
    signature: String?,
    exceptions: Array<String>?
) {
    private val method = MethodNode(access, name, desc, signature, exceptions)
    private val instructionBuilder = InstructionBuilder()

    fun allocVariable(): Int {
        return method.maxLocals++
    }

    fun allocBigVariable(): Int {
        val index = method.maxLocals

        method.maxLocals += 2
        return index
    }

    fun getInstructionBuilder(): InstructionBuilder {
        return instructionBuilder
    }

    fun build(): MethodNode {
        method.instructions.add(instructionBuilder.build())

        return method
    }
}
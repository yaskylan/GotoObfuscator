package org.g0to.utils

import org.objectweb.asm.tree.MethodNode

class MethodBuilder {
    private val method = MethodNode()
    private val instructionBuilder = InstructionBuilder()

    init {
        method.tryCatchBlocks = ArrayList()
    }

    fun visit(block: MethodNode.() -> Unit): MethodBuilder {
        method.block()

        return this
    }

    fun allocSlot(): Int {
        return method.maxLocals++
    }

    fun allocSlot64(): Int {
        val index = method.maxLocals

        method.maxLocals += 2
        return index
    }

    fun instructions(block: InstructionBuilder.() -> Unit): MethodBuilder {
        block(instructionBuilder)

        return this
    }

    fun build(): MethodNode {
        method.instructions.add(instructionBuilder.build())

        return method
    }
}
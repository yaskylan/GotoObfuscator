package org.g0to.utils

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode

class InstructionBuffer(
    private val methodNode: MethodNode
) {
    private val buffer = ArrayList<Operation>()

    fun replace(markInstruction: AbstractInsnNode, instruction: AbstractInsnNode) {
        insert(markInstruction, instruction)
        remove(markInstruction)
    }

    fun replace(markInstruction: AbstractInsnNode, instruction: InsnList) {
        insert(markInstruction, instruction)
        remove(markInstruction)
    }

    fun insertFirst(instruction: AbstractInsnNode) {
        buffer.add(InsertOperation(null, instruction, false))
    }

    fun insertFirst(instruction: InsnList) {
        buffer.add(InsertOperation(null, instruction, false))
    }

    fun insert(markInstruction: AbstractInsnNode, instruction: AbstractInsnNode) {
        buffer.add(InsertOperation(markInstruction, instruction, false))
    }

    fun insert(markInstruction: AbstractInsnNode, instruction: InsnList) {
        buffer.add(InsertOperation(markInstruction, instruction, false))
    }

    fun insertBefore(markInstruction: AbstractInsnNode, instruction: AbstractInsnNode) {
        buffer.add(InsertOperation(markInstruction, instruction, true))
    }

    fun insertBefore(markInstruction: AbstractInsnNode, instruction: InsnList) {
        buffer.add(InsertOperation(markInstruction, instruction, true))
    }

    fun remove(instruction: AbstractInsnNode) {
        buffer.add(RemoveOperation(instruction))
    }

    fun apply() {
        val instructions = methodNode.instructions

        for (operation in buffer) {
            when (operation) {
                is InsertOperation -> {
                    val markInstruction = operation.markInstruction

                    if (markInstruction == null) {
                        when (operation.instruction) {
                            is AbstractInsnNode -> instructions.insert(operation.instruction)
                            is InsnList -> instructions.insert(operation.instruction)
                            else -> throw IllegalStateException("'operation.instruction' should be AbstractInsnNode or InsnList")
                        }
                    } else {
                        when (operation.instruction) {
                            is AbstractInsnNode -> {
                                if (operation.isBefore) {
                                    instructions.insertBefore(markInstruction, operation.instruction)
                                } else {
                                    instructions.insert(markInstruction, operation.instruction)
                                }
                            }
                            is InsnList -> {
                                if (operation.isBefore) {
                                    instructions.insertBefore(markInstruction, operation.instruction)
                                } else {
                                    instructions.insert(markInstruction, operation.instruction)
                                }
                            }
                            else -> throw IllegalStateException("'operation.instruction' should be AbstractInsnNode or InsnList")
                        }
                    }
                }
                is RemoveOperation -> {
                    instructions.remove(operation.instruction)
                }
            }
        }
    }

    private abstract class Operation

    private class InsertOperation(
        val markInstruction: AbstractInsnNode?,
        val instruction: Any,
        val isBefore: Boolean
    ) : Operation()

    private class RemoveOperation(
        val instruction: AbstractInsnNode
    ) : Operation()
}
package org.g0to.transformer.features.stringencryption

import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.MethodBuilder
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

fun isMakeConcatWithConstants(instruction: AbstractInsnNode): Boolean {
    return instruction is InvokeDynamicInsnNode
            && instruction.name.equals("makeConcatWithConstants")
            && instruction.bsmArgs[0].toString().find { it != '\u0001' } != null
}

fun processMakeConcatWithConstants(classWrapper: ClassWrapper,
                                   instruction: InvokeDynamicInsnNode,
                                   bootstrapName: String,
                                   buffer: InstructionBuffer): MethodNode {
    val recipe = instruction.bsmArgs[0].toString()
    val accumulatedString = StringBuilder()
    val newRecipe = StringBuilder()
    val constantList = ArrayList<String>()

    fun flushAccumulatedString() {
        if (accumulatedString.isNotEmpty()) {
            constantList.add(accumulatedString.toString())
            accumulatedString.setLength(0)

            newRecipe.append('\u0002')
        }
    }

    var bsmArgIndex = 1

    for (c in recipe) {
        when (c) {
            '\u0001' -> {
                flushAccumulatedString()

                newRecipe.append('\u0001')
            }
            '\u0002' -> {
                flushAccumulatedString()

                constantList.add(instruction.bsmArgs[bsmArgIndex++].toString())
                newRecipe.append('\u0002')
            }
            else -> {
                accumulatedString.append(c)
            }
        }
    }

    flushAccumulatedString()

    if (constantList.isEmpty()) {
        throw IllegalStateException()
    }

    val bootstrap = createIndyBootstrap(bootstrapName, constantList)

    buffer.replace(instruction, InstructionBuilder.buildInsnList {
        invokedynamic(
            instruction.name,
            instruction.desc,
            Handle(
                Opcodes.H_INVOKESTATIC,
                classWrapper.getClassName(),
                bootstrap.name,
                bootstrap.desc,
                classWrapper.isInterface()
            ),
            newRecipe.toString()
        )
    })

    return bootstrap
}

private fun createIndyBootstrap(name: String,
                                constantList: ArrayList<String>): MethodNode {
    val methodBuilder = MethodBuilder().visit {
        this.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
        this.name = name
        this.desc = "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;"
    }

    val varCaller = methodBuilder.allocSlot()
    val varName = methodBuilder.allocSlot()
    val varType = methodBuilder.allocSlot()
    val varRecipe = methodBuilder.allocSlot()

    methodBuilder.instructions {
        aload(varCaller)
        aload(varName)
        aload(varType)
        aload(varRecipe)
        number(constantList.size)
        anewarray("java/lang/Object")
        dup()

        for ((index, constant) in constantList.withIndex()) {
            number(index)
            ldc(constant)
            aastore()

            if (index != constantList.lastIndex) {
                dup()
            }
        }

        invokestatic(
            "java/lang/invoke/StringConcatFactory",
            "makeConcatWithConstants",
            "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
        )

        areturn()
    }

    return methodBuilder.build()
}
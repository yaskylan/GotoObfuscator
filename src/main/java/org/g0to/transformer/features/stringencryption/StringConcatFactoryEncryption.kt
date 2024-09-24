package org.g0to.transformer.features.stringencryption

import org.g0to.transformer.features.stringencryption.StringEncryption.PlainText
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

fun processMakeConcatWithConstants(instance: StringEncryption,
                                           classWrapper: ClassWrapper,
                                           instruction: InvokeDynamicInsnNode,
                                           buffer: InstructionBuffer,
                                           plainTexts: ArrayList<PlainText>,
                                           keyOfClass: Int,
                                           decryptMethod: MethodNode): MethodNode {
    val recipe = instruction.bsmArgs[0].toString()
    val accumulatedString = StringBuilder()
    val newRecipe = StringBuilder()
    val constantList = ArrayList<PlainText>()

    fun flushAccumulatedString() {
        if (accumulatedString.isNotEmpty()) {
            constantList.add(PlainText(accumulatedString.toString(), instance.generateKey()))
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

                constantList.add(PlainText(instruction.bsmArgs[bsmArgIndex++].toString(), instance.generateKey()))
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

    val bootstrap = createIndyBootstrap(
        classWrapper, "_goto_makeConcatWithConstants_" + plainTexts.size,
        Array(constantList.size) {
            val plaintext = constantList[it]

            Triple(
                ((plainTexts.size + it) xor keyOfClass) ushr 16,
                ((plainTexts.size + it) xor keyOfClass) and  0xFFFF,
                instance.getLong(plaintext.key)
            )
        },
        decryptMethod
    )

    buffer.replace(instruction, InstructionBuilder.buildInsnList {
        invokeDynamic(
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

    plainTexts.addAll(constantList)

    return bootstrap
}

private fun createIndyBootstrap(classWrapper: ClassWrapper,
                                name: String,
                                dataArray: Array<Triple<Int, Int, Long>>,
                                decryptMethod: MethodNode): MethodNode {
    val methodBuilder = MethodBuilder(
        Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
        name,
        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;",
        null,
        null
    )

    val varCaller = methodBuilder.allocVariable()
    val varName = methodBuilder.allocVariable()
    val varType = methodBuilder.allocVariable()
    val varRecipe = methodBuilder.allocVariable()

    methodBuilder.getInstructionBuilder().block {
        aload(varCaller)
        aload(varName)
        aload(varType)
        aload(varRecipe)
        number(dataArray.size)
        anewArray("java/lang/Object")
        dup()

        for ((index, data) in dataArray.withIndex()) {
            number(index)
            number(data.first)
            number(data.second)
            number(data.third)
            invokeStatic(
                classWrapper.getClassName(),
                decryptMethod.name,
                decryptMethod.desc,
                classWrapper.isInterface()
            )
            aastore()

            if (index != dataArray.lastIndex) {
                dup()
            }
        }

        invokeStatic(
            "java/lang/invoke/StringConcatFactory",
            "makeConcatWithConstants",
            "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
        )

        areturn()
    }

    return methodBuilder.build()
}
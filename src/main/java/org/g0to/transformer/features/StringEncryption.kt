package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.dictionary.ClassDictionary
import org.g0to.transformer.Transformer
import org.g0to.utils.ASMUtils
import org.g0to.utils.InstructionBuffer
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.MethodBuilder
import org.g0to.utils.Utils.nextNonZeroInt
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier
import java.util.concurrent.ThreadLocalRandom

class StringEncryption(
    setting: TransformerBaseSetting
) : Transformer<StringEncryption.Setting>("StringEncryption", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetClasses { classWrapper ->
            var shouldProcess = false

            for (method in classWrapper.getMethods()) {
                if (method.instructions.find { ASMUtils.isString(it) } != null) {
                    shouldProcess = true
                    break
                }
            }

            if (!shouldProcess) {
                return@foreachTargetClasses
            }

            val classDictionary = core.conf.dictionary.newClassDictionary(classWrapper)
            val keyOfClass = ThreadLocalRandom.current().nextInt(0xFFFFFF, Int.MAX_VALUE)
            val keyMap = IntArray(256) { ThreadLocalRandom.current().nextNonZeroInt() }
            val plainTexts = ArrayList<String>()
            val storeField = FieldNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL, classDictionary.randFieldName(), "[Ljava/lang/String;", null, null)
            val markField = FieldNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL, classDictionary.randFieldName(), "[B", null, null)
            val decryptMethod = createDecryptMethod(keyOfClass, keyMap, classDictionary, classWrapper.getClassName(), storeField, markField)

            for (methodNode in classWrapper.getMethods()) {
                val buffer = InstructionBuffer(methodNode)

                for (instruction in methodNode.instructions) {
                    if (!ASMUtils.isString(instruction)) {
                        continue
                    }

                    val plainText = ASMUtils.getString(instruction)

                    buffer.replace(instruction, InstructionBuilder()
                        .number((plainTexts.size xor keyOfClass) ushr 16)
                        .number((plainTexts.size xor keyOfClass) and 0xFFFF)
                        .methodInsn(
                            Opcodes.INVOKESTATIC,
                            classWrapper.getClassName(),
                            decryptMethod.name,
                            decryptMethod.desc,
                            Modifier.isInterface(classWrapper.getModifier())
                        ).build())

                    plainTexts.add(plainText)
                    accumulated++
                }

                buffer.apply()
            }

            classWrapper.addField(storeField)
            classWrapper.addField(markField)
            classWrapper.addMethod(decryptMethod)

            // Process clinit
            ASMUtils.getClinit(classWrapper.classNode)
                .instructions
                .insert(addInit(
                    plainTexts,
                    keyOfClass,
                    keyMap,
                    classWrapper.getClassName(),
                    storeField,
                    markField
                ))
        }

        logger.info("Encrypted $accumulated strings")
    }

    private fun createDecryptMethod(keyOfClass: Int,
                                    keyMap: IntArray,
                                    classDictionary: ClassDictionary,
                                    className: String,
                                    storeField: FieldNode,
                                    markField: FieldNode): MethodNode {
        val methodBuilder = MethodBuilder(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC, classDictionary.randMethodName("(II)Ljava/lang/String;"), "(II)Ljava/lang/String;", null, null)
        val insnBuilder = methodBuilder.getInstructionBuilder()
        val l1 = LabelNode()
        val l2 = LabelNode()
        val l3 = LabelNode()
        val l4 = LabelNode()
        val l5 = LabelNode()
        val l6 = LabelNode()
        val switchLabels = Array(256) { LabelNode() }

        // Variables
        val varHigh = methodBuilder.allocVariable()
        val varLow = methodBuilder.allocVariable()
        val varIndex = methodBuilder.allocVariable()
        val varString = methodBuilder.allocVariable()
        val varBuffer = methodBuilder.allocVariable()
        val varForI = methodBuilder.allocVariable()
        val varXorKey = methodBuilder.allocVariable()

        insnBuilder.lable(l1)
        // Decrypt index
        insnBuilder.iload(varHigh)
            .number(16)
            .ishl()
            .iload(varLow)
            .ior()
            .number(keyOfClass)
            .ixor()
            .istore(varIndex)
        // Push mark field to the stack to check the mark
        insnBuilder.getStatic(className, markField.name, markField.desc)
            .iload(varIndex)
            .baload()
        // If mark value is not equal to 0 then we go directly to the l6
        insnBuilder.ifne(l6)

        // Decrypt block
        insnBuilder.lable(l2)
        insnBuilder.getStatic(className, storeField.name, storeField.desc)
            .iload(varIndex)
            .aaload()
            .astore(varString)

        // char[] buffer = new char[varString.length()];
        insnBuilder.aload(varString)
            .invokeVirtual("java/lang/String", "length", "()I")
            .newArray(Opcodes.T_CHAR)
            .astore(varBuffer)
        // int i = 0;
        insnBuilder.number(0)
            .istore(varForI)

        // for (; i < varString.length(); i++)
        insnBuilder.lable(l3)
        insnBuilder.iload(varForI)
            .aload(varString)
            .invokeVirtual("java/lang/String", "length", "()I")
            .if_icmpge(l5)

        // int xorKey = keyMap[(i ^ varIndex ^ keyOfClass) & 0xFF]; but implemented by using switch
        insnBuilder.iload(varForI)
            .iload(varIndex)
            .ixor()
            .number(keyOfClass)
            .ixor()
            .number(0xFF)
            .iand()
            .tableSwitch(0, 254, switchLabels.last(), *switchLabels.copyOfRange(0, 255))

        for ((index, switchLabel) in switchLabels.withIndex()) {
            insnBuilder.lable(switchLabel)
                .number(keyMap[index])
                .istore(varXorKey)

            if (index != switchLabels.lastIndex) {
                insnBuilder.agoto(l4)
            }
        }

        // Decrypt core
        insnBuilder.lable(l4)
        insnBuilder.aload(varBuffer)
            .iload(varForI)
            .aload(varString)
            .iload(varForI)
            .invokeVirtual("java/lang/String", "charAt", "(I)C")
            .number(keyOfClass)
            .ixor()
            .iload(varXorKey)
            .ixor()
            .i2c()
            .castore()
        // Return to l3
        insnBuilder.iinc(varForI, 1).agoto(l3)

        insnBuilder.lable(l5)
        // Store decrypted string to storeField
        insnBuilder.getStatic(className, storeField.name, storeField.desc)
            .iload(varIndex)
            .anew("java/lang/String")
            .dup()
            .aload(varBuffer)
            .invokeSpecial("java/lang/String", "<init>", "([C)V")
            .aastore()
        // Set mark to a non-zero value
        insnBuilder.getStatic(className, markField.name, markField.desc)
            .iload(varIndex)
            .number(66)
            .bastore()

        insnBuilder.lable(l6)
            .getStatic(className, storeField.name, storeField.desc)
            .iload(varIndex)
            .aaload()
            .areturn()

        return methodBuilder.build()
    }

    private fun addInit(plainTexts: ArrayList<String>,
                        keyOfClass: Int,
                        keyMap: IntArray,
                        className: String,
                        storeField: FieldNode,
                        markField: FieldNode): InsnList {
        val builder = InstructionBuilder()

        builder.lable(LabelNode())
        builder.number(plainTexts.size)
            .anewArray("java/lang/String")
            .dup()
        for ((index, s) in plainTexts.withIndex()) {
            builder.number(index)
                .ldc(encryptString(s, index, keyOfClass, keyMap))
                .aastore()

            if (index != plainTexts.lastIndex) {
                builder.dup()
            }
        }
        builder.putStatic(className, storeField.name, storeField.desc)

        builder.number(plainTexts.size)
            .newArray(Opcodes.T_BYTE)
            .putStatic(className, markField.name, markField.desc)

        return builder.build()
    }

    private fun encryptString(s: String, indexOfString: Int, keyOfClass: Int, keyMap: IntArray): String {
        return CharArray(s.length) {
            (s[it].code xor keyOfClass xor (keyMap[(it xor indexOfString xor keyOfClass) and 0xFF])).toChar()
        }.concatToString()
    }
}
package org.g0to.transformer.features.stringencryption

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.dictionary.ClassDictionary
import org.g0to.transformer.Transformer
import org.g0to.utils.ASMUtils
import org.g0to.utils.InstructionBuilder
import org.g0to.utils.MethodBuilder
import org.g0to.utils.Utils.nextNonZeroInt
import org.g0to.utils.extensions.modify
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.concurrent.ThreadLocalRandom

class StringEncryption(
    setting: TransformerBaseSetting
) : Transformer<StringEncryption.Setting>("StringEncryption", setting as Setting) {
    class Setting : TransformerBaseSetting()

    private val decryptMethodDesc = "(IIJ)Ljava/lang/String;"

    override fun run(core: Core) {
        var accumulated = 0

        core.foreachTargetClasses { classWrapper ->
            var shouldProcess = false

            foreachMethod@ for (method in classWrapper.getMethods()) {
                for (instruction in method.instructions) {
                    if (ASMUtils.isString(instruction) || isMakeConcatWithConstants(instruction)) {
                        shouldProcess = true
                        break@foreachMethod
                    }
                }
            }

            if (!shouldProcess) {
                return@foreachTargetClasses
            }

            val classDictionary = core.conf.dictionary.newClassDictionary(classWrapper)
            val keyOfClass = ThreadLocalRandom.current().nextInt(0xFFFFFF, Int.MAX_VALUE)
            val keyMap = IntArray(256) { ThreadLocalRandom.current().nextNonZeroInt() }
            val encryptedField = FieldNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL, classDictionary.randFieldName(), "[Ljava/lang/String;", null, null)
            val decryptedField = FieldNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL, classDictionary.randFieldName(), "[Ljava/lang/String;", null, null)
            val decryptMethod = createDecryptMethod(keyOfClass, keyMap, classDictionary, classWrapper.getClassName(), encryptedField, decryptedField)
            val indyConcatMethods = ArrayList<MethodNode>()
            val plainTexts = LinkedHashMap<String, PlainText>()

            classWrapper.getMethods().forEach { method -> method.modify { buffer ->
                for (instruction in method.instructions) {
                    if (isMakeConcatWithConstants(instruction)) {
                        indyConcatMethods.add(processMakeConcatWithConstants(
                            classWrapper,
                            instruction as InvokeDynamicInsnNode,
                            classDictionary.randStaticMethodName("(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;"),
                            buffer
                        ))
                    }
                }
            } }

            indyConcatMethods.forEach {
                classWrapper.addMethod(it)
            }

            classWrapper.getMethods().forEach { method -> method.modify { buffer ->
                for (instruction in method.instructions) {
                    if (!ASMUtils.isString(instruction)) {
                        continue
                    }

                    val plaintext = plainTexts.computeIfAbsent(ASMUtils.getString(instruction)) {
                        PlainText(it, plainTexts.size, generateKey())
                    }

                    buffer.replace(instruction, InstructionBuilder.buildInsnList {
                        number((plaintext.index xor keyOfClass) ushr 16)
                        number((plaintext.index xor keyOfClass) and  0xFFFF)
                        number(getLong(plaintext.key))
                        invokestatic(
                            classWrapper.getClassName(),
                            decryptMethod.name,
                            decryptMethod.desc,
                            classWrapper.isInterface()
                        )
                    })

                    accumulated++
                }
            } }

            classWrapper.addField(encryptedField)
            classWrapper.addField(decryptedField)
            classWrapper.addMethod(decryptMethod)

            // Process clinit
            ASMUtils.getClinit(classWrapper.classNode)
                .instructions
                .insert(addInit(
                    plainTexts,
                    keyOfClass,
                    keyMap,
                    classWrapper,
                    encryptedField,
                    decryptedField
                ))
        }

        logger.info("Encrypted $accumulated strings")
    }

    private fun createDecryptMethod(keyOfClass: Int,
                                    keyMap: IntArray,
                                    classDictionary: ClassDictionary,
                                    className: String,
                                    encryptedField: FieldNode,
                                    decryptedField: FieldNode): MethodNode {
        val methodBuilder = MethodBuilder().visit {
            access = Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC
            name = classDictionary.randStaticMethodName(decryptMethodDesc)
            desc = decryptMethodDesc
        }

        val startLabel = LabelNode()
        val covertLong2ByteForeachLabel = LabelNode()
        val covertLong2ByteForeachJumpLabel = LabelNode()
        val charArrayForeachLabel = LabelNode()
        val ifJNotEQ8Label = LabelNode()
        val keyMapSwitchDefaultLabel = LabelNode()
        val charArrayForeachJumpLabel = LabelNode()
        val returnLabel = LabelNode()
        val switchLabels = Array(256) { LabelNode() }

        // Variables
        val varHigh = methodBuilder.allocSlot()
        val varLow = methodBuilder.allocSlot()
        val varKeyLong = methodBuilder.allocSlot64()
        // ===
        val varDecryptedString = methodBuilder.allocSlot()
        val varKeyBytes = methodBuilder.allocSlot()
        val varKeyBytesI = methodBuilder.allocSlot()
        val varIndex = methodBuilder.allocSlot()
        val varEncryptedString = methodBuilder.allocSlot()
        val varBuffer = methodBuilder.allocSlot()
        val varForI = methodBuilder.allocSlot()
        val varForJ = methodBuilder.allocSlot()
        val varXorKey = methodBuilder.allocSlot()

        methodBuilder.instructions {
            label(startLabel) {
                // Decrypt index
                iload(varHigh)
                number(16)
                ishl()
                iload(varLow)
                ior()
                number(keyOfClass)
                ixor()
                dup()
                istore(varIndex)

                getstatic(className, decryptedField.name, decryptedField.desc)
                swap()
                aaload()
                dup()
                astore(varDecryptedString)
                // If the value is not null then we go directly to the returnLabel
                ifNonNull(returnLabel)

                // new byte[8] and store
                number(8)
                newArray(Opcodes.T_BYTE)
                astore(varKeyBytes)
                // int i = 0;
                number(0)
                istore(varKeyBytesI)
            }

            // covert long to byte[]
            label(covertLong2ByteForeachLabel) {
                iload(varKeyBytesI)
                number(8)
                if_icmpge(covertLong2ByteForeachJumpLabel)
                aload(varKeyBytes)
                iload(varKeyBytesI)
                lload(varKeyLong)
                number(64)
                iinc(varKeyBytesI, 1)
                iload(varKeyBytesI)
                number(8)
                imul()
                isub()
                lushr()
                l2i()
                i2b()
                bastore()
                agoto(covertLong2ByteForeachLabel)
            }

            // get encrypted string and store
            label(covertLong2ByteForeachJumpLabel) {
                getstatic(className, encryptedField.name, encryptedField.desc)
                iload(varIndex)
                aaload()
                astore(varEncryptedString)
            }

            // char[] buffer = new char[varString.length()];
            aload(varEncryptedString)
            invokevirtual("java/lang/String", "length", "()I")
            newArray(Opcodes.T_CHAR)
            astore(varBuffer)

            // int i = 0; int j = 0;
            number(0)
            istore(varForI)
            number(0)
            istore(varForJ)

            label(charArrayForeachLabel) {
                // for (; i < varString.length(); i++)
                iload(varForI)
                aload(varEncryptedString)
                invokevirtual("java/lang/String", "length", "()I")
                if_icmpge(charArrayForeachJumpLabel)
                // if (j == 8) j = 0
                iload(varForJ)
                number(8)
                if_icmpne(ifJNotEQ8Label)
                number(0)
                istore(varForJ)

                // int xorKey = keyMap[(i ^ varIndex ^ keyOfClass) & 0xFF]; but implemented by using switch
                label(ifJNotEQ8Label) {
                    iload(varForI)
                    iload(varIndex)
                    ixor()
                    number(keyOfClass)
                    ixor()
                    number(0xFF)
                    iand()
                    tableswitch(0, 254, switchLabels.last(), *switchLabels.copyOfRange(0, 255))

                    for ((index, switchLabel) in switchLabels.withIndex()) {
                        label(switchLabel) {
                            number(keyMap[index])
                            istore(varXorKey)

                            if (index != switchLabels.lastIndex) {
                                agoto(keyMapSwitchDefaultLabel)
                            }
                        }
                    }
                }

                label(keyMapSwitchDefaultLabel) {
                    // Decrypt core
                    aload(varBuffer)
                    iload(varForI)
                    aload(varEncryptedString)
                    iload(varForI)
                    invokevirtual("java/lang/String", "charAt", "(I)C")
                    number(keyOfClass)
                    ixor()
                    iload(varXorKey)
                    ixor()
                    aload(varKeyBytes)
                    iload(varForJ)
                    baload()
                    ixor()
                    i2c()
                    castore()
                }

                // Return to charArrayForeachLabel
                iinc(varForI, 1)
                iinc(varForJ, 1)
                agoto(charArrayForeachLabel)
            }

            label(charArrayForeachJumpLabel) {
                // Store decrypted string
                getstatic(className, decryptedField.name, decryptedField.desc)
                iload(varIndex)
                anew("java/lang/String")
                dup()
                aload(varBuffer)
                invokespecial("java/lang/String", "<init>", "([C)V")
                invokevirtual("java/lang/String", "intern", "()Ljava/lang/String;")
                dup()
                astore(varDecryptedString)
                aastore()
            }

            label(returnLabel) {
                aload(varDecryptedString)
                areturn()
            }
        }

        return methodBuilder.build()
    }

    private fun addInit(plainTexts: HashMap<String, PlainText>,
                        keyOfClass: Int,
                        keyMap: IntArray,
                        classWrapper: ClassWrapper,
                        encryptedField: FieldNode,
                        decryptedField: FieldNode): InsnList {
        return InstructionBuilder.buildInsnList {
            label(LabelNode())
            number(plainTexts.size)
            anewarray("java/lang/String")
            dup()

            for ((index, plaintext) in plainTexts.values.withIndex()) {
                number(index)
                ldc(encryptString(plaintext, keyOfClass, keyMap))
                aastore()

                if (index != plainTexts.size - 1) {
                    dup()
                }
            }
            putstatic(classWrapper.getClassName(), encryptedField.name, encryptedField.desc)

            number(plainTexts.size)
            anewarray("java/lang/String")
            putstatic(classWrapper.getClassName(), decryptedField.name, decryptedField.desc)
        }
    }

    private fun encryptString(plaintext: PlainText, keyOfClass: Int, keyMap: IntArray): String {
        return CharArray(plaintext.value.length) {
            (plaintext.value[it].code
             xor keyOfClass
             xor (keyMap[(it xor plaintext.index xor keyOfClass) and 0xFF])
            ).toChar()
        }.apply { blockXor(this, plaintext.key) }.concatToString()
    }

    private fun blockXor(data: CharArray, key: ByteArray) {
        var i = 0
        var j = 0

        while (i < data.size) {
            if (j == 8) {
                j = 0
            }

            data[i] = (data[i].code xor key[j].toInt()).toChar()
            i++
            j++
        }
    }

    fun generateKey(): ByteArray {
        return ByteArray(8).apply {
            do {
                ThreadLocalRandom.current().nextBytes(this)
            } while (this.all { it.toInt() == 0 })
        }
    }

    fun getLong(bArray: ByteArray): Long {
        return ((bArray[0].toLong() and 0xFFL) shl 56  or (
                (bArray[1].toLong() and 0xFFL) shl 48) or (
                (bArray[2].toLong() and 0xFFL) shl 40) or (
                (bArray[3].toLong() and 0xFFL) shl 32) or (
                (bArray[4].toLong() and 0xFFL) shl 24) or (
                (bArray[5].toLong() and 0xFFL) shl 16) or (
                (bArray[6].toLong() and 0xFFL) shl 8)  or (
                 bArray[7].toLong() and 0xFFL))
    }

    class PlainText(
        val value: String,
        val index: Int,
        val key: ByteArray
    )
}
package org.g0to.dictionary

import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Type

class ClassDictionary(
    private val classWrapper: ClassWrapper,
    words: CharArray,
    length: Int
) : Dictionary(words, length) {
    fun randFieldName(): String {
        val blackList = HashSet<String>(this.blackList)

        for (field in classWrapper.classNode.fields) {
            blackList.add(field.name)
        }

        return randString(blackList)
    }

    fun randMethodName(desc: String): String {
        val argumentTypeOfTargetMethod = Type.getArgumentTypes(desc)
        val blackList = HashSet<String>(this.blackList)

        for (method in classWrapper.classNode.methods) {
            if (Type.getArgumentTypes(method.desc).contentEquals(argumentTypeOfTargetMethod)) {
                blackList.add(method.name)
            }
        }

        return randString(blackList)
    }
}
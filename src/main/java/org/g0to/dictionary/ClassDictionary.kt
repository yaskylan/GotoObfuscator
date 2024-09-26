package org.g0to.dictionary

import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Type

class ClassDictionary(
    private val classWrapper: ClassWrapper,
    words: CharArray,
    length: Int
) : Dictionary(words, length) {
    private val fieldBlackList = HashSet<String>()
    private val methodBlackList = HashSet<String>()

    fun randFieldName(): String {
        val blackList = HashSet.newHashSet<String>(this.blackList.size)
        blackList.addAll(this.blackList)
        blackList.addAll(this.fieldBlackList)

        for (field in classWrapper.classNode.fields) {
            blackList.add(field.name)
        }

        return randString(blackList).also {
            this.fieldBlackList.add(it)
        }
    }

    fun randStaticMethodName(desc: String): String {
        val argTypes = Type.getArgumentTypes(desc)
        val blackList = HashSet.newHashSet<String>(this.blackList.size)
        blackList.addAll(this.blackList)
        blackList.addAll(this.methodBlackList)

        for (method in classWrapper.classNode.methods) {
            if (Type.getArgumentTypes(method.desc).contentEquals(argTypes)) {
                blackList.add(method.name)
            }
        }

        return randString(blackList).also {
            this.methodBlackList.add(it)
        }
    }
}
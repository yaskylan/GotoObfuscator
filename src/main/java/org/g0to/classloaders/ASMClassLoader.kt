package org.g0to.classloaders

import org.g0to.wrapper.ClassWrapper

interface ASMClassLoader {
    fun getClassWrapper(name: String) : ClassWrapper?
}
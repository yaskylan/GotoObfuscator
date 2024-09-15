package org.g0to.classloaders

import org.g0to.core.Core
import org.g0to.wrapper.ClassWrapper

class GlobalClassManager(private val core: Core): ASMClassLoader {
    fun getClassWrapperNonNull(name: String): ClassWrapper {
        return getClassWrapper(name) ?: throw ClassNotFoundException(name)
    }

    override fun getClassWrapper(name: String): ClassWrapper? {
        val sequences = arrayOf(
            core.targetJar,
            core.extLoader
        )

        for (cloader in sequences) {
            val classWrapper = cloader.getClassWrapper(name)

            if (classWrapper != null) {
                return classWrapper
            }
        }

        return null
    }
}
package org.g0to.asm

import org.g0to.core.Core
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import kotlin.math.min

class ModifiedClassWriter : ClassWriter {
    private val core: Core

    constructor(core: Core, flags: Int) : this(core, null, flags)

    constructor(core: Core, classReader: ClassReader?, flags: Int) : super(classReader, flags) {
        this.core = core
    }

    override fun getCommonSuperClass(t1: String, t2: String) : String {
        val superClasses1 = getSuperClasses(t1)
        val superClasses2 = getSuperClasses(t2)
        val size = min(superClasses1.size, superClasses2.size)
        var i = 0

        while (i < size && superClasses1[i] == superClasses2[i]) {
            i++
        }

        return if (i == 0) {
            "java/lang/Object"
        } else {
            superClasses1[i - 1]
        }
    }

    private fun getSuperClasses(className: String) : List<String> {
        return ArrayList<String>().apply {
            var classWrapper = core.globalClassManager.getClassWrapper(className)
                ?: throw ClassNotFoundException("Class '$className' not found")

            while (classWrapper.getClassName() != "java/lang/Object") {
                add(classWrapper.getClassName())
                classWrapper = core.globalClassManager.getClassWrapper(classWrapper.getSuperName()!!)
                    ?: throw ClassNotFoundException("Class '${classWrapper.getSuperName()}' not found")
            }

            reverse()
        }
    }
}

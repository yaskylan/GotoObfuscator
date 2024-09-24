package org.g0to.classloaders

import org.g0to.core.Core
import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.tree.ClassNode

class SyntheticClasses(private val core: Core): ASMClassLoader {
    val classes = HashMap<String, ClassWrapper>()

    override fun getClassWrapper(name: String): ClassWrapper? {
        return classes[name]
    }

    fun addClassNode(classNode: ClassNode) {
        classes[classNode.name] = ClassWrapper(core, classNode, this)
    }
}
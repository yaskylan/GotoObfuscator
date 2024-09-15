package org.g0to.wrapper

import org.g0to.asm.ModifiedClassWriter
import org.g0to.classloaders.ASMClassLoader
import org.g0to.classloaders.ExtLoader
import org.g0to.core.Core
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier

class ClassWrapper(
    private val core: Core,
    var classNode: ClassNode,
    val classLoader: ASMClassLoader
) {
    fun getModifier(): Int = classNode.access
    fun getClassName(): String = classNode.name
    fun getSuperName(): String? = classNode.superName
    fun getInterfaces(): List<String> = classNode.interfaces
    fun getMethods(): List<MethodNode> = classNode.methods

    fun reloadClassNode(classNode: ClassNode) {
        this.classNode = classNode
    }

    fun isInterface(): Boolean {
        return Modifier.isInterface(classNode.access)
    }

    fun addField(fieldNode: FieldNode) {
        classNode.fields.add(fieldNode)
    }

    fun addMethod(methodNode: MethodNode) {
        classNode.methods.add(methodNode)
    }

    fun toByteArray(): ByteArray {
        return ModifiedClassWriter(core, ClassWriter.COMPUTE_FRAMES).apply {
            classNode.accept(this)
        }.toByteArray()
    }

    fun isExternal(): Boolean {
        return classLoader is ExtLoader
    }

    override fun toString(): String {
        return "ClassWrapper(${getClassName()})"
    }
}
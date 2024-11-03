package org.g0to.wrapper

import org.g0to.asm.ModifiedClassWriter
import org.g0to.classloaders.ASMClassLoader
import org.g0to.classloaders.ExtLoader
import org.g0to.core.Core
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
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
    fun getFields(): List<FieldNode> = classNode.fields

    fun reloadClassNode(classNode: ClassNode) {
        this.classNode = classNode
    }

    fun isInterface(): Boolean {
        return Modifier.isInterface(classNode.access)
    }

    fun allocMethodName(base: String, desc: String): String {
        val methodNames = getMethods()
            .filter { it.desc == desc }
            .map { it.name }
            .toHashSet()

        var index = 0
        var name: String

        do {
            name = base + (index++)
        } while (methodNames.contains(name))

        return name
    }

    fun addField(fieldNode: FieldNode) {
        val fields = classNode.fields

        fields.forEach {
            if (it.name == fieldNode.name && it.desc == fieldNode.desc) {
                throw IllegalStateException("Duplicate field: ${it.name}${it.desc}")
            }
        }

        fields.add(fieldNode)
    }

    fun addMethod(methodNode: MethodNode) {
        val methods = classNode.methods

        methods.forEach {
            if (it.name == methodNode.name && Type.getArgumentTypes(it.desc).contentEquals(Type.getArgumentTypes(methodNode.desc))) {
                throw IllegalStateException("Duplicate method: ${it.name}${it.desc}")
            }
        }

        methods.add(methodNode)
    }

    fun toByteArray(): ByteArray {
        return ModifiedClassWriter(core, ClassWriter.COMPUTE_FRAMES).apply {
            classNode.accept(this)
        }.toByteArray()
    }

    fun isAnnotationPresent(annotationName: String): Boolean {
        val desc = "L$annotationName;"

        return classNode.invisibleAnnotations.find { it.desc == desc }?.let { true }
            ?: classNode.visibleAnnotations.find { it.desc == desc }?.let { true }
            ?: false
    }

    fun isExternal(): Boolean {
        return classLoader is ExtLoader
    }

    fun isModule(): Boolean {
        return (classNode.access and Opcodes.ACC_MODULE) != 0
    }

    override fun toString(): String {
        return "ClassWrapper(${getClassName()})"
    }
}
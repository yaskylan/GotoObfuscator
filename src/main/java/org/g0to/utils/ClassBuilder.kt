package org.g0to.utils

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

class ClassBuilder {
    private val classNode = ClassNode()

    fun visit(block: ClassNode.() -> Unit): ClassBuilder {
        classNode.block()

        return this
    }

    fun method(method: MethodNode): ClassBuilder {
        classNode.methods.add(method)
        return this
    }

    fun method(block: MethodBuilder.() -> Unit): ClassBuilder {
        return method(MethodBuilder().apply(block).build())
    }

    fun field(field: FieldNode): ClassBuilder {
        classNode.fields.add(field)
        return this
    }

    fun field(access: Int,
              name: String,
              desc: String,
              signature: String? = null,
              value: Any? = null,
              block: FieldNode.() -> Unit): ClassBuilder {
        return field(FieldNode(access, name, desc, signature, value).apply(block))
    }

    fun build() = classNode
}
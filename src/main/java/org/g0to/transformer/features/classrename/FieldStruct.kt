package org.g0to.transformer.features.classrename

import org.objectweb.asm.tree.FieldNode

class FieldStruct(
    val owner: ClassStruct,
    val node: FieldNode
) {
    var mappedName: String? = null

    fun id() = node.name + node.desc
    fun name() = node.name
    fun desc() = node.desc
    fun hasMappedName(): Boolean = mappedName != null

    fun getFinalName(): String {
        if (mappedName == null) {
            return name()
        }

        return mappedName!!
    }

    fun idEqual(fieldStruct: FieldStruct): Boolean {
        return id() == fieldStruct.id()
    }

    override fun toString(): String {
        return "FieldStruct(owner=$owner, node=$node, mappedName=$mappedName)"
    }
}
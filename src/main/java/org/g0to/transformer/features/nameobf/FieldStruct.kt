package org.g0to.transformer.features.nameobf

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import java.lang.reflect.Modifier

class FieldStruct(
    val owner: ClassStruct,
    val node: FieldNode
) {
    var mappedName: String? = null

    fun isStatic() = Modifier.isStatic(node.access)
    fun isFinal() = Modifier.isFinal(node.access)
    fun isPublic() = Modifier.isPublic(node.access)
    fun isProtected() = Modifier.isProtected(node.access)
    fun isPrivate() = Modifier.isPrivate(node.access)
    fun isSynthetic() = (node.access and Opcodes.ACC_SYNTHETIC) != 0

    fun isEnumField(): Boolean {
        if (owner.isEnum() && isStatic() && isFinal()) {
            if (isPublic()) {
                return desc().equals('L' + owner.getClassName() + ';')
            } else if (isPrivate()) {
                return name().equals("\$VALUES")
                        && desc().equals("[L" + owner.getClassName() + ';')
            }
        }

        return false
    }

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

    fun shouldRename(): Boolean {
        return !isSynthetic() && !isEnumField()
    }

    override fun toString(): String {
        return "FieldStruct(owner=$owner, node=$node, mappedName=$mappedName)"
    }
}
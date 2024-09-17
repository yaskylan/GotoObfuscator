package org.g0to.transformer.features.nameobf

import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier

class MethodStruct(
    val owner: ClassStruct,
    val node: MethodNode
) {
    var mappedName: String? = null

    val superMethod: MethodStruct? by lazy { searchSuperMethod(owner) }

    private fun searchSuperMethod(classStruct: ClassStruct): MethodStruct? {
        val superClass = classStruct.superClass ?: return null

        for (method in superClass.methods.values) {
            if (idEqual(method)) {
                return method
            }
        }

        for (iface in classStruct.interfaces) {
            for (method in iface.methods.values) {
                if (idEqual(method)) {
                    return method
                }
            }
        }

        return searchSuperMethod(classStruct)
    }

    fun isMain() = node.name == "main" && node.desc == "([Ljava/lang/String;)V"
    fun isSpacial() = node.name[0] == '<'
    fun isAbstract() = Modifier.isAbstract(node.access)
    fun isNative() = Modifier.isNative(node.access)
    fun isStatic() = Modifier.isStatic(node.access)
    fun isPublic() = Modifier.isPublic(node.access)
    fun isProtected() = Modifier.isProtected(node.access)
    fun isPrivate() = Modifier.isPrivate(node.access)

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

    fun idEqual(methodStruct: MethodStruct): Boolean {
        return id() == methodStruct.id()
    }

    fun shouldRename(): Boolean {
        return !isSpacial() && !isNative() && !isMain()
    }

    override fun toString(): String {
        return "MethodStruct(owner=$owner, node=$node, mappedName=$mappedName)"
    }
}
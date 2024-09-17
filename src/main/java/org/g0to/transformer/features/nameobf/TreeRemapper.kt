package org.g0to.transformer.features.nameobf

import org.objectweb.asm.Type
import org.objectweb.asm.commons.Remapper

class TreeRemapper(
    private val classTree: ClassTree
) : Remapper() {
    override fun map(internalName: String): String {
        val classStruct = classTree.classes[internalName]

        if (classStruct == null || classStruct.isExternal() || !classStruct.hasMappedName()) {
            return internalName
        }

        return classStruct.mappedName!!
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val classStruct = classTree.classes[owner]
        if (classStruct == null || classStruct.isExternal()) {
            return name
        }

        val mappedName = classStruct.searchField(name, descriptor)?.mappedName ?: return name
        return mappedName
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        if (owner[0] == '[') {
            return name
        }

        val classStruct = classTree.classes[owner]
        if (classStruct == null || classStruct.isExternal()) {
            return name
        }

        return classStruct.searchMethod(name, descriptor)?.mappedName ?: name
    }

    override fun mapAnnotationAttributeName(descriptor: String, name: String): String {
        return mapMethodName(Type.getType(descriptor).className.replace(".", "/"), name, descriptor)
    }

    override fun mapRecordComponentName(owner: String, name: String, descriptor: String): String {
        return mapMethodName(owner, name, descriptor)
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        val classStruct = classTree.classes[Type.getReturnType(descriptor).className.replace(".", "/")]
        if (classStruct == null || classStruct.isExternal()) {
            return name
        }

        return searchAbstractMethod(classStruct, name)?.mappedName ?: name
    }

    private fun searchAbstractMethod(classStruct: ClassStruct, name: String): MethodStruct? {
        for (method in classStruct.getMethods()) {
            if (method.name() == name && method.isAbstract()) {
                return method
            }
        }

        for (parent in classStruct.parents) {
            val method = searchAbstractMethod(parent, name)

            if (method != null) {
                return method
            }
        }

        return null
    }
}
package org.g0to.transformer.features.classrename

import org.g0to.wrapper.ClassWrapper
import org.objectweb.asm.Opcodes

class ClassStruct(
    val classWrapper: ClassWrapper,
    val superClass: ClassStruct?,
    val interfaces: ArrayList<ClassStruct>
) {
    val subClasses = ArrayList<ClassStruct>()
    val fields = HashMap<String, FieldStruct>()
    val methods = HashMap<String, MethodStruct>()
    val parents: ArrayList<ClassStruct> by lazy {
        ArrayList<ClassStruct>().apply {
            if (superClass != null) {
                add(superClass)
            }

            addAll(interfaces)
        }
    }

    var mappedName: String? = null

    fun init() {
        initClass()
        initFields()
        initMethods()
    }

    private fun initClass() {
        superClass?.subClasses?.add(this)

        for (iface in interfaces) {
            iface.subClasses.add(this)
        }
    }

    private fun initFields() {
        for (field in classWrapper.classNode.fields) {
            val fieldStruct = FieldStruct(this, field)

            fields[fieldStruct.id()] = fieldStruct
        }
    }

    private fun initMethods() {
        for (method in classWrapper.classNode.methods) {
            val methodStruct = MethodStruct(this, method)

            methods[methodStruct.id()] = methodStruct
        }
    }

    fun shouldRename(setting: ClassRename.Setting): Boolean {
        return methods.values.none { it.isNative() || (!setting.renameMain && it.isMain()) }
    }

    fun getClassName(): String = classWrapper.getClassName()
    fun hasMappedName(): Boolean = mappedName != null

    fun getFinalName(): String {
        if (mappedName == null) {
            return getClassName()
        }

        return mappedName!!
    }

    fun isEnum() = (classWrapper.classNode.access and Opcodes.ACC_ENUM) != 0

    fun getField(name: String, desc: String): FieldStruct? {
        return fields[name + desc]
    }

    fun getMethod(name: String, desc: String): MethodStruct? {
        return methods[name + desc]
    }

    fun searchField(name: String, desc: String): FieldStruct? {
        var fieldStruct: FieldStruct? = getField(name, desc)

        if (fieldStruct == null) {
            for (parent in parents) {
                val parentField = parent.searchField(name, desc)

                if (parentField != null) {
                    fieldStruct = parentField
                    break
                }
            }
        }

        return fieldStruct
    }

    fun searchMethod(name: String, desc: String): MethodStruct? {
        var methodStruct: MethodStruct? = getMethod(name, desc)

        if (methodStruct == null) {
            for (parent in parents) {
                val parentMethod = parent.searchMethod(name, desc)

                if (parentMethod != null) {
                    methodStruct = parentMethod
                    break
                }
            }
        }

        return methodStruct
    }

    fun getFields() = fields.values
    fun getMethods() = methods.values

    fun isExternal() = classWrapper.isExternal()

    override fun toString(): String {
        return "ClassStruct(classWrapper=$classWrapper, mappedName=$mappedName)"
    }
}
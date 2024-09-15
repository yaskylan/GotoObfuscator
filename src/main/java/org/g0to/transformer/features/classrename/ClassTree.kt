package org.g0to.transformer.features.classrename

import org.g0to.core.Core
import org.g0to.wrapper.ClassWrapper

class ClassTree {
    val classes = HashMap<String, ClassStruct>()

    fun init(core: Core) {
        for (cw in core.targetJar.getClasses()) {
            if (classes.containsKey(cw.getClassName())) {
                continue
            }

            loadClassStruct(core, cw)
        }
    }

    private fun loadClassStruct(core: Core, cw: ClassWrapper): ClassStruct {
        classes[cw.getClassName()].also { cached ->
            if (cached != null) {
                return cached
            }
        }

        val superClass: ClassStruct?
        findSuperClass(core, cw).also {
            superClass = if (it == null) {
                null
            } else {
                loadClassStruct(core, it)
            }
        }

        val interfaces = ArrayList<ClassStruct>()
        for (ifaceName in cw.getInterfaces()) {
            interfaces.add(loadClassStruct(core, core.globalClassManager.getClassWrapperNonNull(ifaceName)))
        }

        val classStruct = ClassStruct(cw, superClass, interfaces)
        classStruct.init()

        classes[cw.getClassName()] = classStruct

        return classStruct
    }

    private fun findSuperClass(core: Core, cw: ClassWrapper): ClassWrapper? {
        val superName = cw.getSuperName() ?: return null

        return core.globalClassManager.getClassWrapperNonNull(superName)
    }
}
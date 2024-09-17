package org.g0to.exclusion

import com.google.gson.annotations.SerializedName
import org.apache.logging.log4j.LogManager

class ExclusionManager(excludeSetting: ExcludeSetting?) {
    class ExcludeSetting(
        @SerializedName("classes")
        val classes: Array<String>?,
        @SerializedName("methods")
        val methods: Array<String>?,
        @SerializedName("fields")
        val fields: Array<String>?
    )

    private val logger = LogManager.getLogger("ExcludeManager")
    private val classes = ArrayList<ClassObject>()
    private val methods = ArrayList<MemberObject>()
    private val fields = ArrayList<MemberObject>()

    init {
        if (excludeSetting != null) {
            parseClasses(classes, excludeSetting.classes)
            parseMembers(methods, excludeSetting.methods)
            parseMembers(fields, excludeSetting.fields)
        }
    }

    private fun parseClasses(dst: ArrayList<ClassObject>, src: Array<String>?) {
        if (src == null) {
            return
        }

        for (s in src) {
            dst.add(ClassObject(s))
        }
    }

    private fun parseMembers(dst: ArrayList<MemberObject>, src: Array<String>?) {
        if (src == null) {
            return
        }

        for (s in src) {
            val split = s.split("\u0020")

            if (split.size < 3) {
                logger.warn("Parameter too short. {}", s)
            } else {
                dst.add(MemberObject(split[0], split[1], split[2]))
            }
        }
    }

    fun isExcludedClass(classFullName: String): Boolean {
        if (classes.isEmpty()) {
            return false
        }

        return match(classes, classFullName).second
    }

    fun isExcludedField(owner: String, name: String, desc: String): Boolean {
        if (fields.isEmpty()) {
            return false
        }

        return isExcludedMember(fields, owner, name, desc)
    }

    fun isExcludedMethod(owner: String, name: String, desc: String): Boolean {
        if (methods.isEmpty()) {
            return false
        }

        return isExcludedMember(methods, owner, name, desc)
    }

    private fun isExcludedMember(comparisonList: List<MemberObject>, owner: String, name: String, desc: String): Boolean {
        val (memberObject, isMatch) = match(comparisonList, owner)

        if (memberObject == null || !isMatch) {
            return false
        }

        val ignoreName = memberObject.name == "*"
        val ignoreDesc = memberObject.desc == "*"

        return when {
            ignoreName && ignoreDesc -> {
                true
            }
            ignoreName -> {
                memberObject.desc == desc
            }
            ignoreDesc -> {
                memberObject.name == name
            }
            else -> {
                memberObject.name == name && memberObject.desc == desc
            }
        }
    }

    private fun <T : ClassObject> match(comparisonList: List<T>, classFullName: String) : Pair<T?, Boolean> {
        val packageName: String?
        val className: String

        splitClassFullName(classFullName).also {
            packageName = it[0]
            className = it[1]!!
        }

        for (compareObject in comparisonList) {
            when (compareObject.className) {
                "*" -> {
                    when {
                        packageName == null && compareObject.packageName == null -> {
                            return Pair(compareObject, true)
                        }
                        packageName != null && compareObject.packageName != null -> {
                            if (compareObject.packageName == packageName) {
                                return Pair(compareObject, true)
                            }
                        }
                    }
                }
                "**" -> {
                    when {
                        compareObject.packageName == null -> {
                            return Pair(compareObject, true)
                        }
                        packageName != null -> {
                            if (packageName.startsWith(compareObject.packageName)) {
                                return Pair(compareObject, true)
                            }
                        }
                    }
                }
                className -> {
                    return when {
                        packageName == null && compareObject.packageName == null -> {
                            Pair(compareObject, true)
                        }
                        packageName == null || compareObject.packageName == null -> {
                            Pair(compareObject, false)
                        }
                        else -> {
                            Pair(compareObject, compareObject.packageName == packageName)
                        }
                    }
                }
            }
        }

        return Pair(null, false)
    }

    private fun splitClassFullName(s: String): Array<String?> {
        val index = s.lastIndexOf('/')

        if (index == -1) {
            return arrayOf(null, s)
        }

        return arrayOf(s.substring(0, index), s.substring(index + 1))
    }

    private open inner class ClassObject(classFullName: String) {
        val packageName: String?
        val className: String

        init {
            val split = splitClassFullName(classFullName)

            this.packageName = split[0]
            this.className = split[1]!!
        }
    }

    private inner class MemberObject(
        classFullName: String,
        val name: String,
        val desc: String
    ) : ClassObject(classFullName)
}
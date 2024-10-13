package org.g0to.utils.extensions

import org.g0to.utils.InstructionBuffer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier

fun MethodNode.isPublic() = Modifier.isPublic(access)
fun MethodNode.isProtected() = Modifier.isProtected(access)
fun MethodNode.isPrivate() = Modifier.isPrivate(access)
fun MethodNode.isNative() = Modifier.isNative(access)
fun MethodNode.isAbstract() = Modifier.isAbstract(access)
fun MethodNode.inSynchronized() = Modifier.isSynchronized(access)
fun MethodNode.isStrict() = Modifier.isStrict(access)
fun MethodNode.isSynthetic() = (access and Opcodes.ACC_SYNTHETIC) != 0
fun MethodNode.isInitializer() = name == "<init>" || name == "<clinit>"

fun MethodNode.modify(block: (InstructionBuffer) -> Unit) = InstructionBuffer(this).apply(block).apply()
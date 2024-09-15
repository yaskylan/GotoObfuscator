package org.g0to.transformer

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.transformer.features.*
import org.g0to.transformer.features.classrename.ClassRename

object TransformerRegistry {
    private val registry = HashMap<String, Class<out Transformer<*>>>()

    init {
        registry["StringEncrypt"] = StringEncrypt::class.java
        registry["FlowObfuscate"] = FlowObfuscate::class.java
        registry["NumberEncrypt"] = NumberEncrypt::class.java
        registry["GotoReplacer"] = GotoReplacer::class.java
        registry["InvokeProxy"] = InvokeProxy::class.java
        registry["ClassRename"] = ClassRename::class.java
    }

    fun newTransformerByName(name: String, setting: TransformerBaseSetting): Transformer<*>? {
        val transformerClass = registry[name]
            ?: return null
        val constructor = transformerClass.getConstructor(TransformerBaseSetting::class.java)
            ?: throw IllegalStateException("The constructor of ${transformerClass.name} is not illegal")

        return constructor.newInstance(setting)
    }
}
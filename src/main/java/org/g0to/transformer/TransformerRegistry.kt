package org.g0to.transformer

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.transformer.features.*
import org.g0to.transformer.features.nameobf.NameObfuscation

object TransformerRegistry {
    private val registry = HashMap<String, Class<out Transformer<*>>>()

    init {
        registry["StringEncryption"] = StringEncryption::class.java
        registry["NumberEncryption"] = NumberEncryption::class.java
        registry["NameObfuscation"] = NameObfuscation::class.java
        registry["FlowObfuscation"] = FlowObfuscation::class.java
        registry["VariableRename"] = VariableRename::class.java
        registry["GotoReplacer"] = GotoReplacer::class.java
        registry["InvokeProxy"] = InvokeProxy::class.java
    }

    fun newTransformerByName(name: String, setting: TransformerBaseSetting): Transformer<*>? {
        val transformerClass = registry[name]
            ?: return null
        val constructor = transformerClass.getConstructor(TransformerBaseSetting::class.java)
            ?: throw IllegalStateException("The constructor of ${transformerClass.name} is not illegal")

        return constructor.newInstance(setting)
    }
}
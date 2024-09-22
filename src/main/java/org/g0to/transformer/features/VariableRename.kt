package org.g0to.transformer.features

import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core
import org.g0to.transformer.Transformer

class VariableRename(
    setting: TransformerBaseSetting
) : Transformer<VariableRename.Setting>("VariableRename", setting as Setting) {
    class Setting : TransformerBaseSetting()

    override fun run(core: Core) {
        core.foreachTargetMethods { _, method ->
            if (method.localVariables == null || method.localVariables.isEmpty()) {
                return@foreachTargetMethods
            }

            for (localVariable in method.localVariables) {
                localVariable.name = "v"
            }
        }
    }
}
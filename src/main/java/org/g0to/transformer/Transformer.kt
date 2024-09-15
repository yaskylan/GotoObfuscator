package org.g0to.transformer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.g0to.conf.transformer.settings.TransformerBaseSetting
import org.g0to.core.Core

abstract class Transformer<T : TransformerBaseSetting>(
    val name: String,
    val setting: T
) {
    protected val logger: Logger = LogManager.getLogger(name)

    open fun preRun() {
        logger.info("Running")
    }

    abstract fun run(core: Core)
}

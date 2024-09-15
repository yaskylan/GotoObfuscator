package org.g0to

import com.google.gson.GsonBuilder
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.g0to.conf.Configuration
import org.g0to.core.Core
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val commandLine = DefaultParser().parse(Options().init(), args)
    val conf = parseConf(requireNotNull(commandLine.getOptionValue("c")) { "Configuration path is required" })
    Configurator.setRootLevel(Level.getLevel(conf.logLevel))

    val core = Core(conf)
    core.init()
    core.run()
    core.done()
}

fun parseConf(path: String): Configuration {
    val gson = GsonBuilder().create()
    val reader = InputStreamReader(FileInputStream(path), StandardCharsets.UTF_8)

    return gson.fromJson(reader, Configuration::class.java)
}

fun Options.init(): Options {
    addRequiredOption("c", "config", true, "The configuration")

    return this
}
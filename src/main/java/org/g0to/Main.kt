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
    println("GOTO Obfuscator")
    println(BuildInfo.getBuildInfo().toString())
    println("QQ Group: 967325513")
    println("TG Group: https://t.me/+LBe1J5tZekFkYjI1")
    println("Github:   https://github.com/yaskylan/GotoObfuscator/")

    val commandLine = DefaultParser().parse(Options().init(), args)

    if (commandLine.getOptionValue("h") != null || commandLine.getOptionValue("c") == null) {
        printHelp()
        return
    }

    val conf = parseConf(commandLine.getOptionValue("c"))
    Configurator.setRootLevel(Level.getLevel(conf.logLevel))

    val core = Core(conf)
    core.init()
    core.run()
    core.done()
}

fun printHelp() {
    println("Usage: java -jar Goto.jar -c <configuration path>")
}

fun parseConf(path: String): Configuration {
    val gson = GsonBuilder().create()
    val reader = InputStreamReader(FileInputStream(path), StandardCharsets.UTF_8)

    return gson.fromJson(reader, Configuration::class.java)
}

fun Options.init(): Options {
    addOption("h", "help", false, "Print help")
    addOption("c", "config", true, "The configuration")

    return this
}
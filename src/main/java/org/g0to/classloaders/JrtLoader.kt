package org.g0to.classloaders

import org.apache.logging.log4j.LogManager
import org.g0to.core.Core
import java.io.Closeable
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.*

class JrtLoader(core: Core) : Closeable {
    companion object {
        private val logger = LogManager.getLogger("JrtLoader")
    }

    private val jrtFileSystem = FileSystems.newFileSystem(
        URI.create("jrt:/"),
        Collections.singletonMap("java.home", core.conf.jdkPath)
    )
    private val modules = Files.list(jrtFileSystem.getPath("/modules")).toList()

    fun findClass(name: String): ByteArray? {
        for (module in modules) {
            val classPath = module.resolve(name)

            if (Files.exists(classPath)) {
                logger.trace("Jrt found class: {}", name)

                return Files.readAllBytes(classPath)
            }
        }

        return null
    }

    override fun close() {
        jrtFileSystem.close()
    }
}
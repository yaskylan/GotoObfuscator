import java.nio.charset.StandardCharsets
import java.time.Instant

plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "org.g0to"
version = "1.0.1"

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks {
    shadowJar {
        archiveFileName.set("Goto.jar")
        archiveClassifier.set("")

        manifest {
            attributes("Main-Class" to "org.g0to.MainKt")
        }

        exclude("META-INF/MANIFEST.MF")

        kotlin.run {
            val buildInfoFile = File(buildDir, "buildinfo")

            buildInfoFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write("${project.version}\n")
                writer.write("${Instant.now()}\n")
            }

            from(buildInfoFile) {
                into("")
            }
        }
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        kotlin.srcDirs("src/main/java")
        resources.srcDir("src/main/resources")
    }

    test {
        java.srcDir("src/test/java")
        kotlin.srcDirs("src/test/java")
        resources.srcDir("src/test/resources")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-analysis:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("org.ow2.asm:asm-util:9.7")
    implementation("org.ow2.asm:asm:9.7")

    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("commons-io:commons-io:2.16.1")
    implementation("commons-cli:commons-cli:1.8.0")

    implementation("com.google.code.gson:gson:2.11.0")
}

kotlin {
    jvmToolchain(21)
}
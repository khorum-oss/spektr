import java.io.File

plugins {
    id("com.gradleup.shadow") version "9.3.1"
}

val versionFile = file("version.txt")
val currentVersion = versionFile.readText().trim()
version = currentVersion

group = "org.khorum.oss.spektr.ghost-book"

val jarBaseName = "haunted-house-tracker-test-api"
val dockerJarsDir: File = rootProject.file("examples/docker/endpoint-jars")

dependencies {
    implementation(project(":dsl"))
    implementation(project(":examples:common"))
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("$jarBaseName-$version.jar")

    exclude("org/khorum/oss/spektr/dsl/**")

    mergeServiceFiles()

    doLast {
        // Remove old versions of this JAR from docker folder
        dockerJarsDir.listFiles()?.filter {
            it.name.startsWith("$jarBaseName-") && it.name.endsWith(".jar")
        }?.forEach { it.delete() }

        // Copy new JAR to docker folder
        val builtJar = archiveFile.get().asFile
        builtJar.copyTo(File(dockerJarsDir, builtJar.name), overwrite = true)
        println("Copied ${builtJar.name} to ${dockerJarsDir.path}")

        // Increment patch version
        val parts = currentVersion.split(".").toMutableList()
        parts[2] = (parts[2].toInt() + 1).toString()
        val newVersion = parts.joinToString(".")
        versionFile.writeText(newVersion)
        println("Version incremented: $currentVersion -> $newVersion")
    }
}

tasks.bootJar { enabled = false }

tasks.jar {
    enabled = false
}


plugins {
    id("org.khorum.oss.plugins.open.spektr")
}

val currentVersion = file("version.txt").readText().trim()
version = currentVersion

group = "org.khorum.oss.spektr.ghost-book"

dependencies {
    implementation("org.khorum.oss.spektr:spektr-dsl:1.0.8")
    implementation(project(":examples:common"))
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

spektr {
    apiProvider {
        jarBaseName = "haunted-house-tracker-test-api"
        versionFile = "version.txt"
        dockerJarsDir = "examples/docker/endpoint-jars"
    }
}

tasks.build { dependsOn(tasks.cacheAndVersionJar) }

tasks.bootJar { enabled = false }

tasks.jar {
    enabled = false
}

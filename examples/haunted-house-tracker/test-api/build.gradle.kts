
plugins {
    id("org.khorum.oss.spektr.plugin")
}

val currentVersion = file("version.txt").readText().trim()
version = currentVersion

group = "org.khorum.oss.spektr.haunted-house-tracker"

dependencies {
    implementation(project(":dsl"))
    implementation(project(":examples:common"))
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

spektr {
    apiProvider {
        jarBaseName = "ghost-book-test-api"
        versionFile = "version.txt"
        dockerJarsDir = "examples/docker/endpoint-jars"
    }
}

tasks.build { dependsOn(tasks.cacheAndVersionJar) }

tasks.bootJar { enabled = false }

tasks.jar {
    enabled = false
}

version = "1.0.0"

group = "org.khorum.oss.spektr.haunted-house-tracker"

dependencies {
    implementation(project(":dsl"))
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.bootJar { enabled = false }

tasks.jar {
    enabled = true
    archiveFileName.set("ghost-book-test-api-$version.jar")
}
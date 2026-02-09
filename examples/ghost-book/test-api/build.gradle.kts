version = "1.0.0"

dependencies {
    implementation(project(":dsl"))
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.bootJar { enabled = false }
tasks.jar {
    enabled = true
    archiveFileName.set("haunted-house-tracker-test-api-$version.jar")
}
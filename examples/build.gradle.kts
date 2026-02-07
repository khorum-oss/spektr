// Examples module is a library, not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":dsl"))
}
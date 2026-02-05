
// DSL module is a pure Kotlin library, not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
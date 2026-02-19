
val dslVersion: String by rootProject.extra

group = "org.khorum.oss.spektr"
version = dslVersion

// DSL module is a pure Kotlin library, not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

tasks.test {
    useJUnitPlatform()
}
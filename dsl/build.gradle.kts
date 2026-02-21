
val dslVersion: String by rootProject.extra

plugins {
    id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.spektr"
version = dslVersion

// DSL module is a pure Kotlin library, not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

tasks.test {
    useJUnitPlatform()
}
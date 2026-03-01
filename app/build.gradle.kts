import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.khorum.oss.plugins.open.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.open.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv
import kotlin.apply

val spektrVersion: String by rootProject.extra

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.1.0-M1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces")
    id("com.google.cloud.tools.jib")
}

version = spektrVersion

dependencies {
    implementation("org.khorum.oss.spektr:spektr-dsl:1.0.8")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-webservices")

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host")

    implementation("tools.jackson.module:jackson-module-kotlin")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webservices-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}


digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    isPlugin = true
    dryRun = false
}

tasks.uploadToDigitalOceanSpaces?.apply {
    val task: Task = tasks.mavenGeneratedArtifacts ?: throw Exception("mavenGeneratedArtifacts task not found")
    dependsOn(task)
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"  // Must match the name expected by the DO Spaces plugin
    name = "Spektr App"
    description = """
            An application for mocking out APIs.
        """
    websiteUrl = "https://github.com/khorum-oss/spektr/tree/main/app"

    licenses {
        license {
            name = "MIT License"
            url = "https://opensource.org/license/mit"
        }
    }

    developers {
        developer {
            id = "khorum-oss"
            name = "Khorum OSS Team"
            email = "khorum.oss@gmail.com"
            organization = "Khorum OSS"
        }
    }

    scm {
        connection.set("https://github.com/khorum-oss/spektr.git")
    }
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/khorum-oss/spektr"
        tags = setOf("latest", version.toString())
    }
    container {
        ports = listOf("8080")
        environment = mapOf(
            "ENDPOINT_JARS_DIR" to "/app/endpoint-jars"
        )
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
    extraDirectories {
        paths {
            path {
                setFrom("src/main/jib")
                into = "/"
            }
        }
    }
}
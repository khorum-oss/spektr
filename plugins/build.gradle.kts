import org.khorum.oss.plugins.open.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.open.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv

val pluginVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.plugins.open"
version = pluginVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("spektr")
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("spektrPlugin") {
            id = "org.khorum.oss.plugins.open.spektr"
            version = version.toString()
            implementationClass = "org.khorum.oss.plugins.open.spektr.SpektrPlugin"
        }
    }
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
    name = "Spektr Plugin"
    description = """
            A plugin for configuring Spektr endpoints in Gradle projects.
        """
    websiteUrl = "https://github.com/khorum-oss/spektr/tree/main/secrets"

    licenses {
        license {
            name = "MIT License"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
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
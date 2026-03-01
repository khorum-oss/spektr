plugins {
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
	id("org.springframework.boot") version "4.1.0-M1"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	id("org.jetbrains.kotlinx.kover") version "0.7.6"
	id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.4" apply false
	id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.4" apply false
	id("org.khorum.oss.plugins.open.secrets") version "1.0.4"
	id("org.khorum.oss.plugins.open.spektr") version "1.0.13" apply false
	id("org.khorum.oss.plugins.open.pipeline") version "1.0.4" apply false
	id("com.google.cloud.tools.jib") version "3.5.3" apply false
}

group = "org.khorum.oss"

extra["spektrVersion"] = file("app/VERSION").readText().trim()

// Root project is not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = false }

repositories {
	mavenCentral()
	maven {
		url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com")
	}
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.jetbrains.kotlinx.kover")

	repositories {
		mavenCentral()
		maven {
			url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com")
		}
	}

	dependencies {
		implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
		testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	// Disable Kover instrumentation globally to avoid race condition
	// with kover-agent.args file during parallel builds (Kover 0.7.x bug)
	extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
		disable()
	}

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}
}

detekt {
	buildUponDefaultConfig = true
	allRules = false
	config.setFrom(files("$rootDir/detekt.yml"))
	baseline = file("$rootDir/detekt-baseline.xml")
	parallel = true
}

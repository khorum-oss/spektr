plugins {
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
	id("org.springframework.boot") version "4.1.0-M1"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	id("org.jetbrains.dokka") version "1.9.20"
	id("org.jetbrains.kotlinx.kover") version "0.7.6"
	id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.0" apply false
	id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.0" apply false
	id("org.khorum.oss.plugins.open.secrets") version "1.0.0"
	id("org.khorum.oss.plugins.open.pipeline") version "1.0.0" apply false
}

group = "org.khorum.oss"

extra["dslVersion"] = "1.0.0"
extra["spektrVersion"] = "1.0.0"
extra["pluginVersion"] = "1.0.0"

// Root project is not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = false }

repositories {
	mavenCentral()
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.jetbrains.kotlinx.kover")

	repositories {
		mavenCentral()
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
}

detekt {
	buildUponDefaultConfig = true
	allRules = false
	config.setFrom(files("$rootDir/detekt.yml"))
	baseline = file("$rootDir/detekt-baseline.xml")
	parallel = true
}

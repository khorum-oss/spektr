plugins {
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
	id("org.springframework.boot") version "4.1.0-M1"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	id("org.jetbrains.dokka") version "2.1.0" apply false
	id("org.jetbrains.dokka-javadoc") version "2.1.0" apply false
	id("org.jetbrains.kotlinx.kover") version "0.7.6"
	id("org.sonarqube") version "7.0.0.6105"
	id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.3" apply false
	id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.3" apply false
	id("org.khorum.oss.plugins.open.secrets") version "1.0.0"
	id("org.khorum.oss.plugins.open.spektr") version "1.0.17" apply false
	id("org.khorum.oss.plugins.open.pipeline") version "1.0.0" apply false
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

	// Override jackson-bom to fix GHSA-72hv-8253-57qq (async parser DoS)
	ext["jackson-bom.version"] = "3.1.0"

	repositories {
		mavenCentral()
		maven {
			url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com")
		}
	}

	val loggingVersion = "4.0.0-beta-2"

	dependencies {
		implementation("io.github.microutils:kotlin-logging:$loggingVersion")

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

sonar {
	properties {
		property("sonar.projectKey", "khorum-oss_spektr")
		property("sonar.organization", "khorum-oss")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths",
			"${layout.buildDirectory.get()}/reports/kover/report.xml")
	}
}
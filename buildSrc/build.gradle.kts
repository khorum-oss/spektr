plugins {
    kotlin("jvm") version "2.3.0"
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.3.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.8") // For mocking in Kotlin
    testImplementation(gradleTestKit()) // For Gradle project/test DSLs
}

gradlePlugin {
    plugins {
        create("spektrPlugin") {
            id = "org.khorum.oss.spektr.plugin"
            version = "0.0.1"
            implementationClass = "org.khorum.oss.spektr.plugin.SpektrPlugin"
        }
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

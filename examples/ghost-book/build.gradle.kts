import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.1.0-M1"
    id("io.spring.dependency-management") version "1.1.7"
}

buildscript {
    dependencies {
        classpath("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        classpath("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    }
}

dependencies {
    implementation(project(":dsl"))
    implementation(project(":examples:common"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    implementation("wsdl4j:wsdl4j:1.6.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("tools.jackson.module:jackson-module-kotlin")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

    testImplementation(project(":examples:test-common"))
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:2.0.2")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
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

// Task to build the Spektr Docker image
tasks.register<Exec>("buildSpektrImage") {
    group = "docker"
    description = "Builds the spektr:local Docker image"
    workingDir = rootProject.projectDir
    commandLine("docker", "build", "-t", "spektr:local", ".")

    onlyIf {
        val process = ProcessBuilder("docker", "images", "-q", "spektr:local")
            .redirectErrorStream(true)
            .start()

        process.inputStream.read().toString().isEmpty()
    }
}

// Task to prepare test environment (build image + JARs)
tasks.register("prepareTestEnv") {
    group = "verification"
    description = "Prepares test environment: builds Docker image and test-api JARs"
    dependsOn("buildSpektrImage")
    dependsOn(":examples:ghost-book:test-api:shadowJar")
}

tasks.test {
    useJUnitPlatform()
    dependsOn("prepareTestEnv")
}

// Task to generate XSD from JAXB-annotated Kotlin classes
tasks.register<JavaExec>("generateXsd") {
    description = "Generates XSD schema from JAXB-annotated Ghost classes"
    group = "build"

    dependsOn(":examples:common:classes")

    mainClass.set("org.khorum.oss.spekter.examples.common.XsdGeneratorKt")
    classpath = project(":examples:common").sourceSets["main"].runtimeClasspath +
        configurations["runtimeClasspath"]

    args = listOf(
        file("src/main/resources/xsd/ghosts.xsd").absolutePath
    )
}

// Generate XSD before processing resources
// Note: Temporarily disabled due to JAXB annotation issues in domain classes
// tasks.processResources {
//     dependsOn("generateXsd")
// }
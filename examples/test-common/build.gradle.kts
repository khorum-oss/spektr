plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.1.0-M1"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator-test")
    implementation("org.springframework.boot:spring-boot-starter-webflux-test")
    implementation("org.springframework.boot:spring-boot-testcontainers")
    implementation("org.testcontainers:testcontainers:2.0.2")
    implementation("org.testcontainers:junit-jupiter:1.21.3")
    implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    implementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    implementation("io.projectreactor:reactor-test")
}
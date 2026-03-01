rootProject.name = "spektr"

include(
    "app",
    "examples",
    "examples:ghost-book",
    "examples:ghost-book:test-api",
    "examples:haunted-house-tracker",
    "examples:haunted-house-tracker:test-api",
    "examples:common"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com")
        }
    }
}
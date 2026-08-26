pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.baole.konture") version "0.8.2"
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "sample"

include(":app")
include(":domain")
include(":data")
include(":konture-test")

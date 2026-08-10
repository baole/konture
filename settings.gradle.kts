pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    val applyPlugin = System.getProperty("idea.active") == "true" ||
        System.getProperty("idea.sync.active") == "true" ||
        System.getProperty("konture.applyPlugin") == "true" ||
        System.getenv("KONTURE_APPLY_PLUGIN") == "true"
    if (applyPlugin) {
        repositories {
            mavenLocal()
            mavenCentral()
        }
        dependencies {
            classpath("io.github.baole.konture:plugin-gradle:0.7.7")
        }
    }
}

val applyPlugin = System.getProperty("idea.active") == "true" ||
    System.getProperty("idea.sync.active") == "true" ||
    System.getProperty("konture.applyPlugin") == "true" ||
    System.getenv("KONTURE_APPLY_PLUGIN") == "true"

enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "konture"

include("core")
include("library")
include("plugin-gradle")

include("konture-test")

if (applyPlugin) {
    apply(plugin = "io.github.baole.konture")
}

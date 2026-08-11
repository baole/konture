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
    val applyPlugin = System.getProperty("idea.active").toBoolean() ||
        System.getProperty("idea.sync.active").toBoolean() ||
        System.getProperty("konture.applyPlugin").toBoolean()

    gradle.extensions.add("applyPlugin", applyPlugin)

    if (applyPlugin) {
        val kontureVersion = providers.gradleProperty("version").get()
        repositories {
            mavenLocal()
            mavenCentral()
        }
        dependencies {
            classpath("io.github.baole.konture:plugin-gradle:$kontureVersion")
        }
    }
}

val applyPlugin = gradle.extensions.get("applyPlugin") as Boolean

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

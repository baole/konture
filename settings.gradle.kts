val applyPlugin = System.getProperty("idea.active") == "true" ||
    System.getProperty("idea.sync.active") == "true" ||
    System.getProperty("konture.applyPlugin") == "true" ||
    System.getenv("KONTURE_APPLY_PLUGIN") == "true"

System.setProperty("konture.applyPluginInternal", applyPlugin.toString())

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

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

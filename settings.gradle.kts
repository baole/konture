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

val isIdeSync = System.getProperty("idea.active") == "true" ||
    System.getProperty("idea.sync.active") == "true" ||
    providers.systemProperty("idea.active").orNull == "true" ||
    providers.systemProperty("idea.sync.active").orNull == "true" ||
    providers.systemProperty("konture.includeTest").orNull == "true"

if (isIdeSync) {
    includeBuild("konture-test")
}

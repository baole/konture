buildscript {
    val isKonturePluginRequested =
        System.getProperty("archTest") != null ||
            gradle.startParameter.taskNames.any { taskName ->
                taskName.contains("generateArchitectureLayout") ||
                    taskName.contains("generateDependencyGraph")
            }

    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        if (isKonturePluginRequested) {
            classpath("io.github.baole.konture:plugin-gradle:0.7.2")
        }
    }
}

plugins {
    id("konture.root")
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.nmcp)
    `maven-publish`
}

val isKonturePluginRequested =
    System.getProperty("archTest") != null ||
        gradle.startParameter.taskNames.any { taskName ->
            taskName.contains("generateArchitectureLayout") ||
                taskName.contains("generateDependencyGraph")
        }

if (isKonturePluginRequested) {
    pluginManager.apply("io.github.baole.konture")
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}


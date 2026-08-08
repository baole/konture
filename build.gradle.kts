/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

val applyPlugin = System.getProperty("idea.active") == "true" ||
    System.getProperty("idea.sync.active") == "true" ||
    System.getProperty("konture.applyPlugin") == "true" ||
    System.getenv("KONTURE_APPLY_PLUGIN") == "true"

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        val applyPlugin = System.getProperty("idea.active") == "true" ||
            System.getProperty("idea.sync.active") == "true" ||
            System.getProperty("konture.applyPlugin") == "true" ||
            System.getenv("KONTURE_APPLY_PLUGIN") == "true"

        if (applyPlugin) {
            val versionFile = file("gradle/libs.versions.toml")
            if (versionFile.exists()) {
                val kontureVersion = versionFile.readLines()
                    .firstOrNull { it.trim().startsWith("konture =") }
                    ?.substringAfter("=")
                    ?.replace("\"", "")
                    ?.trim()

                if (kontureVersion != null) {
                    classpath("io.github.baole.konture:plugin-gradle:$kontureVersion")
                }
            }
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

if (applyPlugin) {
    pluginManager.apply("io.github.baole.konture")
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

val copyLayoutToTest by tasks.registering(Copy::class) {
    if (applyPlugin) {
        dependsOn("generateArchitectureLayout")
    }
    from(layout.buildDirectory.dir("konture"))
    into(file("konture-test/build/konture"))
}

tasks.register<GradleBuild>("runKontureTest") {
    group = "Verification"
    description = "Runs tests in the independent konture-test module."
    dir = file("konture-test")
    tasks = listOf("test")
    dependsOn(
        ":core:publishToMavenLocal",
        ":library:publishToMavenLocal",
        ":plugin-gradle:publishToMavenLocal",
        copyLayoutToTest
    )
}

tasks.named("check") {
    if (applyPlugin) {
        dependsOn("runKontureTest")
    }
}

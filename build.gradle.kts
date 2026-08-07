/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.konture)
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

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

val copyLayoutToTest by tasks.registering(Copy::class) {
    dependsOn("generateArchitectureLayout")
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
    dependsOn("runKontureTest")
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("konture.kotlin")
    id("konture.quality")
    id("konture.publishing")
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.baole"

description = "Shared data models and JSON structures for Konture"

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "konture-core"
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

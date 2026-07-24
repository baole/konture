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
    alias(libs.plugins.dokka)
}

group = "io.github.baole"

description = "Konture primary public API library"

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "konture"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

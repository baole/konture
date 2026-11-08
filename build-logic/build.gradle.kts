/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    maven {
        name = "MavenCentral"
        url = uri("https://repo.maven.apache.org/maven2")
        allowInsecureContinueWhenDisabled.set(true)
    }
    maven {
        name = "MavenCentralKotlinFallback"
        url = uri("https://repo1.maven.org/maven2")
        content {
            includeGroup("org.jetbrains.kotlin")
        }
    }
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.detekt.plugin)
    implementation(libs.ktlint.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.nmcp.plugin)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}


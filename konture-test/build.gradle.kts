/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.konture)
}



dependencies {
    // Reference published Maven coordinates from mavenLocal()
    testImplementation("io.github.baole:konture:${libs.versions.konture.get()}")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

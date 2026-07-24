/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("konture.kotlin")
}

if (System.getProperty("archTest") != null) {
    pluginManager.apply("io.github.baole.konture")
}

dependencies {
    // Local project dependency ensures compiles always succeed without requiring publishToMavenLocal first
    testImplementation(project(":library"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    onlyIf {
        System.getProperty("archTest") != null
    }
}

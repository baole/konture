/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

val applyPlugin = gradle.extensions.get("applyPlugin") as Boolean ||
    System.getProperty("konture.applyPluginInternal").toBoolean()

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
    pluginManager.apply("io.github.baole.konture.internal")
}

tasks.register("runKontureTest") {
    group = "Verification"
    description = "Runs tests in the independent konture-test module."
    dependsOn(":konture-test:test")
}

tasks.named("check") {
    if (applyPlugin) {
        dependsOn("runKontureTest")
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

val applyPlugin = providers.systemProperty("idea.active").orNull.toBoolean() ||
    providers.systemProperty("idea.sync.active").orNull.toBoolean() ||
    providers.systemProperty("konture.applyPlugin").orNull.toBoolean() ||
    providers.systemProperty("konture.applyPluginInternal").orNull.toBoolean()

plugins {
    id("konture.root")
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.nmcp.aggregation)
    `maven-publish`
}

nmcpAggregation {
    centralPortal {
        username.set(
            providers.gradleProperty("mavenCentralUsername")
                .orElse(providers.environmentVariable("MAVEN_CENTRAL_USERNAME")),
        )
        password.set(
            providers.gradleProperty("mavenCentralPassword")
                .orElse(providers.environmentVariable("MAVEN_CENTRAL_PASSWORD")),
        )
        publishingType.set("AUTOMATIC")
    }
}

dependencies {
    nmcpAggregation(project(":core"))
    nmcpAggregation(project(":library"))
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

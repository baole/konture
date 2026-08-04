/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `java-gradle-plugin`
    id("konture.kotlin")
    id("konture.quality")
    id("konture.publishing")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.publish)
}
private val descriptionText =
    """
    Gradle configuration-time capture agent that feeds
    Konture your project's real build graph, enabling fast,
    build-tool-aware Kotlin architecture tests across Android,
    KMP, and JVM projects.
    """.trimIndent()
private val groupId = "io.github.baole.konture"

description = descriptionText
group = groupId
version = "0.7.5"

gradlePlugin {
    website.set("https://baole.github.io/konture")
    vcsUrl.set("https://github.com/baole/konture.git")

    plugins {
        create("konture") {
            id = groupId
            implementationClass = "io.github.baole.konture.plugin.KonturePlugin"
            displayName = "Kotlin Architecture Testing Tool Plugin"
            description = descriptionText
            tags.set(listOf("kotlin", "architecture", "testing", "archunit", "konture"))
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin.api)
    implementation(project(":core"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlin.gradle.plugin)
    testImplementation(gradleTestKit())
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

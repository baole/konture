/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Direct, standard subproject dependency ensures compiles and execution always succeed natively
    testImplementation(project(":library"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val isKonturePluginApplied = System.getProperty("idea.active") == "true" ||
    System.getProperty("idea.sync.active") == "true" ||
    System.getProperty("konture.applyPlugin") == "true" ||
    System.getProperty("konture.applyPluginInternal") == "true" ||
    System.getenv("KONTURE_APPLY_PLUGIN") == "true" ||
    rootProject.pluginManager.hasPlugin("io.github.baole.konture.internal")

tasks.withType<Test> {
    useJUnitPlatform()
    enabled = isKonturePluginApplied
}

tasks.processTestResources {
    val parentLayout = file("../build/konture/layout_v2.json")
    val parentDeps = file("../build/konture/dependencies.json")

    if (isKonturePluginApplied) {
        dependsOn(":generateArchitectureLayout")
        if (rootProject.tasks.findByName("generateDependencyGraph") != null) {
            dependsOn(":generateDependencyGraph")
        }

        from(parentLayout) {
            into("konture")
        }
        from(files(parentDeps)) {
            into("konture")
        }
    }
}

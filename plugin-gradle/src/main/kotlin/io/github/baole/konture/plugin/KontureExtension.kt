/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Gradle DSL extension for configuring Konture on consumer modules.
 *
 * Typically applied inside dedicated architecture test subprojects (e.g. `:konture-test`).
 */
open class KontureExtension(
    private val project: Project,
) {
    val excludeModules: ListProperty<String> = project.objects.listProperty(String::class.java).convention(emptyList())
    val excludePackages: ListProperty<String> = project.objects.listProperty(String::class.java).convention(emptyList())
    val excludeClasses: ListProperty<String> = project.objects.listProperty(String::class.java).convention(emptyList())
    val excludeConfigurations: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(listOf("test", "benchmark", "profile", "testedapks"))
    val logLevel: Property<String> = project.objects.property(String::class.java).convention("INFO")

    fun logLevel(level: String) {
        logLevel.set(level)
    }

    fun excludeModules(vararg modules: String) {
        excludeModules.addAll(*modules)
    }

    fun excludePackages(vararg packages: String) {
        excludePackages.addAll(*packages)
    }

    fun excludeClasses(vararg classes: String) {
        excludeClasses.addAll(*classes)
    }

    fun excludeConfigurations(vararg configurations: String) {
        excludeConfigurations.addAll(*configurations)
    }
}

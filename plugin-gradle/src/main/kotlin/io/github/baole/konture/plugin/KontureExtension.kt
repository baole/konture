/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
 *
 * @property excludeModules List of Gradle module paths to exclude from parsing.
 * @property excludePackages List of package patterns to exclude from architecture rules.
 * @property excludeClasses List of class patterns to exclude from architecture rules.
 * @property excludeConfigurations List of Gradle dependency configuration names to ignore.
 * @property logLevel Target log level verbosity for Konture rule processing.
 * @property baselinePath Relative file path for storing baseline violations.
 * @property language Target language code / locale for Konture error messages.
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
    val baselinePath: Property<String> = project.objects.property(String::class.java).convention("konture-baseline.json")
    val language: Property<String> = project.objects.property(String::class.java).convention("en")

    /** Sets the Konture log verbosity level (e.g., "DEBUG", "INFO", "WARN"). */
    fun logLevel(level: String) {
        logLevel.set(level)
    }

    /** Sets the baseline violation file path relative to the project directory. */
    fun baselinePath(path: String) {
        baselinePath.set(path)
    }

    /** Sets the target language code for violation messages (e.g., "en", "es"). */
    fun language(lang: String) {
        language.set(lang)
    }

    /** Appends module paths to exclude from parsing. */
    fun excludeModules(vararg modules: String) {
        excludeModules.addAll(*modules)
    }

    /** Appends package patterns to exclude from architecture checking. */
    fun excludePackages(vararg packages: String) {
        excludePackages.addAll(*packages)
    }

    /** Appends class patterns to exclude from architecture checking. */
    fun excludeClasses(vararg classes: String) {
        excludeClasses.addAll(*classes)
    }

    /** Appends dependency configuration names to ignore during analysis. */
    fun excludeConfigurations(vararg configurations: String) {
        excludeConfigurations.addAll(*configurations)
    }
}

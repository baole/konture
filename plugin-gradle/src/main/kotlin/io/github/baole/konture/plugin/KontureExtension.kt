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
 */
public open class KontureExtension(
    private val project: Project,
) {
    public val excludeModules: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())
    public val excludePackages: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())
    public val excludeClasses: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())
    public val excludeConfigurations: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(listOf("test", "benchmark", "profile", "testedapks"))
    public val logLevel: Property<String> = project.objects.property(String::class.java).convention("INFO")
    public val baselinePath: Property<String> =
        project.objects.property(
            String::class.java,
        ).convention("konture-baseline.json")
    public val language: Property<String> = project.objects.property(String::class.java).convention("en")

    public fun logLevel(level: String) {
        logLevel.set(level)
    }

    public fun baselinePath(path: String) {
        baselinePath.set(path)
    }

    public fun language(lang: String) {
        language.set(lang)
    }

    public fun excludeModules(vararg modules: String) {
        excludeModules.addAll(*modules)
    }

    public fun excludePackages(vararg packages: String) {
        excludePackages.addAll(*packages)
    }

    public fun excludeClasses(vararg classes: String) {
        excludeClasses.addAll(*classes)
    }

    public fun excludeConfigurations(vararg configurations: String) {
        excludeConfigurations.addAll(*configurations)
    }
}

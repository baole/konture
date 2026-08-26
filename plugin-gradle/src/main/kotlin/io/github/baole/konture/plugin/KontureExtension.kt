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
    /** List of module paths to exclude from architecture rules. */
    public val excludeModules: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())

    /** List of package names to exclude from architecture rules. */
    public val excludePackages: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())

    /** List of class names to exclude from architecture rules. */
    public val excludeClasses: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(emptyList())

    /** List of build configuration names to exclude from architecture rules. */
    public val excludeConfigurations: ListProperty<String> =
        project.objects
            .listProperty(String::class.java)
            .convention(listOf("test*", "benchmark*", "testedapks"))

    /** Diagnostic log level string. */
    public val logLevel: Property<String> = project.objects.property(String::class.java).convention("INFO")

    /** Relative path to the baseline JSON file. */
    public val baselinePath: Property<String> =
        project.objects.property(
            String::class.java,
        ).convention("konture-baseline.json")

    /** Locale string for error and assertion message internationalization. */
    public val language: Property<String> = project.objects.property(String::class.java).convention("en")

    /** Sets the diagnostic [level]. */
    public fun logLevel(level: String) {
        logLevel.set(level)
    }

    /** Sets the baseline relative [path]. */
    public fun baselinePath(path: String) {
        baselinePath.set(path)
    }

    /** Sets the message locale [lang]. */
    public fun language(lang: String) {
        language.set(lang)
    }

    /** Adds [modules] to the excluded modules list. */
    public fun excludeModules(vararg modules: String) {
        excludeModules.addAll(*modules)
    }

    /** Adds [packages] to the excluded packages list. */
    public fun excludePackages(vararg packages: String) {
        excludePackages.addAll(*packages)
    }

    /** Adds [classes] to the excluded classes list. */
    public fun excludeClasses(vararg classes: String) {
        excludeClasses.addAll(*classes)
    }

    /** Adds [configurations] to the excluded build configurations list. */
    public fun excludeConfigurations(vararg configurations: String) {
        excludeConfigurations.addAll(*configurations)
    }
}

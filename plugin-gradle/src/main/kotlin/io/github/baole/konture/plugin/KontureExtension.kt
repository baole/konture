/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
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

    /** Whether to log informational messages when resolved violations exist in baseline. */
    public val reportResolvedViolations: Property<Boolean> =
        project.objects.property(
            Boolean::class.javaObjectType,
        ).convention(KontureConstants.DEFAULT_REPORT_RESOLVED_VIOLATIONS)

    /** Whether to fail test execution if resolved violations exist in baseline (ratchet mode). */
    public val failOnResolvedViolations: Property<Boolean> =
        project.objects.property(Boolean::class.javaObjectType).convention(false)

    /** Sets the diagnostic [level]. */
    public fun logLevel(level: String) {
        logLevel.set(level)
    }

    /**
     * Analysis performance and caching configuration.
     *
     * ```kotlin
     * konture {
     *     analysis {
     *         incremental = true
     *         cache = true
     *     }
     * }
     * ```
     */
    public val analysis: AnalysisConfig = AnalysisConfig(project)

    /** Sets the baseline relative [path]. */
    public fun baselinePath(path: String) {
        baselinePath.set(path)
    }

    /** Sets the message locale [lang]. */
    public fun language(lang: String) {
        language.set(lang)
    }

    /** Configures whether to log informational messages when resolved violations exist in baseline. */
    public fun reportResolvedViolations(enabled: Boolean) {
        reportResolvedViolations.set(enabled)
    }

    /** Configures whether to fail test execution if resolved violations exist in baseline (ratchet mode). */
    public fun failOnResolvedViolations(enabled: Boolean) {
        failOnResolvedViolations.set(enabled)
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

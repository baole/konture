/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import java.util.Locale

/**
 * Main entry point for Konture. All architecture assertion builders, scoping builders,
 * and graph configurations are extended from or accessible through this object.
 */
public object Konture {
    /**
     * System property key used to override the minimum severity failure threshold.
     */
    public const val PROPERTY_FAIL_ON_SEVERITY: String = KontureConstants.PROPERTY_FAIL_ON_SEVERITY

    /**
     * System property key used to override the path of baseline files.
     */
    public const val PROPERTY_BASELINE_PATH: String = KontureConstants.PROPERTY_BASELINE_PATH

    /**
     * System property key used to override the target translation language / locale.
     */
    public const val PROPERTY_LOCALE: String = KontureConstants.PROPERTY_LOCALE

    /**
     * System property key used to enable/disable baseline generation mode.
     */
    public const val PROPERTY_BASELINE_GENERATE: String = KontureConstants.PROPERTY_BASELINE_GENERATE

    /**
     * System property key used to override the target output directory for baseline files.
     */
    public const val PROPERTY_BASELINE_DIR: String = KontureConstants.PROPERTY_BASELINE_DIR

    /**
     * System property key used to override the output format (e.g. human, problem_matcher).
     */
    public const val PROPERTY_OUTPUT_FORMAT: String = KontureConstants.PROPERTY_OUTPUT_FORMAT

    /**
     * Default baseline filename fallback when no custom path is configured.
     */
    public const val DEFAULT_BASELINE_FILENAME: String = KontureConstants.DEFAULT_BASELINE_FILENAME

    /**
     * System property key used to enable/disable incremental AST analysis and caching.
     */
    public const val PROPERTY_INCREMENTAL_ENABLED: String = KontureConstants.PROPERTY_INCREMENTAL_ENABLED

    /**
     * Default value for incremental AST analysis.
     */
    public const val DEFAULT_INCREMENTAL_ENABLED: Boolean = KontureConstants.DEFAULT_INCREMENTAL_ENABLED

    /**
     * Whether incremental AST analysis and source hashing are enabled.
     * When enabled, unchanged Kotlin files are resolved from the AST cache without re-parsing.
     * Can be configured via system property "konture.incremental.enabled" or programmatically.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var incremental: Boolean
        get() {
            if (KontureRuntimeStateProvider.currentState.isIncrementalOverridden) {
                return KontureRuntimeStateProvider.currentState.incremental
            }
            val systemProp = System.getProperty(PROPERTY_INCREMENTAL_ENABLED)
            return systemProp?.toBoolean() ?: KontureRuntimeStateProvider.currentState.incremental
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(
                    incremental = value,
                    isIncrementalOverridden = true,
                )
        }

    /**
     * The output format used when formatting architecture violation reports upon failure.
     * Can be configured via system property "konture.output.format" or programmatically.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var outputFormat: OutputFormat
        get() {
            val systemProp = System.getProperty(PROPERTY_OUTPUT_FORMAT)
            return if (systemProp != null) {
                try {
                    OutputFormat.valueOf(systemProp.uppercase(Locale.ROOT))
                } catch (_: IllegalArgumentException) {
                    KontureRuntimeStateProvider.currentState.outputFormat
                }
            } else {
                KontureRuntimeStateProvider.currentState.outputFormat
            }
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(outputFormat = value)
        }

    /**
     * The minimum severity threshold required for rule violations to trigger test failure.
     * When null, Konture runs in audit / dry-run mode (no violations cause test failure).
     * Can be configured via system property "konture.fail.on.severity" or programmatically.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var failOnSeverity: Severity?
        get() {
            if (KontureRuntimeStateProvider.currentState.isFailOnSeverityOverridden) {
                return KontureRuntimeStateProvider.currentState.failOnSeverity
            }
            val systemProp = System.getProperty(PROPERTY_FAIL_ON_SEVERITY)
            return if (systemProp != null) {
                when (systemProp.trim().lowercase(Locale.ROOT)) {
                    "none" -> null
                    "info" -> Severity.INFO
                    "warning" -> Severity.WARNING
                    "error" -> Severity.ERROR
                    else -> KontureRuntimeStateProvider.currentState.failOnSeverity
                }
            } else {
                KontureRuntimeStateProvider.currentState.failOnSeverity
            }
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(
                    failOnSeverity = value,
                    isFailOnSeverityOverridden = true,
                )
        }

    /**
     * The target translation locale for architectural guardrail messages.
     * Can be configured via system property "konture.locale" or programmatically.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var locale: Locale
        get() {
            if (KontureRuntimeStateProvider.currentState.isLocaleOverridden) {
                return KontureRuntimeStateProvider.currentState.locale
            }
            /** Filter or assertion criteria for system prop. */
            val systemProp = System.getProperty(PROPERTY_LOCALE)
            return if (systemProp != null) {
                Locale.forLanguageTag(systemProp)
            } else {
                KontureRuntimeStateProvider.currentState.locale
            }
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(
                    locale = value,
                    isLocaleOverridden = true,
                )
        }

    /**
     * Lazily and thread-safely loads the [ProjectGraph] from the default resource path on first use,
     * or retrieves the default graph if already initialized.
     */
    public val projectGraph: ProjectGraph
        get() {
            /** Filter or assertion criteria for state. */
            val state = KontureRuntimeStateProvider.currentState
            return state.projectGraph ?: run {
                /** Filter or assertion criteria for loaded. */
                val loaded = state.projectGraphLoader.loadFromResource()
                ProjectGraph.setDefault(loaded)
                loaded
            }
        }

    /**
     * The file path of the baseline file relative to the baseline directory.
     * Default value is obtained from system property "konture.baseline.path" or falls back to "konture-baseline.json".
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var baselinePath: String
        get() = System.getProperty(PROPERTY_BASELINE_PATH) ?: KontureRuntimeStateProvider.currentState.baselinePath
        set(value) {
            KontureRuntimeStateProvider.currentState = KontureRuntimeStateProvider.currentState.copy(baselinePath = value)
        }

    /**
     * System property key used to override the path of generated HTML/JSON/SARIF report files.
     */
    public const val PROPERTY_REPORT_PATH: String = KontureConstants.PROPERTY_REPORT_PATH

    /**
     * System property key used to override the path of generated JSON report files.
     */
    public const val PROPERTY_REPORT_JSON_PATH: String = KontureConstants.PROPERTY_REPORT_JSON_PATH

    /**
     * System property key used to override the path of generated SARIF report files.
     */
    public const val PROPERTY_REPORT_SARIF_PATH: String = KontureConstants.PROPERTY_REPORT_SARIF_PATH

    /**
     * System property key used to override the path of generated HTML report files.
     */
    public const val PROPERTY_REPORT_HTML_PATH: String = KontureConstants.PROPERTY_REPORT_HTML_PATH

    /**
     * Default JSON report path.
     */
    public const val DEFAULT_JSON_REPORT_PATH: String = KontureConstants.DEFAULT_JSON_REPORT_PATH

    /**
     * Default SARIF report path.
     */
    public const val DEFAULT_SARIF_REPORT_PATH: String = KontureConstants.DEFAULT_SARIF_REPORT_PATH

    /**
     * Default HTML report path.
     */
    public const val DEFAULT_HTML_REPORT_PATH: String = KontureConstants.DEFAULT_HTML_REPORT_PATH

    /**
     * Target report output path relative to project build directory.
     * Default value is obtained from system property "konture.report.path" or falls back to "build/reports/konture/konture-report.html".
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var reportPath: String
        get() = System.getProperty(PROPERTY_REPORT_PATH) ?: KontureRuntimeStateProvider.currentState.reportPath
        set(value) {
            val state = KontureRuntimeStateProvider.currentState
            KontureRuntimeStateProvider.currentState =
                state.copy(
                    reportPath = value,
                    htmlReportPath = if (value.endsWith(".html")) value else state.htmlReportPath,
                    jsonReportPath = if (value.endsWith(".json")) value else state.jsonReportPath,
                    sarifReportPath = if (value.endsWith(".sarif")) value else state.sarifReportPath,
                )
        }

    /**
     * Target JSON report output path relative to project build directory.
     * Default value is obtained from system property "konture.report.json.path" or falls back to "build/reports/konture/konture-report.json".
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var jsonReportPath: String
        get() =
            System.getProperty(PROPERTY_REPORT_JSON_PATH)
                ?: System.getProperty(PROPERTY_REPORT_PATH)?.takeIf { it.endsWith(".json") }
                ?: if (KontureRuntimeStateProvider.currentState.jsonReportPath != DEFAULT_JSON_REPORT_PATH) {
                    KontureRuntimeStateProvider.currentState.jsonReportPath
                } else if (KontureRuntimeStateProvider.currentState.reportPath.endsWith(".json")) {
                    KontureRuntimeStateProvider.currentState.reportPath
                } else {
                    KontureRuntimeStateProvider.currentState.jsonReportPath
                }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(jsonReportPath = value)
        }

    /**
     * Target SARIF report output path relative to project build directory.
     * Default value is obtained from system property "konture.report.sarif.path" or falls back to "build/reports/konture/konture-report.sarif".
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var sarifReportPath: String
        get() =
            System.getProperty(PROPERTY_REPORT_SARIF_PATH)
                ?: System.getProperty(PROPERTY_REPORT_PATH)?.takeIf { it.endsWith(".sarif") }
                ?: if (KontureRuntimeStateProvider.currentState.sarifReportPath != DEFAULT_SARIF_REPORT_PATH) {
                    KontureRuntimeStateProvider.currentState.sarifReportPath
                } else if (KontureRuntimeStateProvider.currentState.reportPath.endsWith(".sarif")) {
                    KontureRuntimeStateProvider.currentState.reportPath
                } else {
                    KontureRuntimeStateProvider.currentState.sarifReportPath
                }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(sarifReportPath = value)
        }

    /**
     * Target HTML report output path relative to project build directory.
     * Default value is obtained from system property "konture.report.html.path" or falls back to "build/reports/konture/konture-report.html".
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var htmlReportPath: String
        get() =
            System.getProperty(PROPERTY_REPORT_HTML_PATH)
                ?: System.getProperty(PROPERTY_REPORT_PATH)?.takeIf { it.endsWith(".html") }
                ?: if (KontureRuntimeStateProvider.currentState.htmlReportPath != DEFAULT_HTML_REPORT_PATH) {
                    KontureRuntimeStateProvider.currentState.htmlReportPath
                } else if (KontureRuntimeStateProvider.currentState.reportPath.endsWith(".html")) {
                    KontureRuntimeStateProvider.currentState.reportPath
                } else {
                    KontureRuntimeStateProvider.currentState.htmlReportPath
                }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(htmlReportPath = value)
        }

    /**
     * Flag indicating whether to generate violations into the baseline file rather than throwing [AssertionError].
     * Default value is obtained from system property "konture.baseline.generate" (as boolean) or falls back to false.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var generateBaseline: Boolean
        get() = System.getProperty(PROPERTY_BASELINE_GENERATE)?.toBoolean() ?: KontureRuntimeStateProvider.currentState.generateBaseline
        set(value) {
            KontureRuntimeStateProvider.currentState = KontureRuntimeStateProvider.currentState.copy(generateBaseline = value)
        }

    /**
     * System property key used to enable reporting of resolved baseline violations.
     */
    public const val PROPERTY_REPORT_RESOLVED_VIOLATIONS: String = KontureConstants.PROPERTY_REPORT_RESOLVED_VIOLATIONS

    /**
     * System property key used to fail the build when resolved baseline violations are detected.
     */
    public const val PROPERTY_FAIL_ON_RESOLVED_VIOLATIONS: String = KontureConstants.PROPERTY_FAIL_ON_RESOLVED_VIOLATIONS

    /**
     * Default value for reporting resolved baseline violations.
     */
    public const val DEFAULT_REPORT_RESOLVED_VIOLATIONS: Boolean = KontureConstants.DEFAULT_REPORT_RESOLVED_VIOLATIONS

    /**
     * Default value for failing on resolved baseline violations.
     */
    public const val DEFAULT_FAIL_ON_RESOLVED_VIOLATIONS: Boolean = KontureConstants.DEFAULT_FAIL_ON_RESOLVED_VIOLATIONS

    /**
     * Whether to log and report baseline violations that have been resolved (fixed in code).
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var reportResolvedViolations: Boolean
        get() {
            if (KontureRuntimeStateProvider.currentState.isReportResolvedViolationsOverridden) {
                return KontureRuntimeStateProvider.currentState.reportResolvedViolations
            }
            val prop =
                System.getProperty(PROPERTY_REPORT_RESOLVED_VIOLATIONS)
                    ?: System.getProperty("konture.baseline.report.resolved")
            return prop?.toBoolean() ?: KontureRuntimeStateProvider.currentState.reportResolvedViolations
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(
                    reportResolvedViolations = value,
                    isReportResolvedViolationsOverridden = true,
                )
        }

    /**
     * Whether to fail the test execution if resolved violations exist in the baseline (ratchet mode).
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var failOnResolvedViolations: Boolean
        get() {
            if (KontureRuntimeStateProvider.currentState.isFailOnResolvedViolationsOverridden) {
                return KontureRuntimeStateProvider.currentState.failOnResolvedViolations
            }
            val prop =
                System.getProperty(PROPERTY_FAIL_ON_RESOLVED_VIOLATIONS)
                    ?: System.getProperty("konture.baseline.fail.on.resolved")
            return prop?.toBoolean() ?: KontureRuntimeStateProvider.currentState.failOnResolvedViolations
        }
        set(value) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(
                    failOnResolvedViolations = value,
                    isFailOnResolvedViolationsOverridden = true,
                )
        }

    /**
     * Checks ratchet status and throws an [AssertionError] if resolved violations exist
     * in the baseline and [failOnResolvedViolations] is enabled.
     */
    public fun checkRatchet() {
        BaselineManager.checkRatchet()
    }

    /**
     * Resets the current thread-local runtime state to default values and clears the shared report accumulator.
     * Note: This method is intended for test harness setup and teardown.
     */
    public fun reset() {
        KontureRuntimeStateProvider.reset()
    }
}

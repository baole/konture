/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

/**
 * Shared constant definitions for the Konture architecture testing framework.
 */
public object KontureConstants {
    /**
     * System property key used to override the path of baseline files.
     */
    public const val PROPERTY_BASELINE_PATH: String = "konture.baseline.path"

    /**
     * System property key used to override the target translation language / locale.
     */
    public const val PROPERTY_LOCALE: String = "konture.locale"

    /**
     * System property key used to enable/disable baseline generation mode.
     */
    public const val PROPERTY_BASELINE_GENERATE: String = "konture.baseline.generate"

    /**
     * System property key used to override the target output directory for baseline files.
     */
    public const val PROPERTY_BASELINE_DIR: String = "konture.baseline.dir"

    /**
     * System property key used to override the output format (e.g. human, problem_matcher).
     */
    public const val PROPERTY_OUTPUT_FORMAT: String = "konture.output.format"

    /**
     * System property key used to override the path of generated HTML/JSON/SARIF report files.
     */
    public const val PROPERTY_REPORT_PATH: String = "konture.report.path"

    /**
     * Default output report file path relative to project build directory.
     */
    @Deprecated("Use DEFAULT_HTML_REPORT_PATH instead.", ReplaceWith("DEFAULT_HTML_REPORT_PATH"))
    public const val DEFAULT_REPORT_PATH: String = "build/reports/konture/konture-report.html"

    /**
     * Current release version of Konture.
     */
    public const val VERSION: String = "0.8.1"

    /**
     * System property key used to override the path of generated HTML report files.
     */
    public const val PROPERTY_REPORT_HTML_PATH: String = "konture.report.html.path"

    /**
     * System property key used to override the path of generated JSON report files.
     */
    public const val PROPERTY_REPORT_JSON_PATH: String = "konture.report.json.path"

    /**
     * System property key used to override the path of generated SARIF report files.
     */
    public const val PROPERTY_REPORT_SARIF_PATH: String = "konture.report.sarif.path"

    /**
     * Default JSON report output file path.
     */
    public const val DEFAULT_JSON_REPORT_PATH: String = "build/reports/konture/konture-report.json"

    /**
     * Default SARIF report output file path.
     */
    public const val DEFAULT_SARIF_REPORT_PATH: String = "build/reports/konture/konture-report.sarif"

    /**
     * Default HTML report output file path.
     */
    public const val DEFAULT_HTML_REPORT_PATH: String = "build/reports/konture/konture-report.html"

    /**
     * Default baseline filename fallback when no custom path is configured.
     */
    public const val DEFAULT_BASELINE_FILENAME: String = "konture-baseline.json"

    /**
     * System property key used to override the minimum severity failure threshold.
     */
    public const val PROPERTY_FAIL_ON_SEVERITY: String = "konture.fail.on.severity"

    /**
     * System property key used to enable reporting of resolved baseline violations.
     */
    public const val PROPERTY_REPORT_RESOLVED_VIOLATIONS: String = "konture.reportResolvedViolations"

    /**
     * System property key used to fail the build when resolved baseline violations are detected.
     */
    public const val PROPERTY_FAIL_ON_RESOLVED_VIOLATIONS: String = "konture.failOnResolvedViolations"

    /**
     * Default value for reporting resolved baseline violations.
     */
    public const val DEFAULT_REPORT_RESOLVED_VIOLATIONS: Boolean = true

    /**
     * Default value for failing on resolved baseline violations.
     */
    public const val DEFAULT_FAIL_ON_RESOLVED_VIOLATIONS: Boolean = false

    /**
     * System property key used to enable/disable incremental AST analysis and caching.
     */
    public const val PROPERTY_INCREMENTAL_ENABLED: String = "konture.incremental.enabled"

    /**
     * Default value for incremental AST analysis.
     */
    public const val DEFAULT_INCREMENTAL_ENABLED: Boolean = true

    /**
     * System property key used to enable/disable persistent disk caching of analysis results.
     */
    public const val PROPERTY_CACHE_ENABLED: String = "konture.cache.enabled"

    /**
     * System property key used to override the persistent cache directory.
     */
    public const val PROPERTY_CACHE_DIR: String = "konture.cache.dir"

    /**
     * System property key used to override the persistent cache fingerprint.
     * The fingerprint namespaces the disk cache so rule or configuration changes
     * automatically invalidate previously stored entries.
     */
    public const val PROPERTY_CACHE_FINGERPRINT: String = "konture.cache.fingerprint"

    /**
     * Default value for persistent disk caching.
     */
    public const val DEFAULT_CACHE_ENABLED: Boolean = false

    /**
     * Default persistent cache directory (resolved against the current working directory).
     */
    public const val DEFAULT_CACHE_DIR: String = ".konture/cache"
}

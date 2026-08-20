/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
    public const val DEFAULT_REPORT_PATH: String = "build/reports/konture-report.html"

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
}

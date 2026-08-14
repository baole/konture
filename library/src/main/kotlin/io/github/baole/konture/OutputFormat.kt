/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Output format modes for formatting architecture violation reports.
 */
public enum class OutputFormat {
    /** Standard human-readable multi-line report format suitable for assertion outputs. */
    HUMAN,

    /** Compact single-line format matching standard compiler/IDE problem matchers
     * (path:line:col: Konture [ruleId]: message). */
    PROBLEM_MATCHER,

    /** Semantic HTML format suitable for HTML test runners and reporting tools. */
    HTML,

    /** Structured JSON format for programmatic ingestion. */
    JSON,

    /** Static Analysis Results Interchange Format (SARIF) for security and CI scanners. */
    SARIF,
}

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

/**
 * Shared constant definitions for the Konture architecture testing framework.
 */
object KontureConstants {
    /**
     * System property key used to override the path of baseline files.
     */
    const val PROPERTY_BASELINE_PATH = "konture.baseline.path"

    /**
     * System property key used to enable/disable baseline generation mode.
     */
    const val PROPERTY_BASELINE_GENERATE = "konture.baseline.generate"

    /**
     * System property key used to override the target output directory for baseline files.
     */
    const val PROPERTY_BASELINE_DIR = "konture.baseline.dir"

    /**
     * Default baseline filename fallback when no custom path is configured.
     */
    const val DEFAULT_BASELINE_FILENAME = "konture-baseline.json"
}

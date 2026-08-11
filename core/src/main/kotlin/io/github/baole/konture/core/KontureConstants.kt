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
     * Default baseline filename fallback when no custom path is configured.
     */
    public const val DEFAULT_BASELINE_FILENAME: String = "konture-baseline.json"
}

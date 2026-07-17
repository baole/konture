/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.impl.ProjectGraphLoader

/**
 * Main entry point for Konture. All architecture assertion builders, scoping builders,
 * and graph configurations are extended from or accessible through this object.
 */
object Konture {
    /**
     * System property key used to override the path of baseline files.
     */
    const val PROPERTY_BASELINE_PATH = KontureConstants.PROPERTY_BASELINE_PATH

    /**
     * System property key used to enable/disable baseline generation mode.
     */
    const val PROPERTY_BASELINE_GENERATE = KontureConstants.PROPERTY_BASELINE_GENERATE

    /**
     * System property key used to override the target output directory for baseline files.
     */
    const val PROPERTY_BASELINE_DIR = KontureConstants.PROPERTY_BASELINE_DIR

    /**
     * Default baseline filename fallback when no custom path is configured.
     */
    const val DEFAULT_BASELINE_FILENAME = KontureConstants.DEFAULT_BASELINE_FILENAME

    /**
     * Lazily and thread-safely loads the [ProjectGraph] from the default resource path on first use,
     * or retrieves the default graph if already initialized.
     */
    val projectGraph: ProjectGraph
        get() =
            if (ProjectGraph.isDefaultInitialized()) {
                ProjectGraph.getDefault()
            } else {
                lazyGraph
            }

    private val lazyGraph: ProjectGraph by lazy {
        ProjectGraphLoader.loadFromResource()
    }

    private var _baselinePath: String? = null

    /**
     * The file path of the baseline file relative to the baseline directory.
     * Default value is obtained from system property "konture.baseline.path" or falls back to "konture-baseline.json".
     */
    var baselinePath: String
        get() = System.getProperty(PROPERTY_BASELINE_PATH) ?: _baselinePath ?: DEFAULT_BASELINE_FILENAME
        set(value) {
            _baselinePath = value
        }

    private var _generateBaseline: Boolean? = null

    /**
     * Flag indicating whether to generate violations into the baseline file rather than throwing [AssertionError].
     * Default value is obtained from system property "konture.baseline.generate" (as boolean) or falls back to false.
     */
    var generateBaseline: Boolean
        get() = System.getProperty(PROPERTY_BASELINE_GENERATE)?.toBoolean() ?: _generateBaseline ?: false
        set(value) {
            _generateBaseline = value
        }
}

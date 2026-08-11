/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import java.util.Locale

/**
 * Main entry point for Konture. All architecture assertion builders, scoping builders,
 * and graph configurations are extended from or accessible through this object.
 */
public object Konture {
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
     * Default baseline filename fallback when no custom path is configured.
     */
    public const val DEFAULT_BASELINE_FILENAME: String = KontureConstants.DEFAULT_BASELINE_FILENAME

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
     * Flag indicating whether to generate violations into the baseline file rather than throwing [AssertionError].
     * Default value is obtained from system property "konture.baseline.generate" (as boolean) or falls back to false.
     * Backed by ThreadLocal state; safe under parallel test execution.
     */
    public var generateBaseline: Boolean
        get() = System.getProperty(PROPERTY_BASELINE_GENERATE)?.toBoolean() ?: KontureRuntimeStateProvider.currentState.generateBaseline
        set(value) {
            KontureRuntimeStateProvider.currentState = KontureRuntimeStateProvider.currentState.copy(generateBaseline = value)
        }
}

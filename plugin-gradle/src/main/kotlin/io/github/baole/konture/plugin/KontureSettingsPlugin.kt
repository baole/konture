/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Settings companion plugin for Konture.
 *
 * Registers the `gradle.lifecycle.beforeProject` callback on the consumer's behalf
 * to apply [KonturePlugin] to every project in an Isolated Projects compatible manner.
 */
class KontureSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        try {
            settings.gradle.lifecycle.beforeProject { project ->
                project.pluginManager.apply("io.github.baole.konture.internal")
            }
        } catch (_: NoSuchMethodError) {
            @Suppress("DEPRECATION")
            settings.gradle.beforeProject { project ->
                project.pluginManager.apply("io.github.baole.konture.internal")
            }
        }
    }
}

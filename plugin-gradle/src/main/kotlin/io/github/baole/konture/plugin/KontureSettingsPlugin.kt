/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Settings companion plugin for Konture.
 *
 * Registers the `gradle.lifecycle.beforeProject` callback on the consumer's behalf
 * to apply [KonturePlugin] to every project in an Isolated Projects compatible manner.
 */
public class KontureSettingsPlugin : Plugin<Any> {
    override fun apply(target: Any) {
        when (target) {
            is Settings -> {
                try {
                    target.gradle.lifecycle.beforeProject { project ->
                        project.pluginManager.apply("io.github.baole.konture.internal")
                    }
                } catch (_: NoSuchMethodError) {
                    @Suppress("DEPRECATION")
                    target.gradle.beforeProject { project ->
                        project.pluginManager.apply("io.github.baole.konture.internal")
                    }
                }
            }
            is Project -> {
                target.pluginManager.apply("io.github.baole.konture.internal")
            }
            else -> throw IllegalArgumentException("Konture plugin can only be applied to Settings or Project")
        }
    }
}

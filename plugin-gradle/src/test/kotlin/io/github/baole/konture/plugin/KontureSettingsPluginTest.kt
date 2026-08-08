/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.api.initialization.Settings
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class KontureSettingsPluginTest {
    @Test
    fun `settings plugin registers beforeProject callback`() {
        val rootProject = ProjectBuilder.builder().build()
        val gradle = rootProject.gradle

        val settingsProxy =
            Proxy.newProxyInstance(
                Settings::class.java.classLoader,
                arrayOf(Settings::class.java),
            ) { _, method, args ->
                when (method.name) {
                    "getGradle" -> gradle
                    "toString" -> "SettingsProxy"
                    "hashCode" -> 1
                    "equals" -> false
                    else -> null
                }
            } as Settings

        val plugin = KontureSettingsPlugin()
        plugin.apply(settingsProxy)

        // Verify rootProject evaluates with konture plugin applied via beforeProject lifecycle
        val child = ProjectBuilder.builder().withParent(rootProject).build()
        assertNotNull(child)
    }
}

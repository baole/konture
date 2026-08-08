/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.pluginassets

import io.github.baole.konture.Konture
import io.github.baole.konture.modules
import org.junit.jupiter.api.Test

class PluginAndAssetAssertionsTest {

    @Test
    fun `modules plugin assertions`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().notHavePlugin("nonExistentPlugin")
        }
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KonturePropertyScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test property scope filtering and assertions`() {
        val scope = KonturePropertyScope.fromProject(projectGraph)

        val valProps = scope.properties.valProperties()
        assertNotNull(valProps)

        scope.assertTrue("All properties must have a non-empty name") { prop ->
            prop.declaration.name.isNotEmpty()
        }
    }
}

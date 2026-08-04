/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KontureFunctionScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test function scope filtering and assertions`() {
        val scope = KontureFunctionScope.fromProject(projectGraph)

        val funcs = scope.functions.withNameEndingWith("Action")
        assertNotNull(funcs)

        scope.assertTrue("All functions must have a non-empty name") { func ->
            func.declaration.name.isNotEmpty()
        }
    }
}

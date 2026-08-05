/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SourceSetSelectionTest {
    @Test
    fun `test SourceSetId properties and enums`() {
        val id = SourceSetId(":app", "main", SourceSetKind.JVM, SourceSetRole.PRODUCTION)

        assertEquals(":app", id.modulePath)
        assertEquals("main", id.name)
        assertEquals(SourceSetKind.JVM, id.kind)
        assertEquals(SourceSetRole.PRODUCTION, id.role)

        assertEquals(3, SourceSetKind.entries.size)
        assertEquals(2, SourceSetRole.entries.size)
    }

    @Test
    fun `test SourceSets factory methods and SourceSetSelector combinators`() {
        val mainJvm = SourceSetId(":app", "main", SourceSetKind.JVM, SourceSetRole.PRODUCTION)
        val testJvm = SourceSetId(":app", "test", SourceSetKind.JVM, SourceSetRole.TEST)
        val commonMain = SourceSetId(":core", "commonMain", SourceSetKind.KMP, SourceSetRole.PRODUCTION)

        // SourceSets.named
        val namedSelector = SourceSets.named("main", "commonMain")
        assertTrue(namedSelector.matches(mainJvm))
        assertFalse(namedSelector.matches(testJvm))
        assertTrue(namedSelector.matches(commonMain))

        // SourceSets.matchingName
        val globSelector = SourceSets.matchingName("*Main")
        assertFalse(globSelector.matches(mainJvm))
        assertTrue(globSelector.matches(commonMain))

        // SourceSets.of, tests, production
        val prodSelector = SourceSets.production()
        assertTrue(prodSelector.matches(mainJvm))
        assertFalse(prodSelector.matches(testJvm))

        val testSelector = SourceSets.tests()
        assertFalse(testSelector.matches(mainJvm))
        assertTrue(testSelector.matches(testJvm))

        val ofKindSelector = SourceSets.of(kind = SourceSetKind.KMP)
        assertFalse(ofKindSelector.matches(mainJvm))
        assertTrue(ofKindSelector.matches(commonMain))

        // SourceSets.inModule
        val moduleSelector = SourceSets.inModule(":app")
        assertTrue(moduleSelector.matches(mainJvm))
        assertFalse(moduleSelector.matches(commonMain))

        // Selector combinators: and, or, not
        val prodInApp = SourceSets.production() and SourceSets.inModule(":app")
        assertTrue(prodInApp.matches(mainJvm))
        assertFalse(prodInApp.matches(testJvm))
        assertFalse(prodInApp.matches(commonMain))

        val prodOrTestInApp = SourceSets.inModule(":app") or SourceSets.inModule(":core")
        assertTrue(prodOrTestInApp.matches(mainJvm))
        assertTrue(prodOrTestInApp.matches(commonMain))

        val notTests = !SourceSets.tests()
        assertTrue(notTests.matches(mainJvm))
        assertFalse(notTests.matches(testJvm))
    }

    @Test
    fun `test FileDeclaration membershipsFor fallback behavior`() {
        val fileWithNoSourceSets = FileDeclaration("Empty.kt", "com.example")
        val membershipsDefault = fileWithNoSourceSets.membershipsFor(":my-module")

        assertEquals(1, membershipsDefault.size)
        assertEquals(":my-module", membershipsDefault[0].modulePath)
        assertEquals("main", membershipsDefault[0].name)
        assertEquals(SourceSetKind.JVM, membershipsDefault[0].kind)
        assertEquals(SourceSetRole.PRODUCTION, membershipsDefault[0].role)

        val customId = SourceSetId(":my-module", "customMain", SourceSetKind.KMP, SourceSetRole.PRODUCTION)
        val fileWithSourceSets = FileDeclaration("Custom.kt", "com.example", sourceSets = listOf(customId))
        val membershipsCustom = fileWithSourceSets.membershipsFor(":my-module")

        assertEquals(1, membershipsCustom.size)
        assertEquals("customMain", membershipsCustom[0].name)
    }
}

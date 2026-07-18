/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KontureScopeConstructionTest : KontureScopeTestFixture() {
    @Test
    fun `test Scopes operators`() {
        val scope1 = KontureScope(listOf(classA))
        val scope2 = KontureScope(listOf(classB))

        val combined = scope1 + scope2
        assertEquals(2, combined.classes.size)
        assertTrue(combined.classes.contains(classA))
        assertTrue(combined.classes.contains(classB))

        val subtracted = combined - scope2
        assertEquals(1, subtracted.classes.size)
        assertTrue(subtracted.classes.contains(classA))
        assertFalse(subtracted.classes.contains(classB))
    }

    @Test
    fun `test Companion builders`() {
        val fileList = listOf(fileA, fileB, fileC)
        val mockModule =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = "/core",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = fileList,
            )
        val mockGraph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        // Test explicit ProjectGraph passing
        val scopeFromProj = KontureScope.fromProject(mockGraph)
        assertEquals(3, scopeFromProj.classes.size)

        val scopeFromMod = KontureScope.fromModule(":core", mockGraph)
        assertEquals(3, scopeFromMod.classes.size)

        assertThrows<IllegalArgumentException> {
            KontureScope.fromModule(":nonexistent", mockGraph)
        }

        val scopeFromPkg = KontureScope.fromPackage("com.example", mockGraph)
        assertEquals(2, scopeFromPkg.classes.size) // com.example and com.example.service

        // Test default projectGraph lookup via setDefault
        ProjectGraph.setDefault(mockGraph)

        val defaultScopeFromProj = KontureScope.fromProject()
        assertEquals(3, defaultScopeFromProj.classes.size)

        val defaultScopeFromMod = KontureScope.fromModule(":core")
        assertEquals(3, defaultScopeFromMod.classes.size)

        val defaultScopeFromPkg = KontureScope.fromPackage("com.example")
        assertEquals(2, defaultScopeFromPkg.classes.size)
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.setalgebra

import io.github.baole.konture.Konture
import io.github.baole.konture.classScopeFromPackage
import io.github.baole.konture.fileScopeFromPackage
import io.github.baole.konture.functionScopeFromPackage
import io.github.baole.konture.minus
import io.github.baole.konture.moduleScopeFromModule
import io.github.baole.konture.plus
import io.github.baole.konture.propertyScopeFromPackage
import io.github.baole.konture.sliceScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScopeSetAlgebraTest {

    private val pkg = "io.github.baole.konture.tests.setalgebra"

    @Test
    fun `KontureScope set algebra operators plus and minus`() {
        val scopeA = Konture.classScopeFromPackage(pkg)
        val scopeB = Konture.classScopeFromPackage("io.github.baole.konture.tests.declarative")

        val union = scopeA + scopeB
        assertTrue(union.classes.any { it.name == "SetAlgebraClassA" })
        assertTrue(union.classes.any { it.name == "DeclarativeClass" })

        val diff = union - scopeB
        assertTrue(diff.classes.any { it.name == "SetAlgebraClassA" })
        assertFalse(diff.classes.any { it.name == "DeclarativeClass" })
    }

    @Test
    fun `KontureFileScope set algebra operators plus and minus`() {
        val scopeA = Konture.fileScopeFromPackage(pkg)
        val scopeB = Konture.fileScopeFromPackage("io.github.baole.konture.tests.declarative")

        val union = scopeA + scopeB
        assertTrue(union.files.any { it.name == "SetAlgebraTargets.kt" })
        assertTrue(union.files.any { it.name == "DeclarativeTargets.kt" })

        val diff = union - scopeB
        assertTrue(diff.files.any { it.name == "SetAlgebraTargets.kt" })
        assertFalse(diff.files.any { it.name == "DeclarativeTargets.kt" })
    }

    @Test
    fun `KontureFunctionScope set algebra operators plus and minus`() {
        val scopeA = Konture.functionScopeFromPackage(pkg)
        val scopeB = Konture.functionScopeFromPackage("io.github.baole.konture.tests.declarative")

        val union = scopeA + scopeB
        assertTrue(union.functions.any { it.declaration.name == "funcA" })
        assertTrue(union.functions.any { it.declaration.name == "declarativeFunction" })

        val diff = union - scopeB
        assertTrue(diff.functions.any { it.declaration.name == "funcA" })
        assertFalse(diff.functions.any { it.declaration.name == "declarativeFunction" })
    }

    @Test
    fun `KonturePropertyScope set algebra operators plus and minus`() {
        val scopeA = Konture.propertyScopeFromPackage(pkg)
        val scopeB = Konture.propertyScopeFromPackage("io.github.baole.konture.tests.declarative")

        val union = scopeA + scopeB
        assertTrue(union.properties.any { it.declaration.name == "propA" })
        assertTrue(union.properties.any { it.declaration.name == "declarativeProperty" })

        val diff = union - scopeB
        assertTrue(diff.properties.any { it.declaration.name == "propA" })
        assertFalse(diff.properties.any { it.declaration.name == "declarativeProperty" })
    }

    @Test
    fun `KontureModuleScope set algebra operators plus and minus`() {
        val scopeA = Konture.moduleScopeFromModule(":konture-test")
        val scopeB = Konture.moduleScopeFromModule(":library")

        val union = scopeA + scopeB
        assertEquals(2, union.modules.size)

        val diff = union - scopeB
        assertEquals(1, diff.modules.size)
        assertEquals(":konture-test", diff.modules.first().path)
    }

    @Test
    fun `KontureSliceScope set algebra operators plus and minus`() {
        val scopeA = Konture.sliceScope("io.github.baole.konture.tests.(*)..")
        val scopeB = scopeA.byKey("declarative")

        val union = scopeA + scopeB
        assertTrue(union.slices.any { it.key == "setalgebra" })
        assertTrue(union.slices.any { it.key == "declarative" })

        val diff = scopeA - scopeB
        assertTrue(diff.slices.any { it.key == "setalgebra" })
        assertFalse(diff.slices.any { it.key == "declarative" })
    }
}

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KontureScopeAssertionsTest : KontureScopeTestFixture() {
    @Test
    fun `test Class assertion functions`() {
        val classes = listOf(classWithKdoc)
        val kontureScope = KontureScope(classes)

        // Success pathway
        classes.assertTrue { it.name.startsWith("Class") }
        kontureScope.assertTrue { it.name.startsWith("Class") }
        classes.assertHasKDoc()
        kontureScope.assertHasKDoc()

        // Failure pathway
        val errorList =
            assertThrows<AssertionError> {
                listOf(classA).assertTrue("Custom fail msg") { it.name == "Invalid" }
            }
        assertTrue(errorList.message!!.contains("Custom fail msg"))
        assertTrue(errorList.message!!.contains("ClassA"))

        val errorScope =
            assertThrows<AssertionError> {
                KontureScope(listOf(classA)).assertTrue { it.name == "Invalid" }
            }
        assertTrue(errorScope.message!!.contains("ClassA"))

        val kdocListErr =
            assertThrows<AssertionError> {
                listOf(classA).assertHasKDoc("Missing Kdoc")
            }
        assertTrue(kdocListErr.message!!.contains("Missing Kdoc"))

        val kdocScopeErr =
            assertThrows<AssertionError> {
                KontureScope(listOf(classA)).assertHasKDoc()
            }
        assertTrue(kdocScopeErr.message!!.contains("ClassA"))
    }
}

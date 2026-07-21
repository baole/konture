/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeclaredClassScannerTest {
    private val environment = PsiEnvironment()

    @Test
    fun `test collectFqNames`() {
        val content =
            """
            package com.example
            
            class Outer {
                class Inner {
                    object NestedObject
                }
                companion object
            }
            interface SomeInterface
            """.trimIndent()

        val ktFile = environment.createKtFile("TestFile.kt", content)
        val fqNames = DeclaredClassScanner.collectFqNames(ktFile)

        assertEquals(
            setOf(
                "com.example.Outer",
                "com.example.Outer.Inner",
                "com.example.Outer.Inner.NestedObject",
                "com.example.Outer.Companion",
                "com.example.SomeInterface",
            ),
            fqNames,
        )
    }

    @Test
    fun `test enclosingScopes with offset`() {
        val content =
            """
            package com.example
            
            class Outer {
                class Inner {
                    fun foo() {}
                }
            }
            """.trimIndent()
        val ktFile = environment.createKtFile("TestFile.kt", content)

        // Find offset of "foo"
        val offset = content.indexOf("fun foo")
        val scopes = DeclaredClassScanner.enclosingScopes(ktFile, offset)

        assertEquals(
            listOf("com.example.Outer.Inner", "com.example.Outer"),
            scopes,
        )
    }

    @Test
    fun `test enclosingScopes with classFqName`() {
        val scopes1 = DeclaredClassScanner.enclosingScopes("com.example.Outer.Inner", "com.example")
        assertEquals(
            listOf("com.example.Outer.Inner", "com.example.Outer"),
            scopes1,
        )

        val scopes2 = DeclaredClassScanner.enclosingScopes("Outer.Inner", "")
        assertEquals(
            listOf("Outer.Inner", "Outer"),
            scopes2,
        )
    }

    @Test
    fun `test scopedCandidates`() {
        val scopes = listOf("com.example.Outer.Inner", "com.example.Outer")

        val candidates1 = DeclaredClassScanner.scopedCandidates("com.example.Foo", "com.example", scopes)
        assertEquals(
            listOf("com.example.Outer.Inner.Foo", "com.example.Outer.Foo"),
            candidates1,
        )

        // If package doesn't match
        val candidates2 = DeclaredClassScanner.scopedCandidates("other.pkg.Foo", "com.example", scopes)
        assertTrue(candidates2.isEmpty())
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeAliasScannerTest {
    private val environment = PsiEnvironment()

    @Test
    fun `test scan basic type alias`() {
        val content =
            """
            package com.example

            import java.util.ArrayList as JArrayList

            typealias StringList = List<String>
            typealias GenericMap<K, V> = Map<K, V>
            typealias AliasedList = JArrayList<String>
            """.trimIndent()

        val ktFile = environment.createKtFile("TestFile.kt", content)
        val aliases = TypeAliasScanner.scan(ktFile, content)

        assertEquals(3, aliases.size)
        val strList = aliases["com.example.StringList"]
        assertNotNull(strList)
        assertEquals("List<String>", strList?.underlyingType)
        assertEquals(emptyList<String>(), strList?.typeParameters)

        val mapAlias = aliases["com.example.GenericMap"]
        assertNotNull(mapAlias)
        assertEquals(listOf("K", "V"), mapAlias?.typeParameters)
    }

    @Test
    fun `test scan nested type alias in class`() {
        val content =
            """
            package com.example

            class Outer {
                class Target
                typealias LocalTarget = Target
            }
            """.trimIndent()

        val ktFile = environment.createKtFile("TestFile.kt", content)
        val aliases = TypeAliasScanner.scan(ktFile, content)

        val localAlias = aliases["com.example.Outer.LocalTarget"]
        assertNotNull(localAlias)
        assertEquals("com.example.Outer.Target", localAlias?.underlyingType)
    }

    @Test
    fun `test scan with comments and complex formatting`() {
        val content =
            """
            package com.example

            // Comment line
            /* Block comment */
            typealias ComplexHandler<in T, out R> = /* inline comment */ (T) -> R
            typealias SuspendFunc = suspend (String) -> Unit;
            """.trimIndent()

        val ktFile = environment.createKtFile("TestFile.kt", content)
        val aliases = TypeAliasScanner.scan(ktFile, content)

        assertTrue(aliases.containsKey("com.example.ComplexHandler"))
        assertEquals(listOf("T", "R"), aliases["com.example.ComplexHandler"]?.typeParameters)

        val suspendFunc = aliases["com.example.SuspendFunc"]
        assertNotNull(suspendFunc)
        assertTrue(suspendFunc?.underlyingType?.startsWith("suspend") == true)
    }
}

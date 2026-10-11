/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.test

import io.github.baole.konture.Visibility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SnippetAssertionHarnessTest {
    @Test
    fun `test parseSnippet extracts class metadata and imports`() {
        val file =
            SnippetAssertionHarness.parseSnippet(
                """
                package com.example.service

                import io.github.baole.konture.Konture
                import java.io.File

                class UserService(private val name: String) {
                    fun doWork(): String = "done"
                    val isActive: Boolean = true
                }
                """,
            )

        assertEquals("com.example.service", file.packageName)
        assertEquals(2, file.imports.size)
        assertTrue(file.imports.contains("io.github.baole.konture.Konture"))

        val clazz = file.classes.single { it.name == "UserService" }
        assertEquals("com.example.service.UserService", clazz.fqName)
        assertFalse(clazz.isInterface)
        assertFalse(clazz.isAbstract)
    }

    @Test
    fun `test parseClass extracts class directly`() {
        val clazz =
            SnippetAssertionHarness.parseClass(
                """
                package com.example.model
                data class UserDto(val id: String, val age: Int)
                """,
            )

        assertEquals("UserDto", clazz.name)
        assertEquals("com.example.model.UserDto", clazz.fqName)
    }

    @Test
    fun `test parseFunction and parseProperty extract members`() {
        val snippet =
            """
            package com.example.util
            val globalConfig: String = "debug"
            fun calculateTax(amount: Double): Double = amount * 0.2
            """

        val prop = SnippetAssertionHarness.parseProperty(snippet, "globalConfig")
        assertEquals("globalConfig", prop.name)
        assertEquals(Visibility.PUBLIC, prop.visibility)

        val func = SnippetAssertionHarness.parseFunction(snippet, "calculateTax")
        assertEquals("calculateTax", func.name)
        assertEquals(Visibility.PUBLIC, func.visibility)
    }
}

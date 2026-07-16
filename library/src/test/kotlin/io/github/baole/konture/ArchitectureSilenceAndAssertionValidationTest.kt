/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ArchitectureSilenceAndAssertionValidationTest : RuleBuildersTestBase() {
    @Test
    fun `test ClassesRuleBuilder requires assertions`() {
        val builder =
            ClassesRuleBuilder(projectGraph)
                .that()
                .haveNameStartingWith("ClassA")

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("has no assertion"))
    }

    @Test
    fun `test ClassesRuleBuilder throws on empty match by default`() {
        val builder =
            ClassesRuleBuilder(projectGraph)
                .that()
                .haveNameStartingWith("NonExistentClass")
                .should()
                .beInterfaces()

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("No classes matched the filter criteria"))
    }

    @Test
    fun `test ClassesRuleBuilder passes on empty match when allowEmpty is enabled`() {
        val builder =
            ClassesRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .haveNameStartingWith("NonExistentClass")
                .should()
                .beInterfaces()

        // Should not throw
        builder.check()
    }

    @Test
    fun `test FilesRuleBuilder requires assertions`() {
        val builder =
            FilesRuleBuilder(projectGraph)
                .that()
                .haveNameEndingWith(".kt")

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("has no assertion"))
    }

    @Test
    fun `test FilesRuleBuilder throws on empty match by default`() {
        val builder =
            FilesRuleBuilder(projectGraph)
                .that()
                .haveNameEndingWith(".nonexistent")
                .should()
                .beDocumentedWithKDoc()

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("No files matched the filter criteria"))
    }

    @Test
    fun `test FilesRuleBuilder passes on empty match when allowEmpty is enabled`() {
        val builder =
            FilesRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .haveNameEndingWith(".nonexistent")
                .should()
                .beDocumentedWithKDoc()

        // Should not throw
        builder.check()
    }

    @Test
    fun `test FunctionsRuleBuilder requires assertions`() {
        val builder =
            FunctionsRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .resideInAPackage("com.example")

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("has no assertion"))
    }

    @Test
    fun `test FunctionsRuleBuilder throws on empty match by default`() {
        val builder =
            FunctionsRuleBuilder(projectGraph)
                .that()
                .resideInAPackage("nonexistent")
                .should()
                .bePublic()

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("No functions matched the filter criteria"))
    }

    @Test
    fun `test FunctionsRuleBuilder passes on empty match when allowEmpty is enabled`() {
        val builder =
            FunctionsRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .resideInAPackage("nonexistent")
                .should()
                .bePublic()

        // Should not throw
        builder.check()
    }

    @Test
    fun `test ModulesRuleBuilder requires assertions`() {
        val builder =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNamePath(":moduleA")

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("has no assertion"))
    }

    @Test
    fun `test ModulesRuleBuilder throws on empty match by default`() {
        val builder =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNamePath(":nonexistent")
                .should()
                .satisfy { _ -> true }

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("No modules matched the filter criteria"))
    }

    @Test
    fun `test ModulesRuleBuilder passes on empty match when allowEmpty is enabled`() {
        val builder =
            ModulesRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .haveNamePath(":nonexistent")
                .should()
                .satisfy { _ -> true }

        // Should not throw
        builder.check()
    }

    @Test
    fun `test PropertiesRuleBuilder requires assertions`() {
        val builder =
            PropertiesRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .resideInAPackage("com.example")

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("has no assertion"))
    }

    @Test
    fun `test PropertiesRuleBuilder throws on empty match by default`() {
        val builder =
            PropertiesRuleBuilder(projectGraph)
                .that()
                .resideInAPackage("nonexistent")
                .should()
                .bePublic()

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }
        assertTrue(exception.message!!.contains("No properties matched the filter criteria"))
    }

    @Test
    fun `test PropertiesRuleBuilder passes on empty match when allowEmpty is enabled`() {
        val builder =
            PropertiesRuleBuilder(projectGraph)
                .allowEmpty()
                .that()
                .resideInAPackage("nonexistent")
                .should()
                .bePublic()

        // Should not throw
        builder.check()
    }
}

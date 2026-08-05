/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublicApiGapsVerificationTest : RuleBuildersTestBase() {

    @Test
    fun `test files reified and annotation methods`() {
        assertDoesNotThrow {
            FilesRuleBuilder(projectGraph)
                .that().containClass<PublicApiGapsVerificationTest>()
                .and().haveImportOf<Test>()
                .and().containClassesWithAnnotation<Test>()
            FilesRuleBuilder(projectGraph)
                .should().haveAnnotationOf<Test>()
        }
    }

    @Test
    fun `test classes reified usage and module location assertions`() {
        assertDoesNotThrow {
            ClassesRuleBuilder(projectGraph)
                .that().haveNameEndingWith("Test")
            ClassesRuleBuilder(projectGraph)
                .should().notCall<String>()
                .andShould().notReferenceClass<List<*>>()
                .andShould().resideInAModule(":library")
                .andShould().notResideInAModule(":app")
        }
    }

    @Test
    fun `test functions parameter count type filtering and module assertions`() {
        assertDoesNotThrow {
            FunctionsRuleBuilder(projectGraph)
                .that().haveParameterCount(0)
                .and().haveParameterCount { it >= 0 }
                .and().haveParameterOf<String>()
            FunctionsRuleBuilder(projectGraph)
                .should().resideInAModule(":library")
                .andShould().notResideInAModule(":app")
        }
    }

    @Test
    fun `test modules aliases and negative plugin assertions`() {
        val builder = ModulesRuleBuilder(projectGraph)
        builder.that().haveName(":core")
        builder.should().notHavePlugin("com.android.application").andShould().notHavePlugins("com.android.library")

        val violations = mutableListOf<String>()
        builder.getShouldAssertion()?.invoke(moduleA, projectGraph, violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test slices overloads and infix dependency assertions`() {
        assertDoesNotThrow {
            SlicesRuleBuilder(projectGraph)
                .that().haveKeyStartingWith("core", "library")
                .and().haveKeyEndingWith("feature")
                .and().containClass<PublicApiGapsVerificationTest>()
                .and().containClassesInPackage("io.github.baole.konture..")
                .and().containClassesInPackage(listOf("io.github.baole.konture.."))
                .and().containClassesInPackage("io.github.baole.konture..", "io.github.baole..")
                .and().containClassesInPackage { it.startsWith("io.github.baole") }
            SlicesRuleBuilder(projectGraph)
                .should().notDependOnSlice("app")
                .andShould().dependOnSlice("core")
                .andShould().notDependOnSlices("ui", "web")
                .andShould().notDependOnSlices(listOf("ui", "web"))
        }
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ClassesShouldDependencyCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test ClassesShouldDependencyAssertions KDoc and access rules`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vKdoc = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beDocumentedWithKDoc()
            .getShouldAssertion()!!(classWithKdoc, listOf(classWithKdoc), vKdoc)
        assertTrue(vKdoc.isEmpty())

        val vOnlyAccessVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage("com.example..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyAccessVararg)
        assertTrue(vOnlyAccessVararg.isEmpty())

        val vOnlyAccessSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage("com.example..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyAccessSingle)
        assertTrue(vOnlyAccessSingle.isEmpty())

        val vOnlyAccessList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage(listOf("com.example.."))
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyAccessList)
        assertTrue(vOnlyAccessList.isEmpty())

        val vNotAccessVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage("com.other..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotAccessVararg)
        assertTrue(vNotAccessVararg.isEmpty())

        val vNotAccessSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage("com.other..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotAccessSingle)
        assertTrue(vNotAccessSingle.isEmpty())

        val vNotAccessList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage(listOf("com.other.."))
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotAccessList)
        assertTrue(vNotAccessList.isEmpty())

        val vOnlyDependVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage("com.example..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyDependVararg)
        assertTrue(vOnlyDependVararg.isEmpty())

        val vOnlyDependSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage("com.example..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyDependSingle)
        assertTrue(vOnlyDependSingle.isEmpty())

        val vOnlyDependList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage(listOf("com.example.."))
            .getShouldAssertion()!!(classA, listOf(classA, classB), vOnlyDependList)
        assertTrue(vOnlyDependList.isEmpty())

        val vNotDependVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.other..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotDependVararg)
        assertTrue(vNotDependVararg.isEmpty())

        val vNotDependSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.other..")
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotDependSingle)
        assertTrue(vNotDependSingle.isEmpty())

        val vNotDependList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage(listOf("com.other.."))
            .getShouldAssertion()!!(classA, listOf(classA, classB), vNotDependList)
        assertTrue(vNotDependList.isEmpty())
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassesShouldPackageAssertionsTest : RuleBuildersTestBase() {
    @Test
    fun `test inPackage single string`() {
        val rulePassing = ClassesRuleBuilder(projectGraph).should().inPackage("com.example")
        val assertionPassing = rulePassing.getShouldAssertion()!!
        val violationsPassing = mutableListOf<String>()
        assertionPassing(classA, emptyList(), violationsPassing)
        assertTrue(violationsPassing.isEmpty())

        val ruleFailing = ClassesRuleBuilder(projectGraph).should().inPackage("com.other")
        val assertionFailing = ruleFailing.getShouldAssertion()!!
        val violationsFailing = mutableListOf<String>()
        assertionFailing(classA, emptyList(), violationsFailing)
        assertEquals(1, violationsFailing.size)
        assertTrue(violationsFailing[0].contains("ClassA"))
    }

    @Test
    fun `test inPackage list and vararg overloads`() {
        val ruleList = ClassesRuleBuilder(projectGraph).should().inPackage(listOf("com.example", "com.other"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVararg = ClassesRuleBuilder(projectGraph).should().inPackage("com.other", "com.invalid")
        val violationsVararg = mutableListOf<String>()
        ruleVararg.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test inPackage lambda and description overloads`() {
        val rulePredicate = ClassesRuleBuilder(projectGraph).should().inPackage { it.startsWith("com.ex") }
        val violationsPredicate = mutableListOf<String>()
        rulePredicate.getShouldAssertion()!!(classA, emptyList(), violationsPredicate)
        assertTrue(violationsPredicate.isEmpty())

        val ruleDescPredicate =
            ClassesRuleBuilder(
                projectGraph,
            ).should().inPackage("custom pkg") { it == "com.other" }
        val violationsDesc = mutableListOf<String>()
        ruleDescPredicate.getShouldAssertion()!!(classA, emptyList(), violationsDesc)
        assertEquals(1, violationsDesc.size)
        assertTrue(violationsDesc[0].contains("custom pkg"))
    }

    @Test
    fun `test nameEndsWith single list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().nameEndsWith("ClassA")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().nameEndsWith(listOf("ClassA", "ClassB"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().nameEndsWith("Foo", "Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test nameStartsWith single list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().nameStartsWith("Class")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().nameStartsWith(listOf("Class", "My"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().nameStartsWith("Foo", "Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test named predicate and description overloads`() {
        val rulePred = ClassesRuleBuilder(projectGraph).should().named { it.endsWith("A") }
        val violationsPred = mutableListOf<String>()
        rulePred.getShouldAssertion()!!(classA, emptyList(), violationsPred)
        assertTrue(violationsPred.isEmpty())

        val ruleDesc = ClassesRuleBuilder(projectGraph).should().named("must be Foo") { it == "Foo" }
        val violationsDesc = mutableListOf<String>()
        ruleDesc.getShouldAssertion()!!(classA, emptyList(), violationsDesc)
        assertEquals(1, violationsDesc.size)
        assertTrue(violationsDesc[0].contains("must be Foo"))
    }

    @Test
    fun `test nameMatches pattern list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().nameMatches("Class*")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().nameMatches(listOf("Class*", "*A"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().nameMatches("Foo*", "*Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }
}

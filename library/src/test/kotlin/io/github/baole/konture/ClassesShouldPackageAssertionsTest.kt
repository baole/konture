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
    fun `test resideInAPackage single string`() {
        val rulePassing = ClassesRuleBuilder(projectGraph).should().resideInAPackage("com.example")
        val assertionPassing = rulePassing.getShouldAssertion()!!
        val violationsPassing = mutableListOf<String>()
        assertionPassing(classA, emptyList(), violationsPassing)
        assertTrue(violationsPassing.isEmpty())

        val ruleFailing = ClassesRuleBuilder(projectGraph).should().resideInAPackage("com.other")
        val assertionFailing = ruleFailing.getShouldAssertion()!!
        val violationsFailing = mutableListOf<String>()
        assertionFailing(classA, emptyList(), violationsFailing)
        assertEquals(1, violationsFailing.size)
        assertTrue(violationsFailing[0].contains("ClassA"))
    }

    @Test
    fun `test resideInAPackage list and vararg overloads`() {
        val ruleList = ClassesRuleBuilder(projectGraph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVararg = ClassesRuleBuilder(projectGraph).should().resideInAPackage("com.other", "com.invalid")
        val violationsVararg = mutableListOf<String>()
        ruleVararg.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test resideInAPackage lambda and description overloads`() {
        val rulePredicate = ClassesRuleBuilder(projectGraph).should().resideInAPackage { it.startsWith("com.ex") }
        val violationsPredicate = mutableListOf<String>()
        rulePredicate.getShouldAssertion()!!(classA, emptyList(), violationsPredicate)
        assertTrue(violationsPredicate.isEmpty())

        val ruleDescPredicate =
            ClassesRuleBuilder(
                projectGraph,
            ).should().resideInAPackage("custom pkg") { it == "com.other" }
        val violationsDesc = mutableListOf<String>()
        ruleDescPredicate.getShouldAssertion()!!(classA, emptyList(), violationsDesc)
        assertEquals(1, violationsDesc.size)
        assertTrue(violationsDesc[0].contains("custom pkg"))
    }

    @Test
    fun `test haveNameEndingWith single list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().haveNameEndingWith("ClassA")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().haveNameEndingWith(listOf("ClassA", "ClassB"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().haveNameEndingWith("Foo", "Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test haveNameStartingWith single list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().haveNameStartingWith("Class")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().haveNameStartingWith(listOf("Class", "My"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().haveNameStartingWith("Foo", "Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test haveName predicate and description overloads`() {
        val rulePred = ClassesRuleBuilder(projectGraph).should().haveName { it.endsWith("A") }
        val violationsPred = mutableListOf<String>()
        rulePred.getShouldAssertion()!!(classA, emptyList(), violationsPred)
        assertTrue(violationsPred.isEmpty())

        val ruleDesc = ClassesRuleBuilder(projectGraph).should().haveName("must be Foo") { it == "Foo" }
        val violationsDesc = mutableListOf<String>()
        ruleDesc.getShouldAssertion()!!(classA, emptyList(), violationsDesc)
        assertEquals(1, violationsDesc.size)
        assertTrue(violationsDesc[0].contains("must be Foo"))
    }

    @Test
    fun `test haveNameMatching pattern list and vararg overloads`() {
        val ruleSingle = ClassesRuleBuilder(projectGraph).should().haveNameMatching("Class*")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(classA, emptyList(), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = ClassesRuleBuilder(projectGraph).should().haveNameMatching(listOf("Class*", "*A"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(classA, emptyList(), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = ClassesRuleBuilder(projectGraph).should().haveNameMatching("Foo*", "*Bar")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(classA, emptyList(), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }
}

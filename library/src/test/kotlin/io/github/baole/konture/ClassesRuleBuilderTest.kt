/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClassesRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test classes rule builder logical and - or - xor - not filtering`() {
        // 1. class in packageName "com.example" AND is interface
        val rule1 =
            ClassesRuleBuilder(projectGraph)
                .that()
                .resideInAPackage("com.example")
                .and()
                .areInterfaces()
        val pred1 = rule1.getThatPredicate()!!
        assertFalse(pred1(classA))
        assertTrue(pred1(classB))
        assertFalse(pred1(classC))

        // 2. ClassB OR ClassC
        val rule2 =
            ClassesRuleBuilder(projectGraph)
                .that()
                .haveNameStartingWith("ClassB")
                .or()
                .haveNameStartingWith("ClassC")
        val pred2 = rule2.getThatPredicate()!!
        assertFalse(pred2(classA))
        assertTrue(pred2(classB))
        assertTrue(pred2(classC))

        // 3. packageName is "com.example" XOR is interface
        // ClassA: package (T) XOR interface (F) -> T
        // ClassB: package (T) XOR interface (T) -> F
        // ClassC: package (F) XOR interface (F) -> F
        val rule3 =
            ClassesRuleBuilder(projectGraph)
                .that()
                .resideInAPackage("com.example")
                .xor()
                .areInterfaces()
        val pred3 = rule3.getThatPredicate()!!
        assertTrue(pred3(classA))
        assertFalse(pred3(classB))
        assertFalse(pred3(classC))

        // 4. NOT ClassA
        val rule4 =
            ClassesRuleBuilder(projectGraph)
                .not()
                .haveNameStartingWith("ClassA")
        val pred4 = rule4.getThatPredicate()!!
        assertFalse(pred4(classA))
        assertTrue(pred4(classB))
        assertTrue(pred4(classC))
    }

    @Test
    fun `test classes rule builder assertions logical and - or - xor - not`() {
        val rule1 =
            ClassesRuleBuilder(projectGraph)
                .should()
                .satisfy { cls, violations ->
                    if (!cls.name.startsWith("Class")) {
                        violations.add("name")
                    }
                }.andShould()
                .satisfy { cls, violations ->
                    if (cls.isInterface) {
                        violations.add("no interfaces")
                    }
                }

        val assertion1 = rule1.getShouldAssertion()!!

        // classA is not interface, starts with Class -> passes both
        val vA = mutableListOf<String>()
        assertion1(classA, emptyList(), vA)
        assertTrue(vA.isEmpty())

        // classB is interface -> fails second
        val vB = mutableListOf<String>()
        assertion1(classB, emptyList(), vB)
        assertEquals(1, vB.size)
    }
}

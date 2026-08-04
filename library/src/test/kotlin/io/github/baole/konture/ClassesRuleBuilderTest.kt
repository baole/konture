/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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

    @Test
    fun `class violation location includes module and source set`() {
        val error =
            assertThrows(AssertionError::class.java) {
                ClassesRuleBuilder(projectGraph)
                    .that()
                    .haveNameStartingWith("ClassA")
                    .should()
                    .beInterfaces()
                    .check()
            }
        assertTrue(
            error.message!!.contains("(at :moduleA, main source set) (src/ClassA.kt)"),
            "Expected uniform module + source set location, got: ${error.message}",
        )
    }

    @Test
    fun `test classes rule builder notCall and notReferenceClass`() {
        val usageCall =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Logger.log",
                filePath = "/src/ClassA.kt",
                line = 10,
                column = 5,
                enclosingClass = "ClassA",
                rawExpression = "Logger.log",
            )
        val usageRef =
            SourceUsage(
                kind = UsageKind.CLASS_REFERENCE,
                targetFqName = "com.example.Service",
                filePath = "/src/ClassA.kt",
                line = 12,
                column = 5,
                enclosingClass = "ClassA",
                rawExpression = "Service",
            )
        val fileDeclWithUsages =
            FileDeclaration("ClassA.kt", "com.example", classes = listOf(classA), usages = listOf(usageCall, usageRef), filePath = "/src/ClassA.kt")

        val moduleWithUsages = moduleA.copy(files = listOf(fileDeclWithUsages))
        val graphWithUsages = ProjectGraph(builds = mapOf(":" to listOf(moduleWithUsages)))

        val ruleCall =
            ClassesRuleBuilder(graphWithUsages)
                .should()
                .notCall("com.example.Logger.log")
        val vCall = mutableListOf<String>()
        ruleCall.getShouldAssertion()!!(classA, emptyList(), vCall)
        assertEquals(1, vCall.size)
        assertTrue(vCall[0].contains("Logger.log"))

        val ruleRef =
            ClassesRuleBuilder(graphWithUsages)
                .should()
                .notReferenceClass("com.example.Service")
        val vRef = mutableListOf<String>()
        ruleRef.getShouldAssertion()!!(classA, emptyList(), vRef)
        assertEquals(1, vRef.size)
        assertTrue(vRef[0].contains("com.example.Service"))
    }

    @Test
    fun `test classes rule builder notCall with class FQN and method FQN`() {
        val analyticsCall =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Analytics.trackEvent",
                filePath = "/src/ClassA.kt",
                line = 15,
                column = 5,
                enclosingClass = "ClassA",
                rawExpression = "analytics.trackEvent",
            )
        val logEventCall =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.LogEvent.trackEvent",
                filePath = "/src/ClassA.kt",
                line = 16,
                column = 5,
                enclosingClass = "ClassA",
                rawExpression = "logEvent.trackEvent",
            )
        val fileDecl =
            FileDeclaration(
                "ClassA.kt",
                "com.example",
                classes = listOf(classA),
                usages = listOf(analyticsCall, logEventCall),
                filePath = "/src/ClassA.kt",
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(moduleA.copy(files = listOf(fileDecl)))))

        // 1. Method FQN matching
        val rule1 = ClassesRuleBuilder(graph).should().notCall("com.example.Analytics.trackEvent")
        val v1 = mutableListOf<String>()
        rule1.getShouldAssertion()!!(classA, emptyList(), v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("analytics.trackEvent"))

        // 2. Class FQN matching (bans any call on com.example.Analytics)
        val rule2 = ClassesRuleBuilder(graph).should().notCall("com.example.Analytics")
        val v2 = mutableListOf<String>()
        rule2.getShouldAssertion()!!(classA, emptyList(), v2)
        assertEquals(1, v2.size)
        assertTrue(v2[0].contains("analytics.trackEvent"))
    }
}

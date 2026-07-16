/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PropertyAssertionScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test allProperties assertions`() {
        val prop1 =
            PropertyDeclaration(
                name = "id",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "kotlin.String",
                isVal = true,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                kdocText = "/** KDoc */",
            )
        val prop2 =
            PropertyDeclaration(
                name = "count",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "kotlin.Int",
                isVal = false, // var
                annotations = emptyList(),
                kdocText = null,
            )

        val targetClass =
            ClassDeclaration(
                name = "Service",
                fqName = "com.example.Service",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Service.kt",
                properties = listOf(prop1, prop2),
            )

        // Test val requirement (should fail due to prop2)
        val ruleVal = ClassesRuleBuilder(projectGraph).should().allProperties { beVal() }
        val assertVal = ruleVal.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertVal(targetClass, emptyList(), v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("Property count in class com.example.Service has violations"))

        // Test var requirement (should fail due to prop1)
        val ruleVar = ClassesRuleBuilder(projectGraph).should().allProperties { beVar() }
        val assertVar = ruleVar.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertVar(targetClass, emptyList(), v2)
        assertEquals(1, v2.size)

        // Test type requirement (should fail due to prop2)
        val ruleType = ClassesRuleBuilder(projectGraph).should().allProperties { haveType("kotlin.String") }
        val assertType = ruleType.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertType(targetClass, emptyList(), v3)
        assertEquals(1, v3.size)
    }

    @Test
    fun `test property assertion scope consistency`() {
        val prop =
            PropertyDeclaration(
                name = "myProp",
                type = "Int",
                annotations = listOf(AnnotationDeclaration("P1", "com.example.P1")),
                modifiers = setOf(Modifier.CONST),
                visibility = Visibility.PRIVATE,
                kdocText = "KDoc",
                isVal = true,
                isExtension = false,
            )

        val classWithMembers =
            ClassDeclaration(
                name = "ClassWithMembers",
                fqName = "com.example.ClassWithMembers",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithMembers.kt",
                properties = listOf(prop),
            )

        val ruleProp =
            ClassesRuleBuilder(projectGraph).should().allProperties {
                haveAllAnnotationsOf(listOf("P1"))
                haveAllAnnotationsOf("P1")
                haveAnyAnnotationOf(listOf("P1", "P2"))
                haveAnyAnnotationOf("P1", "P2")
                haveAllModifiers(listOf(Modifier.CONST))
                haveAllModifiers(Modifier.CONST)
                haveAnyModifier(listOf(Modifier.CONST, Modifier.LATEINIT))
                haveAnyModifier(Modifier.CONST, Modifier.LATEINIT)
                haveAnyVisibility(listOf(Visibility.PRIVATE))
                haveAnyVisibility(Visibility.PRIVATE)
                haveNameMatching("my*")
                haveNameStartingWith("my")
                haveNameEndingWith("Prop")
                bePrivate()
                haveAnnotationOf("P1")
                beDocumentedWithKDoc()
            }
        val assertProp = ruleProp.getShouldAssertion()!!
        val vProp = mutableListOf<String>()
        assertProp(classWithMembers, emptyList(), vProp)
        assertTrue(vProp.isEmpty(), "Expected no violations: $vProp")
    }

    @Test
    fun `test property assertion scope failure messages`() {
        val prop =
            PropertyDeclaration(
                name = "count",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.LATEINIT),
                type = "kotlin.Int",
                isVal = false, // var
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                kdocText = null,
            )

        val classWithMembers =
            ClassDeclaration(
                name = "ClassWithMembers",
                fqName = "com.example.ClassWithMembers",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithMembers.kt",
                properties = listOf(prop),
            )

        val failures =
            mapOf<String, PropertyAssertionScope.() -> Unit>(
                "should have name matching any of: 'id'" to { haveNameMatching("id") },
                "should have name starting with any of: 'id'" to { haveNameStartingWith("id") },
                "should have name ending with any of: 'id'" to { haveNameEndingWith("id") },
                "should be internal (was 'public')" to { beInternal() },
                "should be private (was 'public')" to { bePrivate() },
                "should be protected (was 'public')" to { beProtected() },
                "should be public" to {
                    // check pass first, we'll verify it doesn't fail
                    bePublic()
                },
                "should be documented with KDoc" to { beDocumentedWithKDoc() },
                "should be declared as val" to { beVal() },
                "should have all annotations: P1, P2" to { haveAllAnnotationsOf("P1", "P2") },
                "should have at least one annotation of: P1, P2" to { haveAnyAnnotationOf("P1", "P2") },
                "should have all modifiers: const" to { haveAllModifiers(Modifier.CONST) },
                "should have at least one modifier of: const, open" to
                    { haveAnyModifier(Modifier.CONST, Modifier.OPEN) },
                "should have visibility of: private, protected" to
                    { haveAnyVisibility(Visibility.PRIVATE, Visibility.PROTECTED) },
                "should be annotated with any of: @P1" to { haveAnnotationOf("P1") },
                "should have type of any of: 'Int'" to { haveType("Int") },
            )

        failures.forEach { (expectedMessage, assertion) ->
            val rule = ClassesRuleBuilder(projectGraph).should().allProperties { assertion() }
            val assertFn = rule.getShouldAssertion()!!
            val violations = mutableListOf<String>()
            assertFn(classWithMembers, emptyList(), violations)
            if (expectedMessage == "should be public") {
                assertTrue(violations.isEmpty(), "Expected public check to pass but got: $violations")
            } else {
                assertEquals(1, violations.size, "Expected 1 violation for $expectedMessage")
                assertTrue(
                    violations[0].contains(expectedMessage),
                    "Expected violation message '${violations[0]}' to contain '$expectedMessage'",
                )
            }
        }

        // Test beVar passes on prop (since isVal = false)
        val ruleVar = ClassesRuleBuilder(projectGraph).should().allProperties { beVar() }
        val assertVar = ruleVar.getShouldAssertion()!!
        val violationsVar = mutableListOf<String>()
        assertVar(classWithMembers, emptyList(), violationsVar)
        assertTrue(violationsVar.isEmpty(), "Expected var check to pass but got: $violationsVar")
    }

    @Test
    fun `test property assertion scope plural logical OR matching`() {
        val annotation = AnnotationDeclaration(name = "Inject", fqName = "javax.inject.Inject")
        val prop =
            PropertyDeclaration(
                name = "userData",
                type = "com.example.User",
                isVal = true,
                annotations = listOf(annotation),
                modifiers = emptySet(),
                visibility = Visibility.PUBLIC,
                kdocText = null,
            )

        // All of these should PASS since at least one pattern matches (logical OR)
        val passingScope =
            PropertyAssertionScope().apply {
                haveNameMatching("get*", "user*")
                haveNameStartingWith("get", "user")
                haveNameEndingWith("Data", "Model")
                haveType("Int", "com.example.User")
                haveAnnotationOf("Bind", "Inject")
            }

        val passViolations = mutableListOf<String>()
        for (assertion in passingScope.assertions) {
            assertion(prop, passViolations)
        }
        assertTrue(passViolations.isEmpty(), "Expected logical OR to pass but got: $passViolations")

        // All of these should FAIL since none of the patterns match
        val failingScope =
            PropertyAssertionScope().apply {
                haveNameMatching("find*", "load*")
                haveNameStartingWith("find", "load")
                haveNameEndingWith("Entity", "Dto")
                haveType("Int", "Double")
                haveAnnotationOf("Post", "Delete")
            }

        val failViolations = mutableListOf<String>()
        for (assertion in failingScope.assertions) {
            assertion(prop, failViolations)
        }
        assertEquals(5, failViolations.size)
        assertTrue(failViolations.any { it.contains("should have name matching any of: 'find*', 'load*'") })
        assertTrue(failViolations.any { it.contains("should have name starting with any of: 'find', 'load'") })
        assertTrue(failViolations.any { it.contains("should have name ending with any of: 'Entity', 'Dto'") })
        assertTrue(failViolations.any { it.contains("should have type of any of: 'Int', 'Double'") })
        assertTrue(failViolations.any { it.contains("should be annotated with any of: @Post, @Delete") })
    }

    @Test
    fun `test property assertion scope overloads list and singular`() {
        val annotation = AnnotationDeclaration(name = "Inject", fqName = "javax.inject.Inject")
        val prop =
            PropertyDeclaration(
                name = "userData",
                type = "com.example.User",
                isVal = true,
                annotations = listOf(annotation),
                modifiers = emptySet(),
                visibility = Visibility.PUBLIC,
                kdocText = null,
            )

        // Single string overloads (passing)
        val passingSingleScope =
            PropertyAssertionScope().apply {
                haveNameMatching("user*")
                haveNameStartingWith("user")
                haveNameEndingWith("Data")
                haveType("com.example.User")
                haveAnnotationOf("Inject")
            }

        val passSingleViolations = mutableListOf<String>()
        for (assertion in passingSingleScope.assertions) {
            assertion(prop, passSingleViolations)
        }
        assertTrue(passSingleViolations.isEmpty(), "Expected singular overloads to pass but got: $passSingleViolations")

        // List overloads (passing OR match)
        val passingListScope =
            PropertyAssertionScope().apply {
                haveNameMatching(listOf("get*", "user*"))
                haveNameStartingWith(listOf("get", "user"))
                haveNameEndingWith(listOf("Data", "Model"))
                haveType(listOf("Int", "com.example.User"))
                haveAnnotationOf(listOf("Bind", "Inject"))
            }

        val passListViolations = mutableListOf<String>()
        for (assertion in passingListScope.assertions) {
            assertion(prop, passListViolations)
        }
        assertTrue(passListViolations.isEmpty(), "Expected list overloads to pass but got: $passListViolations")

        // List overloads (failing match)
        val failingListScope =
            PropertyAssertionScope().apply {
                haveNameMatching(listOf("find*", "load*"))
                haveNameStartingWith(listOf("find", "load"))
                haveNameEndingWith(listOf("Entity", "Dto"))
                haveType(listOf("Int", "Double"))
                haveAnnotationOf(listOf("Post", "Delete"))
            }

        val failListViolations = mutableListOf<String>()
        for (assertion in failingListScope.assertions) {
            assertion(prop, failListViolations)
        }
        assertEquals(5, failListViolations.size)
    }
}

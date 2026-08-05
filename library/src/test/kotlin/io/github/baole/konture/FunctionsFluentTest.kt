/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionsFluentTest : RuleBuildersTestBase() {
    @Test
    fun `test FunctionsRuleBuilder that and should extensions`() {
        val rule =
            FunctionsRuleBuilder(projectGraph)
                .that { name == "myFunc" }
                .should {
                    check(returnType == "Unit", "Must return Unit")
                }

        val func =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "MyClass", ":app", "/src/MyClass.kt")

        val thatPred = rule.getThatPredicate()!!
        assertTrue(thatPred(funcCtx))

        val violations = mutableListOf<String>()
        rule.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test FunctionDeclarationShouldContext properties and annotation assertions`() {
        val anno1 = AnnotationDeclaration("MyAnno", "com.example.MyAnno")
        val anno2 = AnnotationDeclaration("OtherAnno", "com.example.OtherAnno")
        val param1 = ParameterDeclaration("p1", "String", false, emptyList())

        val func =
            FunctionDeclaration(
                name = "execute",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND),
                returnType = "String",
                parameters = listOf(param1),
                annotations = listOf(anno1, anno2),
                kdocText = "/** Execute doc */",
                isExtension = true,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Service", ":app", "/src/Service.kt")
        val violations = mutableListOf<String>()
        val context = FunctionDeclarationShouldContext(funcCtx, listOf(funcCtx), violations)

        assertEquals("execute", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals("Service", context.className)
        assertEquals(":app", context.modulePath)
        assertEquals("/src/Service.kt", context.filePath)
        assertEquals(Visibility.PUBLIC, context.visibility)
        assertEquals(setOf(Modifier.SUSPEND), context.modifiers)
        assertEquals("String", context.returnType)
        assertEquals(listOf(param1), context.parameters)
        assertEquals(listOf(anno1, anno2), context.annotations)
        assertEquals("/** Execute doc */", context.kdocText)
        assertTrue(context.isExtension)

        // addViolation & check
        context.addViolation("Error 1")
        assertEquals(1, violations.size)

        violations.clear()
        context.check(false)
        assertEquals(1, violations.size)

        violations.clear()
        context.check(false, "Check error")
        assertEquals("Check error", violations[0])

        // annotations checks
        assertTrue(context.hasAnnotation("MyAnno"))
        assertFalse(context.hasAnnotation("Missing"))

        assertTrue(context.hasAllAnnotations("MyAnno", "OtherAnno"))
        assertTrue(context.hasAllAnnotations(listOf("MyAnno", "OtherAnno")))
        assertFalse(context.hasAllAnnotations("MyAnno", "Missing"))

        assertTrue(context.hasAnyAnnotation("MyAnno", "Missing"))
        assertTrue(context.hasAnyAnnotation(listOf("MyAnno", "Missing")))
        assertFalse(context.hasAnyAnnotation("Missing"))

        // assert annotations
        violations.clear()
        context.assertAnnotationOf("MyAnno")
        assertTrue(violations.isEmpty())

        context.assertAnnotationOf("Missing")
        assertEquals(1, violations.size)

        violations.clear()
        context.assertAllAnnotationsOf("MyAnno", "OtherAnno")
        assertTrue(violations.isEmpty())

        context.assertAllAnnotationsOf("MyAnno", "Missing")
        assertEquals(1, violations.size)

        violations.clear()
        context.assertAnyAnnotationOf("MyAnno", "Missing")
        assertTrue(violations.isEmpty())

        context.assertAnyAnnotationOf("Missing")
        assertEquals(1, violations.size)

        // parameter matching
        violations.clear()
        context.noneParameterMatches("must not have Int params") { it.type == "Int" }
        assertTrue(violations.isEmpty())

        context.noneParameterMatches("must not have String params") { it.type == "String" }
        assertEquals(1, violations.size)

        violations.clear()
        context.anyParameterMatches("must have String params") { it.type == "String" }
        assertTrue(violations.isEmpty())

        context.anyParameterMatches("must have Int params") { it.type == "Int" }
        assertEquals(1, violations.size)
    }

    @Test
    fun `test FunctionDeclarationContext delegation properties and helper extensions`() {
        val anno1 = AnnotationDeclaration("MyAnno", "com.example.MyAnno")
        val func =
            FunctionDeclaration(
                name = "helper",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.INLINE),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = listOf(anno1),
                kdocText = "KDoc text",
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Helper", ":app", "/src/Helper.kt")

        assertTrue(funcCtx.hasAnnotation("MyAnno"))
        assertTrue(funcCtx.hasAllAnnotations("MyAnno"))
        assertTrue(funcCtx.hasAllAnnotations(listOf("MyAnno")))
        assertTrue(funcCtx.hasAnyAnnotation("MyAnno", "Other"))
        assertTrue(funcCtx.hasAnyAnnotation(listOf("MyAnno", "Other")))

        assertEquals("helper", funcCtx.name)
        assertEquals(Visibility.INTERNAL, funcCtx.visibility)
        assertEquals(setOf(Modifier.INLINE), funcCtx.modifiers)
        assertEquals("Unit", funcCtx.returnType)
        assertTrue(funcCtx.parameters.isEmpty())
        assertEquals(listOf(anno1), funcCtx.annotations)
        assertFalse(funcCtx.isExtension)
        assertEquals("KDoc text", funcCtx.kdocText)
    }
}

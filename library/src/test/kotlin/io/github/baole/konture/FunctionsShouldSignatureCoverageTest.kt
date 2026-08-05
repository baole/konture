/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsShouldSignatureCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test FunctionsShouldSignatureAssertions return types, annotations and parameters`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val param1 = ParameterDeclaration("arg1", "String", false, emptyList())
        val funcDecl =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = listOf(param1),
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            "MyAnnotation",
                            "com.example.MyAnnotation",
                            listOf(AnnotationArgumentDeclaration("arg", "val")),
                        ),
                    ),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx =
            FunctionDeclarationContext(
                declaration = funcDecl,
                packageName = "com.example",
                className = "ClassA",
                modulePath = ":app",
                filePath = "/src/ClassA.kt",
            )

        val vRetSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveReturnType("String")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vRetSingle)
        assertTrue(vRetSingle.isEmpty())

        val vRetList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveReturnType(listOf("String"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vRetList)
        assertTrue(vRetList.isEmpty())

        val vRetVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveReturnType("String", "Int")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vRetVararg)
        assertTrue(vRetVararg.isEmpty())

        val vAnnotSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnnotSingle)
        assertTrue(vAnnotSingle.isEmpty())

        val vAnnotList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnnotationOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnnotList)
        assertTrue(vAnnotList.isEmpty())

        val vAnnotVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnnotVararg)
        assertTrue(vAnnotVararg.isEmpty())

        val vAnnotArg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnnotationWithArgument("MyAnnotation", "arg", "val")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnnotArg)
        assertTrue(vAnnotArg.isEmpty())

        val vAllAnnotList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAllAnnotList)
        assertTrue(vAllAnnotList.isEmpty())

        val vAllAnnotVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf("MyAnnotation")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAllAnnotVararg)
        assertTrue(vAllAnnotVararg.isEmpty())

        val vAnyAnnotList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnyAnnotationOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnyAnnotList)
        assertTrue(vAnyAnnotList.isEmpty())

        val vAnyAnnotVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnyAnnotationOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnyAnnotVararg)
        assertTrue(vAnyAnnotVararg.isEmpty())

        val vParamsList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveParameterTypes(listOf("String"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vParamsList)
        assertTrue(vParamsList.isEmpty())

        val vParamsVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveParameterTypes("String")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vParamsVararg)
        assertTrue(vParamsVararg.isEmpty())

        val vAnyParamTypeList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnyParameterType(listOf("String"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnyParamTypeList)
        assertTrue(vAnyParamTypeList.isEmpty())

        val vAnyParamTypeVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveAnyParameterType("String", "Int")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vAnyParamTypeVararg)
        assertTrue(vAnyParamTypeVararg.isEmpty())

        val vNoParams = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNoParameters()
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNoParams)
        assertEquals(1, vNoParams.size) // funcCtx has 1 parameter
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsRuleBuilderExtendedTest : KontureScopeTestFixture() {
    @Test
    fun `test functions rule builder additional filters and gates`() {
        val fObj =
            FunctionDeclaration(
                name = "calculateValue",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.OPEN),
                returnType = "kotlin.Double",
                parameters =
                    listOf(
                        ParameterDeclaration("p1", "kotlin.String", hasDefaultValue = false, annotations = emptyList()),
                        ParameterDeclaration("p2", "kotlin.Int", hasDefaultValue = false, annotations = emptyList()),
                    ),
                annotations =
                    listOf(
                        AnnotationDeclaration("ServiceAnno", "com.example.ServiceAnno"),
                        AnnotationDeclaration("HelperAnno", "com.example.HelperAnno"),
                    ),
                isExtension = true,
                kdocText = "/** Calculates */",
            )
        val cls =
            ClassDeclaration(
                name = "Processor",
                fqName = "com.example.Processor",
                packageName = "com.example.service",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Processor.kt",
                functions = listOf(fObj),
            )
        val fileDecl =
            FileDeclaration(
                name = "Processor.kt",
                packageName = "com.example.service",
                classes = listOf(cls),
                topLevelFunctions = emptyList(),
                filePath = "/src/Processor.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":service",
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context =
            FunctionDeclarationContext(fObj, "com.example.service", "Processor", ":service", "/src/Processor.kt")

        // 1. That filters
        assertTrue(FunctionsRuleBuilder(graph).that().haveNameStartingWith("calc").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameStartingWith("fetch").getThatPredicate()!!(context))

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Value").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Data").getThatPredicate()!!(context))

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameMatching("calculate*").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameMatching("fetch*").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(graph)
                .that()
                .beMember()
                .and()
                .satisfy {
                    it.className == "Processor"
                }.getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph)
                .that()
                .beMember()
                .and()
                .satisfy {
                    it.className == "Service"
                }.getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveAnnotationOf("ServiceAnno").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveAnnotationOf("OtherAnno").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("ServiceAnno", "OtherAnno").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("OtherAnno", "Another").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("ServiceAnno", "HelperAnno").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("ServiceAnno", "OtherAnno").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph).that().haveParameterTypes("kotlin.String").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Long").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyParameterType("kotlin.Int", "kotlin.Long").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Long").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(graph)
                .that()
                .resideInAPackage {
                    it.startsWith("com.example")
                }.getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph)
                .that()
                .resideInAPackage {
                    it.startsWith("io.github")
                }.getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Double").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Int").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType("kotlin.Double", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )

        // Custom satisfy
        assertTrue(
            FunctionsRuleBuilder(graph).that().satisfy { it.declaration.isExtension }.getThatPredicate()!!(context),
        )

        // 2. Should assertions
        val assertShould =
            FunctionsRuleBuilder(graph)
                .should()
                .resideInAPackage("com.example.*")
                .andShould()
                .resideInAPackage { it.contains("service") }
                .andShould()
                .haveNameStartingWith("calc")
                .andShould()
                .haveNameEndingWith("Value")
                .andShould()
                .haveNameMatching("*Value")
                .andShould()
                .beProtected()
                .andShould()
                .beOpen()
                .andShould()
                .haveReturnType("kotlin.Double")
                .andShould()
                .haveAnnotationOf("ServiceAnno")
                .andShould()
                .haveAllAnnotationsOf("ServiceAnno", "HelperAnno")
                .andShould()
                .haveAnyAnnotationOf("HelperAnno", "OtherAnno")
                .andShould()
                .haveModifier(Modifier.OPEN)
                .andShould()
                .haveAllModifiers(Modifier.OPEN)
                .andShould()
                .haveAnyModifier(Modifier.OPEN, Modifier.SUSPEND)
                .andShould()
                .haveVisibility(Visibility.PROTECTED)
                .andShould()
                .haveAnyVisibility(Visibility.PROTECTED, Visibility.PUBLIC)
                .andShould()
                .haveReturnType("kotlin.Double", "kotlin.Int")
                .andShould()
                .haveParameterTypes("kotlin.String", "kotlin.Int")
                .andShould()
                .haveAnyParameterType("kotlin.Int")
                .andShould()
                .beExtension()
                .andShould()
                .beDocumentedWithKDoc()
                .andShould()
                .satisfy { it.declaration.name == "calculateValue" }
                .andShould()
                .satisfy { _, violations -> violations.clear() }
                .getShouldAssertion()!!

        val v = mutableListOf<String>()
        assertShould(context, emptyList(), v)
        assertTrue(v.isEmpty(), "Violations list is not empty: $v")

        // Assertion Failure paths
        val assertFailures =
            FunctionsRuleBuilder(graph)
                .should()
                .resideInAPackage("io.github.*")
                .andShould()
                .resideInAPackage { it.contains("domain") }
                .andShould()
                .haveNameStartingWith("fetch")
                .andShould()
                .haveNameEndingWith("Data")
                .andShould()
                .haveNameMatching("fetch*")
                .andShould()
                .bePublic()
                .andShould()
                .bePrivate()
                .andShould()
                .beInternal()
                .andShould()
                .beSuspend()
                .andShould()
                .beInline()
                .andShould()
                .beAbstract()
                .andShould()
                .haveReturnType("kotlin.Int")
                .andShould()
                .haveAnnotationOf("OtherAnno")
                .andShould()
                .haveAllAnnotationsOf("ServiceAnno", "OtherAnno")
                .andShould()
                .haveAnyAnnotationOf("OtherAnno")
                .andShould()
                .haveModifier(Modifier.SUSPEND)
                .andShould()
                .haveAllModifiers(Modifier.OPEN, Modifier.SUSPEND)
                .andShould()
                .haveAnyModifier(Modifier.SUSPEND, Modifier.INLINE)
                .andShould()
                .haveVisibility(Visibility.PUBLIC)
                .andShould()
                .haveAnyVisibility(Visibility.PUBLIC, Visibility.PRIVATE)
                .andShould()
                .haveReturnType("kotlin.Int")
                .andShould()
                .haveParameterTypes("kotlin.Long")
                .andShould()
                .haveAnyParameterType("kotlin.Long")
                .andShould()
                .beExtension()
                .getShouldAssertion()!!

        val vf = mutableListOf<String>()
        assertFailures(context, emptyList(), vf)
        assertTrue(vf.isNotEmpty())

        val nonExtensionContext =
            FunctionDeclarationContext(
                fObj.copy(isExtension = false, kdocText = null),
                "com.example.service",
                "Processor",
                ":service",
                "/src/Processor.kt",
            )
        val vf2 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().beExtension().andShould().beDocumentedWithKDoc().getShouldAssertion()!!(
            nonExtensionContext,
            emptyList(),
            vf2,
        )
        assertEquals(2, vf2.size)

        val vfSatisfy = mutableListOf<String>()
        FunctionsRuleBuilder(graph)
            .should()
            .satisfy { f ->
                f.declaration.name == "something_else"
            }.getShouldAssertion()!!(context, emptyList(), vfSatisfy)
        assertEquals(1, vfSatisfy.size)

        val ruleTrue =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .beProtected()
        ruleTrue.check()

        val ruleFalse =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .bePublic()

        val exception =
            assertThrows(AssertionError::class.java) {
                ruleFalse.check()
            }
        assertTrue(exception.message!!.contains("calculateValue should be public"))

        val ruleOr =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .bePublic()
                .orShould()
                .beProtected()
        ruleOr.check()

        val ruleXor =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .beProtected()
                .xorShould()
                .bePublic()
        ruleXor.check()

        val ruleNot =
            FunctionsRuleBuilder(graph)
                .notShould()
                .bePublic()
        ruleNot.check()
    }

    @Test
    fun `test printMatchedFunctions and printAllFunctions debugging helpers`() {
        val printedMatched = mutableListOf<String>()
        val printedAll = mutableListOf<String>()

        val func1 =
            FunctionDeclaration(
                "fetchData",
                Visibility.PUBLIC,
                emptySet(),
                "String",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val func2 =
            FunctionDeclaration(
                "processData",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fileDecl = FileDeclaration("Service.kt", "com.example", topLevelFunctions = listOf(func1, func2))
        val mockModule = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileDecl))
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        FunctionsRuleBuilder(graph)
            .printAllFunctions { printedAll.add(it.declaration.name) }
            .that { declaration.name == "fetchData" }
            .printMatchedFunctions { printedMatched.add(it.declaration.name) }
            .should().satisfy { true }
            .check()

        assertEquals(listOf("fetchData"), printedMatched)
        assertTrue(printedAll.contains("fetchData"))
        assertTrue(printedAll.contains("processData"))
    }

    @Test
    fun `test functions rule builder overloads`() {
        val fObj =
            FunctionDeclaration(
                name = "processData",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters =
                    listOf(
                        ParameterDeclaration(
                            "id",
                            "Int",
                            hasDefaultValue = false,
                            annotations = emptyList(),
                            resolvedType = "kotlin.Int",
                        ),
                    ),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.String",
            )
        val fileDecl =
            FileDeclaration(
                name = "Processor.kt",
                packageName = "com.example.service",
                classes = emptyList(),
                topLevelFunctions = listOf(fObj),
                filePath = "/src/Processor.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":service",
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val callUsage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Logger.log",
                filePath = "/src/Processor.kt",
                line = 10,
                column = 5,
            )
        val context =
            FunctionDeclarationContext(
                fObj,
                packageName = "com.example.service",
                className = null,
                modulePath = ":service",
                filePath = "/src/Processor.kt",
                usages = listOf(callUsage),
            )

        val notCallRule1 = FunctionsRuleBuilder(graph).should().notCall("com.example.Logger.log")
        val vNotCall1 = mutableListOf<String>()
        notCallRule1.getShouldAssertion()!!(context, emptyList(), vNotCall1)
        assertEquals(1, vNotCall1.size)
        assertTrue(vNotCall1[0].contains("Logger.log"))
        assertTrue(
            vNotCall1[0].contains(":service, unknown source set) (com.example.service.Processor(Processor.kt:10:5)"),
        )

        val notCallRule2 = FunctionsRuleBuilder(graph).should().notCall(String::class)
        val vNotCall2 = mutableListOf<String>()
        notCallRule2.getShouldAssertion()!!(context, emptyList(), vNotCall2)
        assertTrue(vNotCall2.isEmpty())

        val notCallRule3 = FunctionsRuleBuilder(graph).should().notCall<Int>()
        val vNotCall3 = mutableListOf<String>()
        notCallRule3.getShouldAssertion()!!(context, emptyList(), vNotCall3)
        assertTrue(vNotCall3.isEmpty())

        val resideRule1 = FunctionsRuleBuilder(graph).should().resideInAPackage("com.example..", "other..")
        val vReside1 = mutableListOf<String>()
        resideRule1.getShouldAssertion()!!(context, emptyList(), vReside1)
        assertTrue(vReside1.isEmpty())

        val resideRule2 = FunctionsRuleBuilder(graph).should().resideInAPackage("other..", "another..")
        val vReside2 = mutableListOf<String>()
        resideRule2.getShouldAssertion()!!(context, emptyList(), vReside2)
        assertEquals(1, vReside2.size)

        val returnRule1 = FunctionsRuleBuilder(graph).should().haveReturnType(String::class)
        val vReturn1 = mutableListOf<String>()
        returnRule1.getShouldAssertion()!!(context, emptyList(), vReturn1)
        assertTrue(vReturn1.isEmpty())

        val returnRule2 = FunctionsRuleBuilder(graph).should().haveReturnType(Int::class)
        val vReturn2 = mutableListOf<String>()
        returnRule2.getShouldAssertion()!!(context, emptyList(), vReturn2)
        assertEquals(1, vReturn2.size)

        val returnRule3 = FunctionsRuleBuilder(graph).should().haveReturnTypeOf<String>()
        val vReturn3 = mutableListOf<String>()
        returnRule3.getShouldAssertion()!!(context, emptyList(), vReturn3)
        assertTrue(vReturn3.isEmpty())

        val paramRule1 = FunctionsRuleBuilder(graph).should().haveParameterTypes(Int::class)
        val vParam1 = mutableListOf<String>()
        paramRule1.getShouldAssertion()!!(context, emptyList(), vParam1)
        assertTrue(vParam1.isEmpty())

        val paramRule2 = FunctionsRuleBuilder(graph).should().haveParameterTypes(String::class)
        val vParam2 = mutableListOf<String>()
        paramRule2.getShouldAssertion()!!(context, emptyList(), vParam2)
        assertEquals(1, vParam2.size)

        val anyParamRule1 = FunctionsRuleBuilder(graph).should().haveAnyParameterType(Int::class)
        val vAnyParam1 = mutableListOf<String>()
        anyParamRule1.getShouldAssertion()!!(context, emptyList(), vAnyParam1)
        assertTrue(vAnyParam1.isEmpty())

        val anyParamRule2 = FunctionsRuleBuilder(graph).should().haveAnyParameterType(String::class)
        val vAnyParam2 = mutableListOf<String>()
        anyParamRule2.getShouldAssertion()!!(context, emptyList(), vAnyParam2)
        assertEquals(1, vAnyParam2.size)

        val anyParamRule3 = FunctionsRuleBuilder(graph).should().haveAnyParameterTypeOf<Int>()
        val vAnyParam3 = mutableListOf<String>()
        anyParamRule3.getShouldAssertion()!!(context, emptyList(), vAnyParam3)
        assertTrue(vAnyParam3.isEmpty())

        val refUsage =
            SourceUsage(
                kind = UsageKind.CLASS_REFERENCE,
                targetFqName = "com.example.Context",
                filePath = "/src/Processor.kt",
                line = 12,
                column = 5,
            )
        val contextWithRef = context.copy(usages = listOf(refUsage))
        val refRule = FunctionsRuleBuilder(graph).should().notReferenceClass("com.example.Context")
        val vRef = mutableListOf<String>()
        refRule.getShouldAssertion()!!(contextWithRef, emptyList(), vRef)
        assertEquals(1, vRef.size)

        val anyOfRule =
            FunctionsRuleBuilder(graph).should().anyOf(
                { resideInAPackage("com.example..") },
                { resideInAPackage("other..") },
            )
        val vAnyOf = mutableListOf<String>()
        anyOfRule.getShouldAssertion()!!(context, emptyList(), vAnyOf)
        assertTrue(vAnyOf.isEmpty())
    }

    @Test
    fun `test FunctionsRuleBuilder printMatchedFunctions printAllFunctions and allowEmpty`() {
        var matchedCount = 0
        var allCount = 0

        val func =
            FunctionDeclaration(
                "func",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fileDecl = FileDeclaration("Service.kt", "com.example", topLevelFunctions = listOf(func))
        val mockModule = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), listOf(fileDecl))
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val builder =
            FunctionsRuleBuilder(graph)
                .printMatchedFunctions { matchedCount++ }
                .printAllFunctions { allCount++ }

        assertEquals(1, allCount)

        val funcCtx = FunctionDeclarationContext(func, "com.example", null, ":app", "/src/Service.kt")
        val violations = mutableListOf<String>()
        builder.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violations)
        assertEquals(1, matchedCount)

        val emptyGraph = ProjectGraph(emptyMap())
        assertThrows(AssertionError::class.java) {
            FunctionsRuleBuilder(
                emptyGraph,
            ).that().haveNameMatching("nonexistent").should().haveNameEndingWith("Func").check()
        }

        FunctionsRuleBuilder(
            emptyGraph,
        ).allowEmpty().that().haveNameMatching("nonexistent").should().haveNameEndingWith("Func").check()
    }
}

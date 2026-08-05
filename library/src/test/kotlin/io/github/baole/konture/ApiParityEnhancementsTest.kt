/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiParityEnhancementsTest : RuleBuildersTestBase() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        ProjectGraph.setDefault(projectGraph)
    }

    @Test
    fun `test ModulesThat resideInAModule and containPackage`() {
        val builder1 = ModulesRuleBuilder(projectGraph)
        builder1.that().resideInAModule(":moduleA")
        assertTrue(builder1.getThatPredicate()!!(moduleA))

        val builder2 = ModulesRuleBuilder(projectGraph)
        builder2.that().containPackage("com.example..")
        assertTrue(builder2.getThatPredicate()!!(moduleA))

        val builder3 = ModulesRuleBuilder(projectGraph)
        builder3.that().resideInAPackage("com.nonexistent")
        assertFalse(builder3.getThatPredicate()!!(moduleA))
    }

    @Test
    fun `test ModulesShould notCall and notReferenceClass`() {
        val fileWithUsage =
            moduleA.files.first().copy(
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "com.other.Service.execute",
                            rawExpression = "execute()",
                            filePath = "/src/ClassA.kt",
                            line = 10,
                            column = 5,
                        ),
                        SourceUsage(
                            kind = UsageKind.CLASS_REFERENCE,
                            targetFqName = "com.other.CoreModel",
                            rawExpression = "CoreModel",
                            filePath = "/src/ClassA.kt",
                            line = 12,
                            column = 5,
                        ),
                    ),
            )
        val testModule = moduleA.copy(files = listOf(fileWithUsage))

        val violationsCall = mutableListOf<String>()
        val builderCall = ModulesRuleBuilder(projectGraph)
        builderCall.should().notCall("com.other.Service.execute")
        builderCall.getShouldAssertion()!!(testModule, projectGraph, violationsCall)
        assertFalse(violationsCall.isEmpty())

        val violationsRef = mutableListOf<String>()
        val builderRef = ModulesRuleBuilder(projectGraph)
        builderRef.should().notReferenceClass("com.other.CoreModel")
        builderRef.getShouldAssertion()!!(testModule, projectGraph, violationsRef)
        assertFalse(violationsRef.isEmpty())
    }

    @Test
    fun `test SlicesShould notCall and notReferenceClass`() {
        val fileWithUsage =
            moduleA.files.first().copy(
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "com.other.Service.execute",
                            rawExpression = "execute()",
                            filePath = "/src/ClassA.kt",
                            line = 10,
                            column = 5,
                        ),
                    ),
            )
        val modA = moduleA.copy(files = listOf(fileWithUsage))
        val graph = ProjectGraph(mapOf(":" to listOf(modA, moduleB, moduleC)))

        val builder =
            SlicesRuleBuilder(graph)
                .matching("com.(*)..")
                .should()
                .notCall("com.other.Service.execute")

        assertThrows(AssertionError::class.java) {
            builder.check()
        }
    }

    @Test
    fun `test FilesShould package and module dependency assertions`() {
        val fileDecl =
            FileDeclaration(
                name = "TestFile.kt",
                packageName = "com.example",
                imports = listOf("com.other.ClassC"),
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CLASS_REFERENCE,
                            targetFqName = "com.other.ClassC",
                            rawExpression = "ClassC",
                            filePath = "/src/TestFile.kt",
                            line = 5,
                            column = 1,
                        ),
                    ),
            )

        val fileCtx =
            FileDeclarationContext(
                declaration = fileDecl,
                modulePath = ":moduleA",
                sourceSet = null,
            )

        val violationsOkPkg = mutableListOf<String>()
        val builderOkPkg = FilesRuleBuilder(projectGraph)
        builderOkPkg.should().onlyDependOnPackages("com.example..", "com.other..")
        builderOkPkg.getShouldAssertion()!!(fileCtx, emptyList(), violationsOkPkg)
        assertTrue(violationsOkPkg.isEmpty())

        val violationsNotPkg = mutableListOf<String>()
        val builderNotPkg = FilesRuleBuilder(projectGraph)
        builderNotPkg.should().notDependOnPackages("com.other..")
        builderNotPkg.getShouldAssertion()!!(fileCtx, emptyList(), violationsNotPkg)
        assertFalse(violationsNotPkg.isEmpty())
    }

    @Test
    fun `test Konture moduleScopeFromModule`() {
        val scopeA = Konture.moduleScopeFromModule(":moduleA")
        assertEquals(1, scopeA.modules.size)
        assertEquals(":moduleA", scopeA.modules.first().path)

        val scopeAll = Konture.moduleScopeFromModule(":module*")
        assertEquals(3, scopeAll.modules.size)
    }
}

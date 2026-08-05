/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FilesFluentTest : KontureScopeTestFixture() {
    private lateinit var projectGraph: ProjectGraph

    @BeforeEach
    fun initGraph() {
        val module = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), listOf(fileA, fileB, fileC))
        projectGraph = ProjectGraph(mapOf(":" to listOf(module)))
        ProjectGraph.setDefault(projectGraph)
    }

    @Test
    fun `test FilesRuleBuilder that and should fluent extensions`() {
        val rule =
            FilesRuleBuilder(projectGraph)
                .that { name.startsWith("ClassA") }
                .should {
                    check(packageName == "com.example", "Wrong package")
                }

        val fileCtxA = FileDeclarationContext(fileA, ":app")
        val fileCtxB = FileDeclarationContext(fileB, ":app")

        val thatPred = rule.getThatPredicate()!!
        assertTrue(thatPred(fileCtxA))
        assertFalse(thatPred(fileCtxB))

        val violations = mutableListOf<String>()
        rule.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test FileDeclarationShouldContext helper methods`() {
        val fileWithImports =
            FileDeclaration(
                name = "Test.kt",
                packageName = "com.example",
                imports = listOf("com.example.service.UserService", "java.util.*"),
                classes = listOf(classA),
                filePath = "/src/Test.kt",
            )
        val fileCtx = FileDeclarationContext(fileWithImports, ":app")
        val violations = mutableListOf<String>()
        val context = FileDeclarationShouldContext(fileCtx, listOf(fileCtx), violations)

        assertEquals("Test.kt", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals(fileWithImports.imports, context.imports)
        assertEquals(listOf(classA), context.classes)
        assertTrue(context.topLevelFunctions.isEmpty())
        assertTrue(context.topLevelProperties.isEmpty())
        assertEquals("/src/Test.kt", context.filePath)
        assertEquals(":app", context.modulePath)

        // addViolation and check
        context.addViolation("Explicit error")
        assertEquals(1, violations.size)
        assertEquals("Explicit error", violations[0])

        violations.clear()
        context.check(false)
        assertEquals(1, violations.size)

        violations.clear()
        context.check(false, "Custom check failed")
        assertEquals("Custom check failed", violations[0])

        // hasImport
        assertTrue(context.hasImport { it.endsWith("UserService") })
        assertFalse(context.hasImport { it.endsWith("Other") })

        // hasImportContaining
        assertTrue(context.hasImportContaining("service", "util"))
        assertFalse(context.hasImportContaining("none"))

        // containsClassWith
        assertTrue(context.containsClassWith { it.name == "ClassA" })
        assertFalse(context.containsClassWith { it.name == "ClassB" })

        // assertNoWildcardImports
        violations.clear()
        context.assertNoWildcardImports()
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("java.util.*"))

        // assertOnlyOneClassPerFile
        violations.clear()
        context.assertOnlyOneClassPerFile()
        assertTrue(violations.isEmpty())

        val multiClassFile = FileDeclaration("Multi.kt", "com.example", classes = listOf(classA, classB))
        val multiCtx = FileDeclarationContext(multiClassFile, ":app")
        val multiContext = FileDeclarationShouldContext(multiCtx, listOf(multiCtx), violations)
        multiContext.assertOnlyOneClassPerFile()
        assertEquals(1, violations.size)
    }

    @Test
    fun `test FileDeclarationContext helper extension functions and properties`() {
        val fileWithImports =
            FileDeclaration(
                "ClassA.kt",
                "com.example",
                imports = listOf("com.example.other.*", "java.util.List"),
                classes = listOf(classA),
            )
        val fileCtxA = FileDeclarationContext(fileWithImports, ":app")

        assertTrue(fileCtxA.hasImport { it == "com.example.other.*" || it == "java.util.List" })
        assertTrue(fileCtxA.hasImportContaining("other"))
        assertTrue(fileCtxA.containsClassWith { it.name == "ClassA" })

        assertEquals("ClassA.kt", fileCtxA.name)
        assertEquals("com.example", fileCtxA.packageName)
        assertEquals(fileWithImports.imports, fileCtxA.imports)
        assertEquals(listOf(classA), fileCtxA.classes)
        assertTrue(fileCtxA.topLevelFunctions.isEmpty())
        assertTrue(fileCtxA.topLevelProperties.isEmpty())
    }
}

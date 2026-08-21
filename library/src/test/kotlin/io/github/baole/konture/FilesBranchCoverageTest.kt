/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FilesBranchCoverageTest : RuleBuildersTestBase() {
    @Test
    fun `test FilesThat inModule and notInModule filter branches`() {
        val fileA =
            FileDeclaration(name = "UserRepo.kt", packageName = "com.example.repo", filePath = "/repo/UserRepo.kt")
        val fileB =
            FileDeclaration(
                name = "UserService.kt",
                packageName = "com.example.service",
                filePath = "/service/UserService.kt",
            )
        val fileC = FileDeclaration(name = "Util.kt", packageName = "com.example.util", filePath = "/util/Util.kt")

        val modA = Module(":", ":core:repo", "repo", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val modB = Module(":", ":app:service", "service", emptyList(), emptyList(), emptyList(), listOf(fileB))
        val graph = ProjectGraph(mapOf(":" to listOf(modA, modB)))

        val ctxA = FileDeclarationContext(fileA, ":core:repo", null)
        val ctxB = FileDeclarationContext(fileB, ":app:service", null)
        val ctxC = FileDeclarationContext(fileC, "", null)

        // inModule branches
        val pInModNoColon = FilesRuleBuilder(graph).that().inModule("core:repo").getThatPredicate()!!
        assertTrue(pInModNoColon(ctxA))
        assertFalse(pInModNoColon(ctxB))

        val pInModColon = FilesRuleBuilder(graph).that().inModule(":core:repo").getThatPredicate()!!
        assertTrue(pInModColon(ctxA))
        assertFalse(pInModColon(ctxB))

        val pInModGlob = FilesRuleBuilder(graph).that().inModule("**:service").getThatPredicate()!!
        assertFalse(pInModGlob(ctxA))
        assertFalse(pInModGlob(ctxB))

        val pInModEmpty = FilesRuleBuilder(graph).that().inModule("").getThatPredicate()!!
        assertFalse(pInModEmpty(ctxA))

        val pInModList =
            FilesRuleBuilder(
                graph,
            ).that().inModules(listOf("core:repo", ":app:service")).getThatPredicate()!!
        assertTrue(pInModList(ctxA))
        assertTrue(pInModList(ctxB))

        val pInModListNoColon = FilesRuleBuilder(graph).that().inModules(listOf("core:repo")).getThatPredicate()!!
        assertTrue(pInModListNoColon(ctxA))
        assertFalse(pInModListNoColon(ctxB))

        // notInModule branches
        val pNotInModNoColon = FilesRuleBuilder(graph).that().notInModule("core:repo").getThatPredicate()!!
        assertFalse(pNotInModNoColon(ctxA))
        assertTrue(pNotInModNoColon(ctxB))

        val pNotInModColon = FilesRuleBuilder(graph).that().notInModule(":core:repo").getThatPredicate()!!
        assertFalse(pNotInModColon(ctxA))
        assertTrue(pNotInModColon(ctxB))

        val pNotInModGlob = FilesRuleBuilder(graph).that().notInModule("**:repo").getThatPredicate()!!
        assertFalse(pNotInModGlob(ctxA))
        assertTrue(pNotInModGlob(ctxB))

        val pNotInModList =
            FilesRuleBuilder(
                graph,
            ).that().notInModules(listOf("core:repo", ":app:service")).getThatPredicate()!!
        assertFalse(pNotInModList(ctxA))
        assertFalse(pNotInModList(ctxB))

        val pNotInModListNoColon = FilesRuleBuilder(graph).that().notInModules(listOf("core:repo")).getThatPredicate()!!
        assertFalse(pNotInModListNoColon(ctxA))
        assertTrue(pNotInModListNoColon(ctxB))
    }

    @Test
    fun `test FilesShouldContentAssertions import and structure branches`() {
        val classOrder =
            ClassDeclaration(
                name = "Order",
                fqName = "com.example.service.Order",
                packageName = "com.example.service",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/OrderService.kt",
            )
        val fileWithImports =
            FileDeclaration(
                name = "OrderService.kt",
                packageName = "com.example.service",
                filePath = "/src/OrderService.kt",
                imports = listOf("com.example.model.Order", "com.example.util.*"),
                classes = listOf(classOrder),
            )
        val fileCtx = FileDeclarationContext(fileWithImports, ":app", null)
        val allFiles = listOf(fileCtx)
        val graph = ProjectGraph(emptyMap())

        // haveImportOf branches
        val vImpPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveImportOf("com.example.model.Order")
            .getShouldAssertion()!!(fileCtx, allFiles, vImpPass)
        assertTrue(vImpPass.isEmpty())

        val vImpFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveImportOf("com.example.model.User")
            .getShouldAssertion()!!(fileCtx, allFiles, vImpFail)
        assertEquals(1, vImpFail.size)

        // notHaveImportOf branches
        val vNotImpPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveImportOf("com.example.model.User")
            .getShouldAssertion()!!(fileCtx, allFiles, vNotImpPass)
        assertTrue(vNotImpPass.isEmpty())

        val vNotImpFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveImportOf("com.example.model.Order")
            .getShouldAssertion()!!(fileCtx, allFiles, vNotImpFail)
        assertEquals(1, vNotImpFail.size)

        // notContainWildcardImports
        val vWildcardFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainWildcardImports()
            .getShouldAssertion()!!(fileCtx, allFiles, vWildcardFail)
        assertEquals(1, vWildcardFail.size)

        // containClass / notContainClass
        val vContainPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass("Order")
            .getShouldAssertion()!!(fileCtx, allFiles, vContainPass)
        assertTrue(vContainPass.isEmpty())

        val vContainFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass("NonExistent")
            .getShouldAssertion()!!(fileCtx, allFiles, vContainFail)
        assertEquals(1, vContainFail.size)

        val vNotContainPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainClass("NonExistent")
            .getShouldAssertion()!!(fileCtx, allFiles, vNotContainPass)
        assertTrue(vNotContainPass.isEmpty())

        val vNotContainFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainClass("Order")
            .getShouldAssertion()!!(fileCtx, allFiles, vNotContainFail)
        assertEquals(1, vNotContainFail.size)
    }
}

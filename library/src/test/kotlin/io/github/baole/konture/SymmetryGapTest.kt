/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class SymmetryGapTest {

    private fun testGraph(): ProjectGraph {
        val cls = ClassDeclaration(
            name = "BaseService",
            fqName = "io.github.baole.konture.BaseService",
            packageName = "io.github.baole.konture",
            isInterface = false,
            isAbstract = true,
            annotations = emptyList(),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/BaseService.kt",
            modifiers = setOf(Modifier.OPEN, Modifier.ABSTRACT),
        )

        val overrideFunc = FunctionDeclaration(
            name = "execute",
            visibility = Visibility.PUBLIC,
            modifiers = setOf(Modifier.OVERRIDE, Modifier.OPEN),
            returnType = "Unit",
            parameters = emptyList(),
            annotations = emptyList(),
            kdocText = null,
            isExtension = false,
        )

        val overrideProp = PropertyDeclaration(
            name = "tag",
            type = "String",
            visibility = Visibility.PUBLIC,
            modifiers = setOf(Modifier.OVERRIDE, Modifier.OPEN),
            isVal = true,
            annotations = emptyList(),
            kdocText = null,
            isExtension = false,
        )

        val file = FileDeclaration(
            name = "BaseService.kt",
            packageName = "io.github.baole.konture",
            classes = listOf(cls),
            topLevelFunctions = listOf(overrideFunc),
            topLevelProperties = listOf(overrideProp),
            filePath = "/src/BaseService.kt",
        )

        val mod = Module(":", ":core", "core", emptyList(), emptyList(), emptyList(), listOf(file))
        return ProjectGraph(mapOf(":" to listOf(mod)))
    }

    @Test
    fun `ClassesShould beOpen, beTopLevel work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            ClassesRuleBuilder(graph)
                .that().areTopLevel()
                .and().areOpen()
                .should().beOpen()
                .andShould().beTopLevel()
                .check()
        }
    }

    @Test
    fun `FunctionsShould beOverride works`() {
        val graph = testGraph()
        assertDoesNotThrow {
            FunctionsRuleBuilder(graph)
                .that().areOverride()
                .should().beOverride()
                .andShould().beOpen()
                .check()
        }
    }

    @Test
    fun `PropertiesShould beOpen, beOverride work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            PropertiesRuleBuilder(graph)
                .that().areOverride()
                .should().beOverride()
                .andShould().beOpen()
                .check()
        }
    }
}

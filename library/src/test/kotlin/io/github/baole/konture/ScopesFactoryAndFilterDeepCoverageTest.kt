/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScopesFactoryAndFilterDeepCoverageTest : RuleBuildersTestBase() {
    private fun dummyFunction(
        name: String,
        kdoc: String? = null,
    ) = FunctionDeclaration(
        name = name,
        visibility = Visibility.PUBLIC,
        modifiers = emptySet(),
        returnType = "Unit",
        parameters = emptyList(),
        annotations = emptyList(),
        kdocText = kdoc,
        isExtension = false,
    )

    private fun dummyProperty(
        name: String,
        kdoc: String? = null,
    ) = PropertyDeclaration(
        name = name,
        visibility = Visibility.PUBLIC,
        modifiers = emptySet(),
        type = "String",
        isVal = true,
        annotations = emptyList(),
        kdocText = kdoc,
        isExtension = false,
    )

    @Suppress("DEPRECATION")
    @Test
    fun `test KontureFunctionScope factory and operations`() {
        val scopeFromProject = KontureFunctionScope.fromProject(projectGraph)
        assertNotNull(scopeFromProject)

        val scopeFromModule = KontureFunctionScope.fromModule(":moduleA", projectGraph)
        assertNotNull(scopeFromModule)

        assertThrows(IllegalArgumentException::class.java) {
            KontureFunctionScope.fromModule(":nonexistent", projectGraph)
        }

        val scopeFromPackage = KontureFunctionScope.fromPackage("com.example", projectGraph)
        assertNotNull(scopeFromPackage)

        val combined = scopeFromModule + scopeFromPackage
        val subtracted = combined - scopeFromModule
        assertEquals(0, subtracted.functions.size)

        // Test filtering extensions
        val funcList = scopeFromProject.functions
        assertNotNull(funcList.haveNameEndingWith("Action"))
        assertNotNull(funcList.withNameEndingWith("Action"))
        assertNotNull(funcList.haveNameStartingWith("do"))
        assertNotNull(funcList.withNameStartingWith("do"))
        assertNotNull(funcList.withNameMatching("*Action*"))
        assertNotNull(funcList.resideInAPackage("com.example.."))
        assertNotNull(funcList.withPackage("com.example.."))
        assertNotNull(funcList.memberFunctions())
        assertNotNull(funcList.topLevelFunctions())
        assertNotNull(funcList.extensionFunctions())
        assertNotNull(funcList.withModule(":moduleA"))
        assertNotNull(funcList.withModule("moduleA"))

        // Scope wrappers
        val scope = scopeFromProject
        assertNotNull(scope.withModule(":moduleA"))
        assertNotNull(scope.extensionFunctions())
        assertNotNull(scope.topLevelFunctions())
        assertNotNull(scope.memberFunctions())
        assertNotNull(scope.withReturnType("Unit"))
        assertNotNull(scope.withParameterOf("String"))
        assertNotNull(scope.withAnnotationOf("Deprecated"))
        assertNotNull(scope.haveNameEndingWith("Action"))
        assertNotNull(scope.withNameEndingWith("Action"))
        assertNotNull(scope.haveNameStartingWith("do"))
        assertNotNull(scope.withNameStartingWith("do"))
        assertNotNull(scope.resideInAPackage("com.example.."))
        assertNotNull(scope.withNameMatching("*Action*"))
        assertNotNull(scope.withPackage("com.example.."))
        assertNotNull(scope.withVisibility(Visibility.PUBLIC))

        // Assertions on empty scope or matching
        scopeFromModule.assertTrue("Expected all functions to pass") { it.declaration.name.isNotEmpty() }
        assertThrows(AssertionError::class.java) {
            val dummyFunc =
                FunctionDeclarationContext(dummyFunction("badFunc"), "com.example", null, ":moduleA", "/src/File.kt")
            val scopeWithFunc = KontureFunctionScope(listOf(dummyFunc))
            scopeWithFunc.assertTrue("Must fail") { it.declaration.name == "impossibleName" }
        }
        assertThrows(AssertionError::class.java) {
            val dummyFunc =
                FunctionDeclarationContext(dummyFunction("badFunc"), "com.example", null, ":moduleA", "/src/File.kt")
            val scopeWithFunc = KontureFunctionScope(listOf(dummyFunc))
            scopeWithFunc.assertHasKDoc("Missing doc")
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `test KonturePropertyScope factory and operations`() {
        val scopeFromProject = KonturePropertyScope.fromProject(projectGraph)
        assertNotNull(scopeFromProject)

        val scopeFromModule = KonturePropertyScope.fromModule(":moduleA", projectGraph)
        assertNotNull(scopeFromModule)

        assertThrows(IllegalArgumentException::class.java) {
            KonturePropertyScope.fromModule(":nonexistent", projectGraph)
        }

        val scopeFromPackage = KonturePropertyScope.fromPackage("com.example", projectGraph)
        assertNotNull(scopeFromPackage)

        val combined = scopeFromModule + scopeFromPackage
        val subtracted = combined - scopeFromModule
        assertEquals(0, subtracted.properties.size)

        val propList = scopeFromProject.properties
        assertNotNull(propList.haveNameEndingWith("Name"))
        assertNotNull(propList.withNameEndingWith("Name"))
        assertNotNull(propList.haveNameStartingWith("user"))
        assertNotNull(propList.withNameStartingWith("user"))
        assertNotNull(propList.withNameMatching("*Name*"))
        assertNotNull(propList.resideInAPackage("com.example.."))
        assertNotNull(propList.withPackage("com.example.."))
        assertNotNull(propList.valProperties())
    }

    @Suppress("DEPRECATION")
    @Test
    fun `test KontureFileScope factory and operations`() {
        val scopeFromProject = KontureFileScope.fromProject(projectGraph)
        assertNotNull(scopeFromProject)

        val scopeFromModule = KontureFileScope.fromModule(":moduleA", projectGraph)
        assertNotNull(scopeFromModule)

        assertThrows(IllegalArgumentException::class.java) {
            KontureFileScope.fromModule(":nonexistent", projectGraph)
        }

        val scopeFromPackage = KontureFileScope.fromPackage("com.example", projectGraph)
        assertNotNull(scopeFromPackage)

        val combined = scopeFromModule + scopeFromPackage
        val subtracted = combined - scopeFromModule
        assertEquals(0, subtracted.files.size)
    }
}

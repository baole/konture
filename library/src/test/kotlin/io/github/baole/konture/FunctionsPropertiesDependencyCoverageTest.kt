/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsPropertiesDependencyCoverageTest : KontureScopeTestFixture() {
    private fun FunctionsRuleBuilder.checkAssertions(
        func: FunctionDeclarationContext,
        all: List<FunctionDeclarationContext>,
        violations: MutableList<String>,
    ) {
        val assertion = this.getShouldAssertion() ?: return
        assertion(func, all, violations)
    }

    private fun PropertiesRuleBuilder.checkAssertions(
        prop: PropertyDeclarationContext,
        all: List<PropertyDeclarationContext>,
        violations: MutableList<String>,
    ) {
        val assertion = this.getShouldAssertion() ?: return
        assertion(prop, all, violations)
    }

    @Test
    fun `test FunctionsShouldDependencyAssertions all methods and branches`() {
        val paramType = ParameterDeclaration("p", "com.target.TargetType", false, emptyList())
        val usage = SourceUsage(UsageKind.CALL, "com.external.ExternalService.call", "/src/FunctionsFile.kt", 15, 1)
        val fileWithUsage =
            FileDeclaration(
                name = "FunctionsFile.kt",
                packageName = "com.example",
                filePath = "/src/FunctionsFile.kt",
            )
        val func =
            FunctionDeclaration(
                name = "doWork",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "com.other.ReturnType",
                parameters = listOf(paramType),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx =
            FunctionDeclarationContext(
                declaration = func,
                packageName = "com.example",
                className = null,
                modulePath = ":app",
                filePath = "/src/FunctionsFile.kt",
                usages = listOf(usage),
            )
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsage))),
                ),
            )

        // onlyDependOnPackages
        val vOnlyPass = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().onlyDependOnPackages(listOf("com.target..", "com.other..", "com.external.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vOnlyPass)
        assertTrue(vOnlyPass.isEmpty())

        val vOnlyPassVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().onlyDependOnPackages("com.target..", "com.other..", "com.external..")
            .checkAssertions(funcCtx, listOf(funcCtx), vOnlyPassVararg)
        assertTrue(vOnlyPassVararg.isEmpty())

        val vOnlyFail = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().onlyDependOnPackages(listOf("com.target.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vOnlyFail)
        assertEquals(1, vOnlyFail.size)

        // notDependOnPackages
        val vNotPass = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notDependOnPackages(listOf("com.forbidden.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vNotPass)
        assertTrue(vNotPass.isEmpty())

        val vNotPassVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notDependOnPackages("com.forbidden..")
            .checkAssertions(funcCtx, listOf(funcCtx), vNotPassVararg)
        assertTrue(vNotPassVararg.isEmpty())

        val vNotFail = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notDependOnPackages(listOf("com.target.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vNotFail)
        assertEquals(1, vNotFail.size)

        // dependOnPackages
        val vDepPass = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().dependOnPackages(listOf("com.target.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vDepPass)
        assertTrue(vDepPass.isEmpty())

        val vDepPassVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().dependOnPackages("com.target..")
            .checkAssertions(funcCtx, listOf(funcCtx), vDepPassVararg)
        assertTrue(vDepPassVararg.isEmpty())

        val vDepFail = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().dependOnPackages(listOf("com.missing.."))
            .checkAssertions(funcCtx, listOf(funcCtx), vDepFail)
        assertEquals(1, vDepFail.size)

        // dependOnPackageOf / onlyDependOnPackageOf / notDependOnPackageOf
        val vDepOfPass = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().dependOnPackageOf(String::class)
            .checkAssertions(funcCtx, listOf(funcCtx), vDepOfPass)
        assertEquals(1, vDepOfPass.size) // kotlin package is not in deps

        val vOnlyDepOfFail = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().onlyDependOnPackageOf(String::class)
            .checkAssertions(funcCtx, listOf(funcCtx), vOnlyDepOfFail)
        assertEquals(1, vOnlyDepOfFail.size)

        val vNotDepOfPass = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notDependOnPackageOf(String::class)
            .checkAssertions(funcCtx, listOf(funcCtx), vNotDepOfPass)
        assertTrue(vNotDepOfPass.isEmpty())
    }

    @Test
    fun `test PropertiesShouldDependencyAssertions all methods and branches`() {
        val usage = SourceUsage(UsageKind.CALL, "com.external.ExternalService.call", "/src/PropsFile.kt", 15, 1)
        val fileWithUsage =
            FileDeclaration(
                name = "PropsFile.kt",
                packageName = "com.example",
                filePath = "/src/PropsFile.kt",
            )
        val prop =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "com.other.PropertyType",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val propCtx =
            PropertyDeclarationContext(
                declaration = prop,
                packageName = "com.example",
                className = null,
                modulePath = ":app",
                filePath = "/src/PropsFile.kt",
                usages = listOf(usage),
            )
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsage))),
                ),
            )

        // onlyDependOnPackages
        val vOnlyPass = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().onlyDependOnPackages(listOf("com.other..", "com.external.."))
            .checkAssertions(propCtx, listOf(propCtx), vOnlyPass)
        assertTrue(vOnlyPass.isEmpty())

        val vOnlyPassVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().onlyDependOnPackages("com.other..", "com.external..")
            .checkAssertions(propCtx, listOf(propCtx), vOnlyPassVararg)
        assertTrue(vOnlyPassVararg.isEmpty())

        val vOnlyFail = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().onlyDependOnPackages(listOf("com.other.."))
            .checkAssertions(propCtx, listOf(propCtx), vOnlyFail)
        assertEquals(1, vOnlyFail.size)

        // notDependOnPackages
        val vNotPass = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notDependOnPackages(listOf("com.forbidden.."))
            .checkAssertions(propCtx, listOf(propCtx), vNotPass)
        assertTrue(vNotPass.isEmpty())

        val vNotPassVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notDependOnPackages("com.forbidden..")
            .checkAssertions(propCtx, listOf(propCtx), vNotPassVararg)
        assertTrue(vNotPassVararg.isEmpty())

        val vNotFail = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notDependOnPackages(listOf("com.other.."))
            .checkAssertions(propCtx, listOf(propCtx), vNotFail)
        assertEquals(1, vNotFail.size)

        // dependOnPackages
        val vDepPass = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().dependOnPackages(listOf("com.other.."))
            .checkAssertions(propCtx, listOf(propCtx), vDepPass)
        assertTrue(vDepPass.isEmpty())

        val vDepPassVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().dependOnPackages("com.other..")
            .checkAssertions(propCtx, listOf(propCtx), vDepPassVararg)
        assertTrue(vDepPassVararg.isEmpty())

        val vDepFail = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().dependOnPackages(listOf("com.missing.."))
            .checkAssertions(propCtx, listOf(propCtx), vDepFail)
        assertEquals(1, vDepFail.size)

        // dependOnPackageOf / onlyDependOnPackageOf / notDependOnPackageOf
        val vDepOfPass = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().dependOnPackageOf(String::class)
            .checkAssertions(propCtx, listOf(propCtx), vDepOfPass)
        assertEquals(1, vDepOfPass.size)

        val vOnlyDepOfFail = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().onlyDependOnPackageOf(String::class)
            .checkAssertions(propCtx, listOf(propCtx), vOnlyDepOfFail)
        assertEquals(1, vOnlyDepOfFail.size)

        val vNotDepOfPass = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notDependOnPackageOf(String::class)
            .checkAssertions(propCtx, listOf(propCtx), vNotDepOfPass)
        assertTrue(vNotDepOfPass.isEmpty())
    }
}

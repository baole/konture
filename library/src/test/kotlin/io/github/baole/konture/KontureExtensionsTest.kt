/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KontureExtensionsTest {
    private lateinit var graph: ProjectGraph

    @BeforeEach
    fun setUp() {
        val classA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
            )
        val funcA =
            FunctionDeclaration(
                name = "funcA",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val propA =
            PropertyDeclaration(
                name = "propA",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fileA =
            FileDeclaration(
                name = "ClassA.kt",
                packageName = "com.example",
                classes = listOf(classA),
                topLevelFunctions = listOf(funcA),
                topLevelProperties = listOf(propA),
            )
        val moduleA =
            Module(
                buildId = ":",
                path = ":submodule",
                projectDir = "submodule",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileA),
            )
        graph = ProjectGraph(mapOf(":" to listOf(moduleA)))
    }

    @Test
    fun `test Konture extensions entry points and block DSLs`() {
        ProjectGraph.setDefault(graph)
        val konture = Konture

        assertNotNull(konture.modules())
        assertNotNull(konture.classes())
        assertNotNull(konture.classes(SourceSets.production()))
        assertNotNull(konture.layeredArchitecture())
        assertNotNull(konture.functions())
        assertNotNull(konture.functions(SourceSets.production()))
        assertNotNull(konture.properties())
        assertNotNull(konture.properties(SourceSets.production()))
        assertNotNull(konture.files())
        assertNotNull(konture.files(SourceSets.production()))
        assertNotNull(konture.slices())
        assertNotNull(konture.slices(SourceSets.production()))

        // assertNoCycles
        konture.assertNoCycles()
        konture.assertNoCycles(includeTestConfigurations = false)

        // scopes
        assertNotNull(konture.scope)
        assertNotNull(konture.scope(SourceSets.production()))
        assertNotNull(konture.scopeFromModule(":submodule"))
        assertNotNull(konture.scopeFromModule(":submodule", SourceSets.production()))
        assertNotNull(konture.scopeFromPackage("com.example"))
        assertNotNull(konture.scopeFromPackage("com.example", SourceSets.production()))

        assertNotNull(konture.fileScope)
        assertNotNull(konture.fileScope(SourceSets.production()))
        assertNotNull(konture.fileScopeFromModule(":submodule"))
        assertNotNull(konture.fileScopeFromPackage("com.example"))

        assertNotNull(konture.functionScope)
        assertNotNull(konture.functionScope(SourceSets.production()))
        assertNotNull(konture.functionScopeFromModule(":submodule"))
        assertNotNull(konture.functionScopeFromPackage("com.example"))

        assertNotNull(konture.propertyScope)
        assertNotNull(konture.propertyScope(SourceSets.production()))
        assertNotNull(konture.propertyScopeFromModule(":submodule"))
        assertNotNull(konture.propertyScopeFromPackage("com.example"))

        // Block-based DSLs
        konture.modules {
            that().haveNamePath(":submodule").should().notDependOnModule(":core")
        }
        konture.classes {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.classes(SourceSets.production()) {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.functions {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.functions(SourceSets.production()) {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.properties {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.properties(SourceSets.production()) {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.files {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.files(SourceSets.production()) {
            that().resideInAPackage("com.example").should().resideInAPackage("com.example")
        }
        konture.slices {
            allowEmpty().matching("com.example.(*)..").should().beFreeOfCycles()
        }
        konture.slices(SourceSets.production()) {
            allowEmpty().matching("com.example.(*)..").should().beFreeOfCycles()
        }

        konture.architecture {
            classes { that().resideInAPackage("com.example").should().resideInAPackage("com.example") }
        }

        konture.layered {
            layer("Domain").definedBy("com.example")
        }
    }
}

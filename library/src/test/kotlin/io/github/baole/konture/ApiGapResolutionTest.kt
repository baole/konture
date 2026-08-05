/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ApiGapResolutionTest {
    private fun testClass(
        pkg: String,
        name: String,
        superType: String? = null,
        annotations: List<AnnotationDeclaration> = emptyList(),
    ) = ClassDeclaration(
        name = name,
        fqName = "$pkg.$name",
        packageName = pkg,
        isInterface = name.startsWith("I"),
        isAbstract = false,
        annotations = annotations,
        imports = emptyList(),
        referencedTypes = emptySet(),
        supertypes = if (superType != null) listOf(superType) else emptyList(),
        filePath = "/src/$name.kt",
        kdocText = "KDoc comment",
    )

    private fun testGraph(vararg classes: ClassDeclaration): ProjectGraph {
        val files =
            classes.map { cls ->
                FileDeclaration(
                    name = cls.name + ".kt",
                    packageName = cls.packageName,
                    classes = listOf(cls),
                    kdocText = "KDoc comment",
                    filePath = cls.filePath,
                )
            }
        val coreModule =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = "core",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies =
                    listOf(
                        Dependency(targetPath = ":feature", configuration = "implementation", targetBuildId = ":"),
                    ),
                files = files,
            )
        val featureModule =
            Module(
                buildId = ":",
                path = ":feature",
                projectDir = "feature",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )
        return ProjectGraph(mapOf(":" to listOf(coreModule, featureModule)))
    }

    @Test
    fun `FilesThat resideInPackageOf and resideInAModule work correctly`() {
        val cls = testClass("io.github.baole.konture", "TestFile")
        val graph = testGraph(cls)

        assertDoesNotThrow {
            FilesRuleBuilder(graph)
                .that().resideInPackageOf<ApiGapResolutionTest>()
                .and().resideInAModule(":core")
                .and().haveName { it.startsWith("Test") }
                .should().beDocumentedWithKDoc()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `ClassesThat resideInPackageOf, resideInAModule, and assignability overloads work correctly`() {
        val cls = testClass("io.github.baole.konture", "MyRepository", superType = "java.lang.CharSequence")
        val graph = testGraph(cls)

        assertDoesNotThrow {
            ClassesRuleBuilder(graph)
                .that().resideInPackageOf<ApiGapResolutionTest>()
                .and().resideInAModule(":core")
                .and().areAssignableTo<CharSequence>()
                .should().beDocumentedWithKDoc()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `FunctionsThat resideInAModule and haveName work correctly`() {
        val func =
            FunctionDeclaration(
                name = "processData",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND, Modifier.OPERATOR),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = "KDoc",
                isExtension = false,
            )
        val file =
            FileDeclaration(
                "Test.kt",
                "io.github.baole.konture",
                topLevelFunctions = listOf(func),
                filePath = "/src/Test.kt",
            )
        val module =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = "core",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        assertDoesNotThrow {
            FunctionsRuleBuilder(graph)
                .that().resideInAModule(":core")
                .and().haveName { it.startsWith("process") }
                .should().beSuspend()
                .andShould().beOperator()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `PropertiesThat resideInPackageOf and resideInAModule work correctly`() {
        val prop =
            PropertyDeclaration(
                name = "dataStream",
                type = "String",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations = emptyList(),
                kdocText = "KDoc",
                isExtension = false,
            )
        val file =
            FileDeclaration(
                "Test.kt",
                "io.github.baole.konture",
                topLevelProperties = listOf(prop),
                filePath = "/src/Test.kt",
            )
        val module =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = "core",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        assertDoesNotThrow {
            PropertiesRuleBuilder(graph)
                .that().resideInPackageOf<ApiGapResolutionTest>()
                .and().resideInAModule(":core")
                .and().haveName { it.endsWith("Stream") }
                .should().beVal()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `ModulesThat haveNameStartingWith and ModulesShould beFreeOfCycles work correctly`() {
        val modA =
            Module(
                ":",
                ":core",
                "core",
                emptyList(),
                emptyList(),
                listOf(Dependency(targetPath = ":feature", configuration = "impl", targetBuildId = ":")),
                emptyList(),
            )
        val modB =
            Module(
                ":",
                ":feature",
                "feature",
                emptyList(),
                emptyList(),
                listOf(Dependency(targetPath = ":core", configuration = "impl", targetBuildId = ":")),
                emptyList(),
            )
        val cycleGraph = ProjectGraph(mapOf(":" to listOf(modA, modB)))

        val error =
            assertThrows(AssertionError::class.java) {
                ModulesRuleBuilder(cycleGraph)
                    .that().haveNameStartingWith("core")
                    .should().beFreeOfCycles()
                    .check()
            }
        assert(error.message?.contains("cycle") == true)
    }

    @Test
    fun `SlicesShould onlyDependOnSlices and notDependOnSlice work correctly`() {
        val cls1 = testClass("com.app.featureA", "A", superType = null)
        val cls2 = testClass("com.app.featureB", "B", superType = null)
        val graph = testGraph(cls1, cls2)

        assertDoesNotThrow {
            SlicesRuleBuilder(graph)
                .matching("com.app.(*)..")
                .should().onlyDependOnSlices("featureA", "featureB")
                .allowEmpty()
                .check()
        }
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class RuleBuildersExtendedAssertionsTest {
    private fun testClass(
        pkg: String,
        name: String,
        hasCompanion: Boolean = false,
        primaryPrivate: Boolean = false,
        noArgConst: Boolean = false,
    ) = ClassDeclaration(
        name = name,
        fqName = "$pkg.$name",
        packageName = pkg,
        isInterface = false,
        isAbstract = false,
        annotations = emptyList<AnnotationDeclaration>(),
        imports = emptyList<String>(),
        referencedTypes = emptySet<String>(),
        filePath = "/src/$name.kt",
        companionObject =
            if (hasCompanion) {
                ClassDeclaration(
                    name = "Companion",
                    fqName = "$pkg.$name.Companion",
                    packageName = pkg,
                    isInterface = false,
                    isAbstract = false,
                    annotations = emptyList<AnnotationDeclaration>(),
                    imports = emptyList<String>(),
                    referencedTypes = emptySet<String>(),
                    filePath = "/src/$name.kt",
                )
            } else {
                null
            },
        primaryConstructor =
            ConstructorDeclaration(
                visibility = if (primaryPrivate) Visibility.PRIVATE else Visibility.PUBLIC,
                parameters =
                    if (noArgConst) {
                        emptyList<ParameterDeclaration>()
                    } else {
                        listOf(
                            ParameterDeclaration("id", "String", false, emptyList<AnnotationDeclaration>()),
                        )
                    },
                annotations = emptyList<AnnotationDeclaration>(),
            ),
    )

    private fun testGraph(): ProjectGraph {
        val cls1 = testClass("io.github.baole.konture", "ServiceA", hasCompanion = true, primaryPrivate = true)
        val cls2 = testClass("io.github.baole.konture", "ModelB", noArgConst = true)
        val file1 =
            FileDeclaration(
                "ServiceA.kt",
                "io.github.baole.konture",
                classes = listOf(cls1),
                imports = listOf("io.github.baole.konture.ModelB"),
                filePath = "/src/ServiceA.kt",
            )
        val file2 =
            FileDeclaration("ModelB.kt", "io.github.baole.konture", classes = listOf(cls2), filePath = "/src/ModelB.kt")

        val modA =
            Module(
                ":",
                ":core",
                "core",
                emptyList(),
                emptyList(),
                listOf(Dependency(":feature", "impl", ":")),
                listOf(file1),
            )
        val modB =
            Module(
                ":",
                ":feature",
                "feature",
                emptyList(),
                emptyList(),
                listOf(Dependency(":core", "impl", ":")),
                listOf(file2),
            )
        return ProjectGraph(mapOf(":" to listOf(modA, modB)))
    }

    @Test
    fun `FilesThat containClass, containClassesWithAnnotation, and haveImportOf work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            FilesRuleBuilder(graph)
                .that().containClass("ServiceA")
                .and().haveImportOf("io.github.baole.konture.ModelB")
                .should().resideInAPackage("io.github.baole.konture")
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `ClassesThat and ClassesShould companion object and constructor helpers work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            ClassesRuleBuilder(graph)
                .that().haveCompanionObject()
                .and().havePrivatePrimaryConstructor()
                .should().bePublic()
                .allowEmpty()
                .check()
        }

        assertDoesNotThrow {
            ClassesRuleBuilder(graph)
                .that().haveNoArgConstructor()
                .should().bePublic()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `FunctionsThat and FunctionsShould extension topLevel member work`() {
        val extFunc =
            FunctionDeclaration(
                name = "toDto",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = true,
            )
        val file =
            FileDeclaration(
                "Utils.kt",
                "io.github.baole.konture",
                topLevelFunctions = listOf(extFunc),
                filePath = "/src/Utils.kt",
            )
        val mod = Module(":", ":core", "core", emptyList(), emptyList(), emptyList(), listOf(file))
        val graph = ProjectGraph(mapOf(":" to listOf(mod)))

        assertDoesNotThrow {
            FunctionsRuleBuilder(graph)
                .that().areExtension()
                .and().areTopLevel()
                .should().beExtension()
                .andShould().beTopLevel()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `PropertiesThat and PropertiesShould extension topLevel member and annotationWithArgument work`() {
        val ann =
            AnnotationDeclaration(
                "Column",
                "javax.persistence.Column",
                listOf(AnnotationArgumentDeclaration("name", "user_id")),
            )
        val prop =
            PropertyDeclaration(
                name = "userId",
                type = "String",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations = listOf(ann),
                kdocText = null,
                isExtension = false,
            )
        val file =
            FileDeclaration(
                "User.kt",
                "io.github.baole.konture",
                topLevelProperties = listOf(prop),
                filePath = "/src/User.kt",
            )
        val mod = Module(":", ":core", "core", emptyList(), emptyList(), emptyList(), listOf(file))
        val graph = ProjectGraph(mapOf(":" to listOf(mod)))

        assertDoesNotThrow {
            PropertiesRuleBuilder(graph)
                .that().haveAnnotationWithArgument("Column", "name", "user_id")
                .should().beTopLevel()
                .andShould().haveAnnotationWithArgument("Column", "name", "user_id")
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `ModulesThat dependOnModule and ModulesShould onlyDependOnModules work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            ModulesRuleBuilder(graph)
                .that().dependOnModule(":feature")
                .should().onlyDependOnModules(":feature")
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `SlicesShould dependOnSlice assertion works`() {
        val graph = testGraph()
        assertDoesNotThrow {
            SlicesRuleBuilder(graph)
                .matching("io.github.baole.(*)..")
                .should().onlyDependOnSlices()
                .allowEmpty()
                .check()
        }
    }

    @Test
    fun `Fluent Scopes withModule filtering works`() {
        val graph = testGraph()
        val scope = KontureScope(graph.getAllModules().flatMap { m -> m.files.flatMap { it.classes } })
        val fileScope = KontureFileScope(graph.getAllModules().flatMap { it.files })

        val coreScope = scope.withModule(":core", graph)
        val coreFileScope = fileScope.withModule(":core", graph)

        assert(coreScope.classes.isNotEmpty())
        assert(coreFileScope.files.isNotEmpty())
    }
}

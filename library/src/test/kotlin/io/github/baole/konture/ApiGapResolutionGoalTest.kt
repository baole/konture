/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ApiGapResolutionGoalTest {
    private fun testGraph(): ProjectGraph {
        val ann =
            AnnotationDeclaration(
                "Entity",
                "javax.persistence.Entity",
                listOf(AnnotationArgumentDeclaration("table", "users")),
            )
        val innerCls =
            ClassDeclaration(
                name = "InnerHelper",
                fqName = "io.github.baole.konture.User.InnerHelper",
                packageName = "io.github.baole.konture",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/User.kt",
                modifiers = setOf(Modifier.INNER),
            )
        val userCls =
            ClassDeclaration(
                name = "User",
                fqName = "io.github.baole.konture.User",
                packageName = "io.github.baole.konture",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(ann),
                imports = listOf("kotlin.collections.List"),
                referencedTypes = emptySet(),
                filePath = "/src/User.kt",
            )
        val topFunc =
            FunctionDeclaration(
                name = "parseUser",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "User",
                parameters = emptyList(),
                annotations = listOf(ann),
                kdocText = null,
                isExtension = false,
            )
        val topProp =
            PropertyDeclaration(
                name = "currentUser",
                type = "User",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations = listOf(ann),
                kdocText = null,
                isExtension = false,
            )
        val file =
            FileDeclaration(
                name = "User.kt",
                packageName = "io.github.baole.konture",
                imports = listOf("kotlin.collections.List"),
                classes = listOf(userCls, innerCls),
                topLevelFunctions = listOf(topFunc),
                topLevelProperties = listOf(topProp),
                filePath = "/src/User.kt",
            )
        val mod = Module(":", ":user", "user", emptyList(), emptyList(), emptyList(), listOf(file))
        return ProjectGraph(mapOf(":" to listOf(mod)))
    }

    @Test
    fun `FilesThat containTopLevelFunctions containTopLevelProperties containClasses work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            FilesRuleBuilder(graph)
                .that().containTopLevelFunctions()
                .and().containTopLevelProperties()
                .and().containClasses()
                .should().resideInAModule(":user")
                .andShould().containClass("User")
                .andShould().haveImportOf("kotlin.collections.List")
                .check()
        }
    }

    @Test
    fun `ClassesThat and ClassesShould inner class assertions work`() {
        val graph = testGraph()
        assertDoesNotThrow {
            ClassesRuleBuilder(graph)
                .that().areInner()
                .should().beInner()
                .check()
        }
    }

    @Test
    fun `FunctionsShould haveAnnotationWithArgument works`() {
        val graph = testGraph()
        assertDoesNotThrow {
            FunctionsRuleBuilder(graph)
                .that().areTopLevel()
                .should().haveAnnotationWithArgument("Entity", "table", "users")
                .check()
        }
    }

    @Test
    fun `Fluent Scope extensions for file, function, and property work`() {
        val graph = testGraph()
        val fileScope = KontureFileScope(graph.getAllModules().flatMap { it.files })
        val funcScope =
            KontureFunctionScope(
                graph.getAllModules().flatMap { m ->
                    m.files.flatMap { f ->
                        f.topLevelFunctions.map { func ->
                            FunctionDeclarationContext(
                                declaration = func,
                                packageName = f.packageName,
                                className = null,
                                modulePath = m.path,
                                filePath = f.filePath,
                                sourceSet = f.sourceSets.firstOrNull(),
                            )
                        }
                    }
                },
            )
        val propScope =
            KonturePropertyScope(
                graph.getAllModules().flatMap { m ->
                    m.files.flatMap { f ->
                        f.topLevelProperties.map { prop ->
                            PropertyDeclarationContext(
                                declaration = prop,
                                packageName = f.packageName,
                                className = null,
                                modulePath = m.path,
                                filePath = f.filePath,
                                sourceSet = f.sourceSets.firstOrNull(),
                            )
                        }
                    }
                },
            )

        val matchedFileScope = fileScope.withImportOf("kotlin.collections.List").containingClass("User")
        val matchedFuncScope = funcScope.topLevelFunctions()
        val matchedPropScope = propScope.topLevelProperties().valProperties()

        assert(matchedFileScope.files.isNotEmpty())
        assert(matchedFuncScope.functions.isNotEmpty())
        assert(matchedPropScope.properties.isNotEmpty())
    }
}

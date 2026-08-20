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

class StructuredSatisfyTest : RuleBuildersTestBase() {
    @Test
    fun `classes satisfy with id and boolean predicate emits structured violation`() {
        val boolPredicate: (ClassDeclaration) -> Boolean = { cls -> cls.name.endsWith("Z") }
        val error =
            assertThrows(AssertionError::class.java) {
                ClassesRuleBuilder(projectGraph)
                    .that().resideInAPackage("com.example")
                    .should().satisfy(
                        id = "class.naming.suffix",
                        description = "Classes in com.example must end with Z",
                    ) { cls -> boolPredicate(cls) }
                    .check()
            }

        assertTrue(error.message!!.contains("Classes in com.example must end with Z"))
    }

    @Test
    fun `classes satisfy with SatisfyContext receiver supports custom addViolation`() {
        val contextPredicate: SatisfyContext<ClassDeclaration>.(ClassDeclaration) -> Boolean = { cls ->
            assertEquals("ClassA", subject.name)
            assertEquals("class.custom.context", id)
            assertEquals("Custom context check", description)
            assertNotNull(graph)
            addViolation("Custom violation for ${cls.fqName}")
            false
        }

        val error =
            assertThrows(AssertionError::class.java) {
                ClassesRuleBuilder(projectGraph)
                    .that().haveName("ClassA")
                    .should().satisfy(
                        id = "class.custom.context",
                        description = "Custom context check",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("Custom violation for com.example.ClassA"))
    }

    @Test
    fun `files satisfy with id and SatisfyContext receiver`() {
        val contextPredicate: SatisfyContext<FileDeclarationContext>.(FileDeclarationContext) -> Boolean = { f ->
            if (f.declaration.name == "ClassB.kt") {
                addViolation("File ${f.declaration.name} violates custom policy")
                false
            } else {
                true
            }
        }

        val error =
            assertThrows(AssertionError::class.java) {
                FilesRuleBuilder(projectGraph)
                    .should().satisfy(
                        id = "file.custom.rule",
                        description = "File check",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("File ClassB.kt violates custom policy"))
    }

    @Test
    fun `functions satisfy with id and SatisfyContext receiver`() {
        val funcDecl =
            FunctionDeclaration(
                name = "doStuff",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fileDecl = FileDeclaration("Func.kt", "com.example", topLevelFunctions = listOf(funcDecl))
        val mod = Module(":", ":mod", "mod", emptyList(), emptyList(), emptyList(), listOf(fileDecl))
        val g = ProjectGraph(mapOf(":" to listOf(mod)))

        val contextPredicate: SatisfyContext<FunctionDeclarationContext>.(FunctionDeclarationContext) -> Boolean = { fn ->
            assertEquals("doStuff", subject.declaration.name)
            assertEquals("function.naming.rule", id)
            addViolation("Function ${fn.declaration.name} is invalid")
            false
        }

        val error =
            assertThrows(AssertionError::class.java) {
                FunctionsRuleBuilder(g)
                    .should().satisfy(
                        id = "function.naming.rule",
                        description = "Function naming policy",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("Function doStuff is invalid"))
    }

    @Test
    fun `properties satisfy with id and SatisfyContext receiver`() {
        val propDecl =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fileDecl = FileDeclaration("Prop.kt", "com.example", topLevelProperties = listOf(propDecl))
        val mod = Module(":", ":mod", "mod", emptyList(), emptyList(), emptyList(), listOf(fileDecl))
        val g = ProjectGraph(mapOf(":" to listOf(mod)))

        val contextPredicate: SatisfyContext<PropertyDeclarationContext>.(PropertyDeclarationContext) -> Boolean = { prop ->
            assertEquals("myProp", subject.declaration.name)
            addViolation("Property ${prop.declaration.name} violates convention")
            false
        }

        val error =
            assertThrows(AssertionError::class.java) {
                PropertiesRuleBuilder(g)
                    .should().satisfy(
                        id = "property.custom.rule",
                        description = "Property policy",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("Property myProp violates convention"))
    }

    @Test
    fun `modules satisfy with id and SatisfyContext receiver`() {
        val contextPredicate: SatisfyContext<Module>.(Module) -> Boolean = { module ->
            assertEquals(":moduleA", subject.path)
            addViolation("Module ${module.path} failed checks")
            false
        }

        val error =
            assertThrows(AssertionError::class.java) {
                ModulesRuleBuilder(projectGraph)
                    .that().resideInModule(":moduleA")
                    .should().satisfy(
                        id = "module.structure.rule",
                        description = "Module structure check",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("Module :moduleA failed checks"))
    }

    @Test
    fun `slices satisfy with id and SatisfyContext receiver`() {
        val contextPredicate: SatisfyContext<List<Slice>>.(List<Slice>) -> Boolean = { _ ->
            assertNotNull(subject)
            assertEquals("slice.coupling.rule", id)
            addViolation("Slice graph violates coupling limit")
            false
        }

        val error =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(projectGraph)
                    .matching("com.(*)..")
                    .should().satisfy(
                        id = "slice.coupling.rule",
                        description = "Slice coupling check",
                        predicate = contextPredicate,
                    )
                    .check()
            }

        assertTrue(error.message!!.contains("Slice graph violates coupling limit"))
    }
}

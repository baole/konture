/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FluentExtensionsDslTest : KontureScopeTestFixture() {
    private lateinit var graph: ProjectGraph
    private lateinit var module: Module
    private lateinit var file1: FileDeclaration
    private lateinit var file2: FileDeclaration
    private lateinit var myClassA: ClassDeclaration
    private lateinit var myClassB: ClassDeclaration
    private lateinit var param1: ParameterDeclaration
    private lateinit var anno1: AnnotationDeclaration
    private lateinit var anno2: AnnotationDeclaration
    private lateinit var func1: FunctionDeclaration
    private lateinit var prop1: PropertyDeclaration
    private lateinit var fileCtx1: FileDeclarationContext
    private lateinit var funcCtx1: FunctionDeclarationContext
    private lateinit var propCtx1: PropertyDeclarationContext

    @org.junit.jupiter.api.BeforeEach
    fun initLocalFixture() {
        anno1 = AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")
        anno2 = AnnotationDeclaration("OtherAnnotation", "com.example.OtherAnnotation")
        param1 = ParameterDeclaration("param1", "String", hasDefaultValue = false, annotations = emptyList())

        myClassA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(anno1),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
            )
        myClassB =
            ClassDeclaration(
                name = "ClassB",
                fqName = "com.example.ClassB",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassB.kt",
            )

        func1 =
            FunctionDeclaration(
                name = "funcNormal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = listOf(param1),
                annotations = listOf(anno1, anno2),
                kdocText = "some function kdoc",
                isExtension = false,
            )
        prop1 =
            PropertyDeclaration(
                name = "propVal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = listOf(anno1),
                kdocText = "some property kdoc",
                isExtension = false,
            )

        file1 =
            FileDeclaration(
                name = "ClassA.kt",
                packageName = "com.example",
                imports = listOf("com.example.other.*", "java.util.List"),
                classes = listOf(myClassA),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                filePath = "/src/ClassA.kt",
            )
        file2 =
            FileDeclaration(
                name = "ClassB.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes = listOf(myClassB),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                filePath = "/src/ClassB.kt",
            )

        module =
            Module(
                buildId = "myBuild",
                path = ":submodule",
                projectDir = "/src/submodule",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file1, file2),
            )

        graph = ProjectGraph(mapOf("myBuild" to listOf(module)))

        fileCtx1 = FileDeclarationContext(file1, ":submodule")
        funcCtx1 = FunctionDeclarationContext(func1, "com.example", "ClassA", ":submodule", "/src/ClassA.kt")
        propCtx1 = PropertyDeclarationContext(prop1, "com.example", "ClassA", ":submodule", "/src/ClassA.kt")
    }

    @Test
    fun `test ModuleShouldContext`() {
        val violations = mutableListOf<String>()
        val context = ModuleShouldContext(module, graph, violations)

        assertEquals("myBuild", context.buildId)
        assertEquals(":submodule", context.path)
        assertEquals("/src/submodule", context.projectDir)
        assertEquals(listOf("kotlin"), context.appliedPlugins)
        assertTrue(context.sourceSets.isEmpty())
        assertTrue(context.dependencies.isEmpty())
        assertEquals(listOf(file1, file2), context.files)
        assertEquals(listOf(myClassA, myClassB), context.classes)

        context.addViolation("Module error")
        assertEquals(1, violations.size)
        assertEquals("Module error", violations[0])
    }

    @Test
    fun `test extra semantic extensions on models`() {
        assertTrue(myClassA.hasAnnotation("MyAnnotation"))
        assertTrue(myClassA.hasAllAnnotations("MyAnnotation"))
        assertTrue(myClassA.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))

        assertTrue(fileCtx1.hasImport { it.contains("java.util") })
        assertTrue(fileCtx1.hasImportContaining("other", "List"))
        assertTrue(fileCtx1.containsClassWith { it.name == "ClassA" })

        assertTrue(funcCtx1.hasAnnotation("MyAnnotation"))
        assertTrue(funcCtx1.hasAllAnnotations("MyAnnotation", "OtherAnnotation"))
        assertTrue(funcCtx1.hasAnyAnnotation("MyAnnotation", "Unknown"))

        assertTrue(propCtx1.hasAnnotation("MyAnnotation"))
        assertTrue(propCtx1.hasAllAnnotations("MyAnnotation"))
        assertTrue(propCtx1.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))
    }

    @Test
    fun `test field delegation extensions`() {
        assertEquals("ClassA.kt", fileCtx1.name)
        assertEquals("com.example", fileCtx1.packageName)
        assertEquals(listOf("com.example.other.*", "java.util.List"), fileCtx1.imports)
        assertEquals(listOf(myClassA), fileCtx1.classes)
        assertTrue(fileCtx1.topLevelFunctions.isEmpty())
        assertTrue(fileCtx1.topLevelProperties.isEmpty())

        assertEquals("funcNormal", funcCtx1.name)
        assertEquals(Visibility.PUBLIC, funcCtx1.visibility)
        assertTrue(funcCtx1.modifiers.isEmpty())
        assertEquals("Unit", funcCtx1.returnType)
        assertEquals(listOf(param1), funcCtx1.parameters)
        assertEquals(listOf(anno1, anno2), funcCtx1.annotations)
        assertFalse(funcCtx1.isExtension)
        assertEquals("some function kdoc", funcCtx1.kdocText)

        assertEquals("propVal", propCtx1.name)
        assertEquals(Visibility.PUBLIC, propCtx1.visibility)
        assertTrue(propCtx1.modifiers.isEmpty())
        assertEquals("String", propCtx1.type)
        assertTrue(propCtx1.isVal)
        assertFalse(propCtx1.isVar)
        assertEquals(listOf(anno1), propCtx1.annotations)
        assertFalse(propCtx1.isExtension)
        assertEquals("some property kdoc", propCtx1.kdocText)
    }

    @Test
    fun `test rule builders DSL should extension`() {
        val classBuilder =
            ClassesRuleBuilder(graph).should {
                assertAnnotationOf("MyAnnotation")
            }
        assertNotNull(classBuilder)

        val fileBuilder =
            FilesRuleBuilder(graph).should {
                assertNoWildcardImports()
            }
        assertNotNull(fileBuilder)

        val funcBuilder =
            FunctionsRuleBuilder(graph).should {
                assertAnnotationOf("MyAnnotation")
            }
        assertNotNull(funcBuilder)

        val propBuilder =
            PropertiesRuleBuilder(graph).should {
                assertAnnotationOf("MyAnnotation")
            }
        assertNotNull(propBuilder)

        val moduleBuilder =
            ModulesRuleBuilder(graph).should {
                addViolation("Module violated")
            }
        assertNotNull(moduleBuilder)
    }

    @Test
    fun `test rule builders DSL that extension`() {
        val classBuilder = ClassesRuleBuilder(graph).that { name == "ClassA" }
        assertNotNull(classBuilder)

        val fileBuilder = FilesRuleBuilder(graph).that { name == "ClassA.kt" }
        assertNotNull(fileBuilder)

        val funcBuilder = FunctionsRuleBuilder(graph).that { name == "funcNormal" }
        assertNotNull(funcBuilder)

        val propBuilder = PropertiesRuleBuilder(graph).that { name == "propVal" }
        assertNotNull(propBuilder)

        val moduleBuilder = ModulesRuleBuilder(graph).that { path == ":submodule" }
        assertNotNull(moduleBuilder)
    }

    @Test
    fun `test boolean should blocks enforce predicate result`() {
        val concreteClass =
            ClassDeclaration(
                name = "ConcreteRepo",
                fqName = "com.example.ConcreteRepo",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ConcreteRepo.kt",
            )
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to
                        listOf(
                            Module(
                                buildId = ":",
                                path = ":app",
                                projectDir = "app",
                                appliedPlugins = emptyList(),
                                sourceSets = emptyList(),
                                dependencies = emptyList(),
                                files =
                                    listOf(
                                        FileDeclaration(
                                            "ConcreteRepo.kt",
                                            "com.example",
                                            classes = listOf(concreteClass),
                                        ),
                                    ),
                            ),
                        ),
                ),
            )

        val rule =
            ClassesRuleBuilder(graph)
                .that { name.endsWith("Repo") }
                .should { isInterface }
        val violations = mutableListOf<String>()
        rule.getShouldAssertion()!!(concreteClass, listOf(concreteClass), violations)
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("failed custom assertion"))

        val interfaceClass = concreteClass.copy(name = "UserRepo", fqName = "com.example.UserRepo", isInterface = true)
        val interfaceViolations = mutableListOf<String>()
        rule.getShouldAssertion()!!(interfaceClass, listOf(interfaceClass), interfaceViolations)
        assertTrue(interfaceViolations.isEmpty())
    }

    @Test
    fun `test check helper in should block records custom message`() {
        val violations = mutableListOf<String>()
        val context =
            ClassDeclarationShouldContext(
                ClassDeclaration(
                    name = "BadName",
                    fqName = "com.example.BadName",
                    packageName = "com.example",
                    isInterface = false,
                    isAbstract = false,
                    annotations = emptyList(),
                    imports = emptyList(),
                    referencedTypes = emptySet(),
                    filePath = "/src/BadName.kt",
                ),
                emptyList(),
                violations,
            )
        context.check(false, "must end with ViewModel")
        assertEquals(1, violations.size)
        assertEquals("must end with ViewModel", violations[0])
    }

    @Test
    fun `test should overloads compile and work as expected`() {
        val testClass =
            ClassDeclaration(
                name = "MyClass",
                fqName = "com.example.MyClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyClass.kt",
            )
        val graph = ProjectGraph(emptyMap())

        val booleanRule =
            ClassesRuleBuilder(graph).should {
                isInterface
            }
        val violations1 = mutableListOf<String>()
        booleanRule.getShouldAssertion()!!(testClass, listOf(testClass), violations1)
        assertEquals(1, violations1.size)

        val unitRule =
            ClassesRuleBuilder(graph).should {
                check(isInterface, "Not interface")
            }
        val violations2 = mutableListOf<String>()
        unitRule.getShouldAssertion()!!(testClass, listOf(testClass), violations2)
        assertEquals(1, violations2.size)
        assertEquals("Not interface", violations2[0])
    }

    @Test
    fun `test should block throws on invalid return types`() {
        val testClass =
            ClassDeclaration(
                name = "MyClass",
                fqName = "com.example.MyClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyClass.kt",
            )
        val graph = ProjectGraph(emptyMap())

        val nullRule =
            ClassesRuleBuilder(graph).should {
                null
            }
        val violations = mutableListOf<String>()
        val exception1 =
            assertThrows(IllegalArgumentException::class.java) {
                nullRule.getShouldAssertion()!!(testClass, listOf(testClass), violations)
            }
        assertTrue(exception1.message!!.contains("A should { } block must return either a Boolean"))
        assertTrue(exception1.message!!.contains("null"))

        val listRule =
            ClassesRuleBuilder(graph).should {
                listOf(1, 2, 3)
            }
        val exception2 =
            assertThrows(IllegalArgumentException::class.java) {
                listRule.getShouldAssertion()!!(testClass, listOf(testClass), violations)
            }
        assertTrue(exception2.message!!.contains("A should { } block must return either a Boolean"))
        assertTrue(exception2.message!!.contains("java.util.Collections") || exception2.message!!.contains("ArrayList"))
    }
}

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(kotlin.experimental.ExperimentalTypeInference::class)

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FluentExtensionsTest {
    private lateinit var anno1: AnnotationDeclaration
    private lateinit var anno2: AnnotationDeclaration

    private lateinit var param1: ParameterDeclaration
    private lateinit var param2: ParameterDeclaration

    private lateinit var propVal: PropertyDeclaration
    private lateinit var propVar: PropertyDeclaration

    private lateinit var funcNormal: FunctionDeclaration
    private lateinit var funcExtension: FunctionDeclaration

    private lateinit var classA: ClassDeclaration
    private lateinit var classB: ClassDeclaration

    private lateinit var file1: FileDeclaration
    private lateinit var file2: FileDeclaration

    private lateinit var fileCtx1: FileDeclarationContext
    private lateinit var fileCtx2: FileDeclarationContext

    private lateinit var funcCtx1: FunctionDeclarationContext
    private lateinit var funcCtx2: FunctionDeclarationContext

    private lateinit var propCtx1: PropertyDeclarationContext
    private lateinit var propCtx2: PropertyDeclarationContext

    private lateinit var module: Module
    private lateinit var graph: ProjectGraph

    @BeforeEach
    fun setUp() {
        anno1 = AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")
        anno2 = AnnotationDeclaration("OtherAnnotation", "com.example.OtherAnnotation")

        param1 = ParameterDeclaration("param1", "String", hasDefaultValue = false, annotations = emptyList())
        param2 = ParameterDeclaration("param2", "Int", hasDefaultValue = false, annotations = emptyList())

        propVal =
            PropertyDeclaration(
                name = "propVal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = listOf(anno1),
                isExtension = false,
                kdocText = "some property kdoc",
            )
        propVar =
            PropertyDeclaration(
                name = "propVar",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.LATEINIT),
                type = "Int",
                isVal = false,
                annotations = emptyList(),
                isExtension = true,
                kdocText = null,
            )

        funcNormal =
            FunctionDeclaration(
                name = "funcNormal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = listOf(param1),
                annotations = listOf(anno1, anno2),
                isExtension = false,
                kdocText = "some function kdoc",
            )
        funcExtension =
            FunctionDeclaration(
                name = "funcExtension",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.SUSPEND, Modifier.OPEN),
                returnType = "Int",
                parameters = listOf(param2),
                annotations = emptyList(),
                isExtension = true,
                kdocText = null,
            )

        classA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(anno1),
                imports = listOf("com.example.other.*"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.DATA),
                supertypes = listOf("ParentType"),
                primaryConstructor = null,
                secondaryConstructors = emptyList(),
                functions = listOf(funcNormal),
                properties = listOf(propVal),
                companionObject = null,
                kdocText = "some class kdoc",
            )

        classB =
            ClassDeclaration(
                name = "ClassB",
                fqName = "com.example.ClassB",
                packageName = "com.example",
                isInterface = true,
                isAbstract = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassB.kt",
                visibility = Visibility.INTERNAL,
                modifiers = emptySet(),
                supertypes = emptyList(),
                primaryConstructor = null,
                secondaryConstructors = emptyList(),
                functions = listOf(funcExtension),
                properties = listOf(propVar),
                companionObject = null,
                kdocText = null,
            )

        file1 =
            FileDeclaration(
                name = "ClassA.kt",
                packageName = "com.example",
                imports = listOf("com.example.other.*", "java.util.List"),
                classes = listOf(classA),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                kdocText = "file A kdoc",
                filePath = "/src/ClassA.kt",
            )

        file2 =
            FileDeclaration(
                name = "ClassB.kt",
                packageName = "com.example",
                imports = listOf("java.util.Set"),
                classes = listOf(classB),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                kdocText = null,
                filePath = "/src/ClassB.kt",
            )

        fileCtx1 = FileDeclarationContext(file1, "submodule")
        fileCtx2 = FileDeclarationContext(file2, "submodule")

        funcCtx1 = FunctionDeclarationContext(funcNormal, "com.example", "ClassA", "submodule", "/src/ClassA.kt")
        funcCtx2 = FunctionDeclarationContext(funcExtension, "com.example", "ClassB", "submodule", "/src/ClassB.kt")

        propCtx1 = PropertyDeclarationContext(propVal, "com.example", "ClassA", "submodule", "/src/ClassA.kt")
        propCtx2 = PropertyDeclarationContext(propVar, "com.example", "ClassB", "submodule", "/src/ClassB.kt")

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

        graph =
            ProjectGraph(
                builds = mapOf("myBuild" to listOf(module)),
            )
    }

    @Test
    fun `test ClassDeclarationShouldContext properties and annotations`() {
        val violations = mutableListOf<String>()
        val contextA = ClassDeclarationShouldContext(classA, listOf(classA, classB), violations)

        assertEquals("ClassA", contextA.name)
        assertEquals("com.example.ClassA", contextA.fqName)
        assertEquals("com.example", contextA.packageName)
        assertFalse(contextA.isInterface)
        assertFalse(contextA.isAbstract)
        assertEquals(listOf(anno1), contextA.annotations)
        assertEquals(listOf("com.example.other.*"), contextA.imports)
        assertTrue(contextA.referencedTypes.isEmpty())
        assertEquals("/src/ClassA.kt", contextA.filePath)
        assertEquals(Visibility.PUBLIC, contextA.visibility)
        assertEquals(setOf(Modifier.DATA), contextA.modifiers)
        assertEquals(listOf("ParentType"), contextA.supertypes)
        assertNull(contextA.primaryConstructor)
        assertTrue(contextA.secondaryConstructors.isEmpty())
        assertEquals(listOf(funcNormal), contextA.functions)
        assertEquals(listOf(propVal), contextA.properties)
        assertNull(contextA.companionObject)
        assertEquals("some class kdoc", contextA.kdocText)

        // annotations checks
        assertTrue(contextA.hasAnnotation("MyAnnotation"))
        assertTrue(contextA.hasAnnotation("com.example.MyAnnotation"))
        assertFalse(contextA.hasAnnotation("OtherAnnotation"))

        assertTrue(contextA.hasAllAnnotations("MyAnnotation"))
        assertTrue(contextA.hasAllAnnotations(listOf("MyAnnotation")))
        assertFalse(contextA.hasAllAnnotations("MyAnnotation", "OtherAnnotation"))

        assertTrue(contextA.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))
        assertTrue(contextA.hasAnyAnnotation(listOf("MyAnnotation", "OtherAnnotation")))
        assertFalse(contextA.hasAnyAnnotation("OtherAnnotation"))

        // assert annotations
        contextA.assertAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        contextA.assertAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("should be annotated with @OtherAnnotation"))

        violations.clear()
        contextA.assertAllAnnotationsOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        contextA.assertAllAnnotationsOf("MyAnnotation", "OtherAnnotation")
        assertEquals(1, violations.size)

        violations.clear()
        contextA.assertAnyAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        contextA.assertAnyAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)
    }

    @Test
    fun `test FileDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context1 = FileDeclarationShouldContext(fileCtx1, listOf(fileCtx1, fileCtx2), violations)

        assertEquals(file1, context1.declaration)
        assertEquals("ClassA.kt", context1.name)
        assertEquals("com.example", context1.packageName)
        assertEquals(listOf("com.example.other.*", "java.util.List"), context1.imports)
        assertEquals(listOf(classA), context1.classes)
        assertTrue(context1.topLevelFunctions.isEmpty())
        assertTrue(context1.topLevelProperties.isEmpty())
        assertEquals("file A kdoc", context1.kdocText)
        assertEquals("/src/ClassA.kt", context1.filePath)
        assertEquals("submodule", context1.modulePath)

        // add violation
        context1.addViolation("Custom violation")
        assertEquals(1, violations.size)
        assertEquals("Custom violation", violations[0])

        violations.clear()
        assertTrue(context1.hasImport { it.contains("java.util") })
        assertFalse(context1.hasImport { it.contains("kotlinx") })

        assertTrue(context1.hasImportContaining("other", "List"))
        assertFalse(context1.hasImportContaining("set", "map"))

        assertTrue(context1.containsClassWith { it.name == "ClassA" })
        assertFalse(context1.containsClassWith { it.name == "ClassB" })

        // assertNoWildcardImports
        context1.assertNoWildcardImports()
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("should not contain wildcard imports"))

        violations.clear()
        val context2 = FileDeclarationShouldContext(fileCtx2, listOf(fileCtx1, fileCtx2), violations)
        context2.assertNoWildcardImports()
        assertTrue(violations.isEmpty())

        // assertOnlyOneClassPerFile
        context1.assertOnlyOneClassPerFile()
        assertTrue(violations.isEmpty())

        val multiClassFile = FileDeclaration("Multi.kt", "com.example", classes = listOf(classA, classB))
        val multiCtx = FileDeclarationContext(multiClassFile, "submodule")
        val contextMulti = FileDeclarationShouldContext(multiCtx, listOf(multiCtx), violations)
        contextMulti.assertOnlyOneClassPerFile()
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("should contain at most one class, but contains 2"))
    }

    @Test
    fun `test FunctionDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context1 = FunctionDeclarationShouldContext(funcCtx1, listOf(funcCtx1, funcCtx2), violations)

        assertEquals(funcNormal, context1.declaration)
        assertEquals("funcNormal", context1.name)
        assertEquals("com.example", context1.packageName)
        assertEquals("ClassA", context1.className)
        assertEquals("submodule", context1.modulePath)
        assertEquals("/src/ClassA.kt", context1.filePath)
        assertEquals(Visibility.PUBLIC, context1.visibility)
        assertTrue(context1.modifiers.isEmpty())
        assertEquals("Unit", context1.returnType)
        assertEquals(listOf(param1), context1.parameters)
        assertEquals(listOf(anno1, anno2), context1.annotations)
        assertEquals("some function kdoc", context1.kdocText)
        assertFalse(context1.isExtension)

        context1.addViolation("Function error")
        assertEquals(1, violations.size)

        violations.clear()
        assertTrue(context1.hasAnnotation("MyAnnotation"))
        assertTrue(context1.hasAnnotation("OtherAnnotation"))
        assertFalse(context1.hasAnnotation("Unknown"))

        assertTrue(context1.hasAllAnnotations("MyAnnotation", "OtherAnnotation"))
        assertTrue(context1.hasAllAnnotations(listOf("MyAnnotation", "OtherAnnotation")))
        assertFalse(context1.hasAllAnnotations("MyAnnotation", "Unknown"))

        assertTrue(context1.hasAnyAnnotation("MyAnnotation", "Unknown"))
        assertTrue(context1.hasAnyAnnotation(listOf("MyAnnotation", "Unknown")))
        assertFalse(context1.hasAnyAnnotation("Unknown"))

        // asserts
        context1.assertAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        context1.assertAnnotationOf("Unknown")
        assertEquals(1, violations.size)

        violations.clear()
        context1.assertAllAnnotationsOf("MyAnnotation", "OtherAnnotation")
        assertTrue(violations.isEmpty())
        context1.assertAllAnnotationsOf("MyAnnotation", "Unknown")
        assertEquals(1, violations.size)

        violations.clear()
        context1.assertAnyAnnotationOf("MyAnnotation", "Unknown")
        assertTrue(violations.isEmpty())
        context1.assertAnyAnnotationOf("Unknown")
        assertEquals(1, violations.size)

        // parameters matching
        violations.clear()
        context1.noneParameterMatches("must not have Int params") { it.type == "Int" }
        assertTrue(violations.isEmpty())
        context1.noneParameterMatches("must not have String params") { it.type == "String" }
        assertEquals(1, violations.size)

        violations.clear()
        context1.anyParameterMatches("must have String params") { it.type == "String" }
        assertTrue(violations.isEmpty())
        context1.anyParameterMatches("must have Int params") { it.type == "Int" }
        assertEquals(1, violations.size)
    }

    @Test
    fun `test PropertyDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context1 = PropertyDeclarationShouldContext(propCtx1, listOf(propCtx1, propCtx2), violations)

        assertEquals(propVal, context1.declaration)
        assertEquals("propVal", context1.name)
        assertEquals("com.example", context1.packageName)
        assertEquals("ClassA", context1.className)
        assertEquals("submodule", context1.modulePath)
        assertEquals("/src/ClassA.kt", context1.filePath)
        assertEquals(Visibility.PUBLIC, context1.visibility)
        assertTrue(context1.modifiers.isEmpty())
        assertEquals("String", context1.type)
        assertTrue(context1.isVal)
        assertFalse(context1.isVar)
        assertEquals(listOf(anno1), context1.annotations)
        assertEquals("some property kdoc", context1.kdocText)
        assertFalse(context1.isExtension)

        context1.addViolation("Property error")
        assertEquals(1, violations.size)

        violations.clear()
        assertTrue(context1.hasAnnotation("MyAnnotation"))
        assertFalse(context1.hasAnnotation("OtherAnnotation"))

        assertTrue(context1.hasAllAnnotations("MyAnnotation"))
        assertTrue(context1.hasAllAnnotations(listOf("MyAnnotation")))
        assertFalse(context1.hasAllAnnotations("MyAnnotation", "OtherAnnotation"))

        assertTrue(context1.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))
        assertTrue(context1.hasAnyAnnotation(listOf("MyAnnotation", "OtherAnnotation")))
        assertFalse(context1.hasAnyAnnotation("OtherAnnotation"))

        // asserts
        context1.assertAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        context1.assertAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)

        violations.clear()
        context1.assertAllAnnotationsOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        context1.assertAllAnnotationsOf("MyAnnotation", "OtherAnnotation")
        assertEquals(1, violations.size)

        violations.clear()
        context1.assertAnyAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())
        context1.assertAnyAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)
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
        assertEquals(listOf(classA, classB), context.classes)

        context.addViolation("Module error")
        assertEquals(1, violations.size)
        assertEquals("Module error", violations[0])
    }

    @Test
    fun `test extra semantic extensions on models`() {
        // ClassDeclaration helpers
        assertTrue(classA.hasAnnotation("MyAnnotation"))
        assertTrue(classA.hasAllAnnotations("MyAnnotation"))
        assertTrue(classA.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))

        // FileDeclarationContext helpers
        assertTrue(fileCtx1.hasImport { it.contains("java.util") })
        assertTrue(fileCtx1.hasImportContaining("other", "List"))
        assertTrue(fileCtx1.containsClassWith { it.name == "ClassA" })

        // FunctionDeclarationContext helpers
        assertTrue(funcCtx1.hasAnnotation("MyAnnotation"))
        assertTrue(funcCtx1.hasAllAnnotations("MyAnnotation", "OtherAnnotation"))
        assertTrue(funcCtx1.hasAnyAnnotation("MyAnnotation", "Unknown"))

        // PropertyDeclarationContext helpers
        assertTrue(propCtx1.hasAnnotation("MyAnnotation"))
        assertTrue(propCtx1.hasAllAnnotations("MyAnnotation"))
        assertTrue(propCtx1.hasAnyAnnotation("MyAnnotation", "OtherAnnotation"))
    }

    @Test
    fun `test field delegation extensions`() {
        // FileDeclarationContext
        assertEquals("ClassA.kt", fileCtx1.name)
        assertEquals("com.example", fileCtx1.packageName)
        assertEquals(listOf("com.example.other.*", "java.util.List"), fileCtx1.imports)
        assertEquals(listOf(classA), fileCtx1.classes)
        assertTrue(fileCtx1.topLevelFunctions.isEmpty())
        assertTrue(fileCtx1.topLevelProperties.isEmpty())

        // FunctionDeclarationContext
        assertEquals("funcNormal", funcCtx1.name)
        assertEquals(Visibility.PUBLIC, funcCtx1.visibility)
        assertTrue(funcCtx1.modifiers.isEmpty())
        assertEquals("Unit", funcCtx1.returnType)
        assertEquals(listOf(param1), funcCtx1.parameters)
        assertEquals(listOf(anno1, anno2), funcCtx1.annotations)
        assertFalse(funcCtx1.isExtension)
        assertEquals("some function kdoc", funcCtx1.kdocText)

        // PropertyDeclarationContext
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

        // 1. Boolean overload
        val booleanRule =
            ClassesRuleBuilder(graph).should {
                isInterface // returns Boolean
            }
        val violations1 = mutableListOf<String>()
        booleanRule.getShouldAssertion()!!(testClass, listOf(testClass), violations1)
        assertEquals(1, violations1.size)

        // 2. Unit overload
        val unitRule =
            ClassesRuleBuilder(graph).should {
                check(isInterface, "Not interface") // returns Unit
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

        // 1. Returning null
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

        // 2. Returning an unsupported type (e.g., List)
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

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FluentExtensionsTest : KontureScopeTestFixture() {
    private lateinit var graph: ProjectGraph
    private lateinit var module: Module
    private lateinit var file1: FileDeclaration
    private lateinit var file2: FileDeclaration
    private lateinit var classA: ClassDeclaration
    private lateinit var classB: ClassDeclaration
    private lateinit var param1: ParameterDeclaration
    private lateinit var anno1: AnnotationDeclaration
    private lateinit var anno2: AnnotationDeclaration
    private lateinit var func1: FunctionDeclaration
    private lateinit var prop1: PropertyDeclaration
    private lateinit var fileCtx1: FileDeclarationContext
    private lateinit var funcCtx1: FunctionDeclarationContext
    private lateinit var propCtx1: PropertyDeclarationContext

    @BeforeEach
    fun setUp() {
        anno1 = AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")
        anno2 = AnnotationDeclaration("OtherAnnotation", "com.example.OtherAnnotation")
        param1 = ParameterDeclaration("param1", "String", hasDefaultValue = false, annotations = emptyList())

        classA =
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
        classB =
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
                classes = listOf(classA),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                filePath = "/src/ClassA.kt",
            )
        file2 =
            FileDeclaration(
                name = "ClassB.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes = listOf(classB),
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
        propCtx1 = PropertyDeclarationContext(prop1, classA, file1, ":submodule", "/src/ClassA.kt")
    }

    @Test
    fun `test ClassDeclarationShouldContext assertions`() {
        val violations = mutableListOf<String>()
        val context = ClassDeclarationShouldContext(classA, listOf(classA, classB), violations)

        assertEquals("ClassA", context.name)
        assertEquals("com.example.ClassA", context.fqName)
        assertEquals("com.example", context.packageName)
        assertFalse(context.isInterface)
        assertFalse(context.isAbstract)
        assertEquals(listOf(anno1), context.annotations)

        context.addViolation("Test violation")
        assertEquals(1, violations.size)
        assertEquals("Test violation", violations[0])
        violations.clear()

        context.assertResideInAPackage("com.example")
        assertTrue(violations.isEmpty())

        context.assertResideInAPackage("com.other")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotResideInAPackage("com.other")
        assertTrue(violations.isEmpty())

        context.assertNotResideInAPackage("com.example")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveName("ClassA")
        assertTrue(violations.isEmpty())

        context.assertHaveName("WrongName")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveName("WrongName")
        assertTrue(violations.isEmpty())

        context.assertNotHaveName("ClassA")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameStartingWith("Class")
        assertTrue(violations.isEmpty())

        context.assertHaveNameStartingWith("Wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameEndingWith("A")
        assertTrue(violations.isEmpty())

        context.assertHaveNameEndingWith("Wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameMatching("Class*")
        assertTrue(violations.isEmpty())

        context.assertHaveNameMatching("Wrong*")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveNameMatching("Wrong*")
        assertTrue(violations.isEmpty())

        context.assertNotHaveNameMatching("Class*")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotBeInterfaces()
        assertTrue(violations.isEmpty())

        val interfaceClass = classA.copy(isInterface = true)
        val interfaceContext = ClassDeclarationShouldContext(interfaceClass, listOf(interfaceClass), violations)
        interfaceContext.assertNotBeInterfaces()
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotBeAbstract()
        assertTrue(violations.isEmpty())

        val abstractClass = classA.copy(isAbstract = true)
        val abstractContext = ClassDeclarationShouldContext(abstractClass, listOf(abstractClass), violations)
        abstractContext.assertNotBeAbstract()
        assertEquals(1, violations.size)
        violations.clear()
    }

    @Test
    fun `test ClassDeclarationShouldContext properties and annotations`() {
        val violations = mutableListOf<String>()
        val context1 = ClassDeclarationShouldContext(classA, listOf(classA), violations)

        context1.assertHaveAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertHaveAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertNotHaveAnnotationOf("OtherAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertNotHaveAnnotationOf("MyAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertAllAnnotationsOf("MyAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertAllAnnotationsOf("MyAnnotation", "OtherAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertAnyAnnotationOf("MyAnnotation", "OtherAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertAnyAnnotationOf("OtherAnnotation", "UnknownAnnotation")
        assertEquals(1, violations.size)
        violations.clear()
    }

    @Test
    fun `test FileDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context = FileDeclarationShouldContext(fileCtx1, violations)

        assertEquals("ClassA.kt", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals(listOf("com.example.other.*", "java.util.List"), context.imports)
        assertEquals(listOf(classA), context.classes)

        context.assertResideInAPackage("com.example")
        assertTrue(violations.isEmpty())

        context.assertResideInAPackage("com.other")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotResideInAPackage("com.other")
        assertTrue(violations.isEmpty())

        context.assertNotResideInAPackage("com.example")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveName("ClassA.kt")
        assertTrue(violations.isEmpty())

        context.assertHaveName("Wrong.kt")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveName("Wrong.kt")
        assertTrue(violations.isEmpty())

        context.assertNotHaveName("ClassA.kt")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameStartingWith("Class")
        assertTrue(violations.isEmpty())

        context.assertHaveNameStartingWith("Wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameEndingWith("kt")
        assertTrue(violations.isEmpty())

        context.assertHaveNameEndingWith("java")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameMatching("Class*.kt")
        assertTrue(violations.isEmpty())

        context.assertHaveNameMatching("Wrong*")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveNameMatching("Wrong*")
        assertTrue(violations.isEmpty())

        context.assertNotHaveNameMatching("Class*.kt")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertContainClass("ClassA")
        assertTrue(violations.isEmpty())

        context.assertContainClass("MissingClass")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotContainClass("MissingClass")
        assertTrue(violations.isEmpty())

        context.assertNotContainClass("ClassA")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveImportOf("java.util.List")
        assertTrue(violations.isEmpty())

        context.assertHaveImportOf("java.util.Map")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveImportOf("java.util.Map")
        assertTrue(violations.isEmpty())

        context.assertNotHaveImportOf("java.util.List")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNoWildcardImports()
        assertEquals(1, violations.size)
        violations.clear()
    }

    @Test
    fun `test FunctionDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context = FunctionDeclarationShouldContext(funcCtx1, violations)

        assertEquals("funcNormal", context.name)
        assertEquals(Visibility.PUBLIC, context.visibility)
        assertTrue(context.modifiers.isEmpty())
        assertEquals("Unit", context.returnType)
        assertEquals(listOf(param1), context.parameters)
        assertEquals(listOf(anno1, anno2), context.annotations)
        assertFalse(context.isExtension)

        context.assertResideInAPackage("com.example")
        assertTrue(violations.isEmpty())

        context.assertResideInAPackage("com.other")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotResideInAPackage("com.other")
        assertTrue(violations.isEmpty())

        context.assertNotResideInAPackage("com.example")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveName("funcNormal")
        assertTrue(violations.isEmpty())

        context.assertHaveName("wrongName")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveName("wrongName")
        assertTrue(violations.isEmpty())

        context.assertNotHaveName("funcNormal")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameStartingWith("func")
        assertTrue(violations.isEmpty())

        context.assertHaveNameStartingWith("wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameEndingWith("Normal")
        assertTrue(violations.isEmpty())

        context.assertHaveNameEndingWith("wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveNameMatching("func*")
        assertTrue(violations.isEmpty())

        context.assertHaveNameMatching("wrong*")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveNameMatching("wrong*")
        assertTrue(violations.isEmpty())

        context.assertNotHaveNameMatching("func*")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertHaveAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())

        context.assertHaveAnnotationOf("UnknownAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertNotHaveAnnotationOf("UnknownAnnotation")
        assertTrue(violations.isEmpty())

        context.assertNotHaveAnnotationOf("MyAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertAllAnnotationsOf("MyAnnotation", "OtherAnnotation")
        assertTrue(violations.isEmpty())

        context.assertAllAnnotationsOf("MyAnnotation", "UnknownAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context.assertAnyAnnotationOf("MyAnnotation", "UnknownAnnotation")
        assertTrue(violations.isEmpty())

        context.assertAnyAnnotationOf("Unknown1", "Unknown2")
        assertEquals(1, violations.size)
        violations.clear()
    }

    @Test
    fun `test PropertyDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context1 = PropertyDeclarationShouldContext(propCtx1, violations)

        assertEquals("propVal", context1.name)
        assertEquals(Visibility.PUBLIC, context1.visibility)
        assertTrue(context1.modifiers.isEmpty())
        assertEquals("String", context1.type)
        assertTrue(context1.isVal)
        assertFalse(context1.isVar)
        assertEquals(listOf(anno1), context1.annotations)
        assertFalse(context1.isExtension)

        context1.assertResideInAPackage("com.example")
        assertTrue(violations.isEmpty())

        context1.assertResideInAPackage("com.other")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertNotResideInAPackage("com.other")
        assertTrue(violations.isEmpty())

        context1.assertNotResideInAPackage("com.example")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertHaveName("propVal")
        assertTrue(violations.isEmpty())

        context1.assertHaveName("wrongProp")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertNotHaveName("wrongProp")
        assertTrue(violations.isEmpty())

        context1.assertNotHaveName("propVal")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertHaveNameStartingWith("prop")
        assertTrue(violations.isEmpty())

        context1.assertHaveNameStartingWith("wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertHaveNameEndingWith("Val")
        assertTrue(violations.isEmpty())

        context1.assertHaveNameEndingWith("wrong")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertHaveNameMatching("prop*")
        assertTrue(violations.isEmpty())

        context1.assertHaveNameMatching("wrong*")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertNotHaveNameMatching("wrong*")
        assertTrue(violations.isEmpty())

        context1.assertNotHaveNameMatching("prop*")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertHaveAnnotationOf("MyAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertHaveAnnotationOf("OtherAnnotation")
        assertEquals(1, violations.size)
        violations.clear()

        context1.assertNotHaveAnnotationOf("OtherAnnotation")
        assertTrue(violations.isEmpty())

        context1.assertNotHaveAnnotationOf("MyAnnotation")
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
}

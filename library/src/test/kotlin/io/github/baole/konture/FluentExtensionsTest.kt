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

    @BeforeEach
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
    fun `test ClassDeclarationShouldContext assertions`() {
        val violations = mutableListOf<String>()
        val context = ClassDeclarationShouldContext(myClassA, listOf(myClassA, myClassB), violations)

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

        context.check(true, "ok")
        assertTrue(violations.isEmpty())

        context.check(false, "err")
        assertEquals(1, violations.size)
        assertEquals("err", violations[0])
    }

    @Test
    fun `test ClassDeclarationShouldContext properties and annotations`() {
        val violations = mutableListOf<String>()
        val context1 = ClassDeclarationShouldContext(classA, listOf(classA), violations)

        context1.check(true, "ok")
        assertTrue(violations.isEmpty())

        context1.check(false, "err")
        assertEquals(1, violations.size)
        assertEquals("err", violations[0])
    }

    @Test
    fun `test FileDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context = FileDeclarationShouldContext(fileCtx1, listOf(fileCtx1), violations)

        assertEquals("ClassA.kt", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals(listOf("com.example.other.*", "java.util.List"), context.imports)
        assertEquals(listOf(classA), context.classes)

        context.check(true, "ok")
        assertTrue(violations.isEmpty())

        context.check(false, "err")
        assertEquals(1, violations.size)
        assertEquals("err", violations[0])
    }

    @Test
    fun `test FunctionDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context = FunctionDeclarationShouldContext(funcCtx1, listOf(funcCtx1), violations)

        assertEquals("funcNormal", context.name)
        assertEquals(Visibility.PUBLIC, context.visibility)
        assertTrue(context.modifiers.isEmpty())
        assertEquals("Unit", context.returnType)
        assertEquals(listOf(param1), context.parameters)
        assertEquals(listOf(anno1, anno2), context.annotations)
        assertFalse(context.isExtension)

        context.check(true, "ok")
        assertTrue(violations.isEmpty())

        context.check(false, "err")
        assertEquals(1, violations.size)
        assertEquals("err", violations[0])
    }

    @Test
    fun `test PropertyDeclarationShouldContext`() {
        val violations = mutableListOf<String>()
        val context1 = PropertyDeclarationShouldContext(propCtx1, listOf(propCtx1), violations)

        assertEquals("propVal", context1.name)
        assertEquals(Visibility.PUBLIC, context1.visibility)
        assertTrue(context1.modifiers.isEmpty())
        assertEquals("String", context1.type)
        assertTrue(context1.isVal)
        assertFalse(context1.isVar)
        assertEquals(listOf(anno1), context1.annotations)
        assertFalse(context1.isExtension)

        context1.check(true, "ok")
        assertTrue(violations.isEmpty())

        context1.check(false, "err")
        assertEquals(1, violations.size)
        assertEquals("err", violations[0])
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.SliceGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FluentContextCoverageExtendedTest : KontureScopeTestFixture() {
    @Test
    fun `test ClassDeclarationShouldContext properties and assertions`() {
        val annotation = AnnotationDeclaration("MyAnn", "com.example.MyAnn")
        val constructor = ConstructorDeclaration(Visibility.PUBLIC, emptyList(), emptyList())
        val func =
            FunctionDeclaration("foo", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false)
        val prop = PropertyDeclaration("bar", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val companion =
            ClassDeclaration("Companion", "com.example.ClassA.Companion", "com.example", false, false, emptyList(), emptyList(), emptySet(), "/src/ClassA.kt")

        val targetClass =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = true,
                isAbstract = true,
                annotations = listOf(annotation),
                imports = listOf("com.example.dep.Dep"),
                referencedTypes = setOf("com.example.dep.Dep"),
                filePath = "/src/ClassA.kt",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.SEALED),
                supertypes = listOf("com.example.Base"),
                primaryConstructor = constructor,
                secondaryConstructors = listOf(constructor),
                functions = listOf(func),
                properties = listOf(prop),
                companionObject = companion,
                kdocText = "/** KDoc */",
            )

        val violations = mutableListOf<String>()
        val context = ClassDeclarationShouldContext(targetClass, listOf(targetClass), violations)

        // Test properties
        assertEquals(targetClass, context.element)
        assertEquals("ClassA", context.name)
        assertEquals("com.example.ClassA", context.fqName)
        assertEquals("com.example", context.packageName)
        assertTrue(context.isInterface)
        assertTrue(context.isAbstract)
        assertEquals(Visibility.INTERNAL, context.visibility)
        assertEquals(setOf(Modifier.SEALED), context.modifiers)
        assertEquals(listOf("com.example.Base"), context.supertypes)
        assertEquals(constructor, context.primaryConstructor)
        assertEquals(listOf(constructor), context.secondaryConstructors)
        assertEquals(listOf(func), context.functions)
        assertEquals(listOf(prop), context.properties)
        assertEquals(companion, context.companionObject)
        assertEquals("/** KDoc */", context.kdocText)
        assertEquals(listOf("com.example.dep.Dep"), context.imports)
        assertEquals(setOf("com.example.dep.Dep"), context.referencedTypes)
        assertEquals("/src/ClassA.kt", context.filePath)
        assertEquals(listOf(annotation), context.annotations)

        // Annotations helper methods
        assertTrue(context.hasAnnotation("MyAnn"))
        assertTrue(context.hasAnnotation("com.example.MyAnn"))
        assertFalse(context.hasAnnotation("Unknown"))

        assertTrue(context.hasAllAnnotations(listOf("MyAnn")))
        assertTrue(context.hasAllAnnotations("MyAnn"))
        assertFalse(context.hasAllAnnotations("MyAnn", "Unknown"))

        assertTrue(context.hasAnyAnnotation(listOf("MyAnn", "Unknown")))
        assertTrue(context.hasAnyAnnotation("MyAnn", "Unknown"))
        assertFalse(context.hasAnyAnnotation("Unknown1", "Unknown2"))

        // Check assertions
        violations.clear()
        context.check(true)
        assertTrue(violations.isEmpty())
        context.check(false)
        assertEquals(1, violations.size)
        context.check(false, "Custom error message")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnnotationOf("MyAnn")
        assertTrue(violations.isEmpty())
        context.assertAnnotationOf("Missing")
        assertEquals(1, violations.size)

        violations.clear()
        context.assertAllAnnotationsOf("MyAnn")
        assertTrue(violations.isEmpty())
        context.assertAllAnnotationsOf(listOf("MyAnn", "Missing"))
        assertEquals(1, violations.size)
        context.assertAllAnnotationsOf("MyAnn", "Missing2")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnyAnnotationOf("MyAnn", "Missing")
        assertTrue(violations.isEmpty())
        context.assertAnyAnnotationOf(listOf("Missing1", "Missing2"))
        assertEquals(1, violations.size)
        context.assertAnyAnnotationOf("Missing3", "Missing4")
        assertEquals(2, violations.size)
    }

    @Test
    fun `test PropertyDeclarationShouldContext properties and assertions`() {
        val annotation = AnnotationDeclaration("PropAnn", "com.example.PropAnn")
        val propDecl =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.OPEN),
                type = "kotlin.String",
                isVal = true,
                annotations = listOf(annotation),
                kdocText = "/** Prop doc */",
                isExtension = true,
            )
        val propCtx = PropertyDeclarationContext(propDecl, "com.example", "MyClass", ":app", "/src/MyClass.kt")
        val violations = mutableListOf<String>()
        val context = PropertyDeclarationShouldContext(propCtx, listOf(propCtx), violations)

        assertEquals(propDecl, context.declaration)
        assertEquals("myProp", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals("MyClass", context.className)
        assertEquals(":app", context.modulePath)
        assertEquals("/src/MyClass.kt", context.filePath)
        assertEquals(Visibility.PROTECTED, context.visibility)
        assertEquals(setOf(Modifier.OPEN), context.modifiers)
        assertEquals("kotlin.String", context.type)
        assertTrue(context.isVal)
        assertFalse(context.isVar)
        assertEquals(listOf(annotation), context.annotations)
        assertEquals("/** Prop doc */", context.kdocText)
        assertTrue(context.isExtension)

        assertTrue(context.hasAnnotation("PropAnn"))
        assertTrue(context.hasAnnotation("com.example.PropAnn"))
        assertFalse(context.hasAnnotation("Unknown"))

        assertTrue(context.hasAllAnnotations(listOf("PropAnn")))
        assertTrue(context.hasAllAnnotations("PropAnn"))
        assertFalse(context.hasAllAnnotations("PropAnn", "Unknown"))

        assertTrue(context.hasAnyAnnotation(listOf("PropAnn", "Unknown")))
        assertTrue(context.hasAnyAnnotation("PropAnn", "Unknown"))
        assertFalse(context.hasAnyAnnotation("Unknown1", "Unknown2"))

        // Check assertions
        violations.clear()
        context.check(true)
        assertTrue(violations.isEmpty())
        context.check(false)
        assertEquals(1, violations.size)
        context.check(false, "Custom prop message")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnnotationOf("PropAnn")
        assertTrue(violations.isEmpty())
        context.assertAnnotationOf("Missing")
        assertEquals(1, violations.size)

        violations.clear()
        context.assertAllAnnotationsOf("PropAnn")
        assertTrue(violations.isEmpty())
        context.assertAllAnnotationsOf(listOf("PropAnn", "Missing"))
        assertEquals(1, violations.size)
        context.assertAllAnnotationsOf("PropAnn", "Missing2")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnyAnnotationOf("PropAnn", "Missing")
        assertTrue(violations.isEmpty())
        context.assertAnyAnnotationOf(listOf("Missing1", "Missing2"))
        assertEquals(1, violations.size)
        context.assertAnyAnnotationOf("Missing3", "Missing4")
        assertEquals(2, violations.size)

        // Test PropertyDeclarationContext extensions
        assertEquals("myProp", propCtx.name)
        assertEquals(Visibility.PROTECTED, propCtx.visibility)
        assertEquals(setOf(Modifier.OPEN), propCtx.modifiers)
        assertEquals("kotlin.String", propCtx.type)
        assertTrue(propCtx.isVal)
        assertFalse(propCtx.isVar)
        assertEquals(listOf(annotation), propCtx.annotations)
        assertTrue(propCtx.isExtension)
        assertEquals("/** Prop doc */", propCtx.kdocText)

        val list = listOf(propCtx)
        assertEquals(1, list.residingInPackage("com.example").size)
        assertEquals(0, list.residingInPackage("com.other").size)
        assertEquals(1, list.residingInModule(":app").size)
        assertEquals(0, list.residingInModule(":other").size)
        assertEquals(1, list.annotatedWith("PropAnn").size)
        assertEquals(0, list.annotatedWith("Missing").size)
    }

    @Test
    fun `test FunctionDeclarationShouldContext properties and assertions`() {
        val annotation = AnnotationDeclaration("FuncAnn", "com.example.FuncAnn")
        val param = ParameterDeclaration("count", "kotlin.Int", false, emptyList())
        val funcDecl =
            FunctionDeclaration(
                name = "doSomething",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND, Modifier.INLINE),
                returnType = "kotlin.Boolean",
                parameters = listOf(param),
                annotations = listOf(annotation),
                kdocText = "/** Func doc */",
                isExtension = true,
            )
        val funcCtx = FunctionDeclarationContext(funcDecl, "com.example", "MyClass", ":app", "/src/MyClass.kt")
        val violations = mutableListOf<String>()
        val context = FunctionDeclarationShouldContext(funcCtx, listOf(funcCtx), violations)

        assertEquals(funcDecl, context.declaration)
        assertEquals("doSomething", context.name)
        assertEquals("com.example", context.packageName)
        assertEquals("MyClass", context.className)
        assertEquals(":app", context.modulePath)
        assertEquals("/src/MyClass.kt", context.filePath)
        assertEquals(Visibility.PUBLIC, context.visibility)
        assertEquals(setOf(Modifier.SUSPEND, Modifier.INLINE), context.modifiers)
        assertEquals("kotlin.Boolean", context.returnType)
        assertEquals(listOf(param), context.parameters)
        assertEquals(listOf(annotation), context.annotations)
        assertEquals("/** Func doc */", context.kdocText)
        assertTrue(context.isExtension)

        assertTrue(context.hasAnnotation("FuncAnn"))
        assertTrue(context.hasAnnotation("com.example.FuncAnn"))
        assertFalse(context.hasAnnotation("Unknown"))

        assertTrue(context.hasAllAnnotations(listOf("FuncAnn")))
        assertTrue(context.hasAllAnnotations("FuncAnn"))
        assertFalse(context.hasAllAnnotations("FuncAnn", "Unknown"))

        assertTrue(context.hasAnyAnnotation(listOf("FuncAnn", "Unknown")))
        assertTrue(context.hasAnyAnnotation("FuncAnn", "Unknown"))
        assertFalse(context.hasAnyAnnotation("Unknown1", "Unknown2"))

        // Check assertions
        violations.clear()
        context.check(true)
        assertTrue(violations.isEmpty())
        context.check(false)
        assertEquals(1, violations.size)
        context.check(false, "Custom func message")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnnotationOf("FuncAnn")
        assertTrue(violations.isEmpty())
        context.assertAnnotationOf("Missing")
        assertEquals(1, violations.size)

        violations.clear()
        context.assertAllAnnotationsOf("FuncAnn")
        assertTrue(violations.isEmpty())
        context.assertAllAnnotationsOf(listOf("FuncAnn", "Missing"))
        assertEquals(1, violations.size)
        context.assertAllAnnotationsOf("FuncAnn", "Missing2")
        assertEquals(2, violations.size)

        violations.clear()
        context.assertAnyAnnotationOf("FuncAnn", "Missing")
        assertTrue(violations.isEmpty())
        context.assertAnyAnnotationOf(listOf("Missing1", "Missing2"))
        assertEquals(1, violations.size)
        context.assertAnyAnnotationOf("Missing3", "Missing4")
        assertEquals(2, violations.size)

        // Test FunctionDeclarationContext extensions
        assertEquals("doSomething", funcCtx.name)
        assertEquals(Visibility.PUBLIC, funcCtx.visibility)
        assertEquals(setOf(Modifier.SUSPEND, Modifier.INLINE), funcCtx.modifiers)
        assertEquals("kotlin.Boolean", funcCtx.returnType)
        assertEquals(listOf(param), funcCtx.parameters)
        assertEquals(listOf(annotation), funcCtx.annotations)
        assertTrue(funcCtx.isExtension)
        assertEquals("/** Func doc */", funcCtx.kdocText)

        val list = listOf(funcCtx)
        assertEquals(1, list.residingInPackage("com.example").size)
        assertEquals(0, list.residingInPackage("com.other").size)
        assertEquals(1, list.residingInModule(":app").size)
        assertEquals(0, list.residingInModule(":other").size)
        assertEquals(1, list.annotatedWith("FuncAnn").size)
        assertEquals(0, list.annotatedWith("Missing").size)
    }

    @Test
    fun `test FileDeclarationShouldContext and ModuleShouldContext and SliceShouldContext`() {
        val fileDecl =
            FileDeclaration(
                name = "TestFile.kt",
                packageName = "com.example",
                imports = listOf("com.example.A", "com.example.B.*"),
                classes = listOf(classA),
                topLevelFunctions = emptyList(),
                topLevelProperties = emptyList(),
                filePath = "/src/TestFile.kt",
                kdocText = "/** File doc */",
            )
        val fileCtx = FileDeclarationContext(fileDecl, ":core")
        val fileViolations = mutableListOf<String>()
        val fileContext = FileDeclarationShouldContext(fileCtx, listOf(fileCtx), fileViolations)

        assertEquals(fileDecl, fileContext.declaration)
        assertEquals("TestFile.kt", fileContext.name)
        assertEquals("com.example", fileContext.packageName)
        assertEquals(listOf("com.example.A", "com.example.B.*"), fileContext.imports)
        assertEquals(listOf(classA), fileContext.classes)
        assertTrue(fileContext.topLevelFunctions.isEmpty())
        assertTrue(fileContext.topLevelProperties.isEmpty())
        assertEquals("/** File doc */", fileContext.kdocText)
        assertEquals("/src/TestFile.kt", fileContext.filePath)
        assertEquals(":core", fileContext.modulePath)

        assertTrue(fileContext.hasImport { it.contains("com.example.A") })
        assertTrue(fileContext.hasImportContaining("A"))
        assertTrue(fileContext.containsClassWith { it.name == "ClassA" })

        fileViolations.clear()
        fileContext.check(true)
        assertTrue(fileViolations.isEmpty())
        fileContext.check(false)
        assertEquals(1, fileViolations.size)
        fileContext.check(false, "Custom file error")
        assertEquals(2, fileViolations.size)

        fileViolations.clear()
        fileContext.assertNoWildcardImports()
        assertEquals(1, fileViolations.size)

        // ModuleShouldContext
        val module =
            Module(
                ":",
                ":app",
                "/app",
                emptyList(),
                emptyList(),
                listOf(Dependency("implementation", ":", ":lib")),
                listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(module)))
        val modViolations = mutableListOf<String>()
        val modContext = ModuleShouldContext(module, graph, modViolations)

        assertEquals(":", modContext.buildId)
        assertEquals(":app", modContext.path)
        assertEquals("/app", modContext.projectDir)
        assertTrue(modContext.appliedPlugins.isEmpty())
        assertTrue(modContext.sourceSets.isEmpty())
        assertEquals(1, modContext.dependencies.size)
        assertEquals(listOf(fileDecl), modContext.files)
        assertEquals(listOf(classA), modContext.classes)

        modViolations.clear()
        modContext.check(true)
        assertTrue(modViolations.isEmpty())
        modContext.check(false)
        assertEquals(1, modViolations.size)
        modContext.check(false, "Custom mod error")
        assertEquals(2, modViolations.size)

        // SliceShouldContext
        val slice = Slice("sliceA", setOf("com.example"), listOf(classA))
        val sliceGraph = SliceGraph(listOf(slice), emptyMap())
        val sliceViolations = mutableListOf<String>()
        val sliceContext = SliceShouldContext(sliceGraph, sliceViolations)

        assertEquals(listOf(slice), sliceContext.slices)
        assertEquals(emptyMap<String, Set<String>>(), sliceContext.adjacency)

        sliceViolations.clear()
        sliceContext.check(true)
        assertTrue(sliceViolations.isEmpty())
        sliceContext.check(false)
        assertEquals(1, sliceViolations.size)
        sliceContext.check(false, "Custom slice error")
        assertEquals(2, sliceViolations.size)
    }

    @Test
    fun `test validateAssertionResult throws IllegalArgumentException when invalid type is returned`() {
        var errorThrown = false
        try {
            validateAssertionResult("invalid return type string")
        } catch (e: IllegalArgumentException) {
            errorThrown = true
            assertTrue(e.message!!.contains("A should { } block must return either a Boolean"))
        }
        assertTrue(errorThrown)

        // Should not throw when Boolean or Unit
        validateAssertionResult(true)
        validateAssertionResult(false)
        validateAssertionResult(Unit)
    }
}

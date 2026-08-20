/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.SliceGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SlicesAssertionsDeepCoverageTest : KontureScopeTestFixture() {
    annotation class SampleAnn

    @Test
    fun `test SlicesShould visibility assertions for classes, functions, and properties`() {
        val funcPublic =
            FunctionDeclaration("funcPub", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false)
        val funcPrivate =
            FunctionDeclaration(
                "funcPriv",
                Visibility.PRIVATE,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                null,
                false,
            )

        val propPublic =
            PropertyDeclaration("propPub", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val propPrivate =
            PropertyDeclaration("propPriv", Visibility.PRIVATE, emptySet(), "String", true, emptyList(), null)

        val fileWithMembers =
            FileDeclaration(
                name = "ClassA.kt",
                packageName = "com.example",
                classes = listOf(classA, classInternal),
                topLevelFunctions = listOf(funcPublic, funcPrivate),
                topLevelProperties = listOf(propPublic, propPrivate),
                filePath = "/app/src/ClassA.kt",
            )

        val module = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileWithMembers))
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        val slice = Slice("sliceA", setOf("com.example"), listOf(classA, classInternal))
        val sliceGraph = SliceGraph(listOf(slice), emptyMap())

        // Classes visibility
        val vClsPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyClassesWithVisibility(Visibility.PUBLIC, Visibility.INTERNAL)
            .checkRuleAssertions(sliceGraph, vClsPass)
        assertTrue(vClsPass.isEmpty())

        val vClsFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyClassesWithVisibility(listOf(Visibility.PUBLIC))
            .checkRuleAssertions(sliceGraph, vClsFail)
        assertEquals(1, vClsFail.size)

        // Functions visibility
        val vFuncPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyFunctionsWithVisibility(Visibility.PUBLIC, Visibility.PRIVATE)
            .checkRuleAssertions(sliceGraph, vFuncPass)
        assertTrue(vFuncPass.isEmpty())

        val vFuncFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyFunctionsWithVisibility(listOf(Visibility.PUBLIC))
            .checkRuleAssertions(sliceGraph, vFuncFail)
        assertEquals(1, vFuncFail.size)

        // Properties visibility
        val vPropPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyPropertiesWithVisibility(Visibility.PUBLIC, Visibility.PRIVATE)
            .checkRuleAssertions(sliceGraph, vPropPass)
        assertTrue(vPropPass.isEmpty())

        val vPropFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containOnlyPropertiesWithVisibility(listOf(Visibility.PUBLIC))
            .checkRuleAssertions(sliceGraph, vPropFail)
        assertEquals(1, vPropFail.size)
    }

    @Test
    fun `test SlicesShould resideInModule and notResideInModule assertions`() {
        val classInApp = classA.copy(filePath = "/app/src/App.kt")
        val classInLib = classB.copy(filePath = "/lib/src/Lib.kt")
        val fileApp =
            FileDeclaration("App.kt", "com.example.app", classes = listOf(classInApp), filePath = "/app/src/App.kt")
        val fileLib =
            FileDeclaration("Lib.kt", "com.example.lib", classes = listOf(classInLib), filePath = "/lib/src/Lib.kt")

        val modApp = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileApp))
        val modLib = Module(":", ":lib", "/lib", emptyList(), emptyList(), emptyList(), listOf(fileLib))
        val graph = ProjectGraph(mapOf(":" to listOf(modApp, modLib)))

        val sliceApp = Slice("appSlice", setOf("com.example.app"), listOf(classInApp))
        val sliceLib = Slice("libSlice", setOf("com.example.lib"), listOf(classInLib))
        val sliceGraph = SliceGraph(listOf(sliceApp, sliceLib), emptyMap())

        // resideInModule
        val vResideSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModule("app")
            .checkRuleAssertions(sliceGraph, vResideSingle)
        assertEquals(1, vResideSingle.size) // libSlice fails

        val vResideList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModules(listOf(":app", ":lib"))
            .checkRuleAssertions(sliceGraph, vResideList)
        assertTrue(vResideList.isEmpty())

        val vResideVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModules(":app", ":lib")
            .checkRuleAssertions(sliceGraph, vResideVararg)
        assertTrue(vResideList.isEmpty())

        // notResideInModule
        val vNotResideSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModule("app")
            .checkRuleAssertions(sliceGraph, vNotResideSingle)
        assertEquals(1, vNotResideSingle.size) // appSlice fails

        val vNotResideList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModules(listOf("app", "lib"))
            .checkRuleAssertions(sliceGraph, vNotResideList)
        assertEquals(2, vNotResideList.size)

        val vNotResideVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModules("other")
            .checkRuleAssertions(sliceGraph, vNotResideVararg)
        assertTrue(vNotResideVararg.isEmpty())
    }

    @Test
    fun `test SlicesShould notCall and notReferenceClass assertions`() {
        val classInApp = classA.copy(filePath = "/app/src/App.kt")
        val usageCall = SourceUsage(UsageKind.CALL, "com.target.TargetService.call", "/app/src/App.kt", 10, 1)
        val usageRef = SourceUsage(UsageKind.CLASS_REFERENCE, "com.target.TargetClass", "/app/src/App.kt", 12, 1)

        val fileWithUsage =
            FileDeclaration(
                name = "App.kt",
                packageName = "com.example.app",
                classes = listOf(classInApp),
                usages = listOf(usageCall, usageRef),
                filePath = "/app/src/App.kt",
            )
        val modApp = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsage))
        val graph = ProjectGraph(mapOf(":" to listOf(modApp)))

        val slice = Slice("appSlice", setOf("com.example.app"), listOf(classInApp))
        val sliceGraph = SliceGraph(listOf(slice), emptyMap())

        val vCall = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notCall("com.target.TargetService.call")
            .checkRuleAssertions(sliceGraph, vCall)
        assertEquals(1, vCall.size)

        val vCallPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notCall("com.safe.SafeService.call")
            .checkRuleAssertions(sliceGraph, vCallPass)
        assertTrue(vCallPass.isEmpty())

        val vRef = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notReferenceClass("com.target.TargetClass")
            .checkRuleAssertions(sliceGraph, vRef)
        assertEquals(1, vRef.size)

        val vRefPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notReferenceClass("com.safe.SafeClass")
            .checkRuleAssertions(sliceGraph, vRefPass)
        assertTrue(vRefPass.isEmpty())
    }

    @Test
    fun `test SlicesShould satisfy, anyOf, allOf, noneOf`() {
        val classInApp = classA.copy(filePath = "/app/src/App.kt")
        val fileApp =
            FileDeclaration("App.kt", "com.example.app", classes = listOf(classInApp), filePath = "/app/src/App.kt")
        val modApp = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileApp))
        val graph = ProjectGraph(mapOf(":" to listOf(modApp)))

        val slice = Slice("appSlice", setOf("com.example.app"), listOf(classInApp))
        val sliceGraph = SliceGraph(listOf(slice), emptyMap())

        // satisfy
        val vSatPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().satisfy("custom-sat", "custom description") { slices ->
            slices.any { it.key == "appSlice" }
        }.checkRuleAssertions(sliceGraph, vSatPass)
        assertTrue(vSatPass.isEmpty())

        val vSatFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().satisfy("custom-fail", "custom fail description") { slices ->
            slices.any { it.key == "missing" }
        }.checkRuleAssertions(sliceGraph, vSatFail)
        assertEquals(1, vSatFail.size)

        // allOf
        val vAllPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().allOf(
            { containClasses() },
            { resideInModule("app") },
        ).checkRuleAssertions(sliceGraph, vAllPass)
        assertTrue(vAllPass.isEmpty())

        val vAllFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().allOf(
            { containClasses() },
            { notContainClasses() },
        ).checkRuleAssertions(sliceGraph, vAllFail)
        assertEquals(1, vAllFail.size)

        // anyOf
        val vAnyPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().anyOf(
            { notContainClasses() },
            { containClasses() },
        ).checkRuleAssertions(sliceGraph, vAnyPass)
        assertTrue(vAnyPass.isEmpty())

        val vAnyFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().anyOf(
            { notContainClasses() },
            { resideInModule("missing") },
        ).checkRuleAssertions(sliceGraph, vAnyFail)
        assertEquals(1, vAnyFail.size)

        // noneOf
        val vNonePass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().noneOf(
            { notContainClasses() },
            { resideInModule("missing") },
        ).checkRuleAssertions(sliceGraph, vNonePass)
        assertTrue(vNonePass.isEmpty())

        val vNoneFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().noneOf(
            { containClasses() },
        ).checkRuleAssertions(sliceGraph, vNoneFail)
        assertEquals(1, vNoneFail.size)
    }

    @Test
    fun `test SlicesShould annotation, package, and class assertions`() {
        val fileApp =
            FileDeclaration(
                "App.kt",
                "com.example.app",
                classes = listOf(classA, classAnnotated),
                filePath = "/app/src/App.kt",
            )
        val modApp = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileApp))
        val graph = ProjectGraph(mapOf(":" to listOf(modApp)))

        val slice = Slice("appSlice", setOf("com.example.app"), listOf(classA, classAnnotated))
        val sliceGraph = SliceGraph(listOf(slice), emptyMap())

        // containClassesWithAnnotation string / class
        val vAnnPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation("com.example.MyAnnotation")
            .checkRuleAssertions(sliceGraph, vAnnPass)
        assertTrue(vAnnPass.isEmpty())

        val vAnnFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation("com.missing.Annotation")
            .checkRuleAssertions(sliceGraph, vAnnFail)
        assertEquals(1, vAnnFail.size)

        val vAnnKClassFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation(SampleAnn::class)
            .checkRuleAssertions(sliceGraph, vAnnKClassFail)
        assertEquals(1, vAnnKClassFail.size)

        // notContainClassesWithAnnotation string / class
        val vNotAnnPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesWithAnnotation("com.missing.Annotation")
            .checkRuleAssertions(sliceGraph, vNotAnnPass)
        assertTrue(vNotAnnPass.isEmpty())

        val vNotAnnFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesWithAnnotation("com.example.MyAnnotation")
            .checkRuleAssertions(sliceGraph, vNotAnnFail)
        assertEquals(1, vNotAnnFail.size)

        val vNotAnnKClassPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesWithAnnotation(SampleAnn::class)
            .checkRuleAssertions(sliceGraph, vNotAnnKClassPass)
        assertTrue(vNotAnnKClassPass.isEmpty())

        // containClassesInPackage / notContainClassesInPackage
        val vPkgPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesInPackage("com.example..")
            .checkRuleAssertions(sliceGraph, vPkgPass)
        assertTrue(vPkgPass.isEmpty())

        val vPkgFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesInPackage("com.other..")
            .checkRuleAssertions(sliceGraph, vPkgFail)
        assertEquals(1, vPkgFail.size)

        val vNotPkgPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesInPackage("com.other..")
            .checkRuleAssertions(sliceGraph, vNotPkgPass)
        assertTrue(vNotPkgPass.isEmpty())

        val vNotPkgFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesInPackage("com.example..")
            .checkRuleAssertions(sliceGraph, vNotPkgFail)
        assertEquals(1, vNotPkgFail.size)

        // notContainClass string / class
        val vNotClsPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClass("com.other.ClassC")
            .checkRuleAssertions(sliceGraph, vNotClsPass)
        assertTrue(vNotClsPass.isEmpty())

        val vNotClsFail = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClass("com.example.ClassA")
            .checkRuleAssertions(sliceGraph, vNotClsFail)
        assertEquals(1, vNotClsFail.size)

        val vNotClsKClassPass = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClass(String::class)
            .checkRuleAssertions(sliceGraph, vNotClsKClassPass)
        assertTrue(vNotClsKClassPass.isEmpty())
    }
}

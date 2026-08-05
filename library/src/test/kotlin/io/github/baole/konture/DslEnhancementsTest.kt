/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Test

class DslEnhancementsTest {
    private val sampleClass1 =
        ClassDeclaration(
            name = "FooService",
            fqName = "com.example.service.FooService",
            packageName = "com.example.service",
            isInterface = false,
            isAbstract = false,
            annotations =
                listOf(
                    AnnotationDeclaration(name = "JvmName", fqName = "kotlin.jvm.JvmName"),
                ),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/com/example/service/FooService.kt",
            visibility = Visibility.PUBLIC,
            properties =
                listOf(
                    PropertyDeclaration(
                        name = "id",
                        type = "kotlin.String",
                        visibility = Visibility.PUBLIC,
                        modifiers = emptySet(),
                        isVal = true,
                        annotations = emptyList(),
                        kdocText = null,
                        isExtension = false,
                    ),
                ),
            functions =
                listOf(
                    FunctionDeclaration(
                        name = "execute",
                        visibility = Visibility.PUBLIC,
                        modifiers = emptySet(),
                        returnType = "kotlin.Unit",
                        parameters = emptyList(),
                        annotations = emptyList(),
                        kdocText = null,
                        isExtension = false,
                    ),
                ),
        )

    private val sampleClass2 =
        ClassDeclaration(
            name = "BarRepository",
            fqName = "com.example.repository.BarRepository",
            packageName = "com.example.repository",
            isInterface = false,
            isAbstract = false,
            annotations = emptyList(),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/com/example/repository/BarRepository.kt",
            visibility = Visibility.PUBLIC,
        )

    private val sampleFile1 =
        FileDeclaration(
            name = "FooService.kt",
            packageName = "com.example.service",
            filePath = "/src/com/example/service/FooService.kt",
            imports = listOf("com.example.repository.BarRepository"),
            classes = listOf(sampleClass1),
            topLevelFunctions =
                listOf(
                    FunctionDeclaration(
                        name = "topFunc",
                        visibility = Visibility.PUBLIC,
                        modifiers = emptySet(),
                        returnType = "kotlin.Unit",
                        parameters = emptyList(),
                        annotations = emptyList(),
                        kdocText = null,
                        isExtension = false,
                    ),
                ),
            topLevelProperties =
                listOf(
                    PropertyDeclaration(
                        name = "topProp",
                        type = "kotlin.String",
                        visibility = Visibility.PUBLIC,
                        modifiers = emptySet(),
                        isVal = true,
                        annotations = emptyList(),
                        kdocText = null,
                        isExtension = false,
                    ),
                ),
        )

    private val sampleFile2 =
        FileDeclaration(
            name = "BarRepository.kt",
            packageName = "com.example.repository",
            filePath = "/src/com/example/repository/BarRepository.kt",
            classes = listOf(sampleClass2),
        )

    private val sampleModule =
        Module(
            buildId = ":",
            path = ":core",
            projectDir = "core",
            appliedPlugins = listOf("kotlin-android", "com.android.library"),
            sourceSets = emptyList(),
            dependencies = emptyList(),
            files = listOf(sampleFile1, sampleFile2),
        )

    private val graph = ProjectGraph(mapOf(":" to listOf(sampleModule)))

    @Test
    fun `test slices DSL enhancements`() {
        val printed = mutableListOf<String>()
        val builder =
            SlicesRuleBuilder(graph)
                .matching("com.example.(*)..")
                .printAllSlices { printed.add(it.key) }

        builder.that().haveKeyStartingWith("serv")
        builder.that().haveKeyEndingWith("ice")
        builder.that().haveKeyMatching("serv*")

        builder.should().satisfy { sliceGraph, _ ->
            check(sliceGraph.slices.isNotEmpty())
        }
        builder.should().satisfy("slice graph non-empty") { sliceGraph ->
            sliceGraph.slices.isNotEmpty()
        }

        builder.should().anyOf({ beFreeOfCycles() })
        builder.should().allOf({ beFreeOfCycles() })
        builder.should().noneOf({ dependOnSlice("non_existent_required_slice") })

        builder.check()
        check(printed.isNotEmpty())
    }

    @Test
    fun `test files DSL enhancements`() {
        val fileBuilder = FilesRuleBuilder(graph)
        fileBuilder.that().haveAnnotationOf("JvmName")
        fileBuilder.that().haveAllAnnotationsOf(listOf("JvmName"))
        fileBuilder.that().haveAnyAnnotationOf(listOf("JvmName"))
        fileBuilder.that().anyOf({ haveName("FooService.kt") })
        fileBuilder.that().allOf({ haveName("FooService.kt") })
        fileBuilder.that().noneOf({ haveName("NonExistent.kt") })

        fileBuilder.should().containTopLevelFunctions()
        fileBuilder.should().containTopLevelProperties()
        fileBuilder.should().notContainClass("NonExistentClass")
        fileBuilder.should().notHaveImportOf("com.example.UnusedImport")

        fileBuilder.check()

        val scope = KontureFileScope(listOf(sampleFile1))
        scope.assertResideInAModule(":core", graph)
        scope.assertNotResideInAModule(":app", graph)
    }

    @Test
    fun `test modules DSL enhancements`() {
        val modBuilder = ModulesRuleBuilder(graph)
        modBuilder.that().applyPlugin("kotlin-android")
        modBuilder.that().havePlugin("kotlin-android")
        modBuilder.that().havePlugins("kotlin-android", "com.android.library")

        modBuilder.should().applyPlugin("kotlin-android")
        modBuilder.should().havePlugin("kotlin-android")
        modBuilder.should().notApplyPlugin("java")
        modBuilder.should().containClasses()
        modBuilder.should().containFiles()

        modBuilder.check()
    }

    @Test
    fun `test functions DSL enhancements`() {
        val funcBuilder = FunctionsRuleBuilder(graph)
        funcBuilder.that().haveNoParameters()
        funcBuilder.that().haveParameterCount(0)
        funcBuilder.that().belongToClass("FooService")
        funcBuilder.that().anyOf({ haveName { it == "execute" } })
        funcBuilder.that().allOf({ haveName { it == "execute" } })
        funcBuilder.that().noneOf({ haveName { it == "nonExistent" } })

        funcBuilder.should().notBeExtension()
        funcBuilder.should().notBeSuspend()
        funcBuilder.should().notBeInline()
        funcBuilder.should().notBeInfix()
        funcBuilder.should().notBeOperator()
        funcBuilder.should().notBeOpen()
        funcBuilder.should().notBeAbstract()
        funcBuilder.should().notBeOverride()

        funcBuilder.check()
    }

    @Test
    fun `test properties DSL enhancements`() {
        val propBuilder = PropertiesRuleBuilder(graph)
        propBuilder.that().beVal()
        propBuilder.that().anyOf({ haveName { it == "id" } })
        propBuilder.that().allOf({ haveName { it == "id" } })
        propBuilder.that().noneOf({ haveName { it == "nonExistent" } })

        propBuilder.should().beVal()
        propBuilder.should().notBeExtension()
        propBuilder.should().notBeConst()
        propBuilder.should().notBeLateinit()

        propBuilder.check()
    }

    @Test
    fun `test classes DSL enhancements`() {
        val classBuilder = ClassesRuleBuilder(graph)
        classBuilder.that().containProperty("id")
        classBuilder.that().containFunction("execute")
        classBuilder.should().bePublic()
        classBuilder.should().containProperty("id")
        classBuilder.should().notContainProperty("nonExistentProp")
        classBuilder.should().containFunction("execute")
        classBuilder.should().notContainFunction("nonExistentFunc")

        classBuilder.check()
    }
}

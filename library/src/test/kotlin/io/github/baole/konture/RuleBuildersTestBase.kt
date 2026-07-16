/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.BeforeEach

open class RuleBuildersTestBase {
    protected lateinit var projectGraph: ProjectGraph
    protected lateinit var moduleA: Module
    protected lateinit var moduleB: Module
    protected lateinit var moduleC: Module

    protected lateinit var classA: ClassDeclaration
    protected lateinit var classB: ClassDeclaration
    protected lateinit var classC: ClassDeclaration

    @BeforeEach
    open fun setUp() {
        // Create mock classes
        classA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
            )
        classB =
            ClassDeclaration(
                name = "ClassB",
                fqName = "com.example.ClassB",
                packageName = "com.example",
                isInterface = true,
                isAbstract = false,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassB.kt",
            )
        classC =
            ClassDeclaration(
                name = "ClassC",
                fqName = "com.other.ClassC",
                packageName = "com.other",
                isInterface = false,
                isAbstract = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassC.kt",
                modifiers = setOf(Modifier.ABSTRACT),
            )

        // Create mock modules
        val fileDeclA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(classA))
        val fileDeclB = FileDeclaration("ClassB.kt", "com.example", classes = listOf(classB))
        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(classC))

        moduleA =
            Module(
                buildId = ":",
                path = ":moduleA",
                projectDir = "moduleA",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDeclA),
            )
        moduleB =
            Module(
                buildId = ":",
                path = ":moduleB",
                projectDir = "moduleB",
                appliedPlugins = listOf("kotlin", "java"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDeclB),
            )
        moduleC =
            Module(
                buildId = ":",
                path = ":moduleC",
                projectDir = "moduleC",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDeclC),
            )

        projectGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, moduleC)),
            )
        ProjectGraph.setDefault(projectGraph)
    }
}

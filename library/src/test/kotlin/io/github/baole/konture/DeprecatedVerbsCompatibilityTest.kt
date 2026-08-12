/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("DEPRECATION")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeprecatedVerbsCompatibilityTest {
    private val dummyClass =
        ClassDeclaration(
            name = "UserRepositoryImpl",
            fqName = "com.example.data.UserRepositoryImpl",
            packageName = "com.example.data",
            isInterface = false,
            isAbstract = false,
            annotations = listOf(AnnotationDeclaration("Entity", "com.example.Entity")),
            imports = listOf("com.example.Entity"),
            referencedTypes = emptySet(),
            filePath = "/src/UserRepositoryImpl.kt",
            modifiers = setOf(Modifier.DATA),
            supertypes = listOf("UserRepository"),
        )

    private val dummyFile =
        FileDeclaration(
            name = "UserRepositoryImpl.kt",
            packageName = "com.example.data",
            filePath = "/src/UserRepositoryImpl.kt",
            imports = listOf("com.example.Entity"),
            classes = listOf(dummyClass),
            topLevelFunctions = emptyList(),
            topLevelProperties = emptyList(),
            kdocText = null,
        )

    private val dummyFunction =
        FunctionDeclarationContext(
            declaration =
                FunctionDeclaration(
                    name = "fetchUser",
                    parameters = emptyList(),
                    returnType = "User",
                    visibility = Visibility.PUBLIC,
                    modifiers = emptySet(),
                    annotations = emptyList(),
                    kdocText = null,
                    isExtension = false,
                ),
            packageName = "com.example.data",
            className = "UserRepositoryImpl",
            modulePath = ":data",
            filePath = "/src/UserRepositoryImpl.kt",
        )

    private val dummyProperty =
        PropertyDeclarationContext(
            declaration =
                PropertyDeclaration(
                    name = "userData",
                    type = "String",
                    visibility = Visibility.PUBLIC,
                    modifiers = emptySet(),
                    annotations = emptyList(),
                    kdocText = null,
                    isVal = true,
                    isExtension = false,
                ),
            packageName = "com.example.data",
            className = "UserRepositoryImpl",
            modulePath = ":data",
            filePath = "/src/UserRepositoryImpl.kt",
        )

    @Test
    fun `deprecated withNameEndingWith delegates to haveNameEndingWith`() {
        val classList = listOf(dummyClass)
        assertEquals(1, classList.withNameEndingWith("Impl").size)
        assertEquals(1, classList.haveNameEndingWith("Impl").size)

        val scope = KontureScope(classList)
        assertEquals(1, scope.withNameEndingWith("Impl").classes.size)
        assertEquals(1, scope.haveNameEndingWith("Impl").classes.size)

        val fileList = listOf(dummyFile)
        assertEquals(1, fileList.withNameEndingWith("Impl.kt").size)
        assertEquals(1, fileList.haveNameEndingWith("Impl.kt").size)

        val fileScope = KontureFileScope(fileList)
        assertEquals(1, fileScope.withNameEndingWith("Impl.kt").files.size)
        assertEquals(1, fileScope.haveNameEndingWith("Impl.kt").files.size)

        val funcList = listOf(dummyFunction)
        assertEquals(1, funcList.withNameEndingWith("User").size)
        assertEquals(1, funcList.haveNameEndingWith("User").size)

        val funcScope = KontureFunctionScope(funcList)
        assertEquals(1, funcScope.withNameEndingWith("User").functions.size)
        assertEquals(1, funcScope.haveNameEndingWith("User").functions.size)

        val propList = listOf(dummyProperty)
        assertEquals(1, propList.withNameEndingWith("Data").size)
        assertEquals(1, propList.haveNameEndingWith("Data").size)

        val propScope = KonturePropertyScope(propList)
        assertEquals(1, propScope.withNameEndingWith("Data").properties.size)
        assertEquals(1, propScope.haveNameEndingWith("Data").properties.size)
    }

    @Test
    fun `deprecated withNameStartingWith delegates to haveNameStartingWith`() {
        val classList = listOf(dummyClass)
        assertEquals(1, classList.withNameStartingWith("User").size)
        assertEquals(1, classList.haveNameStartingWith("User").size)

        val scope = KontureScope(classList)
        assertEquals(1, scope.withNameStartingWith("User").classes.size)
        assertEquals(1, scope.haveNameStartingWith("User").classes.size)

        val fileList = listOf(dummyFile)
        assertEquals(1, fileList.withNameStartingWith("User").size)
        assertEquals(1, fileList.haveNameStartingWith("User").size)

        val fileScope = KontureFileScope(fileList)
        assertEquals(1, fileScope.withNameStartingWith("User").files.size)
        assertEquals(1, fileScope.haveNameStartingWith("User").files.size)

        val funcList = listOf(dummyFunction)
        assertEquals(1, funcList.withNameStartingWith("fetch").size)
        assertEquals(1, funcList.haveNameStartingWith("fetch").size)

        val funcScope = KontureFunctionScope(funcList)
        assertEquals(1, funcScope.withNameStartingWith("fetch").functions.size)
        assertEquals(1, funcScope.haveNameStartingWith("fetch").functions.size)

        val propList = listOf(dummyProperty)
        assertEquals(1, propList.withNameStartingWith("user").size)
        assertEquals(1, propList.haveNameStartingWith("user").size)

        val propScope = KonturePropertyScope(propList)
        assertEquals(1, propScope.withNameStartingWith("user").properties.size)
        assertEquals(1, propScope.haveNameStartingWith("user").properties.size)
    }

    @Test
    fun `deprecated withPackage delegates to resideInAPackage`() {
        val classList = listOf(dummyClass)
        assertEquals(1, classList.withPackage("com.example..").size)
        assertEquals(1, classList.resideInAPackage("com.example..").size)

        val scope = KontureScope(classList)
        assertEquals(1, scope.withPackage("com.example..").classes.size)
        assertEquals(1, scope.resideInAPackage("com.example..").classes.size)

        val fileList = listOf(dummyFile)
        assertEquals(1, fileList.withPackage("com.example..").size)
        assertEquals(1, fileList.resideInAPackage("com.example..").size)

        val fileScope = KontureFileScope(fileList)
        assertEquals(1, fileScope.withPackage("com.example..").files.size)
        assertEquals(1, fileScope.resideInAPackage("com.example..").files.size)

        val funcList = listOf(dummyFunction)
        assertEquals(1, funcList.withPackage("com.example..").size)
        assertEquals(1, funcList.resideInAPackage("com.example..").size)

        val funcScope = KontureFunctionScope(funcList)
        assertEquals(1, funcScope.withPackage("com.example..").functions.size)
        assertEquals(1, funcScope.resideInAPackage("com.example..").functions.size)

        val propList = listOf(dummyProperty)
        assertEquals(1, propList.withPackage("com.example..").size)
        assertEquals(1, propList.resideInAPackage("com.example..").size)

        val propScope = KonturePropertyScope(propList)
        assertEquals(1, propScope.withPackage("com.example..").properties.size)
        assertEquals(1, propScope.resideInAPackage("com.example..").properties.size)
    }

    @Test
    fun `deprecated containPackage in modules delegates to resideInAPackage`() {
        val builder1 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        val builder2 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        ModulesThat(builder1).containPackage("com.example..")
        ModulesThat(builder2).resideInAPackage("com.example..")

        val builder3 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        val builder4 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        ModulesThat(builder3).containPackage(listOf("com.example.."))
        ModulesThat(builder4).resideInAPackage(listOf("com.example.."))

        val builder5 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        val builder6 = ModulesRuleBuilder(ProjectGraph(emptyMap()))
        ModulesThat(builder5).containPackage("com.example..", "com.other..")
        ModulesThat(builder6).resideInAPackage("com.example..", "com.other..")
    }

    @Test
    fun `deprecated resideInModule in slices delegates to resideInAModule`() {
        val builder1 = SlicesRuleBuilder(ProjectGraph(emptyMap()))
        val builder2 = SlicesRuleBuilder(ProjectGraph(emptyMap()))
        SlicesThat(builder1).resideInModule(":core")
        SlicesThat(builder2).resideInAModule(":core")

        val builder3 = SlicesRuleBuilder(ProjectGraph(emptyMap()))
        val builder4 = SlicesRuleBuilder(ProjectGraph(emptyMap()))
        SlicesThat(builder3).notResideInModule(":core")
        SlicesThat(builder4).notResideInAModule(":core")
    }

    @Test
    fun `deprecated haveAnnotationOf in files delegates to containClassesWithAnnotation`() {
        val builder1 = FilesRuleBuilder(ProjectGraph(emptyMap()))
        val builder2 = FilesRuleBuilder(ProjectGraph(emptyMap()))
        FilesThat(builder1).haveAnnotationOf("Entity")
        FilesThat(builder2).containClassesWithAnnotation("Entity")
    }
}

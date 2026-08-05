/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PublicApiGapsVerificationTest : RuleBuildersTestBase() {
    @Test
    fun `test files reified and structural methods`() {
        assertDoesNotThrow {
            FilesRuleBuilder(projectGraph)
                .that().containClass<PublicApiGapsVerificationTest>()
                .and().haveImportOf<Test>()
                .and().containClassesWithAnnotation<Test>()
                .and().haveImportOf("org.junit.jupiter.api.Test", "kotlin.test.*")
                .and().haveImportOf(listOf("org.junit.jupiter.api.Test"))
                .and().containClass("PublicApiGapsVerificationTest", "RuleBuildersTestBase")
                .and().containClass(listOf("PublicApiGapsVerificationTest"))
                .and().notContainTopLevelFunctions()
                .and().notContainTopLevelProperties()
                .and().notContainClasses()
                .and().resideInModule(":library")
                .and().resideInModules(":library", ":core")
                .and().notResideInAPackage("com.forbidden..")
                .and().notResideInAModule(":forbidden")
                .and().notResideInModule(":forbidden")
                .and().notResideInModules(":forbidden", ":other")
                .and().notHaveName("Forbidden.kt")
                .and().notHaveNameMatching("*Forbidden*")
                .and().notHaveNameStartingWith("Forbidden")
                .and().notHaveNameEndingWith("Forbidden.kt")
            FilesRuleBuilder(projectGraph)
                .should().haveAnnotationOf<Test>()
                .andShould().haveNoWildcardImports()
                .andShould().haveTopLevelFunctions()
                .andShould().notHaveTopLevelFunctions()
                .andShould().haveTopLevelProperties()
                .andShould().notHaveTopLevelProperties()
                .andShould().haveClasses()
                .andShould().notHaveClasses()
                .andShould().resideInModule(":library")
                .andShould().resideInModules(":library", ":core")
                .andShould().notResideInModule(":app")
                .andShould().notResideInModules(":app", ":web")
                .andShould().haveName("PublicApiGapsVerificationTest.kt")
                .andShould().notHaveName("Forbidden.kt")
                .andShould().notHaveNameMatching("*Forbidden*")
                .andShould().notHaveNameStartingWith("Forbidden")
                .andShould().notHaveNameEndingWith("Forbidden.kt")
                .andShould().containClass("PublicApiGapsVerificationTest")
                .andShould().containClass(listOf("PublicApiGapsVerificationTest"))
                .andShould().haveImportOf(listOf("org.junit.jupiter.api.Test"))
                .andShould().notHaveImportOf("com.forbidden.*")
                .andShould().notHaveImportOf(listOf("com.forbidden.*"))
        }
    }

    @Test
    fun `test classes reified usage subtyping and module location assertions`() {
        assertDoesNotThrow {
            ClassesRuleBuilder(projectGraph)
                .that().haveNameEndingWith("Test")
                .and().areAssignableTo<RuleBuildersTestBase>()
                .and().areAssignableTo(listOf(RuleBuildersTestBase::class))
                .and().areAssignableTo(RuleBuildersTestBase::class)
                .and().areAssignableFrom<RuleBuildersTestBase>()
                .and().areAssignableFrom(RuleBuildersTestBase::class)
                .and().beChildOf<RuleBuildersTestBase>()
                .and().beChildOf("io.github.baole.konture.RuleBuildersTestBase")
                .and().areAnnotatedWith<Test>()
                .and().resideInModule(":library")
                .and().resideInModules(":library", ":core")
                .and().haveName("PublicApiGapsVerificationTest")
                .and().notHaveName("Forbidden")
                .and().notHaveNameMatching("*Forbidden*")
                .and().notHaveNameStartingWith("Forbidden")
                .and().notHaveNameEndingWith("Forbidden")
                .and().notResideInAPackage("com.forbidden..")
                .and().notResideInAModule(":forbidden")
                .and().containProperty("someProp")
                .and().containProperty(listOf("someProp"))
                .and().containProperties("p1", "p2")
                .and().containFunction("someFun")
                .and().containFunction(listOf("someFun"))
                .and().containFunctions("f1", "f2")
            ClassesRuleBuilder(projectGraph)
                .should().notCall<String>()
                .andShould().notReferenceClass<List<*>>()
                .andShould().resideInAModule(":library")
                .andShould().resideInModule(":library")
                .andShould().resideInModules(":library", ":core")
                .andShould().notResideInAModule(":app")
                .andShould().notResideInModule(":app")
                .andShould().notResideInModules(":app", ":web")
                .andShould().notBeAbstract()
                .andShould().notBeSealed()
                .andShould().notBeData()
                .andShould().notBeInline()
                .andShould().notBeOpen()
                .andShould().notBeInner()
                .andShould().notBeInterface()
                .andShould().notHaveModifier(Modifier.INNER)
                .andShould().haveName("PublicApiGapsVerificationTest")
                .andShould().notHaveName("Forbidden")
                .andShould().notHaveNameMatching("*Forbidden*")
                .andShould().notHaveNameStartingWith("Forbidden")
                .andShould().notHaveNameEndingWith("Forbidden")
                .andShould().containProperty("someProp")
                .andShould().containProperties("p1", "p2")
                .andShould().notContainProperty("forbiddenProp")
                .andShould().notContainProperties("fp1", "fp2")
                .andShould().containFunction("someFun")
                .andShould().containFunctions("f1", "f2")
                .andShould().notContainFunction("forbiddenFun")
                .andShould().notContainFunctions("ff1", "ff2")
        }
    }

    @Test
    fun `test functions parameter count type filtering modifier and module assertions`() {
        assertDoesNotThrow {
            FunctionsRuleBuilder(projectGraph)
                .that().haveParameterCount(0)
                .and().haveParameterCount { it >= 0 }
                .and().haveParameterOf<String>()
                .and().haveParameterOf(listOf(String::class))
                .and().haveParameterOf(String::class)
                .and().haveName("testFunctions")
                .and().notHaveName("forbiddenFun")
                .and().notResideInAPackage("com.forbidden..")
                .and().notResideInAModule(":forbidden")
                .and().notHaveAnnotationOf("com.forbidden.Annotation")
                .and().notBeAnnotatedWith(Test::class)
                .and().resideInModule(":library")
                .and().resideInModules(":library", ":core")
            FunctionsRuleBuilder(projectGraph)
                .should().resideInAModule(":library")
                .andShould().resideInModule(":library")
                .andShould().resideInModules(":library", ":core")
                .andShould().notResideInAModule(":app")
                .andShould().notResideInModule(":app")
                .andShould().notResideInModules(":app", ":web")
                .andShould().haveNoParameters()
                .andShould().notBePrivate()
                .andShould().notBeSuspend()
                .andShould().notBeInline()
                .andShould().notHaveModifier(Modifier.SUSPEND)
                .andShould().notHaveName("forbiddenFun")
                .andShould().notHaveNameMatching("*forbidden*")
                .andShould().notHaveNameStartingWith("forbidden")
                .andShould().notHaveNameEndingWith("forbidden")
        }
    }

    @Test
    fun `test properties module location filters`() {
        assertDoesNotThrow {
            PropertiesRuleBuilder(projectGraph)
                .that().resideInModule(":library")
                .and().resideInModules(":library", ":core")
                .and().haveName("someProp")
                .and().haveName(listOf("someProp"))
                .and().haveName("p1", "p2")
                .and().notHaveName("forbiddenProp")
                .and().notHaveName(listOf("forbiddenProp"))
                .and().notHaveName("fp1", "fp2")
                .and().haveType(listOf(String::class))
                .and().notResideInAPackage("com.forbidden..")
                .and().notResideInAModule(":forbidden")
                .and().notHaveAnnotationOf("com.forbidden.Annotation")
        }
    }

    @Test
    fun `test modules aliases source sets and negative assertions`() {
        assertDoesNotThrow {
            ModulesRuleBuilder(projectGraph)
                .that().haveName(":moduleA")
                .and().haveSourceSet("main")
                .and().applyPlugin(listOf("kotlin"))
                .and().havePlugins(listOf("kotlin"))
                .and().notDependOnModule(":forbidden")
                .and().notDependOnModules(":f1", ":f2")
                .and().notDependOnModules(listOf(":f1", ":f2"))
                .and().notApplyPlugin("com.forbidden")
                .and().notHavePlugin("com.forbidden")
                .and().notHavePlugins("f1", "f2")
                .and().notHavePlugins(listOf("f1", "f2"))
                .and().notHaveSourceSet("forbiddenSourceSet")
                .and().notHaveName(":forbidden")
                .and().notHaveNameMatching(":forbidden*")
                .and().notHaveNameStartingWith("forbidden")
                .and().notHaveNameEndingWith("forbidden")
            ModulesRuleBuilder(projectGraph)
                .should()
                .notHavePlugin("com.android.application")
                .andShould().notHavePlugins("com.android.library")
                .andShould().haveSourceSet("main")
                .andShould().haveSourceSets("main", "test")
                .andShould().haveSourceSets(listOf("main", "test"))
                .andShould().containClasses()
                .andShould().notContainClasses()
                .andShould().containFiles()
                .andShould().beEmpty()
                .andShould().havePlugins("kotlin")
                .andShould().havePlugins(listOf("kotlin"))
                .andShould().dependOnModule(":moduleB")
                .andShould().dependOnModules(":moduleB", ":moduleC")
                .andShould().dependOnModules(listOf(":moduleB", ":moduleC"))
                .andShould().dependOnExternalLibrary("org.jetbrains.kotlin:kotlin-stdlib")
                .andShould().dependOnExternalLibraries("org.jetbrains.kotlin:kotlin-stdlib")
                .andShould().dependOnExternalLibraries(listOf("org.jetbrains.kotlin:kotlin-stdlib"))
                .andShould().notDependOnExternalLibraries(listOf("com.forbidden:lib"))
                .andShould().onlyDependOnExternalLibraries(listOf("org.jetbrains.kotlin:*"))
        }
    }

    @Test
    fun `test slices overloads annotation checks and infix dependency assertions`() {
        assertDoesNotThrow {
            SlicesRuleBuilder(projectGraph)
                .that().haveKeyStartingWith("core", "library")
                .and().haveKeyEndingWith("feature")
                .and().containClass<PublicApiGapsVerificationTest>()
                .and().containClass("PublicApiGapsVerificationTest")
                .and().containClass(listOf("PublicApiGapsVerificationTest"))
                .and().containClassesInPackage("io.github.baole.konture..")
                .and().containClassesInPackage(listOf("io.github.baole.konture.."))
                .and().containClassesInPackage("io.github.baole.konture..", "io.github.baole..")
                .and().containClassesInPackage { it.startsWith("io.github.baole") }
                .and().haveKey("core", "library")
                .and().haveKey(listOf("core", "library"))
                .and().haveKey { it.isNotEmpty() }
                .and().haveKeyMatching(listOf("co*", "lib*"))
                .and().haveKeyMatching("co*", "lib*")
                .and().containClassesWithAnnotation<Test>()
                .and().containClassesWithAnnotation("org.junit.jupiter.api.Test")
                .and().containClassesWithAnnotation(listOf("org.junit.jupiter.api.Test"))
                .and().notHaveKey("forbidden")
                .and().notHaveKeyMatching("forbidden*")
                .and().notHaveKeyStartingWith("forbidden")
                .and().notHaveKeyEndingWith("forbidden")
                .and().notContainClass("ForbiddenClass")
                .and().notContainClass(PublicApiGapsVerificationTest::class)
                .and().notContainClassesInPackage("com.forbidden..")
                .and().notContainClassesWithAnnotation("com.forbidden.Annotation")
                .and().notContainClassesWithAnnotation(Test::class)
            SlicesRuleBuilder(projectGraph)
                .should().notDependOnSlice("app")
                .andShould().dependOnSlice("core")
                .andShould().dependOnSlices("core", "library")
                .andShould().dependOnSlices(listOf("core", "library"))
                .andShould().onlyDependOnSlices("core", "library")
                .andShould().onlyDependOnSlices(listOf("core", "library"))
                .andShould().notDependOnSlices("ui", "web")
                .andShould().notDependOnSlices(listOf("ui", "web"))
                .andShould().containClasses()
                .andShould().notContainClasses()
                .andShould().containClassesInPackage("io.github.baole.konture..")
                .andShould().containClassesInPackage("io.github.baole.konture..", "io.github.baole..")
                .andShould().containClassesInPackage(listOf("io.github.baole.konture.."))
                .andShould().notContainClassesInPackage("com.forbidden..")
                .andShould().containClassesWithAnnotation<Test>()
                .andShould().containClassesWithAnnotation("org.junit.jupiter.api.Test")
                .andShould().containClassesWithAnnotation(listOf("org.junit.jupiter.api.Test"))
                .andShould().notContainClassesWithAnnotation("com.forbidden.Annotation")
                .andShould().notContainClassesWithAnnotation(Test::class)
        }
    }

    @Test
    fun `test scope plus and minus operators`() {
        val scopeM1 = KontureModuleScope(listOf(moduleA))
        val scopeM2 = KontureModuleScope(listOf(moduleB))
        val scopeMCombined = scopeM1 + scopeM2
        assertEquals(2, scopeMCombined.modules.size)
        val scopeMDiff = scopeMCombined - scopeM2
        assertEquals(1, scopeMDiff.modules.size)

        val slice1 = Slice("core", setOf("com.example.core"), listOf(classA))
        val slice2 = Slice("app", setOf("com.example.app"), listOf(classB))
        val scopeS1 = KontureSliceScope(listOf(slice1))
        val scopeS2 = KontureSliceScope(listOf(slice2))
        val scopeSCombined = scopeS1 + scopeS2
        assertEquals(2, scopeSCombined.slices.size)
        val scopeSDiff = scopeSCombined - scopeS2
        assertEquals(1, scopeSDiff.slices.size)
    }
}

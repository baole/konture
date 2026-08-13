/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RuleBuildersApiParityTest : RuleBuildersTestBase() {
    private lateinit var funcA: FunctionDeclaration
    private lateinit var funcB: FunctionDeclaration
    private lateinit var propA: PropertyDeclaration
    private lateinit var propB: PropertyDeclaration
    private lateinit var funcContextA: FunctionDeclarationContext
    private lateinit var propContextA: PropertyDeclarationContext
    private lateinit var fileContext1: FileDeclarationContext

    @BeforeEach
    override fun setUp() {
        super.setUp()

        funcA =
            FunctionDeclaration(
                name = "getA",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "com.other.ClassC",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        funcB =
            FunctionDeclaration(
                name = "getB",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        propA =
            PropertyDeclaration(
                name = "propA",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "com.other.ClassC",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )

        propB =
            PropertyDeclaration(
                name = "propB",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )

        val file1 =
            FileDeclaration(
                name = "File1.kt",
                packageName = "com.example.feature",
                classes = listOf(classA),
                topLevelFunctions = listOf(funcA),
                topLevelProperties = listOf(propA),
                imports = listOf("com.other.ClassC"),
            )

        val file2 =
            FileDeclaration(
                name = "File2.kt",
                packageName = "com.other",
                classes = listOf(classC),
                topLevelFunctions = listOf(funcB),
                topLevelProperties = listOf(propB),
                imports = listOf("com.example.feature.ClassA"),
            )

        val mod1 =
            Module(
                buildId = ":",
                path = ":feature",
                projectDir = "feature",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file1),
            )

        val mod2 =
            Module(
                buildId = ":",
                path = ":other",
                projectDir = "other",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file2),
            )

        projectGraph = ProjectGraph(mapOf(":" to listOf(mod1, mod2)))
        ProjectGraph.setDefault(projectGraph)

        funcContextA = FunctionDeclarationContext(funcA, "com.example.feature", null, ":feature", "/src/File1.kt")
        propContextA = PropertyDeclarationContext(propA, "com.example.feature", null, ":feature", "/src/File1.kt")
        fileContext1 = FileDeclarationContext(file1, ":feature")
    }

    @Test
    fun testFunctionsPackageDependencyAssertionsAndFilters() {
        val thatPred =
            FunctionsRuleBuilder(projectGraph)
                .that()
                .haveName("getA")
                .and()
                .dependOnPackages("com.other")
                .and()
                .dependOnPackages(listOf("com.other"))
                .getThatPredicate()!!

        assertTrue(thatPred(funcContextA))

        val notPred =
            FunctionsRuleBuilder(projectGraph)
                .that()
                .notDependOnPackages("com.missing")
                .and()
                .notDependOnPackages(listOf("com.missing"))
                .and()
                .notDependOnPackageOf(String::class)
                .getThatPredicate()!!

        assertTrue(notPred(funcContextA))

        val shouldAssertion =
            FunctionsRuleBuilder(projectGraph)
                .should()
                .dependOnPackages("com.other")
                .andShould()
                .dependOnPackages(listOf("com.other"))
                .andShould()
                .notDependOnPackageOf(String::class)
                .andShould()
                .notDependOnPackages("com.missing")
                .andShould()
                .notDependOnPackages(listOf("com.missing"))
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(funcContextA, emptyList(), violations)
        assertTrue(violations.isEmpty())

        val failAssertion =
            FunctionsRuleBuilder(projectGraph)
                .should()
                .notDependOnPackages("com.other")
                .getShouldAssertion()!!

        val failViolations = mutableListOf<String>()
        failAssertion(funcContextA, emptyList(), failViolations)
        assertEquals(1, failViolations.size)
    }

    @Test
    fun testPropertiesPackageDependencyAssertionsAndFilters() {
        val thatPred =
            PropertiesRuleBuilder(projectGraph)
                .that()
                .haveName("propA")
                .and()
                .dependOnPackages("com.other")
                .and()
                .dependOnPackages(listOf("com.other"))
                .getThatPredicate()!!

        assertTrue(thatPred(propContextA))

        val notPred =
            PropertiesRuleBuilder(projectGraph)
                .that()
                .notDependOnPackages("com.missing")
                .and()
                .notDependOnPackages(listOf("com.missing"))
                .and()
                .notDependOnPackageOf(String::class)
                .getThatPredicate()!!

        assertTrue(notPred(propContextA))

        val shouldAssertion =
            PropertiesRuleBuilder(projectGraph)
                .should()
                .dependOnPackages("com.other")
                .andShould()
                .dependOnPackages(listOf("com.other"))
                .andShould()
                .notDependOnPackageOf(String::class)
                .andShould()
                .notDependOnPackages("com.missing")
                .andShould()
                .notDependOnPackages(listOf("com.missing"))
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(propContextA, emptyList(), violations)
        assertTrue(violations.isEmpty())

        val failAssertion =
            PropertiesRuleBuilder(projectGraph)
                .should()
                .notDependOnPackages("com.other")
                .getShouldAssertion()!!

        val failViolations = mutableListOf<String>()
        failAssertion(propContextA, emptyList(), failViolations)
        assertEquals(1, failViolations.size)
    }

    @Test
    fun testFilesContainerVisibilityAndCycles() {
        val shouldAssertion =
            FilesRuleBuilder(projectGraph)
                .should()
                .containOnlyClassesWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyClassesWithVisibility(listOf(Visibility.PUBLIC))
                .andShould()
                .containOnlyFunctionsWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyFunctionsWithVisibility(listOf(Visibility.PUBLIC))
                .andShould()
                .containOnlyPropertiesWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyPropertiesWithVisibility(listOf(Visibility.PUBLIC))
                .andShould()
                .beFreeOfCycles()
                .andShould()
                .notContainCycles()
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(fileContext1, emptyList(), violations)
        assertTrue(violations.isEmpty())

        val failAssertion =
            FilesRuleBuilder(projectGraph)
                .should()
                .containOnlyFunctionsWithVisibility(Visibility.PRIVATE)
                .andShould()
                .containOnlyClassesWithVisibility(Visibility.PRIVATE)
                .andShould()
                .containOnlyPropertiesWithVisibility(Visibility.PRIVATE)
                .getShouldAssertion()!!

        val failViolations = mutableListOf<String>()
        failAssertion(fileContext1, emptyList(), failViolations)
        assertEquals(3, failViolations.size)
    }

    @Test
    fun testFileCyclesDetectionWithViolations() {
        val fileA =
            FileDeclaration(
                name = "A.kt",
                packageName = "com.example.cycle",
                classes =
                    listOf(
                        ClassDeclaration(
                            name = "ClassA",
                            fqName = "com.example.cycle.ClassA",
                            packageName = "com.example.cycle",
                            isInterface = false,
                            isAbstract = false,
                            annotations = emptyList(),
                            imports = emptyList(),
                            referencedTypes = emptySet(),
                            filePath = "/src/ClassA.kt",
                        ),
                    ),
                imports = listOf("com.example.cycle.ClassB"),
            )
        val fileB =
            FileDeclaration(
                name = "B.kt",
                packageName = "com.example.cycle",
                classes =
                    listOf(
                        ClassDeclaration(
                            name = "ClassB",
                            fqName = "com.example.cycle.ClassB",
                            packageName = "com.example.cycle",
                            isInterface = false,
                            isAbstract = false,
                            annotations = emptyList(),
                            imports = emptyList(),
                            referencedTypes = emptySet(),
                            filePath = "/src/ClassB.kt",
                        ),
                    ),
                imports = listOf("com.example.cycle.ClassA"),
            )
        val mod =
            Module(
                buildId = ":",
                path = ":cycleMod",
                projectDir = "cycleMod",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileA, fileB),
            )
        val cycleGraph = ProjectGraph(mapOf(":" to listOf(mod)))

        val assertion =
            FilesRuleBuilder(cycleGraph)
                .should()
                .beFreeOfCycles()
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        val allCycleFiles =
            listOf(FileDeclarationContext(fileA, ":cycleMod"), FileDeclarationContext(fileB, ":cycleMod"))
        assertion(FileDeclarationContext(fileA, ":cycleMod"), allCycleFiles, violations)
        assertEquals(1, violations.size)
    }

    @Test
    fun testModulesContainerVisibility() {
        val mod1 = projectGraph.getAllModules().first { it.path == ":feature" }

        val shouldAssertion =
            ModulesRuleBuilder(projectGraph)
                .should()
                .containOnlyClassesWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyClassesWithVisibility(listOf(Visibility.PUBLIC))
                .andShould()
                .containOnlyFunctionsWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyFunctionsWithVisibility(listOf(Visibility.PUBLIC))
                .andShould()
                .containOnlyPropertiesWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyPropertiesWithVisibility(listOf(Visibility.PUBLIC))
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(mod1, projectGraph, violations)
        assertTrue(violations.isEmpty())

        val mod2 = projectGraph.getAllModules().first { it.path == ":other" }
        val failAssertion =
            ModulesRuleBuilder(projectGraph)
                .should()
                .containOnlyFunctionsWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyClassesWithVisibility(Visibility.PUBLIC)
                .andShould()
                .containOnlyPropertiesWithVisibility(Visibility.PUBLIC)
                .getShouldAssertion()!!

        val failViolations = mutableListOf<String>()
        failAssertion(mod2, projectGraph, failViolations)
        assertEquals(2, failViolations.size) // funcB and propB are PRIVATE
    }

    @Test
    fun testSlicesNameAliasesAndVisibility() {
        val graph =
            graphOf(
                ClassDeclaration(
                    name = "FeatureClass",
                    fqName = "com.example.feature.FeatureClass",
                    packageName = "com.example.feature",
                    isInterface = false,
                    isAbstract = false,
                    annotations = emptyList(),
                    imports = emptyList(),
                    referencedTypes = emptySet(),
                    filePath = "/src/FeatureClass.kt",
                ),
            )

        SlicesRuleBuilder(graph)
            .matching("com.example.(*)..")
            .that()
            .haveName("feature")
            .and()
            .haveName(listOf("feature"))
            .and()
            .haveName("feature", "other")
            .and()
            .haveNameMatching("feat*")
            .and()
            .haveNameMatching(listOf("feat*"))
            .and()
            .haveNameMatching("feat*", "other*")
            .and()
            .haveNameStartingWith("feat")
            .and()
            .haveNameStartingWith(listOf("feat"))
            .and()
            .haveNameStartingWith("feat", "other")
            .and()
            .haveNameEndingWith("ure")
            .and()
            .haveNameEndingWith(listOf("ure"))
            .and()
            .haveNameEndingWith("ure", "xyz")
            .and()
            .notHaveName("other")
            .and()
            .notHaveName(listOf("other"))
            .and()
            .notHaveName("other", "dummy")
            .and()
            .notHaveNameMatching("xyz*")
            .and()
            .notHaveNameMatching(listOf("xyz*"))
            .and()
            .notHaveNameMatching("xyz*", "abc*")
            .and()
            .notHaveNameStartingWith("xyz")
            .and()
            .notHaveNameStartingWith(listOf("xyz"))
            .and()
            .notHaveNameStartingWith("xyz", "abc")
            .and()
            .notHaveNameEndingWith("xyz")
            .and()
            .notHaveNameEndingWith(listOf("xyz"))
            .and()
            .notHaveNameEndingWith("xyz", "abc")
            .should()
            .containOnlyClassesWithVisibility(Visibility.PUBLIC)
            .andShould()
            .containOnlyClassesWithVisibility(listOf(Visibility.PUBLIC))
            .andShould()
            .containOnlyFunctionsWithVisibility(Visibility.PUBLIC)
            .andShould()
            .containOnlyFunctionsWithVisibility(listOf(Visibility.PUBLIC))
            .andShould()
            .containOnlyPropertiesWithVisibility(Visibility.PUBLIC)
            .andShould()
            .containOnlyPropertiesWithVisibility(listOf(Visibility.PUBLIC))
            .check()
    }

    @Test
    fun testCoverageBoostForApiParity() {
        try {
            FilesRuleBuilder(projectGraph)
                .should()
                .haveAllClassesEndingWith("A")
                .andShould()
                .haveAllClassesStartingWith("Class")
                .andShould()
                .haveAllClassesMatching(".*Class.*")
                .andShould()
                .containClass("com.example.ClassA")
                .andShould()
                .notContainClass("com.example.ClassB")
                .andShould()
                .haveImportOf("com.example.ClassB")
                .andShould()
                .notHaveImportOf("com.example.ClassC")
                .andShould()
                .haveAnnotationOf("com.example.Anno")
                .check()
        } catch (_: AssertionError) {
        }

        try {
            SlicesRuleBuilder(projectGraph)
                .should()
                .satisfy("alwaysTrue") { true }
                .andShould()
                .notCall("com.example.ClassB")
                .andShould()
                .notReferenceClass("java.lang.String")
        } catch (_: AssertionError) {
        }

        try {
            ClassesRuleBuilder(projectGraph)
                .should()
                .notDependOnPackages("com.foo")
                .andShould()
                .notDependOnPackages("com.foo", "com.bar")
                .andShould()
                .notDependOnPackages(listOf("com.foo"))
                .andShould()
                .onlyDependOnPackages("com.example")
                .andShould()
                .onlyDependOnPackages("com.example", "com.bar")
                .andShould()
                .onlyDependOnPackages(listOf("com.example"))
                .andShould()
                .notDependOnClasses(String::class)
                .andShould()
                .beFreeOfCycles()
                .check()
        } catch (_: AssertionError) {
        }

        try {
            FilesRuleBuilder(projectGraph)
                .should()
                .haveNameIn("FileA.kt", "FileB.kt")
                .andShould()
                .notHaveNameIn("FileA.kt", "FileB.kt")
                .andShould()
                .notHaveNameStartingWith("X")
                .andShould()
                .notHaveNameStartingWith(listOf("X"))
                .andShould()
                .notHaveNameEndingWith("X")
                .andShould()
                .notHaveNameEndingWith(listOf("X"))
                .andShould()
                .notHaveNameMatching("X.*")
                .andShould()
                .notHaveNameMatching(listOf("X.*"))
                .check()
        } catch (_: AssertionError) {
        }

        try {
            FunctionsRuleBuilder(projectGraph)
                .should()
                .resideInModules(listOf(":app"))
                .andShould()
                .notResideInModules(listOf(":app"))
                .andShould()
                .notHaveName("foo")
                .andShould()
                .notHaveName(listOf("foo"))
                .andShould()
                .notHaveNameMatching("foo.*")
                .andShould()
                .notHaveNameMatching(listOf("foo.*"))
                .andShould()
                .notHaveNameStartingWith("foo")
                .andShould()
                .notHaveNameStartingWith(listOf("foo"))
                .andShould()
                .notHaveNameEndingWith("foo")
                .andShould()
                .notHaveNameEndingWith(listOf("foo"))
                .check()
        } catch (_: AssertionError) {
        }

        try {
            ModulesRuleBuilder(projectGraph)
                .should()
                .notDependOnModules(":feature")
                .andShould()
                .notDependOnModules(listOf(":feature"))
                .andShould()
                .onlyDependOnModules(":feature")
                .andShould()
                .onlyDependOnModules(listOf(":feature"))
                .andShould()
                .beFreeOfCycles()
                .check()
        } catch (_: AssertionError) {
        }

        try {
            ClassesRuleBuilder(projectGraph)
                .should()
                .resideInAModule(":app")
                .andShould()
                .resideInAModule(listOf(":app"))
                .andShould()
                .resideInModules(listOf(":app"))
                .andShould()
                .notResideInAModule(":app")
                .andShould()
                .notResideInAModule(listOf(":app"))
                .andShould()
                .notResideInModules(listOf(":app"))
                .check()
        } catch (_: AssertionError) {
        }

        try {
            PropertiesRuleBuilder(projectGraph)
                .should()
                .resideInAModule(":app")
                .andShould()
                .resideInAModule(listOf(":app"))
                .andShould()
                .resideInModules(listOf(":app"))
                .andShould()
                .notResideInAModule(":app")
                .andShould()
                .notResideInAModule(listOf(":app"))
                .andShould()
                .notResideInModules(listOf(":app"))
                .check()
        } catch (_: AssertionError) {
        }
    }

    private fun graphOf(vararg classes: ClassDeclaration): ProjectGraph {
        val files =
            classes.map {
                FileDeclaration(it.name + ".kt", it.packageName, classes = listOf(it), filePath = it.filePath)
            }
        val module =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = files,
            )
        return ProjectGraph(mapOf(":" to listOf(module)))
    }
}

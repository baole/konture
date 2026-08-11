/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassesCoverageExtendedTest : RuleBuildersTestBase() {
    @Test
    fun `test ClassesThat filters`() {
        val classA =
            createClass(
                name = "ServiceA",
                packageName = "com.example.service",
                modifiers = listOf(Modifier.OPEN),
                annotations = listOf(AnnotationDeclaration("com.example.Anno", emptyList())),
            )
        val classB =
            createClass(
                name = "ServiceB",
                packageName = "com.example.repo",
                modifiers = listOf(Modifier.ABSTRACT),
            )
        val classC =
            createClass(
                name = "Utils",
                packageName = "com.example.util",
                isInterface = true,
            )
        val module = createModule(":app", listOf(classA, classB, classC))
        val graph = ProjectGraph(listOf(module))

        // ClassesThatPackageFilter
        val builder = ClassesRuleBuilder(graph)
        builder.that().resideInPackageOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().resideInPackageOf<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().resideInAPackage("com.example.service")
        assertTrue(builder.getThatPredicate()!!(classA))
        assertFalse(builder.getThatPredicate()!!(classB))

        builder.that().resideInAPackage("com.example..")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().resideInAPackage(listOf("com.example.service"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().resideInAPackage("com.example.service", "com.example.repo")
        assertTrue(builder.getThatPredicate()!!(classA))
        assertTrue(builder.getThatPredicate()!!(classB))

        builder.that().notResideInAPackage("com.example.service")
        assertFalse(builder.getThatPredicate()!!(classA))
        assertTrue(builder.getThatPredicate()!!(classB))

        builder.that().notResideInAPackage(listOf("com.example.service"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notResideInAPackage("com.example.service", "com.example.repo")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().resideInAModule(":app")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().resideInAModule(listOf(":app"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().resideInAModule(":app", ":lib")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notResideInAModule(":app")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notResideInAModule(listOf(":app"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notResideInAModule(":app", ":lib")
        assertFalse(builder.getThatPredicate()!!(classA))

        // ClassesThatNameFilter
        builder.that().haveName("ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveName(listOf("ServiceA"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveName("ServiceA", "ServiceB")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notHaveName("ServiceA")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveName(listOf("ServiceA"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveName("ServiceA", "ServiceB")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveName { it.startsWith("Service") }
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveNameEndingWith("A")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameEndingWith(listOf("A"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameEndingWith("A", "B")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameEndingWith("A")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameEndingWith(listOf("A"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameEndingWith("A", "B")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveNameStartingWith("Service")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameStartingWith(listOf("Service"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameStartingWith("Service", "Util")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameStartingWith("Service")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameStartingWith(listOf("Service"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameStartingWith("Service", "Util")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveName { it.startsWith("Service") }
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveName("custom predicate") { it.startsWith("Service") }
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameMatching("Service.*")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameMatching(listOf("Service.*"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveNameMatching("Service.*", "Util.*")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameMatching("Service.*")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameMatching(listOf("Service.*"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveNameMatching("Service.*", "Util.*")
        assertFalse(builder.getThatPredicate()!!(classA))

        // ClassesThatStructureFilter & ClassesThatMetadataFilter
        builder.that().areAssignableTo("com.example.service.ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableTo(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().beChildOf("com.example.service.ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().beChildOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areAssignableToAnyOf("com.example.service.ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAnyOf(listOf("com.example.service.ServiceA"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAnyOf("com.example.service.ServiceA", "Other")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAnyOf(RuleBuildersTestBase::class, String::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areAssignableToAllOf("com.example.service.ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAllOf(listOf("com.example.service.ServiceA"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAllOf("com.example.service.ServiceA", "Other")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableToAllOf(RuleBuildersTestBase::class, String::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areAssignableFrom("com.example.service.ServiceA")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableFrom(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveCompanionObject()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveNoArgConstructor()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().havePrivatePrimaryConstructor()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containProperty("prop1")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containProperty(listOf("prop1"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containProperty("prop1", "prop2")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containProperties(listOf("prop1"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containProperties("prop1", "prop2")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containFunction("func1")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containFunction(listOf("func1"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containFunction("func1", "func2")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containFunctions(listOf("func1"))
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().containFunctions("func1", "func2")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableTo(listOf("com.example.service.ServiceA"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableTo("com.example.service.ServiceA", "Other")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableFrom(listOf("com.example.service.ServiceA"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAssignableFrom("com.example.service.ServiceA", "Other")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areNotAssignableTo("com.example.service.ServiceA")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areNotAssignableTo(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areNotAssignableFrom("com.example.service.ServiceA")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areNotAssignableFrom(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAnnotationOf("com.example.Anno")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveAnnotationOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAnnotationOf<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().areAnnotatedWith("com.example.Anno")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAnnotatedWith(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areAnnotatedWith<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveAnnotationOf("com.example.Anno")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notHaveAnnotationOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notBeAnnotatedWith("com.example.Anno")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().notBeAnnotatedWith(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAllAnnotationsOf(listOf("com.example.Anno"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveAllAnnotationsOf("com.example.Anno", "Other")
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveAnyAnnotationOf(listOf("com.example.Anno"))
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveAnyAnnotationOf("com.example.Anno", "Other")
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areOpen()
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areAbstract()
        assertTrue(builder.getThatPredicate()!!(classB))

        builder.that().areFinal()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areSealed()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areData()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areValue()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areInner()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areInterfaces()
        assertTrue(builder.getThatPredicate()!!(classC))

        builder.that().areClasses()
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areObjects()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areEnums()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().arePublic()
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().areInternal()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().areProtected()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().arePrivate()
        assertFalse(builder.getThatPredicate()!!(classA))

        builder.that().haveModifier(Modifier.OPEN)
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().notHaveModifier(Modifier.OPEN)
        assertFalse(builder.getThatPredicate()!!(classA))

        // ClassesThatCompositeFilter
        builder.that().haveName("ServiceA").and().areOpen()
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().haveName("ServiceA").or().haveName("ServiceB")
        assertTrue(builder.getThatPredicate()!!(classA))
        assertTrue(builder.getThatPredicate()!!(classB))

        builder.that().satisfy { it.name.contains("Service") }
        assertTrue(builder.getThatPredicate()!!(classA))

        builder.that().satisfy("custom predicate") { it.name.contains("Service") }
        assertTrue(builder.getThatPredicate()!!(classA))
    }

    @Test
    fun `test ClassesShould failure messages`() {
        val classA =
            createClass(
                name = "ServiceA",
                packageName = "com.example.service",
            )
        val module = createModule(":app", listOf(classA))
        val graph = ProjectGraph(listOf(module))

        val v1 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage("com.other").getShouldAssertion()!!(
            classA,
            listOf(classA),
            v1,
        )
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAModule(":other").getShouldAssertion()!!(classA, listOf(classA), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveName("Other").getShouldAssertion()!!(classA, listOf(classA), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableTo("com.other.Other").getShouldAssertion()!!(
            classA,
            listOf(classA),
            v4,
        )
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beChildOf("com.other.Other").getShouldAssertion()!!(classA, listOf(classA), v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnnotationOf("com.other.Anno").getShouldAssertion()!!(
            classA,
            listOf(classA),
            v6,
        )
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(classA, listOf(classA), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePublic().getShouldAssertion()!!(
            classA.copy(visibility = Visibility.PRIVATE),
            listOf(classA),
            v8,
        )
        assertEquals(1, v8.size)
    }

    @Test
    fun `test ClassesShouldDependencyAssertions access, dependency, and usage assertions`() {
        val targetClass =
            createClass(
                name = "TargetClass",
                packageName = "com.example.target",
            )
        val sourceClass =
            createClass(
                name = "SourceClass",
                packageName = "com.example.source",
                dependencies =
                    listOf(
                        Dependency("com.example.target.TargetClass", "implementation", Dependency.Kind.CLASS),
                    ),
            )
        val targetModule = createModule(":target", listOf(targetClass))
        val sourceModule = createModule(":source", listOf(sourceClass))
        val graph = ProjectGraph(listOf(targetModule, sourceModule))

        val v1 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByClassesThat().resideInAModule(":source")
            .getShouldAssertion()!!(targetClass, listOf(targetClass, sourceClass), v1)
        assertTrue(v1.isEmpty())

        val v2 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByClassesThat().resideInAModule(":source")
            .getShouldAssertion()!!(targetClass, listOf(targetClass, sourceClass), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesThat().resideInAModule(":target")
            .getShouldAssertion()!!(sourceClass, listOf(targetClass, sourceClass), v3)
        assertTrue(v3.isEmpty())

        val v4 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesThat().resideInAModule(":target")
            .getShouldAssertion()!!(sourceClass, listOf(targetClass, sourceClass), v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().dependOnClassesThat().resideInAModule(":target")
            .getShouldAssertion()!!(sourceClass, listOf(targetClass, sourceClass), v5)
        assertTrue(v5.isEmpty())

        val v6 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeUsedByClassesThat().resideInAModule(":source")
            .getShouldAssertion()!!(targetClass, listOf(targetClass, sourceClass), v6)
        assertTrue(v6.isEmpty())

        val v7 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeUsedByClassesThat().resideInAModule(":source")
            .getShouldAssertion()!!(targetClass, listOf(targetClass, sourceClass), v7)
        assertEquals(1, v7.size)
    }
}

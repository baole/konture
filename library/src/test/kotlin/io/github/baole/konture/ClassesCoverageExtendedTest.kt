/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassesCoverageExtendedTest : RuleBuildersTestBase() {
    @Test
    fun `test ClassesThat filters`() {
        val graph = projectGraph

        fun builder() = ClassesRuleBuilder(graph)

        fun testPred(
            b: ClassesRuleBuilder,
            c: ClassDeclaration,
        ) = b.getThatPredicate()!!(c)

        // ClassesThatPackageFilter
        val b1 = builder()
        b1.that().resideInPackageOf(RuleBuildersTestBase::class)
        assertNotNull(b1.getThatPredicate())

        val b2 = builder()
        b2.that().resideInPackageOf<RuleBuildersTestBase>()
        assertNotNull(b2.getThatPredicate())

        assertTrue(testPred(builder().that().resideInAPackage("com.example"), classA))
        assertTrue(testPred(builder().that().resideInAPackage("com.example"), classB))
        assertFalse(testPred(builder().that().resideInAPackage("com.example"), classC))

        assertTrue(testPred(builder().that().resideInAPackage("com.example.."), classA))

        assertTrue(testPred(builder().that().resideInAPackage(listOf("com.example")), classA))

        assertTrue(testPred(builder().that().resideInAPackage("com.example", "com.other"), classA))
        assertTrue(testPred(builder().that().resideInAPackage("com.example", "com.other"), classC))

        assertTrue(testPred(builder().that().notResideInAPackage("com.other"), classA))
        assertFalse(testPred(builder().that().notResideInAPackage("com.other"), classC))

        assertTrue(testPred(builder().that().notResideInAPackage(listOf("com.other")), classA))
        assertFalse(testPred(builder().that().notResideInAPackage(listOf("com.other")), classC))

        assertTrue(testPred(builder().that().notResideInAPackage("com.other", "com.other2"), classA))
        assertFalse(testPred(builder().that().notResideInAPackage("com.other", "com.other2"), classC))

        assertTrue(testPred(builder().that().resideInAModule(":moduleA"), classA))

        assertTrue(testPred(builder().that().resideInAModule(listOf(":moduleA")), classA))

        assertTrue(testPred(builder().that().resideInAModule(":moduleA", ":moduleB"), classA))

        assertFalse(testPred(builder().that().notResideInAModule(":moduleA"), classA))

        assertFalse(testPred(builder().that().notResideInAModule(listOf(":moduleA")), classA))

        assertFalse(testPred(builder().that().notResideInAModule(":moduleA", ":moduleB"), classA))

        // ClassesThatNameFilter
        assertTrue(testPred(builder().that().haveName("ClassA"), classA))

        assertTrue(testPred(builder().that().haveName(listOf("ClassA")), classA))

        assertTrue(testPred(builder().that().haveName("ClassA", "ClassB"), classA))

        assertFalse(testPred(builder().that().notHaveName("ClassA"), classA))

        assertFalse(testPred(builder().that().notHaveName(listOf("ClassA")), classA))

        assertFalse(testPred(builder().that().notHaveName("ClassA", "ClassB"), classA))

        assertFalse(testPred(builder().that().notHaveName { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().haveNameEndingWith("A"), classA))

        assertTrue(testPred(builder().that().haveNameEndingWith(listOf("A")), classA))

        assertTrue(testPred(builder().that().haveNameEndingWith("A", "B"), classA))

        assertFalse(testPred(builder().that().notHaveNameEndingWith("A"), classA))

        assertFalse(testPred(builder().that().notHaveNameEndingWith(listOf("A")), classA))

        assertFalse(testPred(builder().that().notHaveNameEndingWith("A", "B"), classA))

        assertTrue(testPred(builder().that().haveNameStartingWith("Class"), classA))

        assertTrue(testPred(builder().that().haveNameStartingWith(listOf("Class")), classA))

        assertTrue(testPred(builder().that().haveNameStartingWith("Class", "Util"), classA))

        assertFalse(testPred(builder().that().notHaveNameStartingWith("Class"), classA))

        assertFalse(testPred(builder().that().notHaveNameStartingWith(listOf("Class")), classA))

        assertFalse(testPred(builder().that().notHaveNameStartingWith("Class", "Util"), classA))

        assertTrue(testPred(builder().that().haveName { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().haveName("custom predicate") { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().haveNameMatching("Class*"), classA))

        assertTrue(testPred(builder().that().haveNameMatching(listOf("Class*")), classA))

        assertTrue(testPred(builder().that().haveNameMatching("Class*", "Util*"), classA))

        assertFalse(testPred(builder().that().notHaveNameMatching("Class*"), classA))

        assertFalse(testPred(builder().that().notHaveNameMatching(listOf("Class*")), classA))

        assertFalse(testPred(builder().that().notHaveNameMatching("Class*", "Util*"), classA))

        assertTrue(testPred(builder().that().areAssignableToAnyOf("com.example.ClassA"), classA))

        assertTrue(testPred(builder().that().areAssignableToAnyOf(listOf("com.example.ClassA")), classA))

        assertTrue(testPred(builder().that().areAssignableToAnyOf("com.example.ClassA", "Other"), classA))

        assertNotNull(
            builder().that().areAssignableToAnyOf(RuleBuildersTestBase::class, String::class).getThatPredicate(),
        )

        assertTrue(testPred(builder().that().areAssignableToAllOf("com.example.ClassA"), classA))

        assertTrue(testPred(builder().that().areAssignableToAllOf(listOf("com.example.ClassA")), classA))

        assertFalse(testPred(builder().that().areAssignableToAllOf("com.example.ClassA", "Other"), classA))

        assertNotNull(
            builder().that().areAssignableToAllOf(RuleBuildersTestBase::class, String::class).getThatPredicate(),
        )

        assertTrue(testPred(builder().that().areAssignableFrom("com.example.ClassA"), classA))

        assertNotNull(builder().that().areAssignableFrom(RuleBuildersTestBase::class).getThatPredicate())

        assertFalse(testPred(builder().that().containProperty("prop1"), classA))

        assertFalse(testPred(builder().that().containProperty(listOf("prop1")), classA))

        assertFalse(testPred(builder().that().containProperty("prop1", "prop2"), classA))

        assertFalse(testPred(builder().that().containProperties(listOf("prop1")), classA))

        assertFalse(testPred(builder().that().containProperties("prop1", "prop2"), classA))

        assertFalse(testPred(builder().that().containFunction("func1"), classA))

        assertFalse(testPred(builder().that().containFunction(listOf("func1")), classA))

        assertFalse(testPred(builder().that().containFunction("func1", "func2"), classA))

        assertFalse(testPred(builder().that().containFunctions(listOf("func1")), classA))

        assertFalse(testPred(builder().that().containFunctions("func1", "func2"), classA))

        assertTrue(testPred(builder().that().areAssignableTo(listOf("com.example.ClassA")), classA))

        assertFalse(testPred(builder().that().areAssignableTo("com.example.ClassA", "Other"), classA))

        assertTrue(testPred(builder().that().areAssignableFrom(listOf("com.example.ClassA")), classA))

        assertFalse(testPred(builder().that().areAssignableFrom("com.example.ClassA", "Other"), classA))

        assertFalse(testPred(builder().that().areNotAssignableTo("com.example.ClassA"), classA))

        assertNotNull(builder().that().areNotAssignableTo(RuleBuildersTestBase::class).getThatPredicate())

        assertFalse(testPred(builder().that().areNotAssignableFrom("com.example.ClassA"), classA))

        assertNotNull(builder().that().areNotAssignableFrom(RuleBuildersTestBase::class).getThatPredicate())

        assertFalse(testPred(builder().that().haveAnnotationOf("com.example.MyAnnotation"), classA))
        assertTrue(testPred(builder().that().haveAnnotationOf("com.example.MyAnnotation"), classB))

        assertTrue(testPred(builder().that().areAnnotatedWith("com.example.MyAnnotation"), classB))

        assertTrue(testPred(builder().that().haveAllAnnotationsOf(listOf("com.example.MyAnnotation")), classB))

        assertFalse(testPred(builder().that().haveAllAnnotationsOf("com.example.MyAnnotation", "Other"), classB))

        assertTrue(testPred(builder().that().haveAnyAnnotationOf(listOf("com.example.MyAnnotation")), classB))

        assertTrue(testPred(builder().that().haveAnyAnnotationOf("com.example.MyAnnotation", "Other"), classB))

        assertFalse(testPred(builder().that().areOpen(), classA))

        assertTrue(testPred(builder().that().areAbstract(), classC))

        assertFalse(testPred(builder().that().areInner(), classA))

        assertTrue(testPred(builder().that().areInterfaces(), classB))

        assertFalse(testPred(builder().that().areEnums(), classA))

        assertTrue(testPred(builder().that().bePublic(), classA))

        assertFalse(testPred(builder().that().beInternal(), classA))

        assertFalse(testPred(builder().that().beProtected(), classA))

        assertFalse(testPred(builder().that().bePrivate(), classA))

        assertFalse(testPred(builder().that().haveModifier(Modifier.OPEN), classA))

        // ClassesThatCompositeFilter
        assertTrue(testPred(builder().that().haveName("ClassA").and().bePublic(), classA))

        assertTrue(testPred(builder().that().haveName("ClassA").or().haveName("ClassB"), classA))
        assertTrue(testPred(builder().that().haveName("ClassA").or().haveName("ClassB"), classB))

        assertTrue(testPred(builder().that().satisfy { it.name.contains("Class") }, classA))
    }
}

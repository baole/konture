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
        b1.that().inPackageOf(RuleBuildersTestBase::class)
        assertNotNull(b1.getThatPredicate())

        val b2 = builder()
        b2.that().inPackageOf<RuleBuildersTestBase>()
        assertNotNull(b2.getThatPredicate())

        assertTrue(testPred(builder().that().inPackage("com.example"), classA))
        assertTrue(testPred(builder().that().inPackage("com.example"), classB))
        assertFalse(testPred(builder().that().inPackage("com.example"), classC))

        assertTrue(testPred(builder().that().inPackage("com.example.."), classA))

        assertTrue(testPred(builder().that().inPackage(listOf("com.example")), classA))

        assertTrue(testPred(builder().that().inPackage("com.example", "com.other"), classA))
        assertTrue(testPred(builder().that().inPackage("com.example", "com.other"), classC))

        assertTrue(testPred(builder().that().notInPackage("com.other"), classA))
        assertFalse(testPred(builder().that().notInPackage("com.other"), classC))

        assertTrue(testPred(builder().that().notInPackage(listOf("com.other")), classA))
        assertFalse(testPred(builder().that().notInPackage(listOf("com.other")), classC))

        assertTrue(testPred(builder().that().notInPackage("com.other", "com.other2"), classA))
        assertFalse(testPred(builder().that().notInPackage("com.other", "com.other2"), classC))

        assertTrue(testPred(builder().that().inModule(":moduleA"), classA))

        assertTrue(testPred(builder().that().inModule(listOf(":moduleA")), classA))

        assertTrue(testPred(builder().that().inModule(":moduleA", ":moduleB"), classA))

        assertFalse(testPred(builder().that().notInModule(":moduleA"), classA))

        assertFalse(testPred(builder().that().notInModule(listOf(":moduleA")), classA))

        assertFalse(testPred(builder().that().notInModule(":moduleA", ":moduleB"), classA))

        // ClassesThatNameFilter
        assertTrue(testPred(builder().that().named("ClassA"), classA))

        assertTrue(testPred(builder().that().named(listOf("ClassA")), classA))

        assertTrue(testPred(builder().that().named("ClassA", "ClassB"), classA))

        assertFalse(testPred(builder().that().notNamed("ClassA"), classA))

        assertFalse(testPred(builder().that().notNamed(listOf("ClassA")), classA))

        assertFalse(testPred(builder().that().notNamed("ClassA", "ClassB"), classA))

        assertFalse(testPred(builder().that().notNamed { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().nameEndsWith("A"), classA))

        assertTrue(testPred(builder().that().nameEndsWith(listOf("A")), classA))

        assertTrue(testPred(builder().that().nameEndsWith("A", "B"), classA))

        assertFalse(testPred(builder().that().notNameEndsWith("A"), classA))

        assertFalse(testPred(builder().that().notNameEndsWith(listOf("A")), classA))

        assertFalse(testPred(builder().that().notNameEndsWith("A", "B"), classA))

        assertTrue(testPred(builder().that().nameStartsWith("Class"), classA))

        assertTrue(testPred(builder().that().nameStartsWith(listOf("Class")), classA))

        assertTrue(testPred(builder().that().nameStartsWith("Class", "Util"), classA))

        assertFalse(testPred(builder().that().notNameStartsWith("Class"), classA))

        assertFalse(testPred(builder().that().notNameStartsWith(listOf("Class")), classA))

        assertFalse(testPred(builder().that().notNameStartsWith("Class", "Util"), classA))

        assertTrue(testPred(builder().that().named { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().named("custom predicate") { it.startsWith("Class") }, classA))

        assertTrue(testPred(builder().that().nameMatches("Class*"), classA))

        assertTrue(testPred(builder().that().nameMatches(listOf("Class*")), classA))

        assertTrue(testPred(builder().that().nameMatches("Class*", "Util*"), classA))

        assertFalse(testPred(builder().that().notNameMatches("Class*"), classA))

        assertFalse(testPred(builder().that().notNameMatches(listOf("Class*")), classA))

        assertFalse(testPred(builder().that().notNameMatches("Class*", "Util*"), classA))

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

        assertFalse(testPred(builder().that().annotatedWith("com.example.MyAnnotation"), classA))
        assertTrue(testPred(builder().that().annotatedWith("com.example.MyAnnotation"), classB))

        assertTrue(testPred(builder().that().annotatedWith("com.example.MyAnnotation"), classB))

        assertTrue(testPred(builder().that().annotatedWithAllOf(listOf("com.example.MyAnnotation")), classB))

        assertFalse(testPred(builder().that().annotatedWithAllOf("com.example.MyAnnotation", "Other"), classB))

        assertTrue(testPred(builder().that().annotatedWithAnyOf(listOf("com.example.MyAnnotation")), classB))

        assertTrue(testPred(builder().that().annotatedWithAnyOf("com.example.MyAnnotation", "Other"), classB))

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
        assertTrue(testPred(builder().that().named("ClassA").and().bePublic(), classA))

        assertTrue(testPred(builder().that().named("ClassA").or().named("ClassB"), classA))
        assertTrue(testPred(builder().that().named("ClassA").or().named("ClassB"), classB))

        assertTrue(testPred(builder().that().satisfy { it.name.contains("Class") }, classA))
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

open class ClassesShouldAssertionsTest : RuleBuildersTestBase() {
    private fun builder() = ClassesRuleBuilder(projectGraph)

    @Test
    fun `test classes assertions`() {
        val testClass =
            ClassDeclaration(
                name = "TestClass",
                fqName = "com.test.TestClass",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TestClass.kt",
                supertypes = listOf("SuperInterface", "SuperClass"),
                kdocText = "Some documentation",
            )
        val childClass =
            ClassDeclaration(
                name = "ChildClass",
                fqName = "com.test.ChildClass",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ChildClass.kt",
                supertypes = listOf("TestClass"),
            )

        val assertResideSingle = builder().should().resideInAPackage("com.test").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertResideSingle(testClass, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertResideList = builder().should().resideInAPackage(listOf("com.test")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertResideList(testClass, emptyList(), v2)
        assertTrue(v2.isEmpty())

        val assertResideVararg = builder().should().resideInAPackage("com.test", "com.other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertResideVararg(testClass, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertNotResideSingle = builder().should().notResideInAPackage("com.other").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotResideSingle(testClass, emptyList(), v4)
        assertTrue(v4.isEmpty())

        val assertNotResideList = builder().should().notResideInAPackage(listOf("com.other")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotResideList(testClass, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertNotResideVararg =
            builder().should().notResideInAPackage("com.other", "com.wrong").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotResideVararg(testClass, emptyList(), v6)
        assertTrue(v6.isEmpty())

        val assertModSingle = builder().should().resideInAModule(":moduleA").getShouldAssertion()!!
        val vMod1 = mutableListOf<String>()
        assertModSingle(classA, listOf(classA), vMod1)
        assertTrue(vMod1.isEmpty())

        val assertModList = builder().should().resideInAModule(listOf(":moduleA")).getShouldAssertion()!!
        val vMod2 = mutableListOf<String>()
        assertModList(classA, listOf(classA), vMod2)
        assertTrue(vMod2.isEmpty())

        val assertModVararg = builder().should().resideInAModule(":moduleA", ":moduleB").getShouldAssertion()!!
        val vMod3 = mutableListOf<String>()
        assertModVararg(classA, listOf(classA), vMod3)
        assertTrue(vMod3.isEmpty())

        val assertNotModSingle = builder().should().notResideInAModule(":forbidden").getShouldAssertion()!!
        val vMod4 = mutableListOf<String>()
        assertNotModSingle(testClass, emptyList(), vMod4)
        assertTrue(vMod4.isEmpty())

        val assertNotModList = builder().should().notResideInAModule(listOf(":forbidden")).getShouldAssertion()!!
        val vMod5 = mutableListOf<String>()
        assertNotModList(testClass, emptyList(), vMod5)
        assertTrue(vMod5.isEmpty())

        val assertNotModVararg =
            builder().should().notResideInAModule(":forbidden", ":other").getShouldAssertion()!!
        val vMod6 = mutableListOf<String>()
        assertNotModVararg(testClass, emptyList(), vMod6)
        assertTrue(vMod6.isEmpty())

        val assertNameSingle = builder().should().haveName("TestClass").getShouldAssertion()!!
        val vN1 = mutableListOf<String>()
        assertNameSingle(testClass, emptyList(), vN1)
        assertTrue(vN1.isEmpty())

        val assertNameList = builder().should().haveName(listOf("TestClass")).getShouldAssertion()!!
        val vN2 = mutableListOf<String>()
        assertNameList(testClass, emptyList(), vN2)
        assertTrue(vN2.isEmpty())

        val assertNameVararg = builder().should().haveName("TestClass", "OtherClass").getShouldAssertion()!!
        val vN3 = mutableListOf<String>()
        assertNameVararg(testClass, emptyList(), vN3)
        assertTrue(vN3.isEmpty())

        val assertNotNameSingle = builder().should().notHaveName("WrongClass").getShouldAssertion()!!
        val vN4 = mutableListOf<String>()
        assertNotNameSingle(testClass, emptyList(), vN4)
        assertTrue(vN4.isEmpty())

        val assertNotNameList = builder().should().notHaveName(listOf("WrongClass")).getShouldAssertion()!!
        val vN5 = mutableListOf<String>()
        assertNotNameList(testClass, emptyList(), vN5)
        assertTrue(vN5.isEmpty())

        val assertNotNameVararg = builder().should().notHaveName("Wrong1", "Wrong2").getShouldAssertion()!!
        val vN6 = mutableListOf<String>()
        assertNotNameVararg(testClass, emptyList(), vN6)
        assertTrue(vN6.isEmpty())

        val assertStartSingle = builder().should().haveNameStartingWith("Test").getShouldAssertion()!!
        val vS1 = mutableListOf<String>()
        assertStartSingle(testClass, emptyList(), vS1)
        assertTrue(vS1.isEmpty())

        val assertStartList = builder().should().haveNameStartingWith(listOf("Test")).getShouldAssertion()!!
        val vS2 = mutableListOf<String>()
        assertStartList(testClass, emptyList(), vS2)
        assertTrue(vS2.isEmpty())

        val assertStartVararg = builder().should().haveNameStartingWith("Test", "Other").getShouldAssertion()!!
        val vS3 = mutableListOf<String>()
        assertStartVararg(testClass, emptyList(), vS3)
        assertTrue(vS3.isEmpty())

        val assertNotStartSingle = builder().should().notHaveNameStartingWith("Wrong").getShouldAssertion()!!
        val vS4 = mutableListOf<String>()
        assertNotStartSingle(testClass, emptyList(), vS4)
        assertTrue(vS4.isEmpty())

        val assertNotStartList = builder().should().notHaveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!
        val vS5 = mutableListOf<String>()
        assertNotStartList(testClass, emptyList(), vS5)
        assertTrue(vS5.isEmpty())

        val assertNotStartVararg =
            builder().should().notHaveNameStartingWith("Wrong1", "Wrong2").getShouldAssertion()!!
        val vS6 = mutableListOf<String>()
        assertNotStartVararg(testClass, emptyList(), vS6)
        assertTrue(vS6.isEmpty())

        val assertEndSingle = builder().should().haveNameEndingWith("Class").getShouldAssertion()!!
        val vE1 = mutableListOf<String>()
        assertEndSingle(testClass, emptyList(), vE1)
        assertTrue(vE1.isEmpty())

        val assertEndList = builder().should().haveNameEndingWith(listOf("Class")).getShouldAssertion()!!
        val vE2 = mutableListOf<String>()
        assertEndList(testClass, emptyList(), vE2)
        assertTrue(vE2.isEmpty())

        val assertEndVararg = builder().should().haveNameEndingWith("Class", "Other").getShouldAssertion()!!
        val vE3 = mutableListOf<String>()
        assertEndVararg(testClass, emptyList(), vE3)
        assertTrue(vE3.isEmpty())

        val assertNotEndSingle = builder().should().notHaveNameEndingWith("Wrong").getShouldAssertion()!!
        val vE4 = mutableListOf<String>()
        assertNotEndSingle(testClass, emptyList(), vE4)
        assertTrue(vE4.isEmpty())

        val assertNotEndList = builder().should().notHaveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!
        val vE5 = mutableListOf<String>()
        assertNotEndList(testClass, emptyList(), vE5)
        assertTrue(vE5.isEmpty())

        val assertNotEndVararg = builder().should().notHaveNameEndingWith("Wrong1", "Wrong2").getShouldAssertion()!!
        val vE6 = mutableListOf<String>()
        assertNotEndVararg(testClass, emptyList(), vE6)
        assertTrue(vE6.isEmpty())

        val assertMatchSingle = builder().should().haveNameMatching("Test*").getShouldAssertion()!!
        val vM1 = mutableListOf<String>()
        assertMatchSingle(testClass, emptyList(), vM1)
        assertTrue(vM1.isEmpty())

        val assertMatchList = builder().should().haveNameMatching(listOf("Test*")).getShouldAssertion()!!
        val vM2 = mutableListOf<String>()
        assertMatchList(testClass, emptyList(), vM2)
        assertTrue(vM2.isEmpty())

        val assertMatchVararg = builder().should().haveNameMatching("Test*", "Other*").getShouldAssertion()!!
        val vM3 = mutableListOf<String>()
        assertMatchVararg(testClass, emptyList(), vM3)
        assertTrue(vM3.isEmpty())

        val assertNotMatchSingle = builder().should().notHaveNameMatching("Wrong*").getShouldAssertion()!!
        val vM4 = mutableListOf<String>()
        assertNotMatchSingle(testClass, emptyList(), vM4)
        assertTrue(vM4.isEmpty())

        val assertNotMatchList = builder().should().notHaveNameMatching(listOf("Wrong*")).getShouldAssertion()!!
        val vM5 = mutableListOf<String>()
        assertNotMatchList(testClass, emptyList(), vM5)
        assertTrue(vM5.isEmpty())

        val assertNotMatchVararg = builder().should().notHaveNameMatching("Wrong1*", "Wrong2*").getShouldAssertion()!!
        val vM6 = mutableListOf<String>()
        assertNotMatchVararg(testClass, emptyList(), vM6)
        assertTrue(vM6.isEmpty())

        val assertAnnotSingle = builder().should().haveAnnotationOf("MyAnnotation").getShouldAssertion()!!
        val annotatedClass =
            testClass.copy(annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.test.MyAnnotation")))
        val vA1 = mutableListOf<String>()
        assertAnnotSingle(annotatedClass, emptyList(), vA1)
        assertTrue(vA1.isEmpty())

        val assertAllAnnotList = builder().should().haveAllAnnotationsOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val vA7 = mutableListOf<String>()
        assertAllAnnotList(annotatedClass, emptyList(), vA7)
        assertTrue(vA7.isEmpty())

        val assertAllAnnotVararg = builder().should().haveAllAnnotationsOf("MyAnnotation").getShouldAssertion()!!
        val vA8 = mutableListOf<String>()
        assertAllAnnotVararg(annotatedClass, emptyList(), vA8)
        assertTrue(vA8.isEmpty())

        val assertAnyAnnotList =
            builder().should().haveAnyAnnotationOf(
                listOf("MyAnnotation", "Wrong"),
            ).getShouldAssertion()!!
        val vA9 = mutableListOf<String>()
        assertAnyAnnotList(annotatedClass, emptyList(), vA9)
        assertTrue(vA9.isEmpty())

        val assertAnyAnnotVararg =
            builder().should().haveAnyAnnotationOf(
                "MyAnnotation",
                "Wrong",
            ).getShouldAssertion()!!
        val vA10 = mutableListOf<String>()
        assertAnyAnnotVararg(annotatedClass, emptyList(), vA10)
        assertTrue(vA10.isEmpty())

        val assertAssignableFrom = builder().should().beAssignableFrom("com.test.ChildClass").getShouldAssertion()!!
        val vAssign1 = mutableListOf<String>()
        assertAssignableFrom(testClass, listOf(testClass, childClass), vAssign1)
        assertTrue(vAssign1.isEmpty())

        val assertAssignableFromFail = builder().should().beAssignableFrom("com.test.WrongClass").getShouldAssertion()!!
        val vAssign2 = mutableListOf<String>()
        assertAssignableFromFail(testClass, listOf(testClass, childClass), vAssign2)
        assertEquals(1, vAssign2.size)

        val assertAssignableFromSelf = builder().should().beAssignableFrom("com.test.TestClass").getShouldAssertion()!!
        val vAssign3 = mutableListOf<String>()
        assertAssignableFromSelf(testClass, listOf(testClass, childClass), vAssign3)
        assertTrue(vAssign3.isEmpty())
    }
}

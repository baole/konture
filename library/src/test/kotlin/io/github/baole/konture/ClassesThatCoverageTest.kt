/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
internal class ClassesThatCoverageTest : KontureScopeTestFixture() {
    private lateinit var graph: ProjectGraph

    @BeforeEach
    fun initGraph() {
        graph = ProjectGraph(mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA, fileB, fileC)))))
    }

    private fun checkPred(
        builder: ClassesRuleBuilder,
        classDecl: ClassDeclaration,
    ): Boolean {
        val pred = builder.getThatPredicate() ?: return false
        return pred(classDecl)
    }

    @Test
    fun `test ClassesThat module filters`() {
        val builder1 = ClassesRuleBuilder(graph).that().resideInAModule("app")
        assertTrue(checkPred(builder1, classA))

        val builder2 = ClassesRuleBuilder(graph).that().resideInAModule(listOf("app"))
        assertTrue(checkPred(builder2, classA))

        val builder3 = ClassesRuleBuilder(graph).that().resideInAModule("app", "core")
        assertTrue(checkPred(builder3, classA))

        val builder4 = ClassesRuleBuilder(graph).that().resideInModule("app")
        assertTrue(checkPred(builder4, classA))

        val builder5 = ClassesRuleBuilder(graph).that().resideInModules("app")
        assertTrue(checkPred(builder5, classA))

        val builder6 = ClassesRuleBuilder(graph).that().resideInModules(listOf("app"))
        assertTrue(checkPred(builder6, classA))

        val builder7 = ClassesRuleBuilder(graph).that().notResideInAModule("feature")
        assertTrue(checkPred(builder7, classA))

        val builder8 = ClassesRuleBuilder(graph).that().notResideInAModule(listOf("feature"))
        assertTrue(checkPred(builder8, classA))

        val builder9 = ClassesRuleBuilder(graph).that().notResideInAModule("feature", "other")
        assertTrue(checkPred(builder9, classA))

        val builder10 = ClassesRuleBuilder(graph).that().notResideInModule("feature")
        assertTrue(checkPred(builder10, classA))

        val builder11 = ClassesRuleBuilder(graph).that().notResideInModules("feature")
        assertTrue(checkPred(builder11, classA))

        val builder12 = ClassesRuleBuilder(graph).that().notResideInModules(listOf("feature"))
        assertTrue(checkPred(builder12, classA))
    }

    @Test
    fun `test ClassesThat name and package filters`() {
        val builder1 = ClassesRuleBuilder(graph).that().resideInPackageOf(String::class)
        assertFalse(checkPred(builder1, classA))

        val builder2 = ClassesRuleBuilder(graph).that().haveName("desc") { it == "ClassA" }
        assertTrue(checkPred(builder2, classA))

        val builder3 = ClassesRuleBuilder(graph).that().haveName(listOf("ClassA", "ClassB"))
        assertTrue(checkPred(builder3, classA))

        val builder4 = ClassesRuleBuilder(graph).that().haveName("ClassA", "ClassB")
        assertTrue(checkPred(builder4, classA))

        val builder5 = ClassesRuleBuilder(graph).that().notHaveName("ClassB")
        assertTrue(checkPred(builder5, classA))

        val builder6 = ClassesRuleBuilder(graph).that().notHaveName(listOf("ClassB"))
        assertTrue(checkPred(builder6, classA))

        val builder7 = ClassesRuleBuilder(graph).that().notHaveName("ClassB", "ClassC")
        assertTrue(checkPred(builder7, classA))

        val builder8 = ClassesRuleBuilder(graph).that().notHaveName { it == "ClassB" }
        assertTrue(checkPred(builder8, classA))

        val builder9 = ClassesRuleBuilder(graph).that().haveNameEndingWith(listOf("A", "B"))
        assertTrue(checkPred(builder9, classA))

        val builder10 = ClassesRuleBuilder(graph).that().haveNameEndingWith("A", "B")
        assertTrue(checkPred(builder10, classA))

        val builder11 = ClassesRuleBuilder(graph).that().notHaveNameEndingWith("Z")
        assertTrue(checkPred(builder11, classA))

        val builder12 = ClassesRuleBuilder(graph).that().notHaveNameEndingWith(listOf("Z"))
        assertTrue(checkPred(builder12, classA))

        val builder13 = ClassesRuleBuilder(graph).that().notHaveNameEndingWith("Z", "Y")
        assertTrue(checkPred(builder13, classA))

        val builder14 = ClassesRuleBuilder(graph).that().haveNameStartingWith(listOf("Cl", "My"))
        assertTrue(checkPred(builder14, classA))

        val builder15 = ClassesRuleBuilder(graph).that().haveNameStartingWith("Cl", "My")
        assertTrue(checkPred(builder15, classA))

        val builder16 = ClassesRuleBuilder(graph).that().notHaveNameStartingWith("Z")
        assertTrue(checkPred(builder16, classA))

        val builder17 = ClassesRuleBuilder(graph).that().notHaveNameStartingWith(listOf("Z"))
        assertTrue(checkPred(builder17, classA))

        val builder18 = ClassesRuleBuilder(graph).that().notHaveNameStartingWith("Z", "Y")
        assertTrue(checkPred(builder18, classA))

        val builder19 = ClassesRuleBuilder(graph).that().haveNameMatching(listOf("Class*", "Other*"))
        assertTrue(checkPred(builder19, classA))

        val builder20 = ClassesRuleBuilder(graph).that().haveNameMatching("Class*", "Other*")
        assertTrue(checkPred(builder20, classA))

        val builder21 = ClassesRuleBuilder(graph).that().notHaveNameMatching("Wrong*")
        assertTrue(checkPred(builder21, classA))

        val builder22 = ClassesRuleBuilder(graph).that().notHaveNameMatching(listOf("Wrong*"))
        assertTrue(checkPred(builder22, classA))

        val builder23 = ClassesRuleBuilder(graph).that().notHaveNameMatching("Wrong*", "Bad*")
        assertTrue(checkPred(builder23, classA))
    }

    @Test
    fun `test ClassesThat annotation and visibility filters`() {
        val builder1 = ClassesRuleBuilder(graph).that().haveAnnotationOf(Deprecated::class)
        assertFalse(checkPred(builder1, classA))

        val builder2 = ClassesRuleBuilder(graph).that().haveAnnotationOf<Deprecated>()
        assertFalse(checkPred(builder2, classA))

        val builder3 = ClassesRuleBuilder(graph).that().areAnnotatedWith("com.example.MyAnnotation")
        assertTrue(checkPred(builder3, classAnnotated))

        val builder4 = ClassesRuleBuilder(graph).that().areAnnotatedWith(Deprecated::class)
        assertFalse(checkPred(builder4, classA))

        val builder5 = ClassesRuleBuilder(graph).that().areAnnotatedWith<Deprecated>()
        assertFalse(checkPred(builder5, classA))

        val builder6 = ClassesRuleBuilder(graph).that().haveAllAnnotationsOf("MyAnnotation")
        assertTrue(checkPred(builder6, classAnnotated))

        val builder7 = ClassesRuleBuilder(graph).that().haveAllAnnotationsOf(listOf("MyAnnotation"))
        assertTrue(checkPred(builder7, classAnnotated))

        val builder8 = ClassesRuleBuilder(graph).that().haveAllAnnotationsOf("MyAnnotation", "Other")
        assertFalse(checkPred(builder8, classAnnotated))

        val builder9 = ClassesRuleBuilder(graph).that().haveAnyAnnotationOf("MyAnnotation")
        assertTrue(checkPred(builder9, classAnnotated))

        val builder10 = ClassesRuleBuilder(graph).that().haveAnyAnnotationOf(listOf("MyAnnotation"))
        assertTrue(checkPred(builder10, classAnnotated))

        val builder11 = ClassesRuleBuilder(graph).that().haveAnyAnnotationOf("MyAnnotation", "Other")
        assertTrue(checkPred(builder11, classAnnotated))

        val builder12 = ClassesRuleBuilder(graph).that().haveAnnotationWithArgument("MyAnnotation", "key", "val")
        assertFalse(checkPred(builder12, classAnnotated))

        val builder13 = ClassesRuleBuilder(graph).that().areInterfaces()
        assertTrue(checkPred(builder13, classInterface))

        val builder14 = ClassesRuleBuilder(graph).that().areEnums()
        assertFalse(checkPred(builder14, classA))

        val builder15 = ClassesRuleBuilder(graph).that().areAbstract()
        assertTrue(checkPred(builder15, classAbstract))

        val builder16 = ClassesRuleBuilder(graph).that().haveAnyVisibility(Visibility.PUBLIC)
        assertTrue(checkPred(builder16, classA))

        val builder17 = ClassesRuleBuilder(graph).that().haveAnyVisibility(listOf(Visibility.PUBLIC))
        assertTrue(checkPred(builder17, classA))

        val builder18 = ClassesRuleBuilder(graph).that().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL)
        assertTrue(checkPred(builder18, classA))

        val builder19 = ClassesRuleBuilder(graph).that().bePublic()
        assertTrue(checkPred(builder19, classA))

        val builder20 = ClassesRuleBuilder(graph).that().beInternal()
        assertTrue(checkPred(builder20, classInternal))

        val builder21 = ClassesRuleBuilder(graph).that().bePrivate()
        assertTrue(checkPred(builder21, classPrivate))

        val builder22 = ClassesRuleBuilder(graph).that().beProtected()
        assertTrue(checkPred(builder22, classProtected))
    }

    @Test
    fun `test ClassesThat modifiers assignability constructors and combinators`() {
        val builder1 = ClassesRuleBuilder(graph).that().haveAnyModifier(Modifier.DATA)
        assertTrue(checkPred(builder1, classData))

        val builder2 = ClassesRuleBuilder(graph).that().haveAnyModifier(listOf(Modifier.DATA))
        assertTrue(checkPred(builder2, classData))

        val builder3 = ClassesRuleBuilder(graph).that().haveAnyModifier(Modifier.DATA, Modifier.SEALED)
        assertTrue(checkPred(builder3, classData))

        val builder4 = ClassesRuleBuilder(graph).that().haveAllModifiers(Modifier.DATA)
        assertTrue(checkPred(builder4, classData))

        val builder5 = ClassesRuleBuilder(graph).that().haveAllModifiers(listOf(Modifier.DATA))
        assertTrue(checkPred(builder5, classData))

        val builder6 = ClassesRuleBuilder(graph).that().haveAllModifiers(Modifier.DATA, Modifier.SEALED)
        assertFalse(checkPred(builder6, classData))

        val builder7 = ClassesRuleBuilder(graph).that().beSealed()
        assertTrue(checkPred(builder7, classSealed))

        val builder8 = ClassesRuleBuilder(graph).that().beData()
        assertTrue(checkPred(builder8, classData))

        val builder9 = ClassesRuleBuilder(graph).that().beInline()
        assertTrue(checkPred(builder9, classInline))

        val builder10 = ClassesRuleBuilder(graph).that().areAssignableTo(String::class)
        assertFalse(checkPred(builder10, classA))

        val builder11 = ClassesRuleBuilder(graph).that().areAssignableTo<String>()
        assertFalse(checkPred(builder11, classA))

        val builder12 = ClassesRuleBuilder(graph).that().beChildOf("com.example.ParentType")
        assertTrue(checkPred(builder12, classWithParent))

        val builder13 = ClassesRuleBuilder(graph).that().beChildOf(String::class)
        assertFalse(checkPred(builder13, classA))

        val builder14 = ClassesRuleBuilder(graph).that().beChildOf<String>()
        assertFalse(checkPred(builder14, classA))

        val builder15 = ClassesRuleBuilder(graph).that().areAssignableToAnyOf("com.example.ParentType")
        assertTrue(checkPred(builder15, classWithParent))

        val builder16 = ClassesRuleBuilder(graph).that().areAssignableToAnyOf(listOf("com.example.ParentType"))
        assertTrue(checkPred(builder16, classWithParent))

        val builder17 =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAnyOf("com.example.ParentType", "com.other.Type")
        assertTrue(checkPred(builder17, classWithParent))

        val builder18 = ClassesRuleBuilder(graph).that().areAssignableToAnyOf(String::class, Int::class)
        assertFalse(checkPred(builder18, classA))

        val builder19 = ClassesRuleBuilder(graph).that().areAssignableToAllOf("com.example.ParentType")
        assertTrue(checkPred(builder19, classWithParent))

        val builder20 = ClassesRuleBuilder(graph).that().areAssignableToAllOf(listOf("com.example.ParentType"))
        assertTrue(checkPred(builder20, classWithParent))

        val builder21 =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAllOf("com.example.ParentType", "com.other.Type")
        assertFalse(checkPred(builder21, classWithParent))

        val builder22 = ClassesRuleBuilder(graph).that().areAssignableToAllOf(String::class, Int::class)
        assertFalse(checkPred(builder22, classA))

        val builder23 = ClassesRuleBuilder(graph).that().areAssignableFrom("com.example.ClassA")
        assertTrue(checkPred(builder23, classA))

        val builder24 = ClassesRuleBuilder(graph).that().areAssignableFrom<String>()
        assertFalse(checkPred(builder24, classA))

        val builder25 = ClassesRuleBuilder(graph).that().areAssignableFrom(String::class)
        assertFalse(checkPred(builder25, classA))

        val builder26 = ClassesRuleBuilder(graph).that().haveCompanionObject()
        assertFalse(checkPred(builder26, classA))

        val builder27 = ClassesRuleBuilder(graph).that().haveNoArgConstructor()
        assertFalse(checkPred(builder27, classA))

        val builder28 = ClassesRuleBuilder(graph).that().havePrivatePrimaryConstructor()
        assertFalse(checkPred(builder28, classA))

        val builder29 = ClassesRuleBuilder(graph).that().areOpen()
        assertFalse(checkPred(builder29, classA))

        val builder30 = ClassesRuleBuilder(graph).that().areOverride()
        assertFalse(checkPred(builder30, classA))

        val builder31 = ClassesRuleBuilder(graph).that().areInner()
        assertFalse(checkPred(builder31, classA))

        val builder32 = ClassesRuleBuilder(graph).that().areTopLevel()
        assertTrue(checkPred(builder32, classA))

        val builder33 = ClassesRuleBuilder(graph).that().areNested()
        assertFalse(checkPred(builder33, classA))

        val builder34 = ClassesRuleBuilder(graph).that().containProperty("someProp")
        assertFalse(checkPred(builder34, classA))

        val builder35 = ClassesRuleBuilder(graph).that().containFunction("someFunc")
        assertFalse(checkPred(builder35, classA))

        val builder36 = ClassesRuleBuilder(graph).that().matching { it.name == "ClassA" }
        assertTrue(checkPred(builder36, classA))

        val builder37 = ClassesRuleBuilder(graph).that().satisfy { it.name == "ClassA" }
        assertTrue(checkPred(builder37, classA))

        val builder38 = ClassesRuleBuilder(graph).that().beDocumentedWithKDoc()
        assertTrue(checkPred(builder38, classWithKdoc))

        val builder39 = ClassesRuleBuilder(graph).that().anyOf({ haveName("ClassA") }, { haveName("ClassB") })
        assertTrue(checkPred(builder39, classA))

        val builder40 =
            ClassesRuleBuilder(
                graph,
            ).that().allOf({ haveName("ClassA") }, { resideInAPackage("com.example") })
        assertTrue(checkPred(builder40, classA))

        val builder41 = ClassesRuleBuilder(graph).that().noneOf({ haveName("ClassB") })
        assertTrue(checkPred(builder41, classA))
    }
}

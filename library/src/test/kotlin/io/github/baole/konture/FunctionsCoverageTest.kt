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

internal class FunctionsCoverageTest : KontureScopeTestFixture() {
    private fun createFuncCtx(
        name: String,
        className: String,
        packageName: String,
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        returnType: String = "Unit",
        parameters: List<ParameterDeclaration> = emptyList(),
        annotations: List<AnnotationDeclaration> = emptyList(),
        isExtension: Boolean = false,
        extensionReceiverType: String? = null,
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                parameters = parameters,
                annotations = annotations,
                kdocText = null,
                isExtension = isExtension,
                extensionReceiverType = extensionReceiverType,
            )
        val cls =
            ClassDeclaration(
                name = className,
                fqName = "$packageName.$className",
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/$className.kt",
            )
        val file = FileDeclaration("$className.kt", packageName, classes = listOf(cls))
        return FunctionDeclarationContext(decl, cls, file)
    }

    @Test
    fun `test FunctionsShould name assertions`() {
        val funcCtx = createFuncCtx(name = "myFunc", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertNameSingle = FunctionsRuleBuilder(graph).should().haveName("myFunc").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertNameSingle(funcCtx, listOf(funcCtx), v1)
        assertTrue(v1.isEmpty())

        val assertNameList = FunctionsRuleBuilder(graph).should().haveName(listOf("myFunc")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNameList(funcCtx, listOf(funcCtx), v2)
        assertTrue(v2.isEmpty())

        val assertNameVararg = FunctionsRuleBuilder(graph).should().haveName("myFunc", "other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNameVararg(funcCtx, listOf(funcCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNamePred = FunctionsRuleBuilder(graph).should().haveName { it.startsWith("my") }.getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNamePred(funcCtx, listOf(funcCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNamePredDesc =
            FunctionsRuleBuilder(
                graph,
            ).should().haveName("custom desc") { it.startsWith("my") }.getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNamePredDesc(funcCtx, listOf(funcCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotNameSingle = FunctionsRuleBuilder(graph).should().notHaveName("other").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotNameSingle(funcCtx, listOf(funcCtx), v6)
        assertTrue(v6.isEmpty())

        val assertNotNameList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveName(listOf("other")).getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertNotNameList(funcCtx, listOf(funcCtx), v7)
        assertTrue(v7.isEmpty())

        val assertNotNameVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveName("other1", "other2").getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertNotNameVararg(funcCtx, listOf(funcCtx), v8)
        assertTrue(v8.isEmpty())

        val assertNotNamePred =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveName { it.startsWith("other") }.getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertNotNamePred(funcCtx, listOf(funcCtx), v9)
        assertTrue(v9.isEmpty())

        val assertEndSingle = FunctionsRuleBuilder(graph).should().haveNameEndingWith("Func").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertEndSingle(funcCtx, listOf(funcCtx), v10)
        assertTrue(v10.isEmpty())

        val assertEndList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameEndingWith(listOf("Func")).getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertEndList(funcCtx, listOf(funcCtx), v11)
        assertTrue(v11.isEmpty())

        val assertEndVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameEndingWith("Func", "Other").getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertEndVararg(funcCtx, listOf(funcCtx), v12)
        assertTrue(v12.isEmpty())

        val assertNotEndSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong").getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertNotEndSingle(funcCtx, listOf(funcCtx), v13)
        assertTrue(v13.isEmpty())

        val assertNotEndList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertNotEndList(funcCtx, listOf(funcCtx), v14)
        assertTrue(v14.isEmpty())

        val assertNotEndVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong1", "Wrong2").getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertNotEndVararg(funcCtx, listOf(funcCtx), v15)
        assertTrue(v15.isEmpty())

        val assertStartSingle = FunctionsRuleBuilder(graph).should().haveNameStartingWith("my").getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertStartSingle(funcCtx, listOf(funcCtx), v16)
        assertTrue(v16.isEmpty())

        val assertStartList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameStartingWith(listOf("my")).getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertStartList(funcCtx, listOf(funcCtx), v17)
        assertTrue(v17.isEmpty())

        val assertStartVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my", "other").getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertStartVararg(funcCtx, listOf(funcCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotStartSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("Wrong").getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotStartSingle(funcCtx, listOf(funcCtx), v19)
        assertTrue(v19.isEmpty())

        val assertNotStartList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertNotStartList(funcCtx, listOf(funcCtx), v20)
        assertTrue(v20.isEmpty())

        val assertNotStartVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("Wrong1", "Wrong2").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertNotStartVararg(funcCtx, listOf(funcCtx), v21)
        assertTrue(v21.isEmpty())

        val assertMatchSingle = FunctionsRuleBuilder(graph).should().haveNameMatching("my*").getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertMatchSingle(funcCtx, listOf(funcCtx), v22)
        assertTrue(v22.isEmpty())

        val assertMatchList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameMatching(listOf("my*")).getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertMatchList(funcCtx, listOf(funcCtx), v23)
        assertTrue(v23.isEmpty())

        val assertMatchVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameMatching("my*", "other*").getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertMatchVararg(funcCtx, listOf(funcCtx), v24)
        assertTrue(v24.isEmpty())

        val assertNotMatchSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*").getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertNotMatchSingle(funcCtx, listOf(funcCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotMatchList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching(listOf("wrong*")).getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotMatchList(funcCtx, listOf(funcCtx), v26)
        assertTrue(v26.isEmpty())

        val assertNotMatchVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong1*", "wrong2*").getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertNotMatchVararg(funcCtx, listOf(funcCtx), v27)
        assertTrue(v27.isEmpty())
    }

    @Test
    fun `test FunctionsShould modifier assertions`() {
        val funcCtx =
            createFuncCtx(
                name = "myFunc",
                className = "ClassA",
                packageName = "com.example",
                modifiers = setOf(Modifier.OPERATOR, Modifier.INLINE, Modifier.OPEN, Modifier.SUSPEND),
                visibility = Visibility.PUBLIC,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", emptyList())),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertOperator = FunctionsRuleBuilder(graph).should().beOperator().getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertOperator(funcCtx, listOf(funcCtx), v1)
        assertTrue(v1.isEmpty())

        val assertNotOperator =
            FunctionsRuleBuilder(
                graph,
            ).should().notBeOperator().getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotOperator(
            createFuncCtx("f", "C", "com.example"),
            listOf(funcCtx),
            v2,
        )
        assertTrue(v2.isEmpty())

        val assertInfix = FunctionsRuleBuilder(graph).should().notBeInfix().getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertInfix(funcCtx, listOf(funcCtx), v3)
        assertTrue(v3.isEmpty())

        val assertInline = FunctionsRuleBuilder(graph).should().beInline().getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertInline(funcCtx, listOf(funcCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotInline = FunctionsRuleBuilder(graph).should().notBeInline().getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotInline(
            createFuncCtx("f", "C", "com.example"),
            listOf(funcCtx),
            v5,
        )
        assertTrue(v5.isEmpty())

        val assertSuspend = FunctionsRuleBuilder(graph).should().beSuspend().getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertSuspend(funcCtx, listOf(funcCtx), v6)
        assertTrue(v6.isEmpty())

        val assertNotSuspend = FunctionsRuleBuilder(graph).should().notBeSuspend().getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertNotSuspend(
            createFuncCtx("f", "C", "com.example"),
            listOf(funcCtx),
            v7,
        )
        assertTrue(v7.isEmpty())

        val assertOpen = FunctionsRuleBuilder(graph).should().beOpen().getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertOpen(funcCtx, listOf(funcCtx), v8)
        assertTrue(v8.isEmpty())

        val assertNotOpen = FunctionsRuleBuilder(graph).should().notBeOpen().getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertNotOpen(
            createFuncCtx("f", "C", "com.example"),
            listOf(funcCtx),
            v9,
        )
        assertTrue(v9.isEmpty())

        val assertPublic = FunctionsRuleBuilder(graph).should().bePublic().getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertPublic(funcCtx, listOf(funcCtx), v10)
        assertTrue(v10.isEmpty())

        val assertInternal = FunctionsRuleBuilder(graph).should().notBeInternal().getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertInternal(funcCtx, listOf(funcCtx), v11)
        assertTrue(v11.isEmpty())

        val assertPrivate = FunctionsRuleBuilder(graph).should().notBePrivate().getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertPrivate(funcCtx, listOf(funcCtx), v12)
        assertTrue(v12.isEmpty())

        val assertProtected = FunctionsRuleBuilder(graph).should().notBeProtected().getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertProtected(funcCtx, listOf(funcCtx), v13)
        assertTrue(v13.isEmpty())

        val assertHaveAnnotSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnnotationOf("MyAnnotation").getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertHaveAnnotSingle(funcCtx, listOf(funcCtx), v14)
        assertTrue(v14.isEmpty())

        val assertHaveAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnnotationOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertHaveAnnotList(funcCtx, listOf(funcCtx), v15)
        assertTrue(v15.isEmpty())

        val assertHaveAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnnotationOf("MyAnnotation", "Other").getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertHaveAnnotVararg(funcCtx, listOf(funcCtx), v16)
        assertTrue(v16.isEmpty())

        val assertNotAnnotSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("WrongAnnotation").getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertNotAnnotSingle(funcCtx, listOf(funcCtx), v17)
        assertTrue(v17.isEmpty())

        val assertNotAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf(listOf("WrongAnnotation")).getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertNotAnnotList(funcCtx, listOf(funcCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("Wrong1", "Wrong2").getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotAnnotVararg(funcCtx, listOf(funcCtx), v19)
        assertTrue(v19.isEmpty())

        val assertAllAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertAllAnnotList(funcCtx, listOf(funcCtx), v20)
        assertTrue(v20.isEmpty())

        val assertAllAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("MyAnnotation").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertAllAnnotVararg(funcCtx, listOf(funcCtx), v21)
        assertTrue(v21.isEmpty())

        val assertAnyAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnyAnnotationOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertAnyAnnotList(funcCtx, listOf(funcCtx), v22)
        assertTrue(v22.isEmpty())

        val assertAnyAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnyAnnotationOf("MyAnnotation", "Other").getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertAnyAnnotVararg(funcCtx, listOf(funcCtx), v23)
        assertTrue(v23.isEmpty())
    }

    @Test
    fun `test FunctionsThat filters`() {
        val funcCtx =
            createFuncCtx(
                name = "myFunc",
                className = "ClassA",
                packageName = "com.example",
                modifiers = setOf(Modifier.OPERATOR, Modifier.INLINE, Modifier.OPEN, Modifier.SUSPEND),
                visibility = Visibility.PUBLIC,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", emptyList())),
                returnType = "String",
                isExtension = true,
                extensionReceiverType = "Int",
                parameters = listOf(ParameterDeclaration("x", "Double", false, emptyList())),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        // FunctionsThatPackageFilter
        val builder = FunctionsRuleBuilder(graph)
        builder.that().resideInPackageOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().resideInPackageOf<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().resideInAPackage("com.example")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().resideInAPackage(listOf("com.example"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().resideInAPackage("com.example", "com.other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notResideInAPackage("com.other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notResideInAPackage(listOf("com.other"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notResideInAPackage("com.other1", "com.other2")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().resideInAModule(":app")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().resideInAModule(listOf(":app"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().resideInAModule(":app", ":lib")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        // FunctionsThatNameFilter
        builder.that().haveName("myFunc")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveName(listOf("myFunc"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveName("myFunc", "other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveName { it.startsWith("my") }
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveName("custom predicate") { it.startsWith("my") }
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameEndingWith("Func")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameEndingWith(listOf("Func"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameEndingWith("Func", "Other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameStartingWith("my")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameStartingWith(listOf("my"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameStartingWith("my", "other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameMatching("my*")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameMatching(listOf("my*"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveNameMatching("my*", "other*")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        // FunctionsThatStructureFilter
        builder.that().haveParameterCount(1)
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveParameterCount { it == 1 }
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveParameterOf("Double")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveParameterOf(listOf("Double"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveParameterOf("Double", "Int")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveParameterOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveParameterOf("String")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notHaveParameterOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveParameterOf<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().haveReturnType("String")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveReturnType(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveReturnType("Int")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notHaveReturnType(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveReturnType<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().beExtension()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notBeExtension()
        assertFalse(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveExtensionReceiver("Int")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveExtensionReceiver(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().declaredInClass("ClassA")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().declaredInClass(listOf("ClassA"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().declaredInClass("ClassA", "ClassB")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().declaredInClass(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAnyParameterType("Double")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnyParameterType(listOf("Double"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnyParameterType("Double", "Int")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        // FunctionsThatModifierFilter
        builder.that().areOperator()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().areInfix()
        assertFalse(builder.getThatPredicate()!!(funcCtx))

        builder.that().areInline()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().areSuspend()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().areOpen()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnnotationOf("MyAnnotation")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnnotationOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAnnotationOf<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().areAnnotatedWith("MyAnnotation")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().areAnnotatedWith(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().areAnnotatedWith<RuleBuildersTestBase>()
        assertNotNull(builder.getThatPredicate())

        builder.that().notHaveAnnotationOf("Wrong")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notHaveAnnotationOf(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().notBeAnnotatedWith("Wrong")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().notBeAnnotatedWith(RuleBuildersTestBase::class)
        assertNotNull(builder.getThatPredicate())

        builder.that().haveAllAnnotationsOf(listOf("MyAnnotation"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAllAnnotationsOf("MyAnnotation")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnyAnnotationOf(listOf("MyAnnotation"))
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveAnyAnnotationOf("MyAnnotation", "Other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        // FunctionsThatCompositeFilter
        builder.that().haveName("myFunc").and().areOpen()
        assertTrue(builder.getThatPredicate()!!(funcCtx))

        builder.that().haveName("myFunc").or().haveName("other")
        assertTrue(builder.getThatPredicate()!!(funcCtx))
    }
}

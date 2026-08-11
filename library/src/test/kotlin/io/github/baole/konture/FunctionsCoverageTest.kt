/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsCoverageTest : KontureScopeTestFixture() {
    private fun createFuncCtx(
        name: String = "myFunc",
        className: String? = null,
        packageName: String = "com.example",
        modulePath: String = ":app",
        modifiers: Set<Modifier> = emptySet(),
        visibility: Visibility = Visibility.PUBLIC,
        annotations: List<AnnotationDeclaration> = emptyList(),
        returnType: String = "String",
        isExtension: Boolean = false,
        extensionReceiverType: String? = null,
        parameters: List<ParameterDeclaration> = emptyList(),
        kdocText: String? = null,
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                parameters = parameters,
                annotations = annotations,
                kdocText = kdocText,
                isExtension = isExtension,
                receiverType = extensionReceiverType,
            )
        return FunctionDeclarationContext(decl, packageName, className, modulePath, "/src/FileA.kt")
    }

    @Test
    fun `test FunctionsShould assertions`() {
        val funcCtx =
            createFuncCtx(
                name = "myFunc",
                className = "ClassA",
                packageName = "com.example",
                modifiers = setOf(Modifier.OPERATOR, Modifier.INLINE, Modifier.OPEN, Modifier.SUSPEND),
                visibility = Visibility.PUBLIC,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                returnType = "String",
                isExtension = true,
                extensionReceiverType = "Int",
                parameters = listOf(ParameterDeclaration("x", "Double", false, emptyList())),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertPkgSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().resideInAPackage("com.example").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertPkgSingle(funcCtx, listOf(funcCtx), v1)
        assertTrue(v1.isEmpty())

        val assertPkgList =
            FunctionsRuleBuilder(
                graph,
            ).should().resideInAPackage(listOf("com.example")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertPkgList(funcCtx, listOf(funcCtx), v2)
        assertTrue(v2.isEmpty())

        val assertPkgVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().resideInAPackage("com.example", "com.other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertPkgVararg(funcCtx, listOf(funcCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNotModSingle1 =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(":other").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotModSingle1(funcCtx, listOf(funcCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotModList1 =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(listOf(":other")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotModList1(funcCtx, listOf(funcCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotModVararg1 =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(":other", ":wrong").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotModVararg1(funcCtx, listOf(funcCtx), v6)
        assertTrue(v6.isEmpty())

        val assertModSingle = FunctionsRuleBuilder(graph).should().resideInAModule(":app").getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertModSingle(funcCtx, listOf(funcCtx), v7)
        assertTrue(v7.isEmpty())

        val assertModList =
            FunctionsRuleBuilder(
                graph,
            ).should().resideInAModule(listOf(":app")).getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertModList(funcCtx, listOf(funcCtx), v8)
        assertTrue(v8.isEmpty())

        val assertModVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().resideInAModule(":app", ":core").getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertModVararg(funcCtx, listOf(funcCtx), v9)
        assertTrue(v9.isEmpty())

        val assertNotModSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(":other").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertNotModSingle(funcCtx, listOf(funcCtx), v10)
        assertTrue(v10.isEmpty())

        val assertNotModList =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(listOf(":other")).getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertNotModList(funcCtx, listOf(funcCtx), v11)
        assertTrue(v11.isEmpty())

        val assertNotModVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notResideInAModule(":other", ":wrong").getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertNotModVararg(funcCtx, listOf(funcCtx), v12)
        assertTrue(v12.isEmpty())

        val assertNameSingle = FunctionsRuleBuilder(graph).should().haveName("myFunc").getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertNameSingle(funcCtx, listOf(funcCtx), v13)
        assertTrue(v13.isEmpty())

        val assertNameList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveName(listOf("myFunc")).getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertNameList(funcCtx, listOf(funcCtx), v14)
        assertTrue(v14.isEmpty())

        val assertNameVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveName("myFunc", "other").getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertNameVararg(funcCtx, listOf(funcCtx), v15)
        assertTrue(v15.isEmpty())

        val assertNamePred =
            FunctionsRuleBuilder(
                graph,
            ).should().haveName { it == "myFunc" }.getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertNamePred(funcCtx, listOf(funcCtx), v16)
        assertTrue(v16.isEmpty())

        val assertNamePredDesc =
            FunctionsRuleBuilder(
                graph,
            ).should().haveName { it == "myFunc" }.getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertNamePredDesc(funcCtx, listOf(funcCtx), v17)
        assertTrue(v17.isEmpty())

        val assertNotNameSingle = FunctionsRuleBuilder(graph).should().notHaveName("other").getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertNotNameSingle(funcCtx, listOf(funcCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotNameList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveName(listOf("other")).getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotNameList(funcCtx, listOf(funcCtx), v19)
        assertTrue(v19.isEmpty())

        val assertNotNameVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveName("other", "wrong").getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertNotNameVararg(funcCtx, listOf(funcCtx), v20)
        assertTrue(v20.isEmpty())

        val assertEndSingle = FunctionsRuleBuilder(graph).should().haveNameEndingWith("Func").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertEndSingle(funcCtx, listOf(funcCtx), v21)
        assertTrue(v21.isEmpty())

        val assertEndList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameEndingWith(listOf("Func")).getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertEndList(funcCtx, listOf(funcCtx), v22)
        assertTrue(v22.isEmpty())

        val assertEndVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameEndingWith("Func", "Other").getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertEndVararg(funcCtx, listOf(funcCtx), v23)
        assertTrue(v23.isEmpty())

        val assertNotEndSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong").getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertNotEndSingle(funcCtx, listOf(funcCtx), v24)
        assertTrue(v24.isEmpty())

        val assertNotEndList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertNotEndList(funcCtx, listOf(funcCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotEndVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong", "Bad").getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotEndVararg(funcCtx, listOf(funcCtx), v26)
        assertTrue(v26.isEmpty())

        val assertStartSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my").getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertStartSingle(funcCtx, listOf(funcCtx), v27)
        assertTrue(v27.isEmpty())

        val assertStartList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameStartingWith(listOf("my")).getShouldAssertion()!!
        val v28 = mutableListOf<String>()
        assertStartList(funcCtx, listOf(funcCtx), v28)
        assertTrue(v28.isEmpty())

        val assertStartVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my", "other").getShouldAssertion()!!
        val v29 = mutableListOf<String>()
        assertStartVararg(funcCtx, listOf(funcCtx), v29)
        assertTrue(v29.isEmpty())

        val assertNotStartSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong").getShouldAssertion()!!
        val v30 = mutableListOf<String>()
        assertNotStartSingle(funcCtx, listOf(funcCtx), v30)
        assertTrue(v30.isEmpty())

        val assertNotStartList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith(listOf("wrong")).getShouldAssertion()!!
        val v31 = mutableListOf<String>()
        assertNotStartList(funcCtx, listOf(funcCtx), v31)
        assertTrue(v31.isEmpty())

        val assertNotStartVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong", "bad").getShouldAssertion()!!
        val v32 = mutableListOf<String>()
        assertNotStartVararg(funcCtx, listOf(funcCtx), v32)
        assertTrue(v32.isEmpty())

        val assertMatchSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameMatching("my*").getShouldAssertion()!!
        val v33 = mutableListOf<String>()
        assertMatchSingle(funcCtx, listOf(funcCtx), v33)
        assertTrue(v33.isEmpty())

        val assertMatchList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameMatching(listOf("my*")).getShouldAssertion()!!
        val v34 = mutableListOf<String>()
        assertMatchList(funcCtx, listOf(funcCtx), v34)
        assertTrue(v34.isEmpty())

        val assertMatchVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().haveNameMatching("my*", "other*").getShouldAssertion()!!
        val v35 = mutableListOf<String>()
        assertMatchVararg(funcCtx, listOf(funcCtx), v35)
        assertTrue(v35.isEmpty())

        val assertNotMatchSingle =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*").getShouldAssertion()!!
        val v36 = mutableListOf<String>()
        assertNotMatchSingle(funcCtx, listOf(funcCtx), v36)
        assertTrue(v36.isEmpty())

        val assertNotMatchList =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching(listOf("wrong*")).getShouldAssertion()!!
        val v37 = mutableListOf<String>()
        assertNotMatchList(funcCtx, listOf(funcCtx), v37)
        assertTrue(v37.isEmpty())

        val assertNotMatchVararg =
            FunctionsRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*", "bad*").getShouldAssertion()!!
        val v38 = mutableListOf<String>()
        assertNotMatchVararg(funcCtx, listOf(funcCtx), v38)
        assertTrue(v38.isEmpty())
    }

    @Test
    fun `test FunctionsShould visibilities and annotations`() {
        val funcCtx =
            createFuncCtx(
                name = "myFunc",
                className = "ClassA",
                packageName = "com.example",
                visibility = Visibility.PUBLIC,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertPub = FunctionsRuleBuilder(graph).should().bePublic().getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertPub(funcCtx, listOf(funcCtx), v1)
        assertTrue(v1.isEmpty())

        val assertNotInternal = FunctionsRuleBuilder(graph).should().notBeInternal().getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotInternal(funcCtx, listOf(funcCtx), v2)
        assertTrue(v2.isEmpty())

        val assertNotPriv = FunctionsRuleBuilder(graph).should().notBePrivate().getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNotPriv(funcCtx, listOf(funcCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNotProt = FunctionsRuleBuilder(graph).should().notBeProtected().getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotProt(funcCtx, listOf(funcCtx), v4)
        assertTrue(v4.isEmpty())

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
}

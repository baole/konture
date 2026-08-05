/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
internal class FunctionsCoverageTest : KontureScopeTestFixture() {
    private fun createFuncCtx(
        name: String = "myFunc",
        className: String? = "ClassA",
        packageName: String = "com.example",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        returnType: String = "Unit",
        parameters: List<ParameterDeclaration> = emptyList(),
        annotations: List<AnnotationDeclaration> = emptyList(),
        isExtension: Boolean = false,
        kdocText: String? = null,
        filePath: String = "/src/ClassA.kt",
        modulePath: String = ":app",
        receiverType: String? = null,
    ): FunctionDeclarationContext {
        val funcDecl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                parameters = parameters,
                annotations = annotations,
                kdocText = kdocText,
                isExtension = isExtension,
                receiverType = receiverType,
            )
        return FunctionDeclarationContext(
            declaration = funcDecl,
            packageName = packageName,
            className = className,
            modulePath = modulePath,
            filePath = filePath,
            sourceSet = null,
        )
    }

    @Test
    fun `test FunctionsShould name assertions`() {
        val funcCtx = createFuncCtx(name = "myFunc", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPkgSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAPackage("com.example")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vPkgSingle)
        assertTrue(vPkgSingle.isEmpty())

        val vPkgList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAPackage(listOf("com.example"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vPkgList)
        assertTrue(vPkgList.isEmpty())

        val vPkgVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAPackage("com.example", "com.other")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vPkgVararg)
        assertTrue(vPkgVararg.isEmpty())

        val vPkgPred = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAPackage { it.startsWith("com") }
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vPkgPred)
        assertTrue(vPkgPred.isEmpty())

        val vEndSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameEndingWith("Func")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vEndSingle)
        assertTrue(vEndSingle.isEmpty())

        val vEndList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameEndingWith(listOf("Func"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vEndList)
        assertTrue(vEndList.isEmpty())

        val vEndVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameEndingWith("Func", "Method")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vEndVararg)
        assertTrue(vEndVararg.isEmpty())

        val vStartSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameStartingWith("my")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vStartSingle)
        assertTrue(vStartSingle.isEmpty())

        val vStartList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameStartingWith(listOf("my"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vStartList)
        assertTrue(vStartList.isEmpty())

        val vStartVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameStartingWith("my", "do")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vStartVararg)
        assertTrue(vStartVararg.isEmpty())

        val vMatchSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameMatching("my*")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vMatchSingle)
        assertTrue(vMatchSingle.isEmpty())

        val vMatchList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameMatching(listOf("my*"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vMatchList)
        assertTrue(vMatchList.isEmpty())

        val vMatchVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveNameMatching("my*", "do*")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vMatchVararg)
        assertTrue(vMatchVararg.isEmpty())

        val vModSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule("app")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vModSingle)
        assertTrue(vModSingle.isEmpty())

        val vModList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule(listOf("app"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vModList)
        assertTrue(vModList.isEmpty())

        val vModVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule("app", "core")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vModVararg)
        assertTrue(vModVararg.isEmpty())

        val vNameSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName("myFunc")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNameSingle)
        assertTrue(vNameSingle.isEmpty())

        val vNameList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName(listOf("myFunc"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNameList)
        assertTrue(vNameList.isEmpty())

        val vNameVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName("myFunc", "otherFunc")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNameVararg)
        assertTrue(vNameVararg.isEmpty())

        val vNamePred = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName { it == "myFunc" }
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNamePred)
        assertTrue(vNamePred.isEmpty())

        val vNotNameSingle = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveName("wrong")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotNameSingle)
        assertTrue(vNotNameSingle.isEmpty())

        val vNotNameList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveName(listOf("wrong"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotNameList)
        assertTrue(vNotNameList.isEmpty())

        val vPkgOf = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInPackageOf(String::class)
            .getShouldAssertion()!!(createFuncCtx(packageName = "java.lang"), listOf(funcCtx), vPkgOf)
        assertTrue(vPkgOf.isEmpty())

        val vNotModVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInAModule("core", "feature")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotModVararg)
        assertTrue(vNotModVararg.isEmpty())

        val vNotModList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInModules(listOf("core"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotModList)
        assertTrue(vNotModList.isEmpty())

        val vNotStartList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameStartingWith(listOf("wrong"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotStartList)
        assertTrue(vNotStartList.isEmpty())

        val vNotStartVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameStartingWith("wrong", "bad")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotStartVararg)
        assertTrue(vNotStartVararg.isEmpty())

        val vNotEndList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameEndingWith(listOf("wrong"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotEndList)
        assertTrue(vNotEndList.isEmpty())

        val vNotEndVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameEndingWith("wrong", "bad")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotEndVararg)
        assertTrue(vNotEndVararg.isEmpty())

        val vNotMatchList = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameMatching(listOf("wrong*"))
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotMatchList)
        assertTrue(vNotMatchList.isEmpty())

        val vNotMatchVararg = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveNameMatching("wrong*", "bad*")
            .getShouldAssertion()!!(funcCtx, listOf(funcCtx), vNotMatchVararg)
        assertTrue(vNotMatchVararg.isEmpty())
    }

    @Test
    fun `test FunctionsShould modifier assertions`() {
        val funcCtxAll =
            createFuncCtx(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers =
                    setOf(
                        Modifier.SUSPEND,
                        Modifier.INLINE,
                        Modifier.OPEN,
                        Modifier.ABSTRACT,
                        Modifier.OVERRIDE,
                        Modifier.OPERATOR,
                        Modifier.INFIX,
                    ),
                kdocText = "/** doc */",
                isExtension = true,
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPub = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().bePublic()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vPub)
        assertTrue(vPub.isEmpty())

        val vSuspend = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beSuspend()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vSuspend)
        assertTrue(vSuspend.isEmpty())

        val vInline = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInline()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vInline)
        assertTrue(vInline.isEmpty())

        val vOpen = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOpen()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vOpen)
        assertTrue(vOpen.isEmpty())

        val vAbstract = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beAbstract()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vAbstract)
        assertTrue(vAbstract.isEmpty())

        val vOverride = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOverride()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vOverride)
        assertTrue(vOverride.isEmpty())

        val vOperator = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOperator()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vOperator)
        assertTrue(vOperator.isEmpty())

        val vInfix = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInfix()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vInfix)
        assertTrue(vInfix.isEmpty())

        val vExt = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beExtension()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vExt)
        assertTrue(vExt.isEmpty())

        val vDoc = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beDocumentedWithKDoc()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vDoc)
        assertTrue(vDoc.isEmpty())

        val vNotSuspend = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeSuspend()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotSuspend)
        assertEquals(1, vNotSuspend.size)

        val vNotInline = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeInline()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotInline)
        assertEquals(1, vNotInline.size)

        val vNotOpen = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeOpen()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotOpen)
        assertEquals(1, vNotOpen.size)

        val vNotAbstract = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeAbstract()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotAbstract)
        assertEquals(1, vNotAbstract.size)

        val vNotOverride = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeOverride()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotOverride)
        assertEquals(1, vNotOverride.size)

        val vNotOperator = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeOperator()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotOperator)
        assertEquals(1, vNotOperator.size)

        val vNotInfix = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notBeInfix()
            .getShouldAssertion()!!(funcCtxAll, listOf(funcCtxAll), vNotInfix)
        assertEquals(1, vNotInfix.size)

        val funcCtxInternal = createFuncCtx(className = null, visibility = Visibility.INTERNAL)
        val vInternal = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInternal()
            .getShouldAssertion()!!(funcCtxInternal, listOf(funcCtxInternal), vInternal)
        assertTrue(vInternal.isEmpty())

        val vTopLevel = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beTopLevel()
            .getShouldAssertion()!!(funcCtxInternal, listOf(funcCtxInternal), vTopLevel)
        assertTrue(vTopLevel.isEmpty())

        val funcCtxPrivate = createFuncCtx(visibility = Visibility.PRIVATE)
        val vPrivate = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().bePrivate()
            .getShouldAssertion()!!(funcCtxPrivate, listOf(funcCtxPrivate), vPrivate)
        assertTrue(vPrivate.isEmpty())

        val vMember = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beMember()
            .getShouldAssertion()!!(funcCtxPrivate, listOf(funcCtxPrivate), vMember)
        assertTrue(vMember.isEmpty())

        val funcCtxProtected = createFuncCtx(visibility = Visibility.PROTECTED)
        val vProtected = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beProtected()
            .getShouldAssertion()!!(funcCtxProtected, listOf(funcCtxProtected), vProtected)
        assertTrue(vProtected.isEmpty())
    }

    @Test
    fun `test FunctionsThat filters`() {
        val param = ParameterDeclaration("p1", "String", false, emptyList())
        val annot =
            AnnotationDeclaration(
                "MyAnnotation",
                "com.example.MyAnnotation",
                listOf(AnnotationArgumentDeclaration("arg", "val")),
            )
        val funcCtx =
            createFuncCtx(
                name = "myFunc",
                className = "ClassA",
                packageName = "com.example",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND, Modifier.INLINE, Modifier.OPEN),
                returnType = "Unit",
                parameters = listOf(param),
                annotations = listOf(annot),
                isExtension = true,
                receiverType = "String",
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pPkgSingle = FunctionsRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(pPkgSingle(funcCtx))
        assertFalse(pPkgSingle(createFuncCtx(packageName = "org.other")))

        val pPkgList = FunctionsRuleBuilder(graph).that().resideInAPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(pPkgList(funcCtx))
        assertFalse(pPkgList(createFuncCtx(packageName = "org.other")))

        val pPkgVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example", "com.other").getThatPredicate()!!
        assertTrue(pPkgVararg(funcCtx))
        assertFalse(pPkgVararg(createFuncCtx(packageName = "org.other")))

        val pPkgPred = FunctionsRuleBuilder(graph).that().resideInAPackage { it.startsWith("com") }.getThatPredicate()!!
        assertTrue(pPkgPred(funcCtx))
        assertFalse(pPkgPred(createFuncCtx(packageName = "org.other")))

        val pModSingle = FunctionsRuleBuilder(graph).that().resideInAModule("app").getThatPredicate()!!
        assertTrue(pModSingle(funcCtx))
        assertFalse(pModSingle(createFuncCtx(modulePath = ":other")))

        val pModList = FunctionsRuleBuilder(graph).that().resideInAModule(listOf("app")).getThatPredicate()!!
        assertTrue(pModList(funcCtx))
        assertFalse(pModList(createFuncCtx(modulePath = ":other")))

        val pModVararg = FunctionsRuleBuilder(graph).that().resideInAModule("app", "core").getThatPredicate()!!
        assertTrue(pModVararg(funcCtx))
        assertFalse(pModVararg(createFuncCtx(modulePath = ":other")))

        val pNameSingle = FunctionsRuleBuilder(graph).that().haveName("myFunc").getThatPredicate()!!
        assertTrue(pNameSingle(funcCtx))
        assertFalse(pNameSingle(createFuncCtx(name = "other")))

        val pNameList = FunctionsRuleBuilder(graph).that().haveName(listOf("myFunc")).getThatPredicate()!!
        assertTrue(pNameList(funcCtx))
        assertFalse(pNameList(createFuncCtx(name = "other")))

        val pNameVararg = FunctionsRuleBuilder(graph).that().haveName("myFunc", "other").getThatPredicate()!!
        assertTrue(pNameVararg(funcCtx))
        assertFalse(pNameVararg(createFuncCtx(name = "bad")))

        val pNotNameSingle = FunctionsRuleBuilder(graph).that().notHaveName("other").getThatPredicate()!!
        assertTrue(pNotNameSingle(funcCtx))
        assertFalse(pNotNameSingle(createFuncCtx(name = "other")))

        val pNotNameList = FunctionsRuleBuilder(graph).that().notHaveName(listOf("other")).getThatPredicate()!!
        assertTrue(pNotNameList(funcCtx))
        assertFalse(pNotNameList(createFuncCtx(name = "other")))

        val pNotNameVararg = FunctionsRuleBuilder(graph).that().notHaveName("other", "wrong").getThatPredicate()!!
        assertTrue(pNotNameVararg(funcCtx))
        assertFalse(pNotNameVararg(createFuncCtx(name = "other")))

        val pNotNamePred = FunctionsRuleBuilder(graph).that().notHaveName { it == "other" }.getThatPredicate()!!
        assertTrue(pNotNamePred(funcCtx))
        assertFalse(pNotNamePred(createFuncCtx(name = "other")))

        val pNamePred = FunctionsRuleBuilder(graph).that().haveName { it == "myFunc" }.getThatPredicate()!!
        assertTrue(pNamePred(funcCtx))
        assertFalse(pNamePred(createFuncCtx(name = "other")))

        val pNameDescPred = FunctionsRuleBuilder(graph).that().haveName("desc", { it == "myFunc" }).getThatPredicate()!!
        assertTrue(pNameDescPred(funcCtx))
        assertFalse(pNameDescPred(createFuncCtx(name = "other")))

        val pEndSingle = FunctionsRuleBuilder(graph).that().haveNameEndingWith("Func").getThatPredicate()!!
        assertTrue(pEndSingle(funcCtx))
        assertFalse(pEndSingle(createFuncCtx(name = "other")))

        val pEndList = FunctionsRuleBuilder(graph).that().haveNameEndingWith(listOf("Func")).getThatPredicate()!!
        assertTrue(pEndList(funcCtx))
        assertFalse(pEndList(createFuncCtx(name = "other")))

        val pEndVararg = FunctionsRuleBuilder(graph).that().haveNameEndingWith("Func", "Method").getThatPredicate()!!
        assertTrue(pEndVararg(funcCtx))
        assertFalse(pEndVararg(createFuncCtx(name = "other")))

        val pStartSingle = FunctionsRuleBuilder(graph).that().haveNameStartingWith("my").getThatPredicate()!!
        assertTrue(pStartSingle(funcCtx))
        assertFalse(pStartSingle(createFuncCtx(name = "other")))

        val pStartList = FunctionsRuleBuilder(graph).that().haveNameStartingWith(listOf("my")).getThatPredicate()!!
        assertTrue(pStartList(funcCtx))
        assertFalse(pStartList(createFuncCtx(name = "other")))

        val pStartVararg = FunctionsRuleBuilder(graph).that().haveNameStartingWith("my", "do").getThatPredicate()!!
        assertTrue(pStartVararg(funcCtx))
        assertFalse(pStartVararg(createFuncCtx(name = "other")))

        val pMatchSingle = FunctionsRuleBuilder(graph).that().haveNameMatching("my*").getThatPredicate()!!
        assertTrue(pMatchSingle(funcCtx))
        assertFalse(pMatchSingle(createFuncCtx(name = "other")))

        val pMatchList = FunctionsRuleBuilder(graph).that().haveNameMatching(listOf("my*")).getThatPredicate()!!
        assertTrue(pMatchList(funcCtx))
        assertFalse(pMatchList(createFuncCtx(name = "other")))

        val pMatchVararg = FunctionsRuleBuilder(graph).that().haveNameMatching("my*", "do*").getThatPredicate()!!
        assertTrue(pMatchVararg(funcCtx))
        assertFalse(pMatchVararg(createFuncCtx(name = "other")))

        val pPub = FunctionsRuleBuilder(graph).that().arePublic().getThatPredicate()!!
        assertTrue(pPub(funcCtx))
        assertFalse(pPub(createFuncCtx(visibility = Visibility.PRIVATE)))

        val pExt = FunctionsRuleBuilder(graph).that().areExtension().getThatPredicate()!!
        assertTrue(pExt(funcCtx))
        assertFalse(pExt(createFuncCtx(isExtension = false)))

        val pReceiver = FunctionsRuleBuilder(graph).that().haveExtensionReceiver("String").getThatPredicate()!!
        assertTrue(pReceiver(funcCtx))
        assertFalse(pReceiver(createFuncCtx(receiverType = null)))

        val pMember = FunctionsRuleBuilder(graph).that().areMember().getThatPredicate()!!
        assertTrue(pMember(funcCtx))
        assertFalse(pMember(createFuncCtx(className = null)))

        val pParam = FunctionsRuleBuilder(graph).that().haveParameterOf("String").getThatPredicate()!!
        assertTrue(pParam(funcCtx))
        assertFalse(pParam(createFuncCtx(parameters = emptyList())))

        val pAnnotSingle = FunctionsRuleBuilder(graph).that().haveAnnotationOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotSingle(funcCtx))
        assertFalse(pAnnotSingle(createFuncCtx(annotations = emptyList())))

        val pAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAnnotList(funcCtx))
        assertFalse(pAnnotList(createFuncCtx(annotations = emptyList())))

        val pAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationOf("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnnotVararg(funcCtx))
        assertFalse(pAnnotVararg(createFuncCtx(annotations = emptyList())))

        val pAllAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAllAnnotList(funcCtx))
        assertFalse(pAllAnnotList(createFuncCtx(annotations = emptyList())))

        val pAllAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAllAnnotVararg(funcCtx))
        assertFalse(pAllAnnotVararg(createFuncCtx(annotations = emptyList())))

        val pAnyAnnotList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(listOf("MyAnnotation", "Other")).getThatPredicate()!!
        assertTrue(pAnyAnnotList(funcCtx))
        assertFalse(pAnyAnnotList(createFuncCtx(annotations = emptyList())))

        val pAnyAnnotVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnyAnnotVararg(funcCtx))
        assertFalse(pAnyAnnotVararg(createFuncCtx(annotations = emptyList())))

        val pOpen = FunctionsRuleBuilder(graph).that().areOpen().getThatPredicate()!!
        assertTrue(pOpen(funcCtx))
        assertFalse(pOpen(createFuncCtx(modifiers = emptySet())))

        val pModifier = FunctionsRuleBuilder(graph).that().haveModifier(Modifier.OPEN).getThatPredicate()!!
        assertTrue(pModifier(funcCtx))
        assertFalse(pModifier(createFuncCtx(modifiers = emptySet())))

        val pAllModifiersList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllModifiers(listOf(Modifier.OPEN, Modifier.SUSPEND)).getThatPredicate()!!
        assertTrue(pAllModifiersList(funcCtx))
        assertFalse(pAllModifiersList(createFuncCtx(modifiers = emptySet())))

        val pAllModifiersVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllModifiers(Modifier.OPEN, Modifier.SUSPEND).getThatPredicate()!!
        assertTrue(pAllModifiersVararg(funcCtx))
        assertFalse(pAllModifiersVararg(createFuncCtx(modifiers = emptySet())))

        val pAnyModifierList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyModifier(listOf(Modifier.OPEN, Modifier.ABSTRACT)).getThatPredicate()!!
        assertTrue(pAnyModifierList(funcCtx))
        assertFalse(pAnyModifierList(createFuncCtx(modifiers = emptySet())))

        val pAnyModifierVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyModifier(Modifier.OPEN, Modifier.ABSTRACT).getThatPredicate()!!
        assertTrue(pAnyModifierVararg(funcCtx))
        assertFalse(pAnyModifierVararg(createFuncCtx(modifiers = emptySet())))

        val pVisSingle = FunctionsRuleBuilder(graph).that().haveVisibility(Visibility.PUBLIC).getThatPredicate()!!
        assertTrue(pVisSingle(funcCtx))
        assertFalse(pVisSingle(createFuncCtx(visibility = Visibility.PRIVATE)))

        val pVisList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL)).getThatPredicate()!!
        assertTrue(pVisList(funcCtx))
        assertFalse(pVisList(createFuncCtx(visibility = Visibility.PRIVATE)))

        val pVisVararg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getThatPredicate()!!
        assertTrue(pVisVararg(funcCtx))
        assertFalse(pVisVararg(createFuncCtx(visibility = Visibility.PRIVATE)))

        val pReturnSingle = FunctionsRuleBuilder(graph).that().haveReturnType("Unit").getThatPredicate()!!
        assertTrue(pReturnSingle(funcCtx))
        assertFalse(pReturnSingle(createFuncCtx(returnType = "Int")))

        val pReturnList = FunctionsRuleBuilder(graph).that().haveReturnType(listOf("Unit")).getThatPredicate()!!
        assertTrue(pReturnList(funcCtx))
        assertFalse(pReturnList(createFuncCtx(returnType = "Int")))

        val pReturnVararg = FunctionsRuleBuilder(graph).that().haveReturnType("Unit", "String").getThatPredicate()!!
        assertTrue(pReturnVararg(funcCtx))
        assertFalse(pReturnVararg(createFuncCtx(returnType = "Int")))

        val pParamTypesList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes(listOf("String")).getThatPredicate()!!
        assertTrue(pParamTypesList(funcCtx))
        assertFalse(pParamTypesList(createFuncCtx(parameters = emptyList())))

        val pParamTypesVararg = FunctionsRuleBuilder(graph).that().haveParameterTypes("String").getThatPredicate()!!
        assertTrue(pParamTypesVararg(funcCtx))
        assertFalse(pParamTypesVararg(createFuncCtx(parameters = emptyList())))

        val pAnyParamTypeList =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyParameterType(listOf("String")).getThatPredicate()!!
        assertTrue(pAnyParamTypeList(funcCtx))
        assertFalse(pAnyParamTypeList(createFuncCtx(parameters = emptyList())))

        val pAnyParamTypeVararg = FunctionsRuleBuilder(graph).that().haveAnyParameterType("String").getThatPredicate()!!
        assertTrue(pAnyParamTypeVararg(funcCtx))
        assertFalse(pAnyParamTypeVararg(createFuncCtx(parameters = emptyList())))

        val pAnnotArg =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument("MyAnnotation", "arg", "val").getThatPredicate()!!
        assertTrue(pAnnotArg(funcCtx))
        assertFalse(pAnnotArg(createFuncCtx(annotations = emptyList())))

        val pSatisfy =
            FunctionsRuleBuilder(
                graph,
            ).that().satisfy { it.declaration.name == "myFunc" }.getThatPredicate()!!
        assertTrue(pSatisfy(funcCtx))
        assertFalse(pSatisfy(createFuncCtx(name = "other")))

        val pSuspend = FunctionsRuleBuilder(graph).that().beSuspend().getThatPredicate()!!
        assertTrue(pSuspend(funcCtx))
        assertFalse(pSuspend(createFuncCtx(modifiers = emptySet())))

        val pInline = FunctionsRuleBuilder(graph).that().beInline().getThatPredicate()!!
        assertTrue(pInline(funcCtx))
        assertFalse(pInline(createFuncCtx(modifiers = emptySet())))

        val pParamCount = FunctionsRuleBuilder(graph).that().haveParameterCount(1).getThatPredicate()!!
        assertTrue(pParamCount(funcCtx))
        assertFalse(pParamCount(createFuncCtx(parameters = emptyList())))

        val pParamCountPred = FunctionsRuleBuilder(graph).that().haveParameterCount { it > 0 }.getThatPredicate()!!
        assertTrue(pParamCountPred(funcCtx))
        assertFalse(pParamCountPred(createFuncCtx(parameters = emptyList())))

        val pBelongClass = FunctionsRuleBuilder(graph).that().belongToClass("ClassA").getThatPredicate()!!
        assertTrue(pBelongClass(funcCtx))
        assertFalse(pBelongClass(createFuncCtx(className = "Other")))

        val pAnyOf =
            FunctionsRuleBuilder(graph).that().anyOf(
                { haveName("myFunc") },
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(funcCtx))

        val pAllOf =
            FunctionsRuleBuilder(graph).that().allOf(
                { haveName("myFunc") },
                { resideInAPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(funcCtx))

        val pNoneOf =
            FunctionsRuleBuilder(graph).that().noneOf(
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(funcCtx))
    }

    @Test
    fun `test FunctionsShould failure messages`() {
        val funcCtx = createFuncCtx(name = "myFunc", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val v1 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAPackage(listOf("wrong.pkg")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg", "other").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAPackage { false }.getShouldAssertion()!!(funcCtx, listOf(funcCtx), v4)
        assertEquals(1, v4.size)

        val v7 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAModule("otherMod").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().resideInAModule(listOf("otherMod")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notResideInAModule("app").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notResideInAModule(listOf("app")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName("wrong").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveName(listOf("wrong")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveName("wrong", "bad").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveName { false }.getShouldAssertion()!!(funcCtx, listOf(funcCtx), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notHaveName("myFunc").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notHaveName(listOf("myFunc")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Wrong").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v17)
        assertEquals(1, v17.size)

        val v18 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v18)
        assertEquals(1, v18.size)

        val v19 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith("Func").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v19)
        assertEquals(1, v19.size)

        val v20 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Wrong").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v20)
        assertEquals(1, v20.size)

        val v21 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith("my").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v21)
        assertEquals(1, v21.size)

        val v22 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameMatching("wrong*").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v22)
        assertEquals(1, v22.size)

        val v23 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notHaveNameMatching("my*").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v23)
        assertEquals(1, v23.size)

        val v24 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().bePublic().getShouldAssertion()!!(
            createFuncCtx(visibility = Visibility.PRIVATE),
            listOf(funcCtx),
            v24,
        )
        assertEquals(1, v24.size)

        val v25 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v25)
        assertEquals(1, v25.size)

        val v26 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v26)
        assertEquals(1, v26.size)

        val v27 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v27)
        assertEquals(1, v27.size)

        val v28 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beSuspend().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v28)
        assertEquals(1, v28.size)

        val v29 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInline().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v29)
        assertEquals(1, v29.size)

        val v30 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v30)
        assertEquals(1, v30.size)

        val v31 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beAbstract().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v31)
        assertEquals(1, v31.size)

        val v32 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOverride().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v32)
        assertEquals(1, v32.size)

        val v33 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOperator().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v33)
        assertEquals(1, v33.size)

        val v34 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInfix().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v34)
        assertEquals(1, v34.size)

        val v35 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beExtension().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v35)
        assertEquals(1, v35.size)

        val v36 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beTopLevel().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v36)
        assertEquals(1, v36.size)

        val v37 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().beMember().getShouldAssertion()!!(createFuncCtx(className = null), listOf(funcCtx), v37)
        assertEquals(1, v37.size)

        val v38 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().haveReturnType("Int").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v38)
        assertEquals(1, v38.size)

        val v39 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnnotationOf("Missing").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v39)
        assertEquals(1, v39.size)

        val v40 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf("Missing").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v40)
        assertEquals(1, v40.size)

        val v41 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf("Missing").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v41)
        assertEquals(1, v41.size)

        val v43 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveParameterTypes("Int").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v43)
        assertEquals(1, v43.size)

        val v44 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnyParameterType("Int").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v44)
        assertEquals(1, v44.size)

        val v46 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnnotationWithArgument(
            "Missing",
            "arg",
            "val",
        ).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v46)
        assertEquals(1, v46.size)
    }
}

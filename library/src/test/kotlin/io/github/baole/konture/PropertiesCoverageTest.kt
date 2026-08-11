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

internal class PropertiesCoverageTest : KontureScopeTestFixture() {
    private fun createPropCtx(
        name: String = "myProp",
        className: String? = "ClassA",
        packageName: String = "com.example",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        type: String = "String",
        isVal: Boolean = true,
        annotations: List<AnnotationDeclaration> = emptyList(),
        isExtension: Boolean = false,
        modulePath: String = ":app",
    ): PropertyDeclarationContext {
        val decl =
            PropertyDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                type = type,
                isVal = isVal,
                annotations = annotations,
                kdocText = null,
                isExtension = isExtension,
            )
        val cls =
            className?.let {
                ClassDeclaration(
                    name = it,
                    fqName = "$packageName.$it",
                    packageName = packageName,
                    isInterface = false,
                    isAbstract = false,
                    annotations = emptyList(),
                    imports = emptyList(),
                    referencedTypes = emptySet(),
                    filePath = "/src/$it.kt",
                )
            }
        val file = FileDeclaration("${className ?: "TopLevel"}.kt", packageName, classes = cls?.let { listOf(it) } ?: emptyList())
        return PropertyDeclarationContext(decl, cls, file, modulePath, file.filePath)
    }

    @Test
    fun `test PropertiesShould package, module, name, and visibility assertions`() {
        val propCtx = createPropCtx(name = "myProp", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertPkgSingle = PropertiesRuleBuilder(graph).should().resideInAPackage("com.example").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertPkgSingle(propCtx, listOf(propCtx), v1)
        assertTrue(v1.isEmpty())

        val assertPkgList =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage(listOf("com.example")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertPkgList(propCtx, listOf(propCtx), v2)
        assertTrue(v2.isEmpty())

        val assertPkgVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage("com.example", "com.other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertPkgVararg(propCtx, listOf(propCtx), v3)
        assertTrue(v3.isEmpty())

        val assertPkgPred =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage { it.startsWith("com") }.getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertPkgPred(propCtx, listOf(propCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotPkgSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage("com.other").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotPkgSingle(propCtx, listOf(propCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotPkgList =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage(listOf("com.other")).getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotPkgList(propCtx, listOf(propCtx), v6)
        assertTrue(v6.isEmpty())

        val assertNotPkgVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage("com.other", "org.wrong").getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertNotPkgVararg(propCtx, listOf(propCtx), v7)
        assertTrue(v7.isEmpty())

        val assertModSingle = PropertiesRuleBuilder(graph).should().resideInAModule("app").getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertModSingle(propCtx, listOf(propCtx), v8)
        assertTrue(v8.isEmpty())

        val assertModList = PropertiesRuleBuilder(graph).should().resideInAModule(listOf("app")).getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertModList(propCtx, listOf(propCtx), v9)
        assertTrue(v9.isEmpty())

        val assertModVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAModule("app", "core").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertModVararg(propCtx, listOf(propCtx), v10)
        assertTrue(v10.isEmpty())

        val assertNotModSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule("core").getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertNotModSingle(propCtx, listOf(propCtx), v11)
        assertTrue(v11.isEmpty())

        val assertNotModList =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule(listOf("core")).getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertNotModList(propCtx, listOf(propCtx), v12)
        assertTrue(v12.isEmpty())

        val assertNotModVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule("core", "feature").getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertNotModVararg(propCtx, listOf(propCtx), v13)
        assertTrue(v13.isEmpty())

        val assertNameSingle = PropertiesRuleBuilder(graph).should().haveName("myProp").getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertNameSingle(propCtx, listOf(propCtx), v14)
        assertTrue(v14.isEmpty())

        val assertNameList = PropertiesRuleBuilder(graph).should().haveName(listOf("myProp")).getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertNameList(propCtx, listOf(propCtx), v15)
        assertTrue(v15.isEmpty())

        val assertNameVararg = PropertiesRuleBuilder(graph).should().haveName("myProp", "other").getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertNameVararg(propCtx, listOf(propCtx), v16)
        assertTrue(v16.isEmpty())

        val assertNamePred = PropertiesRuleBuilder(graph).should().haveName { it == "myProp" }.getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertNamePred(propCtx, listOf(propCtx), v17)
        assertTrue(v17.isEmpty())

        val assertNamePredDesc =
            PropertiesRuleBuilder(
                graph,
            ).should().haveName("custom desc") { it == "myProp" }.getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertNamePredDesc(propCtx, listOf(propCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotNameSingle = PropertiesRuleBuilder(graph).should().notHaveName("other").getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotNameSingle(propCtx, listOf(propCtx), v19)
        assertTrue(v19.isEmpty())

        val assertNotNameList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveName(listOf("other")).getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertNotNameList(propCtx, listOf(propCtx), v20)
        assertTrue(v20.isEmpty())

        val assertNotNameVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveName("other", "wrong").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertNotNameVararg(propCtx, listOf(propCtx), v21)
        assertTrue(v21.isEmpty())

        val assertNotNamePred =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveName { it == "other" }.getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertNotNamePred(propCtx, listOf(propCtx), v22)
        assertTrue(v22.isEmpty())

        val assertEndSingle = PropertiesRuleBuilder(graph).should().haveNameEndingWith("Prop").getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertEndSingle(propCtx, listOf(propCtx), v23)
        assertTrue(v23.isEmpty())

        val assertEndList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameEndingWith(listOf("Prop")).getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertEndList(propCtx, listOf(propCtx), v24)
        assertTrue(v24.isEmpty())

        val assertEndVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameEndingWith("Prop", "Field").getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertEndVararg(propCtx, listOf(propCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotEndSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("wrong").getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotEndSingle(propCtx, listOf(propCtx), v26)
        assertTrue(v26.isEmpty())

        val assertNotEndList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(listOf("wrong")).getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertNotEndList(propCtx, listOf(propCtx), v27)
        assertTrue(v27.isEmpty())

        val assertNotEndVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("wrong", "bad").getShouldAssertion()!!
        val v28 = mutableListOf<String>()
        assertNotEndVararg(propCtx, listOf(propCtx), v28)
        assertTrue(v28.isEmpty())

        val assertStartSingle = PropertiesRuleBuilder(graph).should().haveNameStartingWith("my").getShouldAssertion()!!
        val v29 = mutableListOf<String>()
        assertStartSingle(propCtx, listOf(propCtx), v29)
        assertTrue(v29.isEmpty())

        val assertStartList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameStartingWith(listOf("my")).getShouldAssertion()!!
        val v30 = mutableListOf<String>()
        assertStartList(propCtx, listOf(propCtx), v30)
        assertTrue(v30.isEmpty())

        val assertStartVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my", "the").getShouldAssertion()!!
        val v31 = mutableListOf<String>()
        assertStartVararg(propCtx, listOf(propCtx), v31)
        assertTrue(v31.isEmpty())

        val assertNotStartSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong").getShouldAssertion()!!
        val v32 = mutableListOf<String>()
        assertNotStartSingle(propCtx, listOf(propCtx), v32)
        assertTrue(v32.isEmpty())

        val assertNotStartList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith(listOf("wrong")).getShouldAssertion()!!
        val v33 = mutableListOf<String>()
        assertNotStartList(propCtx, listOf(propCtx), v33)
        assertTrue(v33.isEmpty())

        val assertNotStartVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong", "bad").getShouldAssertion()!!
        val v34 = mutableListOf<String>()
        assertNotStartVararg(propCtx, listOf(propCtx), v34)
        assertTrue(v34.isEmpty())

        val assertMatchSingle = PropertiesRuleBuilder(graph).should().haveNameMatching("my*").getShouldAssertion()!!
        val v35 = mutableListOf<String>()
        assertMatchSingle(propCtx, listOf(propCtx), v35)
        assertTrue(v35.isEmpty())

        val assertMatchList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameMatching(listOf("my*")).getShouldAssertion()!!
        val v36 = mutableListOf<String>()
        assertMatchList(propCtx, listOf(propCtx), v36)
        assertTrue(v36.isEmpty())

        val assertMatchVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameMatching("my*", "the*").getShouldAssertion()!!
        val v37 = mutableListOf<String>()
        assertMatchVararg(propCtx, listOf(propCtx), v37)
        assertTrue(v37.isEmpty())

        val assertNotMatchSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*").getShouldAssertion()!!
        val v38 = mutableListOf<String>()
        assertNotMatchSingle(propCtx, listOf(propCtx), v38)
        assertTrue(v38.isEmpty())

        val assertNotMatchList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching(listOf("wrong*")).getShouldAssertion()!!
        val v39 = mutableListOf<String>()
        assertNotMatchList(propCtx, listOf(propCtx), v39)
        assertTrue(v39.isEmpty())

        val assertNotMatchVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*", "bad*").getShouldAssertion()!!
        val v40 = mutableListOf<String>()
        assertNotMatchVararg(propCtx, listOf(propCtx), v40)
        assertTrue(v40.isEmpty())
    }

    @Test
    fun `test PropertiesShould visibilities, modifiers and types`() {
        val propCtx =
            createPropCtx(
                name = "myProp",
                className = "ClassA",
                packageName = "com.example",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN, Modifier.CONST, Modifier.LATEINIT),
                type = "String",
                isVal = true,
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertPub = PropertiesRuleBuilder(graph).should().bePublic().getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertPub(propCtx, listOf(propCtx), v1)
        assertTrue(v1.isEmpty())

        val assertNotInternal = PropertiesRuleBuilder(graph).should().notBeInternal().getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotInternal(propCtx, listOf(propCtx), v2)
        assertTrue(v2.isEmpty())

        val assertNotPriv = PropertiesRuleBuilder(graph).should().notBePrivate().getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNotPriv(propCtx, listOf(propCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNotProt = PropertiesRuleBuilder(graph).should().notBeProtected().getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotProt(propCtx, listOf(propCtx), v4)
        assertTrue(v4.isEmpty())

        val assertVal = PropertiesRuleBuilder(graph).should().beVal().getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertVal(propCtx, listOf(propCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotVar = PropertiesRuleBuilder(graph).should().notBeVar().getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotVar(propCtx, listOf(propCtx), v6)
        assertTrue(v6.isEmpty())

        val assertOpen = PropertiesRuleBuilder(graph).should().beOpen().getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertOpen(propCtx, listOf(propCtx), v7)
        assertTrue(v7.isEmpty())

        val assertNotAbstract = PropertiesRuleBuilder(graph).should().notBeAbstract().getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertNotAbstract(propCtx, listOf(propCtx), v8)
        assertTrue(v8.isEmpty())

        val assertConst = PropertiesRuleBuilder(graph).should().beConst().getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertConst(propCtx, listOf(propCtx), v9)
        assertTrue(v9.isEmpty())

        val assertLateinit = PropertiesRuleBuilder(graph).should().beLateinit().getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertLateinit(propCtx, listOf(propCtx), v10)
        assertTrue(v10.isEmpty())

        val assertNotOverride = PropertiesRuleBuilder(graph).should().notBeOverride().getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertNotOverride(propCtx, listOf(propCtx), v11)
        assertTrue(v11.isEmpty())

        val assertNotTopLevel = PropertiesRuleBuilder(graph).should().notBeTopLevel().getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertNotTopLevel(propCtx, listOf(propCtx), v12)
        assertTrue(v12.isEmpty())

        val assertMember = PropertiesRuleBuilder(graph).should().beMember().getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertMember(propCtx, listOf(propCtx), v13)
        assertTrue(v13.isEmpty())

        val assertModifier = PropertiesRuleBuilder(graph).should().haveModifier(Modifier.OPEN).getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertModifier(propCtx, listOf(propCtx), v14)
        assertTrue(v14.isEmpty())

        val assertAllModifiersList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllModifiers(listOf(Modifier.OPEN, Modifier.CONST)).getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertAllModifiersList(propCtx, listOf(propCtx), v15)
        assertTrue(v15.isEmpty())

        val assertAllModifiersVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllModifiers(Modifier.OPEN, Modifier.CONST).getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertAllModifiersVararg(propCtx, listOf(propCtx), v16)
        assertTrue(v16.isEmpty())

        val assertAnyModifierList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyModifier(listOf(Modifier.OPEN, Modifier.ABSTRACT)).getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertAnyModifierList(propCtx, listOf(propCtx), v17)
        assertTrue(v17.isEmpty())

        val assertAnyModifierVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyModifier(Modifier.OPEN, Modifier.ABSTRACT).getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertAnyModifierVararg(propCtx, listOf(propCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotModifier = PropertiesRuleBuilder(graph).should().notHaveModifier(Modifier.ABSTRACT).getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotModifier(propCtx, listOf(propCtx), v19)
        assertTrue(v19.isEmpty())

        val assertVisSingle = PropertiesRuleBuilder(graph).should().haveVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertVisSingle(propCtx, listOf(propCtx), v20)
        assertTrue(v20.isEmpty())

        val assertVisList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL)).getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertVisList(propCtx, listOf(propCtx), v21)
        assertTrue(v21.isEmpty())

        val assertVisVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertVisVararg(propCtx, listOf(propCtx), v22)
        assertTrue(v22.isEmpty())

        val assertTypeSingle = PropertiesRuleBuilder(graph).should().haveType("String").getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertTypeSingle(propCtx, listOf(propCtx), v23)
        assertTrue(v23.isEmpty())

        val assertTypeList = PropertiesRuleBuilder(graph).should().haveType(listOf("String")).getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertTypeList(propCtx, listOf(propCtx), v24)
        assertTrue(v24.isEmpty())

        val assertTypeVararg = PropertiesRuleBuilder(graph).should().haveType("String", "Int").getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertTypeVararg(propCtx, listOf(propCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotTypeSingle = PropertiesRuleBuilder(graph).should().notHaveType("Int").getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotTypeSingle(propCtx, listOf(propCtx), v26)
        assertTrue(v26.isEmpty())

        val assertNotTypeList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveType(listOf("Int")).getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertNotTypeList(propCtx, listOf(propCtx), v27)
        assertTrue(v27.isEmpty())

        val assertNotTypeVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveType("Int", "Boolean").getShouldAssertion()!!
        val v28 = mutableListOf<String>()
        assertNotTypeVararg(propCtx, listOf(propCtx), v28)
        assertTrue(v28.isEmpty())
    }

    @Test
    fun `test PropertiesShould annotations, satisfies and composites`() {
        val annot =
            AnnotationDeclaration(
                "MyAnnotation",
                "com.example.MyAnnotation",
                listOf(AnnotationArgumentDeclaration("arg", "val")),
            )
        val propCtxAnnot = createPropCtx(annotations = listOf(annot))
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertAnnotSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnnotationOf("MyAnnotation").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAnnotSingle(propCtxAnnot, listOf(propCtxAnnot), v1)
        assertTrue(v1.isEmpty())

        val assertAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnnotationOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertAnnotList(propCtxAnnot, listOf(propCtxAnnot), v2)
        assertTrue(v2.isEmpty())

        val assertAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnnotationOf("MyAnnotation", "Other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnnotVararg(propCtxAnnot, listOf(propCtxAnnot), v3)
        assertTrue(v3.isEmpty())

        val assertAllAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf(listOf("MyAnnotation")).getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertAllAnnotList(propCtxAnnot, listOf(propCtxAnnot), v4)
        assertTrue(v4.isEmpty())

        val assertAllAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("MyAnnotation").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAllAnnotVararg(propCtxAnnot, listOf(propCtxAnnot), v5)
        assertTrue(v5.isEmpty())

        val assertAnyAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyAnnotationOf(listOf("MyAnnotation", "Other")).getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertAnyAnnotList(propCtxAnnot, listOf(propCtxAnnot), v6)
        assertTrue(v6.isEmpty())

        val assertAnyAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyAnnotationOf("MyAnnotation", "Other").getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertAnyAnnotVararg(propCtxAnnot, listOf(propCtxAnnot), v7)
        assertTrue(v7.isEmpty())

        val assertNotAnnotSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("Missing").getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertNotAnnotSingle(propCtxAnnot, listOf(propCtxAnnot), v8)
        assertTrue(v8.isEmpty())

        val assertNotAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf(listOf("Missing")).getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertNotAnnotList(propCtxAnnot, listOf(propCtxAnnot), v9)
        assertTrue(v9.isEmpty())

        val assertNotAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("Missing", "Bad").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertNotAnnotVararg(propCtxAnnot, listOf(propCtxAnnot), v10)
        assertTrue(v10.isEmpty())

        val assertAnnotArg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnnotationWithArgument("MyAnnotation", "arg", "val").getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertAnnotArg(propCtxAnnot, listOf(propCtxAnnot), v11)
        assertTrue(v11.isEmpty())

        val assertSatisfyPred =
            PropertiesRuleBuilder(
                graph,
            ).should().satisfy { it.declaration.name == "myProp" }.getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertSatisfyPred(propCtxAnnot, listOf(propCtxAnnot), v12)
        assertTrue(v12.isEmpty())

        val assertSatisfyDesc =
            PropertiesRuleBuilder(
                graph,
            ).should().satisfy("desc") { it.declaration.name == "myProp" }.getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertSatisfyDesc(propCtxAnnot, listOf(propCtxAnnot), v13)
        assertTrue(v13.isEmpty())

        val vAnyOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().anyOf(
            { resideInAPackage("com.example") },
            { haveName("other") },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnyOf)
        assertTrue(vAnyOf.isEmpty())

        val vAllOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().allOf(
            { resideInAPackage("com.example") },
            { haveName("myProp") },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAllOf)
        assertTrue(vAllOf.isEmpty())

        val vNoneOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().noneOf(
            { resideInAPackage("com.other") },
            { haveName("other") },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vNoneOf)
        assertTrue(vNoneOf.isEmpty())
    }
}

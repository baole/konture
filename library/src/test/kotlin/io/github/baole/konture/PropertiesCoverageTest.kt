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
        kdocText: String? = null,
        filePath: String = "/src/ClassA.kt",
        modulePath: String = ":app",
    ): PropertyDeclarationContext {
        val propDecl =
            PropertyDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                type = type,
                isVal = isVal,
                annotations = annotations,
                kdocText = kdocText,
                isExtension = isExtension,
            )
        return PropertyDeclarationContext(
            declaration = propDecl,
            packageName = packageName,
            className = className,
            modulePath = modulePath,
            filePath = filePath,
            sourceSet = null,
        )
    }

    @Test
    fun `test PropertiesShould package, module, name, and visibility assertions`() {
        val propCtx = createPropCtx(name = "myProp", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPkgSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAPackage("com.example")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vPkgSingle)
        assertTrue(vPkgSingle.isEmpty())

        val vPkgList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAPackage(listOf("com.example"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vPkgList)
        assertTrue(vPkgList.isEmpty())

        val vPkgVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAPackage("com.example", "com.other")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vPkgVararg)
        assertTrue(vPkgVararg.isEmpty())

        val vPkgPred = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAPackage { it.startsWith("com") }
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vPkgPred)
        assertTrue(vPkgPred.isEmpty())

        val vEndSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameEndingWith("Prop")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vEndSingle)
        assertTrue(vEndSingle.isEmpty())

        val vEndList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameEndingWith(listOf("Prop"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vEndList)
        assertTrue(vEndList.isEmpty())

        val vEndVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameEndingWith("Prop", "Field")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vEndVararg)
        assertTrue(vEndVararg.isEmpty())

        val vStartSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameStartingWith("my")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vStartSingle)
        assertTrue(vStartSingle.isEmpty())

        val vStartList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameStartingWith(listOf("my"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vStartList)
        assertTrue(vStartList.isEmpty())

        val vStartVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameStartingWith("my", "the")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vStartVararg)
        assertTrue(vStartVararg.isEmpty())

        val vMatchSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameMatching("my*")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vMatchSingle)
        assertTrue(vMatchSingle.isEmpty())

        val vMatchList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameMatching(listOf("my*"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vMatchList)
        assertTrue(vMatchList.isEmpty())

        val vMatchVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveNameMatching("my*", "the*")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vMatchVararg)
        assertTrue(vMatchVararg.isEmpty())

        // Module residency
        val vModSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule("app")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModSingle)
        assertTrue(vModSingle.isEmpty())

        val vModList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule(listOf("app"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModList)
        assertTrue(vModList.isEmpty())

        val vModVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule("app", "core")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModVararg)
        assertTrue(vModVararg.isEmpty())

        val vModAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInModule("app")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModAlias)
        assertTrue(vModAlias.isEmpty())

        val vModsAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInModules(listOf("app"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModsAlias)
        assertTrue(vModsAlias.isEmpty())

        val vModsVarargAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInModules("app", "core")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vModsVarargAlias)
        assertTrue(vModsVarargAlias.isEmpty())

        val vNotModSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule("core")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModSingle)
        assertTrue(vNotModSingle.isEmpty())

        val vNotModList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule(listOf("core"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModList)
        assertTrue(vNotModList.isEmpty())

        val vNotModVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule("core", "feature")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModVararg)
        assertTrue(vNotModVararg.isEmpty())

        val vNotModAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInModule("core")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModAlias)
        assertTrue(vNotModAlias.isEmpty())

        val vNotModsAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInModules(listOf("core"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModsAlias)
        assertTrue(vNotModsAlias.isEmpty())

        val vNotModsVarargAlias = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInModules("core", "feature")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotModsVarargAlias)
        assertTrue(vNotModsVarargAlias.isEmpty())

        // Name assertions
        val vNameSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName("myProp")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNameSingle)
        assertTrue(vNameSingle.isEmpty())

        val vNameList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName(listOf("myProp"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNameList)
        assertTrue(vNameList.isEmpty())

        val vNameVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName("myProp", "otherProp")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNameVararg)
        assertTrue(vNameVararg.isEmpty())

        val vNamePred = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName { it == "myProp" }
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNamePred)
        assertTrue(vNamePred.isEmpty())

        val vNotNameSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveName("wrong")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotNameSingle)
        assertTrue(vNotNameSingle.isEmpty())

        val vNotNameList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveName(listOf("wrong"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotNameList)
        assertTrue(vNotNameList.isEmpty())

        val vNotNameVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveName("wrong", "bad")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotNameVararg)
        assertTrue(vNotNameVararg.isEmpty())

        val vNotMatchSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameMatching("wrong*")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotMatchSingle)
        assertTrue(vNotMatchSingle.isEmpty())

        val vNotMatchList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameMatching(listOf("wrong*"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotMatchList)
        assertTrue(vNotMatchList.isEmpty())

        val vNotMatchVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameMatching("wrong*", "bad*")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotMatchVararg)
        assertTrue(vNotMatchVararg.isEmpty())

        val vNotStartSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameStartingWith("wrong")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotStartSingle)
        assertTrue(vNotStartSingle.isEmpty())

        val vNotStartList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("wrong"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotStartList)
        assertTrue(vNotStartList.isEmpty())

        val vNotStartVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameStartingWith("wrong", "bad")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotStartVararg)
        assertTrue(vNotStartVararg.isEmpty())

        val vNotEndSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameEndingWith("wrong")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotEndSingle)
        assertTrue(vNotEndSingle.isEmpty())

        val vNotEndList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameEndingWith(listOf("wrong"))
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotEndList)
        assertTrue(vNotEndList.isEmpty())

        val vNotEndVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notHaveNameEndingWith("wrong", "bad")
            .getShouldAssertion()!!(propCtx, listOf(propCtx), vNotEndVararg)
        assertTrue(vNotEndVararg.isEmpty())
    }

    @Test
    fun `test PropertiesShould visibilities, modifiers and types`() {
        val propCtxPublicVal =
            createPropCtx(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN, Modifier.CONST, Modifier.LATEINIT),
                type = "String",
                isVal = false,
                kdocText = "/** doc */",
                isExtension = true,
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPub = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().bePublic()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vPub)
        assertTrue(vPub.isEmpty())

        val vVar = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beVar()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vVar)
        assertTrue(vVar.isEmpty())

        val vOpen = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beOpen()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vOpen)
        assertTrue(vOpen.isEmpty())

        val vConst = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beConst()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vConst)
        assertTrue(vConst.isEmpty())

        val vLateinit = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beLateinit()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vLateinit)
        assertTrue(vLateinit.isEmpty())

        val vExt = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beExtension()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vExt)
        assertTrue(vExt.isEmpty())

        val vDoc = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beDocumentedWithKDoc()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vDoc)
        assertTrue(vDoc.isEmpty())

        // Non-matching
        val vNotExt = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notBeExtension()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vNotExt)
        assertEquals(1, vNotExt.size)

        val vNotConst = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notBeConst()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vNotConst)
        assertEquals(1, vNotConst.size)

        val vNotLateinit = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notBeLateinit()
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vNotLateinit)
        assertEquals(1, vNotLateinit.size)

        // Internal, Private, Protected, Abstract, Override, Member, TopLevel, Val
        val propCtxInternalVal =
            createPropCtx(
                name = "myProp",
                className = null,
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.ABSTRACT, Modifier.OVERRIDE),
                isVal = true,
            )

        val vInternal = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beInternal()
            .getShouldAssertion()!!(propCtxInternalVal, listOf(propCtxInternalVal), vInternal)
        assertTrue(vInternal.isEmpty())

        val vVal = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beVal()
            .getShouldAssertion()!!(propCtxInternalVal, listOf(propCtxInternalVal), vVal)
        assertTrue(vVal.isEmpty())

        val vAbstract = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beAbstract()
            .getShouldAssertion()!!(propCtxInternalVal, listOf(propCtxInternalVal), vAbstract)
        assertTrue(vAbstract.isEmpty())

        val vOverride = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beOverride()
            .getShouldAssertion()!!(propCtxInternalVal, listOf(propCtxInternalVal), vOverride)
        assertTrue(vOverride.isEmpty())

        val vTopLevel = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beTopLevel()
            .getShouldAssertion()!!(propCtxInternalVal, listOf(propCtxInternalVal), vTopLevel)
        assertTrue(vTopLevel.isEmpty())

        val propCtxPrivate = createPropCtx(visibility = Visibility.PRIVATE)
        val vPrivate = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().bePrivate()
            .getShouldAssertion()!!(propCtxPrivate, listOf(propCtxPrivate), vPrivate)
        assertTrue(vPrivate.isEmpty())

        val propCtxProtected = createPropCtx(visibility = Visibility.PROTECTED)
        val vProtected = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beProtected()
            .getShouldAssertion()!!(propCtxProtected, listOf(propCtxProtected), vProtected)
        assertTrue(vProtected.isEmpty())

        val vMember = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beMember()
            .getShouldAssertion()!!(propCtxPrivate, listOf(propCtxPrivate), vMember)
        assertTrue(vMember.isEmpty())

        // Types and modifiers
        val vTypeSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveType("String")
            .getShouldAssertion()!!(propCtxPrivate, listOf(propCtxPrivate), vTypeSingle)
        assertTrue(vTypeSingle.isEmpty())

        val vTypeList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveType(listOf("String"))
            .getShouldAssertion()!!(propCtxPrivate, listOf(propCtxPrivate), vTypeList)
        assertTrue(vTypeList.isEmpty())

        val vTypeVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveType("String", "Int")
            .getShouldAssertion()!!(propCtxPrivate, listOf(propCtxPrivate), vTypeVararg)
        assertTrue(vTypeVararg.isEmpty())

        val vModifier = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveModifier(Modifier.OPEN)
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vModifier)
        assertTrue(vModifier.isEmpty())

        val vAllModifiersList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAllModifiers(listOf(Modifier.OPEN, Modifier.CONST))
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vAllModifiersList)
        assertTrue(vAllModifiersList.isEmpty())

        val vAllModifiersVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAllModifiers(Modifier.OPEN, Modifier.CONST)
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vAllModifiersVararg)
        assertTrue(vAllModifiersVararg.isEmpty())

        val vAnyModifierList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyModifier(listOf(Modifier.OPEN, Modifier.ABSTRACT))
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vAnyModifierList)
        assertTrue(vAnyModifierList.isEmpty())

        val vAnyModifierVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyModifier(Modifier.OPEN, Modifier.ABSTRACT)
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vAnyModifierVararg)
        assertTrue(vAnyModifierVararg.isEmpty())

        val vVisSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveVisibility(Visibility.PUBLIC)
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vVisSingle)
        assertTrue(vVisSingle.isEmpty())

        val vVisList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL))
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vVisList)
        assertTrue(vVisList.isEmpty())

        val vVisVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL)
            .getShouldAssertion()!!(propCtxPublicVal, listOf(propCtxPublicVal), vVisVararg)
        assertTrue(vVisVararg.isEmpty())
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

        val vAnnotArg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("MyAnnotation", "arg", "val")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnnotArg)
        assertTrue(vAnnotArg.isEmpty())

        val vAnnotArgNoName = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("MyAnnotation", null, "val")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnnotArgNoName)
        assertTrue(vAnnotArgNoName.isEmpty())

        val vAnnotSingle = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnnotSingle)
        assertTrue(vAnnotSingle.isEmpty())

        val vAnnotList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnnotList)
        assertTrue(vAnnotList.isEmpty())

        val vAnnotVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnnotVararg)
        assertTrue(vAnnotVararg.isEmpty())

        val vAllAnnotList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAllAnnotationsOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAllAnnotList)
        assertTrue(vAllAnnotList.isEmpty())

        val vAllAnnotVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAllAnnotationsOf("MyAnnotation")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAllAnnotVararg)
        assertTrue(vAllAnnotVararg.isEmpty())

        val vAnyAnnotList = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyAnnotationOf(listOf("MyAnnotation", "Other"))
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnyAnnotList)
        assertTrue(vAnyAnnotList.isEmpty())

        val vAnyAnnotVararg = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnyAnnotationOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnyAnnotVararg)
        assertTrue(vAnyAnnotVararg.isEmpty())

        val vSatisfy1 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().satisfy { it.declaration.name == "myProp" }
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vSatisfy1)
        assertTrue(vSatisfy1.isEmpty())

        val vSatisfy2 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().satisfy {
                prop,
                v,
            ->
            if (prop.declaration.name != "myProp") v.add("error")
        }
            .getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vSatisfy2)
        assertTrue(vSatisfy2.isEmpty())

        val vAnyOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().anyOf(
            { haveName("myProp") },
            { haveName("other") },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAnyOf)
        assertTrue(vAnyOf.isEmpty())

        val vAllOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().allOf(
            { haveName("myProp") },
            { bePublic() },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vAllOf)
        assertTrue(vAllOf.isEmpty())

        val vNoneOf = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().noneOf(
            { haveName("other") },
        ).getShouldAssertion()!!(propCtxAnnot, listOf(propCtxAnnot), vNoneOf)
        assertTrue(vNoneOf.isEmpty())
    }

    @Test
    fun `test PropertiesThat filters`() {
        val annot =
            AnnotationDeclaration(
                "MyAnnotation",
                "com.example.MyAnnotation",
                listOf(AnnotationArgumentDeclaration("arg", "val")),
            )
        val propCtx =
            createPropCtx(
                name = "myProp",
                className = "ClassA",
                packageName = "com.example",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN, Modifier.CONST),
                type = "String",
                isVal = true,
                annotations = listOf(annot),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pPkgSingle = PropertiesRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(pPkgSingle(propCtx))

        val pPkgList = PropertiesRuleBuilder(graph).that().resideInAPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(pPkgList(propCtx))

        val pPkgVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example", "com.other").getThatPredicate()!!
        assertTrue(pPkgVararg(propCtx))

        val pPkgPred =
            PropertiesRuleBuilder(
                graph,
            ).that().resideInAPackage { it.startsWith("com") }.getThatPredicate()!!
        assertTrue(pPkgPred(propCtx))

        val pModSingle = PropertiesRuleBuilder(graph).that().resideInAModule("app").getThatPredicate()!!
        assertTrue(pModSingle(propCtx))

        val pModList = PropertiesRuleBuilder(graph).that().resideInAModule(listOf("app")).getThatPredicate()!!
        assertTrue(pModList(propCtx))

        val pModVararg = PropertiesRuleBuilder(graph).that().resideInAModule("app", "core").getThatPredicate()!!
        assertTrue(pModVararg(propCtx))

        val pModAlias = PropertiesRuleBuilder(graph).that().resideInModule("app").getThatPredicate()!!
        assertTrue(pModAlias(propCtx))

        val pModsAlias = PropertiesRuleBuilder(graph).that().resideInModules(listOf("app")).getThatPredicate()!!
        assertTrue(pModsAlias(propCtx))

        val pModsVarargAlias = PropertiesRuleBuilder(graph).that().resideInModules("app", "core").getThatPredicate()!!
        assertTrue(pModsVarargAlias(propCtx))

        val pNotModSingle = PropertiesRuleBuilder(graph).that().notResideInAModule("core").getThatPredicate()!!
        assertTrue(pNotModSingle(propCtx))

        val pNotModList = PropertiesRuleBuilder(graph).that().notResideInAModule(listOf("core")).getThatPredicate()!!
        assertTrue(pNotModList(propCtx))

        val pNotModVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAModule("core", "feature").getThatPredicate()!!
        assertTrue(pNotModVararg(propCtx))

        val pNotModAlias = PropertiesRuleBuilder(graph).that().notResideInModule("core").getThatPredicate()!!
        assertTrue(pNotModAlias(propCtx))

        val pNotModsAlias = PropertiesRuleBuilder(graph).that().notResideInModules(listOf("core")).getThatPredicate()!!
        assertTrue(pNotModsAlias(propCtx))

        val pNotModsVarargAlias =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInModules("core", "feature").getThatPredicate()!!
        assertTrue(pNotModsVarargAlias(propCtx))

        val pNameSingle = PropertiesRuleBuilder(graph).that().haveName("myProp").getThatPredicate()!!
        assertTrue(pNameSingle(propCtx))

        val pNameList = PropertiesRuleBuilder(graph).that().haveName(listOf("myProp")).getThatPredicate()!!
        assertTrue(pNameList(propCtx))

        val pNameVararg = PropertiesRuleBuilder(graph).that().haveName("myProp", "other").getThatPredicate()!!
        assertTrue(pNameVararg(propCtx))

        val pNotNameSingle = PropertiesRuleBuilder(graph).that().notHaveName("other").getThatPredicate()!!
        assertTrue(pNotNameSingle(propCtx))

        val pNotNameList = PropertiesRuleBuilder(graph).that().notHaveName(listOf("other")).getThatPredicate()!!
        assertTrue(pNotNameList(propCtx))

        val pNotNameVararg = PropertiesRuleBuilder(graph).that().notHaveName("other", "wrong").getThatPredicate()!!
        assertTrue(pNotNameVararg(propCtx))

        val pNotNamePred = PropertiesRuleBuilder(graph).that().notHaveName { it == "other" }.getThatPredicate()!!
        assertTrue(pNotNamePred(propCtx))

        val pNamePred = PropertiesRuleBuilder(graph).that().haveName { it == "myProp" }.getThatPredicate()!!
        assertTrue(pNamePred(propCtx))

        val pNameDescPred =
            PropertiesRuleBuilder(
                graph,
            ).that().haveName("desc", { it == "myProp" }).getThatPredicate()!!
        assertTrue(pNameDescPred(propCtx))

        val pEndSingle = PropertiesRuleBuilder(graph).that().haveNameEndingWith("Prop").getThatPredicate()!!
        assertTrue(pEndSingle(propCtx))

        val pEndList = PropertiesRuleBuilder(graph).that().haveNameEndingWith(listOf("Prop")).getThatPredicate()!!
        assertTrue(pEndList(propCtx))

        val pEndVararg = PropertiesRuleBuilder(graph).that().haveNameEndingWith("Prop", "Field").getThatPredicate()!!
        assertTrue(pEndVararg(propCtx))

        val pNotEndSingle = PropertiesRuleBuilder(graph).that().notHaveNameEndingWith("wrong").getThatPredicate()!!
        assertTrue(pNotEndSingle(propCtx))

        val pNotEndList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameEndingWith(listOf("wrong")).getThatPredicate()!!
        assertTrue(pNotEndList(propCtx))

        val pNotEndVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameEndingWith("wrong", "bad").getThatPredicate()!!
        assertTrue(pNotEndVararg(propCtx))

        val pStartSingle = PropertiesRuleBuilder(graph).that().haveNameStartingWith("my").getThatPredicate()!!
        assertTrue(pStartSingle(propCtx))

        val pStartList = PropertiesRuleBuilder(graph).that().haveNameStartingWith(listOf("my")).getThatPredicate()!!
        assertTrue(pStartList(propCtx))

        val pStartVararg = PropertiesRuleBuilder(graph).that().haveNameStartingWith("my", "the").getThatPredicate()!!
        assertTrue(pStartVararg(propCtx))

        val pNotStartSingle = PropertiesRuleBuilder(graph).that().notHaveNameStartingWith("wrong").getThatPredicate()!!
        assertTrue(pNotStartSingle(propCtx))

        val pNotStartList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith(listOf("wrong")).getThatPredicate()!!
        assertTrue(pNotStartList(propCtx))

        val pNotStartVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith("wrong", "bad").getThatPredicate()!!
        assertTrue(pNotStartVararg(propCtx))

        val pMatchSingle = PropertiesRuleBuilder(graph).that().haveNameMatching("my*").getThatPredicate()!!
        assertTrue(pMatchSingle(propCtx))

        val pMatchList = PropertiesRuleBuilder(graph).that().haveNameMatching(listOf("my*")).getThatPredicate()!!
        assertTrue(pMatchList(propCtx))

        val pMatchVararg = PropertiesRuleBuilder(graph).that().haveNameMatching("my*", "the*").getThatPredicate()!!
        assertTrue(pMatchVararg(propCtx))

        val pNotMatchSingle = PropertiesRuleBuilder(graph).that().notHaveNameMatching("wrong*").getThatPredicate()!!
        assertTrue(pNotMatchSingle(propCtx))

        val pNotMatchList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameMatching(listOf("wrong*")).getThatPredicate()!!
        assertTrue(pNotMatchList(propCtx))

        val pNotMatchVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameMatching("wrong*", "bad*").getThatPredicate()!!
        assertTrue(pNotMatchVararg(propCtx))

        val pNotPub = PropertiesRuleBuilder(graph).that().notBePublic().getThatPredicate()!!
        assertFalse(pNotPub(propCtx))

        val pNotInternal = PropertiesRuleBuilder(graph).that().notBeInternal().getThatPredicate()!!
        assertTrue(pNotInternal(propCtx))

        val pNotPriv = PropertiesRuleBuilder(graph).that().notBePrivate().getThatPredicate()!!
        assertTrue(pNotPriv(propCtx))

        val pNotProt = PropertiesRuleBuilder(graph).that().notBeProtected().getThatPredicate()!!
        assertTrue(pNotProt(propCtx))

        val pBeTopLevel = PropertiesRuleBuilder(graph).that().beTopLevel().getThatPredicate()!!
        assertFalse(pBeTopLevel(propCtx))

        val pBeMember = PropertiesRuleBuilder(graph).that().beMember().getThatPredicate()!!
        assertTrue(pBeMember(propCtx))

        val pAnnotSingle = PropertiesRuleBuilder(graph).that().haveAnnotationOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotSingle(propCtx))

        val pAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAnnotList(propCtx))

        val pAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationOf("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnnotVararg(propCtx))

        val pAllAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAllAnnotList(propCtx))

        val pAllAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAllAnnotVararg(propCtx))

        val pAnyAnnotList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(listOf("MyAnnotation", "Other")).getThatPredicate()!!
        assertTrue(pAnyAnnotList(propCtx))

        val pAnyAnnotVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnyAnnotVararg(propCtx))

        val pOpen = PropertiesRuleBuilder(graph).that().areOpen().getThatPredicate()!!
        assertTrue(pOpen(propCtx))

        val pModifier = PropertiesRuleBuilder(graph).that().haveModifier(Modifier.OPEN).getThatPredicate()!!
        assertTrue(pModifier(propCtx))

        val pAllModifiersList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAllModifiers(listOf(Modifier.OPEN, Modifier.CONST)).getThatPredicate()!!
        assertTrue(pAllModifiersList(propCtx))

        val pAllModifiersVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAllModifiers(Modifier.OPEN, Modifier.CONST).getThatPredicate()!!
        assertTrue(pAllModifiersVararg(propCtx))

        val pAnyModifierList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyModifier(listOf(Modifier.OPEN, Modifier.ABSTRACT)).getThatPredicate()!!
        assertTrue(pAnyModifierList(propCtx))

        val pAnyModifierVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyModifier(Modifier.OPEN, Modifier.ABSTRACT).getThatPredicate()!!
        assertTrue(pAnyModifierVararg(propCtx))

        val pVisSingle = PropertiesRuleBuilder(graph).that().haveVisibility(Visibility.PUBLIC).getThatPredicate()!!
        assertTrue(pVisSingle(propCtx))

        val pVisList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL)).getThatPredicate()!!
        assertTrue(pVisList(propCtx))

        val pVisVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getThatPredicate()!!
        assertTrue(pVisVararg(propCtx))

        val pTypeSingle = PropertiesRuleBuilder(graph).that().haveType("String").getThatPredicate()!!
        assertTrue(pTypeSingle(propCtx))

        val pTypeList = PropertiesRuleBuilder(graph).that().haveType(listOf("String")).getThatPredicate()!!
        assertTrue(pTypeList(propCtx))

        val pTypeVararg = PropertiesRuleBuilder(graph).that().haveType("String", "Int").getThatPredicate()!!
        assertTrue(pTypeVararg(propCtx))

        val pAreTopLevel = PropertiesRuleBuilder(graph).that().areTopLevel().getThatPredicate()!!
        assertFalse(pAreTopLevel(propCtx))

        val pAreMember = PropertiesRuleBuilder(graph).that().areMember().getThatPredicate()!!
        assertTrue(pAreMember(propCtx))

        val pAnnotArg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument("MyAnnotation", "arg", "val").getThatPredicate()!!
        assertTrue(pAnnotArg(propCtx))

        val pSatisfy =
            PropertiesRuleBuilder(
                graph,
            ).that().satisfy { it.declaration.name == "myProp" }.getThatPredicate()!!
        assertTrue(pSatisfy(propCtx))

        val pBeVal = PropertiesRuleBuilder(graph).that().beVal().getThatPredicate()!!
        assertTrue(pBeVal(propCtx))

        val pBeVar = PropertiesRuleBuilder(graph).that().beVar().getThatPredicate()!!
        assertFalse(pBeVar(propCtx))

        val pBeConst = PropertiesRuleBuilder(graph).that().beConst().getThatPredicate()!!
        assertTrue(pBeConst(propCtx))

        val pAnyOf =
            PropertiesRuleBuilder(graph).that().anyOf(
                { haveName("myProp") },
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(propCtx))

        val pAllOf =
            PropertiesRuleBuilder(graph).that().allOf(
                { haveName("myProp") },
                { resideInAPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(propCtx))

        val pNoneOf =
            PropertiesRuleBuilder(graph).that().noneOf(
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(propCtx))

        val pNotPkgSingle = PropertiesRuleBuilder(graph).that().notResideInAPackage("com.other").getThatPredicate()!!
        assertTrue(pNotPkgSingle(propCtx))

        val pNotPkgList =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAPackage(listOf("com.other")).getThatPredicate()!!
        assertTrue(pNotPkgList(propCtx))

        val pNotPkgVararg =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAPackage("com.other", "org.wrong").getThatPredicate()!!
        assertTrue(pNotPkgVararg(propCtx))

        val pNotAnnot = PropertiesRuleBuilder(graph).that().notHaveAnnotationOf("Missing").getThatPredicate()!!
        assertTrue(pNotAnnot(propCtx))
    }

    @Test
    fun `test PropertiesShould failure messages`() {
        val propCtx = createPropCtx(name = "myProp", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val v1 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg").getShouldAssertion()!!(propCtx, listOf(propCtx), v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAPackage(listOf("wrong.pkg")).getShouldAssertion()!!(propCtx, listOf(propCtx), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg", "other.pkg").getShouldAssertion()!!(propCtx, listOf(propCtx), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAPackage { false }.getShouldAssertion()!!(propCtx, listOf(propCtx), v4)
        assertEquals(1, v4.size)

        val v8 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAModule("otherMod").getShouldAssertion()!!(propCtx, listOf(propCtx), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().resideInAModule(listOf("otherMod")).getShouldAssertion()!!(propCtx, listOf(propCtx), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notResideInAModule("app").getShouldAssertion()!!(propCtx, listOf(propCtx), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notResideInAModule(listOf("app")).getShouldAssertion()!!(propCtx, listOf(propCtx), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName("wrong").getShouldAssertion()!!(propCtx, listOf(propCtx), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveName(listOf("wrong")).getShouldAssertion()!!(propCtx, listOf(propCtx), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveName("wrong", "bad").getShouldAssertion()!!(propCtx, listOf(propCtx), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveName { false }.getShouldAssertion()!!(propCtx, listOf(propCtx), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveName("myProp").getShouldAssertion()!!(propCtx, listOf(propCtx), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveName(listOf("myProp")).getShouldAssertion()!!(propCtx, listOf(propCtx), v17)
        assertEquals(1, v17.size)

        val v18 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Wrong").getShouldAssertion()!!(propCtx, listOf(propCtx), v18)
        assertEquals(1, v18.size)

        val v19 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!(propCtx, listOf(propCtx), v19)
        assertEquals(1, v19.size)

        val v20 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith("Prop").getShouldAssertion()!!(propCtx, listOf(propCtx), v20)
        assertEquals(1, v20.size)

        val v21 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith(listOf("Prop")).getShouldAssertion()!!(propCtx, listOf(propCtx), v21)
        assertEquals(1, v21.size)

        val v22 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Wrong").getShouldAssertion()!!(propCtx, listOf(propCtx), v22)
        assertEquals(1, v22.size)

        val v23 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!(propCtx, listOf(propCtx), v23)
        assertEquals(1, v23.size)

        val v24 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith("my").getShouldAssertion()!!(propCtx, listOf(propCtx), v24)
        assertEquals(1, v24.size)

        val v25 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith(listOf("my")).getShouldAssertion()!!(propCtx, listOf(propCtx), v25)
        assertEquals(1, v25.size)

        val v26 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameMatching("wrong*").getShouldAssertion()!!(propCtx, listOf(propCtx), v26)
        assertEquals(1, v26.size)

        val v27 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNameMatching(listOf("wrong*")).getShouldAssertion()!!(propCtx, listOf(propCtx), v27)
        assertEquals(1, v27.size)

        val v28 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameMatching("my*").getShouldAssertion()!!(propCtx, listOf(propCtx), v28)
        assertEquals(1, v28.size)

        val v29 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveNameMatching(listOf("my*")).getShouldAssertion()!!(propCtx, listOf(propCtx), v29)
        assertEquals(1, v29.size)

        val v30 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().bePublic().getShouldAssertion()!!(
            createPropCtx(visibility = Visibility.PRIVATE),
            listOf(propCtx),
            v30,
        )
        assertEquals(1, v30.size)

        val v31 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(propCtx, listOf(propCtx), v31)
        assertEquals(1, v31.size)

        val v32 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(propCtx, listOf(propCtx), v32)
        assertEquals(1, v32.size)

        val v33 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(propCtx, listOf(propCtx), v33)
        assertEquals(1, v33.size)

        val v34 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().beVal().getShouldAssertion()!!(createPropCtx(isVal = false), listOf(propCtx), v34)
        assertEquals(1, v34.size)

        val v35 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().beVar().getShouldAssertion()!!(createPropCtx(isVal = true), listOf(propCtx), v35)
        assertEquals(1, v35.size)

        val v36 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(propCtx, listOf(propCtx), v36)
        assertEquals(1, v36.size)

        val v37 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beAbstract().getShouldAssertion()!!(propCtx, listOf(propCtx), v37)
        assertEquals(1, v37.size)

        val v38 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beOverride().getShouldAssertion()!!(propCtx, listOf(propCtx), v38)
        assertEquals(1, v38.size)

        val v39 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().beTopLevel().getShouldAssertion()!!(propCtx, listOf(propCtx), v39)
        assertEquals(1, v39.size)

        val v40 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().beMember().getShouldAssertion()!!(createPropCtx(className = null), listOf(propCtx), v40)
        assertEquals(1, v40.size)

        val v41 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveType("Int").getShouldAssertion()!!(propCtx, listOf(propCtx), v41)
        assertEquals(1, v41.size)

        val v42 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveType(listOf("Int")).getShouldAssertion()!!(propCtx, listOf(propCtx), v42)
        assertEquals(1, v42.size)

        val v43 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveAnnotationOf("Missing").getShouldAssertion()!!(propCtx, listOf(propCtx), v43)
        assertEquals(1, v43.size)

        val v44 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveAnnotationOf(listOf("Missing")).getShouldAssertion()!!(propCtx, listOf(propCtx), v44)
        assertEquals(1, v44.size)

        val v45 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf(listOf("Missing")).getShouldAssertion()!!(propCtx, listOf(propCtx), v45)
        assertEquals(1, v45.size)

        val v46 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf(listOf("Missing")).getShouldAssertion()!!(propCtx, listOf(propCtx), v46)
        assertEquals(1, v46.size)

        val v47 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveAnnotationWithArgument(
            "Missing",
            "arg",
            "val",
        ).getShouldAssertion()!!(propCtx, listOf(propCtx), v47)
        assertEquals(1, v47.size)
    }
}

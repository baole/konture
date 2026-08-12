/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertiesCoverageThatTest : KontureScopeTestFixture() {
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
        val file =
            FileDeclaration(
                "${className ?: "TopLevel"}.kt",
                packageName,
                classes = cls?.let { listOf(it) } ?: emptyList(),
            )
        return PropertyDeclarationContext(decl, packageName, className, modulePath, file.filePath)
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
    fun `test PropertiesThat module and modifier filters`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val valProp =
            createPropCtx(
                name = "myVal",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.ABSTRACT),
                type = "String",
                isVal = true,
                isExtension = true,
                modulePath = ":app",
            )
        val varProp =
            createPropCtx(
                name = "myVar",
                visibility = Visibility.PRIVATE,
                type = "Int",
                isVal = false,
                isExtension = false,
                modulePath = ":lib",
            )

        var b = PropertiesRuleBuilder(graph)
        b.that().notResideInAModule(":app")
        assertFalse(b.getThatPredicate()!!(valProp))
        assertTrue(b.getThatPredicate()!!(varProp))

        b = PropertiesRuleBuilder(graph)
        b.that().notResideInAModule(listOf(":app"))
        assertFalse(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().areAbstract()
        assertTrue(b.getThatPredicate()!!(valProp))
        assertFalse(b.getThatPredicate()!!(varProp))

        b = PropertiesRuleBuilder(graph)
        b.that().haveTypeOf<String>()
        assertFalse(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().areExtension()
        assertTrue(b.getThatPredicate()!!(valProp))
        assertFalse(b.getThatPredicate()!!(varProp))

        b = PropertiesRuleBuilder(graph)
        b.that().beLateinit()
        assertFalse(b.getThatPredicate()!!(valProp))
    }

    @Test
    fun `test PropertiesThat logical combinators`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val valProp = createPropCtx(isVal = true)

        var b = PropertiesRuleBuilder(graph)
        b.that().anyOf({ beVal() }, { beVar() })
        assertTrue(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().allOf({ beVal() }, { beTopLevel() })
        assertFalse(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().noneOf({ beVar() })
        assertTrue(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().haveImportOf(listOf("com.example.Type"))
        assertFalse(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().haveImportOf("com.example.Type", "com.other.Type")
        assertFalse(b.getThatPredicate()!!(valProp))

        b = PropertiesRuleBuilder(graph)
        b.that().not()
        assertTrue(b.getThatPredicate() == null)
    }
}

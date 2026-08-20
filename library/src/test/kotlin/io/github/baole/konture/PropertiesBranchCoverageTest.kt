/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertiesBranchCoverageTest : KontureScopeTestFixture() {
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
        receiverType: String? = null,
        resolvedType: String? = null,
        modulePath: String = ":app",
        usages: List<SourceUsage> = emptyList(),
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
                receiverType = receiverType,
                resolvedType = resolvedType,
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
        return PropertyDeclarationContext(decl, packageName, className, modulePath, file.filePath, null, usages)
    }

    @Test
    fun `test PropertiesThat module and name filters with all branch variations`() {
        val graph = ProjectGraph(emptyMap())
        val matchingCtx =
            createPropCtx(name = "serviceProp", modulePath = ":app:feature", packageName = "com.example.service")
        val otherCtx = createPropCtx(name = "userProp", modulePath = ":core:lib", packageName = "org.sample")

        // Module paths with different formats: without ':', with ':', with '**', empty
        val pModNoColon = PropertiesRuleBuilder(graph).that().resideInAModule("app:feature").getThatPredicate()!!
        assertTrue(pModNoColon(matchingCtx))
        assertFalse(pModNoColon(otherCtx))

        val pModGlob = PropertiesRuleBuilder(graph).that().resideInAModule("**:feature").getThatPredicate()!!
        assertFalse(pModGlob(matchingCtx))
        assertFalse(pModGlob(otherCtx))

        val pModEmpty = PropertiesRuleBuilder(graph).that().resideInAModule("").getThatPredicate()!!
        assertFalse(pModEmpty(matchingCtx))

        val pModList =
            PropertiesRuleBuilder(
                graph,
            ).that().resideInAModule(listOf("app:feature", "core:lib")).getThatPredicate()!!
        assertTrue(pModList(matchingCtx))
        assertTrue(pModList(otherCtx))

        val pModListNoColon =
            PropertiesRuleBuilder(
                graph,
            ).that().resideInAModule(listOf("app:feature")).getThatPredicate()!!
        assertTrue(pModListNoColon(matchingCtx))
        assertFalse(pModListNoColon(otherCtx))

        val pNotModSingle = PropertiesRuleBuilder(graph).that().notResideInAModule("app:feature").getThatPredicate()!!
        assertFalse(pNotModSingle(matchingCtx))
        assertTrue(pNotModSingle(otherCtx))

        val pNotModSingleNoColon =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAModule("app:feature").getThatPredicate()!!
        assertFalse(pNotModSingleNoColon(matchingCtx))
        assertTrue(pNotModSingleNoColon(otherCtx))

        val pNotModSingleGlob = PropertiesRuleBuilder(graph).that().notResideInAModule("**:lib").getThatPredicate()!!
        assertTrue(pNotModSingleGlob(matchingCtx))
        assertFalse(pNotModSingleGlob(otherCtx))

        val pNotModList =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAModule(listOf("app:feature", "**:lib")).getThatPredicate()!!
        assertFalse(pNotModList(matchingCtx))
        assertFalse(pNotModList(otherCtx))

        val pNotModListNoColon =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAModule(listOf("app:feature")).getThatPredicate()!!
        assertFalse(pNotModListNoColon(matchingCtx))
        assertTrue(pNotModListNoColon(otherCtx))

        val pNotModListGlob =
            PropertiesRuleBuilder(
                graph,
            ).that().notResideInAModule(listOf("**:lib")).getThatPredicate()!!
        assertTrue(pNotModListGlob(matchingCtx))
        assertFalse(pNotModListGlob(otherCtx))

        // Name filters - negative and matching
        val pNotHaveName = PropertiesRuleBuilder(graph).that().notHaveName("serviceProp").getThatPredicate()!!
        assertFalse(pNotHaveName(matchingCtx))
        assertTrue(pNotHaveName(otherCtx))

        val pNotHaveNameList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveName(listOf("serviceProp", "other")).getThatPredicate()!!
        assertFalse(pNotHaveNameList(matchingCtx))
        assertTrue(pNotHaveNameList(otherCtx))

        val pNotHaveNamePred =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveName { it.startsWith("service") }.getThatPredicate()!!
        assertFalse(pNotHaveNamePred(matchingCtx))
        assertTrue(pNotHaveNamePred(otherCtx))

        val pHaveNameStarting = PropertiesRuleBuilder(graph).that().haveNameStartingWith("service").getThatPredicate()!!
        assertTrue(pHaveNameStarting(matchingCtx))
        assertFalse(pHaveNameStarting(otherCtx))

        val pHaveNameStartingList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveNameStartingWith(listOf("service", "other")).getThatPredicate()!!
        assertTrue(pHaveNameStartingList(matchingCtx))
        assertFalse(pHaveNameStartingList(otherCtx))

        val pNotHaveNameStarting =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith("service").getThatPredicate()!!
        assertFalse(pNotHaveNameStarting(matchingCtx))
        assertTrue(pNotHaveNameStarting(otherCtx))

        val pNotHaveNameStartingList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith(listOf("service", "other")).getThatPredicate()!!
        assertFalse(pNotHaveNameStartingList(matchingCtx))
        assertTrue(pNotHaveNameStartingList(otherCtx))

        val pHaveNameEnding = PropertiesRuleBuilder(graph).that().haveNameEndingWith("Prop").getThatPredicate()!!
        assertTrue(pHaveNameEnding(matchingCtx))
        assertTrue(pHaveNameEnding(otherCtx))

        val pHaveNameEndingList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveNameEndingWith(listOf("serviceProp", "other")).getThatPredicate()!!
        assertTrue(pHaveNameEndingList(matchingCtx))
        assertFalse(pHaveNameEndingList(otherCtx))

        val pNotHaveNameEnding =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameEndingWith("serviceProp").getThatPredicate()!!
        assertFalse(pNotHaveNameEnding(matchingCtx))
        assertTrue(pNotHaveNameEnding(otherCtx))

        val pNotHaveNameEndingList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameEndingWith(listOf("serviceProp", "foo")).getThatPredicate()!!
        assertFalse(pNotHaveNameEndingList(matchingCtx))
        assertTrue(pNotHaveNameEndingList(otherCtx))

        val pHaveNameMatching = PropertiesRuleBuilder(graph).that().haveNameMatching("service*").getThatPredicate()!!
        assertTrue(pHaveNameMatching(matchingCtx))
        assertFalse(pHaveNameMatching(otherCtx))

        val pHaveNameMatchingList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveNameMatching(listOf("service*", "foo*")).getThatPredicate()!!
        assertTrue(pHaveNameMatchingList(matchingCtx))
        assertFalse(pHaveNameMatchingList(otherCtx))

        val pNotHaveNameMatching =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameMatching("service*").getThatPredicate()!!
        assertFalse(pNotHaveNameMatching(matchingCtx))
        assertTrue(pNotHaveNameMatching(otherCtx))

        val pNotHaveNameMatchingList =
            PropertiesRuleBuilder(
                graph,
            ).that().notHaveNameMatching(listOf("service*", "foo*")).getThatPredicate()!!
        assertFalse(pNotHaveNameMatchingList(matchingCtx))
        assertTrue(pNotHaveNameMatchingList(otherCtx))
    }

    @Test
    fun `test PropertiesThat modifier, type, and annotation filters with both matching and non-matching`() {
        val graph = ProjectGraph(emptyMap())
        val annotWithArg =
            AnnotationDeclaration(
                name = "Table",
                fqName = "androidx.room.Table",
                arguments = listOf(AnnotationArgumentDeclaration("name", "\"users\"")),
            )
        val annotNoArg =
            AnnotationDeclaration(
                name = "PrimaryKey",
                fqName = "androidx.room.PrimaryKey",
                arguments = emptyList(),
            )

        val ctx =
            createPropCtx(
                name = "id",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN, Modifier.CONST),
                type = "kotlin.String",
                resolvedType = "kotlin.String",
                isVal = true,
                annotations = listOf(annotWithArg, annotNoArg),
                isExtension = true,
                receiverType = "com.example.User",
            )

        val otherCtx =
            createPropCtx(
                name = "count",
                packageName = "org.sample.sub",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.OVERRIDE, Modifier.LATEINIT),
                type = "Int",
                isVal = false,
                annotations = emptyList(),
                isExtension = false,
                className = null,
            )

        // Type matching
        val pHaveType = PropertiesRuleBuilder(graph).that().haveType("kotlin.String").getThatPredicate()!!
        assertTrue(pHaveType(ctx))
        assertFalse(pHaveType(otherCtx))

        val pHaveTypeKClass = PropertiesRuleBuilder(graph).that().haveType(String::class).getThatPredicate()!!
        assertTrue(pHaveTypeKClass(ctx))
        assertFalse(pHaveTypeKClass(otherCtx))

        val pHaveTypeList =
            PropertiesRuleBuilder(
                graph,
            ).that().haveType(listOf("kotlin.String", "Int")).getThatPredicate()!!
        assertTrue(pHaveTypeList(ctx))
        assertTrue(pHaveTypeList(otherCtx))

        // Extension & Top level vs Member
        val pAreExt = PropertiesRuleBuilder(graph).that().areExtension().getThatPredicate()!!
        assertTrue(pAreExt(ctx))
        assertFalse(pAreExt(otherCtx))

        val pAreTopLevel = PropertiesRuleBuilder(graph).that().areTopLevel().getThatPredicate()!!
        assertFalse(pAreTopLevel(ctx))
        assertTrue(pAreTopLevel(otherCtx))

        val pBeTopLevel = PropertiesRuleBuilder(graph).that().beTopLevel().getThatPredicate()!!
        assertFalse(pBeTopLevel(ctx))
        assertTrue(pBeTopLevel(otherCtx))

        val pAreMember = PropertiesRuleBuilder(graph).that().areMember().getThatPredicate()!!
        assertTrue(pAreMember(ctx))
        assertFalse(pAreMember(otherCtx))

        val pBeMember = PropertiesRuleBuilder(graph).that().beMember().getThatPredicate()!!
        assertTrue(pBeMember(ctx))
        assertFalse(pBeMember(otherCtx))

        // Modifiers and Val/Var
        val pBeVal = PropertiesRuleBuilder(graph).that().beVal().getThatPredicate()!!
        assertTrue(pBeVal(ctx))
        assertFalse(pBeVal(otherCtx))

        val pBeVar = PropertiesRuleBuilder(graph).that().beVar().getThatPredicate()!!
        assertFalse(pBeVar(ctx))
        assertTrue(pBeVar(otherCtx))

        val pBeConst = PropertiesRuleBuilder(graph).that().beConst().getThatPredicate()!!
        assertTrue(pBeConst(ctx))
        assertFalse(pBeConst(otherCtx))

        val pBeLateinit = PropertiesRuleBuilder(graph).that().beLateinit().getThatPredicate()!!
        assertFalse(pBeLateinit(ctx))
        assertTrue(pBeLateinit(otherCtx))

        val pNotBePublic = PropertiesRuleBuilder(graph).that().notBePublic().getThatPredicate()!!
        assertFalse(pNotBePublic(ctx))
        assertTrue(pNotBePublic(otherCtx))

        val pNotBePrivate = PropertiesRuleBuilder(graph).that().notBePrivate().getThatPredicate()!!
        assertTrue(pNotBePrivate(ctx))
        assertFalse(pNotBePrivate(otherCtx))

        // Annotations
        val pAnnotWithArg =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument("Table", "name", "users").getThatPredicate()!!
        assertTrue(pAnnotWithArg(ctx))
        assertFalse(pAnnotWithArg(otherCtx))

        val pAnnotWithArgMismatch =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument("Table", "name", "orders").getThatPredicate()!!
        assertFalse(pAnnotWithArgMismatch(ctx))

        val pAnnotWithArgMismatchName =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument("Table", "wrongArg", "users").getThatPredicate()!!
        assertFalse(pAnnotWithArgMismatchName(ctx))

        val pNotHaveAnnot = PropertiesRuleBuilder(graph).that().notHaveAnnotationOf("Table").getThatPredicate()!!
        assertFalse(pNotHaveAnnot(ctx))
        assertTrue(pNotHaveAnnot(otherCtx))

        val pNotResidePkg = PropertiesRuleBuilder(graph).that().notResideInAPackage("org.sample..").getThatPredicate()!!
        assertTrue(pNotResidePkg(ctx))
        assertFalse(pNotResidePkg(otherCtx))
    }

    @Test
    fun `test PropertyDeclarationExtensions collectDependencyPackages branches`() {
        // Without package/dots
        val simpleProp = createPropCtx(type = "Int", resolvedType = null, receiverType = null, usages = emptyList())
        val simplePkgs = simpleProp.collectDependencyPackages()
        assertTrue(simplePkgs.isEmpty())

        // With dot but no upper case (e.g. primitive or package-only segment)
        val fullProp =
            createPropCtx(
                type = "com.example.model.User?",
                resolvedType = "com.example.model.User",
                receiverType = "com.example.service.UserService",
                annotations =
                    listOf(
                        AnnotationDeclaration("Generated", "javax.annotation.processing.Generated"),
                        AnnotationDeclaration("Simple", "Simple"),
                    ),
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "com.example.repo.UserRepo.find",
                            filePath = "/src/ClassA.kt",
                            line = 10,
                            column = 5,
                            possibleTargetFqNames = listOf("com.example.repo.UserRepo", "simpleTarget"),
                        ),
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "simpleCall",
                            filePath = "/src/ClassA.kt",
                            line = 11,
                            column = 5,
                            possibleTargetFqNames = emptyList(),
                        ),
                    ),
            )
        val fullPkgs = fullProp.collectDependencyPackages()
        assertTrue(fullPkgs.contains("com.example.model"))
        assertTrue(fullPkgs.contains("com.example.service"))
        assertTrue(fullPkgs.contains("javax.annotation.processing"))
        assertTrue(fullPkgs.contains("com.example.repo"))
    }

    @Test
    fun `test PropertiesShouldTypeAssertions module and type assertions with all branch variations`() {
        val graph = ProjectGraph(emptyMap())
        val propCtx =
            createPropCtx(
                name = "id",
                modulePath = ":app:feature",
                type = "kotlin.String",
                resolvedType = "kotlin.String",
            )
        val allProps = listOf(propCtx)

        // resideInAModule branches (colon, no colon, glob, empty)
        val v1 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule("app:feature")
            .getShouldAssertion()!!(propCtx, allProps, v1)
        assertTrue(v1.isEmpty())

        val v2 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule("core:lib")
            .getShouldAssertion()!!(propCtx, allProps, v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule("**:feature")
            .getShouldAssertion()!!(propCtx, allProps, v3)
        assertTrue(v3.isEmpty())

        val v4 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule(listOf("app:feature", "core:lib"))
            .getShouldAssertion()!!(propCtx, allProps, v4)
        assertTrue(v4.isEmpty())

        val v5 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().resideInAModule(listOf("core:lib"))
            .getShouldAssertion()!!(propCtx, allProps, v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule("app:feature")
            .getShouldAssertion()!!(propCtx, allProps, v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule("core:lib")
            .getShouldAssertion()!!(propCtx, allProps, v7)
        assertTrue(v7.isEmpty())

        val v8 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule(listOf("app:feature"))
            .getShouldAssertion()!!(propCtx, allProps, v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notResideInAModule(listOf("core:lib"))
            .getShouldAssertion()!!(propCtx, allProps, v9)
        assertTrue(v9.isEmpty())

        // Annotation with argument branches in should
        val annot =
            AnnotationDeclaration(
                "Table",
                "androidx.room.Table",
                listOf(AnnotationArgumentDeclaration("name", "\"users\"")),
            )
        val annotProp = createPropCtx(annotations = listOf(annot))

        val v10 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("Table", "name", "users")
            .getShouldAssertion()!!(annotProp, listOf(annotProp), v10)
        assertTrue(v10.isEmpty())

        val v11 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("Table", "name", "orders")
            .getShouldAssertion()!!(annotProp, listOf(annotProp), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("Table", "nonexistent", "users")
            .getShouldAssertion()!!(annotProp, listOf(annotProp), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().haveAnnotationWithArgument("Entity", "name", "users")
            .getShouldAssertion()!!(annotProp, listOf(annotProp), v13)
        assertEquals(1, v13.size)
    }
}

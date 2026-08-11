/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertiesCoverageTest : KontureScopeTestFixture() {
    private fun createPropCtx(
        name: String = "myProp",
        className: String? = null,
        packageName: String = "com.example",
        modulePath: String = ":app",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        type: String = "String",
        isVal: Boolean = true,
        annotations: List<AnnotationDeclaration> = emptyList(),
    ): PropertyDeclarationContext {
        val decl = PropertyDeclaration(name, visibility, modifiers, type, isVal, annotations, null)
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
        return PropertyDeclarationContext(decl, packageName, className, modulePath, file.filePath)
    }

    @Test
    fun `test PropertiesShould package, module and name assertions`() {
        val propCtx = createPropCtx(name = "myProp", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertPkgSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage("com.example").getShouldAssertion()!!
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

        val assertNotPkgSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage("com.other").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotPkgSingle(propCtx, listOf(propCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotPkgList =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage(listOf("com.other")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotPkgList(propCtx, listOf(propCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotPkgVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAPackage("com.other", "org.wrong").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotPkgVararg(propCtx, listOf(propCtx), v6)
        assertTrue(v6.isEmpty())

        val assertModSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAModule(":app").getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertModSingle(propCtx, listOf(propCtx), v7)
        assertTrue(v7.isEmpty())

        val assertModList =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAModule(listOf(":app")).getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertModList(propCtx, listOf(propCtx), v8)
        assertTrue(v8.isEmpty())

        val assertModVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAModule(":app", ":core").getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertModVararg(propCtx, listOf(propCtx), v9)
        assertTrue(v9.isEmpty())

        val assertNotModSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule(":other").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertNotModSingle(propCtx, listOf(propCtx), v10)
        assertTrue(v10.isEmpty())

        val assertNotModList =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule(listOf(":other")).getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertNotModList(propCtx, listOf(propCtx), v11)
        assertTrue(v11.isEmpty())

        val assertNotModVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notResideInAModule(":other", ":wrong").getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertNotModVararg(propCtx, listOf(propCtx), v12)
        assertTrue(v12.isEmpty())

        val assertNameSingle = PropertiesRuleBuilder(graph).should().haveName("myProp").getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertNameSingle(propCtx, listOf(propCtx), v13)
        assertTrue(v13.isEmpty())

        val assertNameList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveName(listOf("myProp")).getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertNameList(propCtx, listOf(propCtx), v14)
        assertTrue(v14.isEmpty())

        val assertNameVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveName("myProp", "other").getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertNameVararg(propCtx, listOf(propCtx), v15)
        assertTrue(v15.isEmpty())

        val assertNamePred =
            PropertiesRuleBuilder(
                graph,
            ).should().haveName { it == "myProp" }.getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertNamePred(propCtx, listOf(propCtx), v16)
        assertTrue(v16.isEmpty())

        val assertNamePredDesc =
            PropertiesRuleBuilder(
                graph,
            ).should().haveName { it == "myProp" }.getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertNamePredDesc(propCtx, listOf(propCtx), v17)
        assertTrue(v17.isEmpty())

        val assertNotNameSingle = PropertiesRuleBuilder(graph).should().notHaveName("other").getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertNotNameSingle(propCtx, listOf(propCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotNameList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveName(listOf("other")).getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotNameList(propCtx, listOf(propCtx), v19)
        assertTrue(v19.isEmpty())

        val assertNotNameVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveName("other", "wrong").getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertNotNameVararg(propCtx, listOf(propCtx), v20)
        assertTrue(v20.isEmpty())

        val assertEndSingle = PropertiesRuleBuilder(graph).should().haveNameEndingWith("Prop").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertEndSingle(propCtx, listOf(propCtx), v21)
        assertTrue(v21.isEmpty())

        val assertEndList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameEndingWith(listOf("Prop")).getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertEndList(propCtx, listOf(propCtx), v22)
        assertTrue(v22.isEmpty())

        val assertEndVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameEndingWith("Prop", "Val").getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertEndVararg(propCtx, listOf(propCtx), v23)
        assertTrue(v23.isEmpty())

        val assertNotEndSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong").getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertNotEndSingle(propCtx, listOf(propCtx), v24)
        assertTrue(v24.isEmpty())

        val assertNotEndList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertNotEndList(propCtx, listOf(propCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotEndVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith("Wrong", "Bad").getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotEndVararg(propCtx, listOf(propCtx), v26)
        assertTrue(v26.isEmpty())

        val assertStartSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my").getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertStartSingle(propCtx, listOf(propCtx), v27)
        assertTrue(v27.isEmpty())

        val assertStartList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameStartingWith(listOf("my")).getShouldAssertion()!!
        val v28 = mutableListOf<String>()
        assertStartList(propCtx, listOf(propCtx), v28)
        assertTrue(v28.isEmpty())

        val assertStartVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameStartingWith("my", "other").getShouldAssertion()!!
        val v29 = mutableListOf<String>()
        assertStartVararg(propCtx, listOf(propCtx), v29)
        assertTrue(v29.isEmpty())

        val assertNotStartSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong").getShouldAssertion()!!
        val v30 = mutableListOf<String>()
        assertNotStartSingle(propCtx, listOf(propCtx), v30)
        assertTrue(v30.isEmpty())

        val assertNotStartList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith(listOf("wrong")).getShouldAssertion()!!
        val v31 = mutableListOf<String>()
        assertNotStartList(propCtx, listOf(propCtx), v31)
        assertTrue(v31.isEmpty())

        val assertNotStartVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("wrong", "bad").getShouldAssertion()!!
        val v32 = mutableListOf<String>()
        assertNotStartVararg(propCtx, listOf(propCtx), v32)
        assertTrue(v32.isEmpty())

        val assertMatchSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameMatching("my*").getShouldAssertion()!!
        val v33 = mutableListOf<String>()
        assertMatchSingle(propCtx, listOf(propCtx), v33)
        assertTrue(v33.isEmpty())

        val assertMatchList =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameMatching(listOf("my*")).getShouldAssertion()!!
        val v34 = mutableListOf<String>()
        assertMatchList(propCtx, listOf(propCtx), v34)
        assertTrue(v34.isEmpty())

        val assertMatchVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().haveNameMatching("my*", "other*").getShouldAssertion()!!
        val v35 = mutableListOf<String>()
        assertMatchVararg(propCtx, listOf(propCtx), v35)
        assertTrue(v35.isEmpty())

        val assertNotMatchSingle =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*").getShouldAssertion()!!
        val v36 = mutableListOf<String>()
        assertNotMatchSingle(propCtx, listOf(propCtx), v36)
        assertTrue(v36.isEmpty())

        val assertNotMatchList =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching(listOf("wrong*")).getShouldAssertion()!!
        val v37 = mutableListOf<String>()
        assertNotMatchList(propCtx, listOf(propCtx), v37)
        assertTrue(v37.isEmpty())

        val assertNotMatchVararg =
            PropertiesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("wrong*", "bad*").getShouldAssertion()!!
        val v38 = mutableListOf<String>()
        assertNotMatchVararg(propCtx, listOf(propCtx), v38)
        assertTrue(v38.isEmpty())
    }
}

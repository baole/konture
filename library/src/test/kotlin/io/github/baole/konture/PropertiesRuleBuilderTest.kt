/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PropertiesRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test properties rule builder filtering and assertions`() {
        val p1 =
            PropertyDeclaration(
                name = "id",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "kotlin.String",
                isVal = true,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                kdocText = "/** The unique ID */",
            )
        val p2 =
            PropertyDeclaration(
                name = "mutableCount",
                visibility = Visibility.INTERNAL,
                modifiers = emptySet(),
                type = "kotlin.Int",
                isVal = false, // var
                annotations = emptyList(),
                kdocText = null,
            )

        val cls =
            ClassDeclaration(
                name = "MyService",
                fqName = "com.example.MyService",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyService.kt",
                properties = listOf(p1, p2),
            )

        val topLevelProp =
            PropertyDeclaration(
                name = "globalConfig",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "kotlin.String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )

        val fileDecl =
            FileDeclaration(
                name = "MyService.kt",
                packageName = "com.example",
                classes = listOf(cls),
                topLevelProperties = listOf(topLevelProp),
                filePath = "/src/MyService.kt",
            )

        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        // Gather contexts
        val contexts =
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    val top =
                        file.topLevelProperties.map {
                            PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                        }
                    val mem =
                        file.classes.flatMap { c ->
                            c.properties.map {
                                PropertyDeclarationContext(
                                    it,
                                    file.packageName,
                                    c.name,
                                    module.path,
                                    file.filePath,
                                )
                            }
                        }
                    top + mem
                }
            }

        // 1. Filter: resideInAPackage + beMember
        val builder1 =
            PropertiesRuleBuilder(graph)
                .that()
                .resideInAPackage("com.example")
                .and()
                .beMember()
        val pred1 = builder1.getThatPredicate()!!
        val filtered1 = contexts.filter(pred1)
        assertEquals(2, filtered1.size)

        // 2. Filter: beTopLevel
        val builder2 = PropertiesRuleBuilder(graph).that().beTopLevel()
        val pred2 = builder2.getThatPredicate()!!
        val filtered2 = contexts.filter(pred2)
        assertEquals(1, filtered2.size)
        assertEquals("globalConfig", filtered2[0].declaration.name)

        // 3. Assertions checking
        val builder3 =
            PropertiesRuleBuilder(graph)
                .should()
                .bePublic()
                .andShould()
                .beVal()
                .andShould()
                .haveType("kotlin.String")
                .andShould()
                .haveAnnotationOf("MyAnnotation")
                .andShould()
                .beDocumentedWithKDoc()

        val assert3 = builder3.getShouldAssertion()!!
        val v3p1 = mutableListOf<String>()
        assert3(filtered1.first { it.declaration.name == "id" }, contexts, v3p1)
        assertTrue(v3p1.isEmpty())

        val v3p2 = mutableListOf<String>()
        assert3(filtered1.first { it.declaration.name == "mutableCount" }, contexts, v3p2)
        assertEquals(5, v3p2.size) // Not public, not val, different type, no annotation, no kdoc
    }

    @Test
    fun `test properties rule builder additional assertions`() {
        val pExtension =
            PropertyDeclaration(
                name = "extProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "kotlin.Int",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = true,
            )
        val pConst =
            PropertyDeclaration(
                name = "CONST_VAL",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val pLateinit =
            PropertyDeclaration(
                name = "lateinitVar",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.LATEINIT),
                type = "kotlin.String",
                isVal = false, // var
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        val graph = ProjectGraph(emptyMap())
        val contextExt = PropertyDeclarationContext(pExtension, "com.example", null, ":app", "/src/MyService.kt")
        val contextConst = PropertyDeclarationContext(pConst, "com.example", "MyClass", ":app", "/src/MyService.kt")
        val contextLateinit =
            PropertyDeclarationContext(pLateinit, "com.example", "MyClass", ":app", "/src/MyService.kt")

        // 1. Verify extension property assertion
        val builderExt = PropertiesRuleBuilder(graph).should().beExtension()
        val assertExt = builderExt.getShouldAssertion()!!
        val vExtOk = mutableListOf<String>()
        assertExt(contextExt, emptyList(), vExtOk)
        assertTrue(vExtOk.isEmpty())

        val vExtFail = mutableListOf<String>()
        assertExt(contextConst, emptyList(), vExtFail)
        assertEquals(1, vExtFail.size)

        // 2. Verify const property assertion
        val builderConst = PropertiesRuleBuilder(graph).should().beConst()
        val assertConst = builderConst.getShouldAssertion()!!
        val vConstOk = mutableListOf<String>()
        assertConst(contextConst, emptyList(), vConstOk)
        assertTrue(vConstOk.isEmpty())

        val vConstFail = mutableListOf<String>()
        assertConst(contextLateinit, emptyList(), vConstFail)
        assertEquals(1, vConstFail.size)

        // 3. Verify lateinit property assertion
        val builderLateinit = PropertiesRuleBuilder(graph).should().beLateinit()
        val assertLateinit = builderLateinit.getShouldAssertion()!!
        val vLateinitOk = mutableListOf<String>()
        assertLateinit(contextLateinit, emptyList(), vLateinitOk)
        assertTrue(vLateinitOk.isEmpty())

        val vLateinitFail = mutableListOf<String>()
        assertLateinit(contextConst, emptyList(), vLateinitFail)
        assertEquals(1, vLateinitFail.size)
    }

    @Test
    fun `test properties rule builder logic gates and basic filters`() {
        val p1 =
            PropertyDeclaration(
                name = "p1",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "kotlin.Int",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val p2 =
            PropertyDeclaration(
                name = "p2",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "kotlin.String",
                isVal = false,
                annotations = emptyList(),
                kdocText = null,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.example",
                classes = emptyList(),
                topLevelProperties = listOf(p1, p2),
                filePath = "/src/Sample.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val contexts =
            listOf(
                PropertyDeclarationContext(p1, "com.example", null, ":app", "/src/Sample.kt"),
                PropertyDeclarationContext(p2, "com.example", null, ":app", "/src/Sample.kt"),
            )

        // filtering tests
        val ruleName = PropertiesRuleBuilder(graph).that().haveNameStartingWith("p")
        assertEquals(2, contexts.filter(ruleName.getThatPredicate()!!).size)

        val ruleNameEnding = PropertiesRuleBuilder(graph).that().haveNameEndingWith("2")
        assertEquals(1, contexts.filter(ruleNameEnding.getThatPredicate()!!).size)

        val ruleNameMatch = PropertiesRuleBuilder(graph).that().haveNameMatching("*1")
        assertEquals(1, contexts.filter(ruleNameMatch.getThatPredicate()!!).size)

        val ruleSatisfy = PropertiesRuleBuilder(graph).that().satisfy { it.declaration.type == "kotlin.Int" }
        assertEquals(1, contexts.filter(ruleSatisfy.getThatPredicate()!!).size)

        // logic gates on filters
        val ruleXor =
            PropertiesRuleBuilder(graph).that().haveNameStartingWith("p").xor().satisfy {
                it.declaration.type ==
                    "kotlin.Int"
            }
        // p1: starts with p (T) xor type is Int (T) -> F
        // p2: starts with p (T) xor type is Int (F) -> T
        val filtered = contexts.filter(ruleXor.getThatPredicate()!!)
        assertEquals(1, filtered.size)
        assertEquals("p2", filtered[0].declaration.name)

        // logic gates on assertions
        val ruleAndShould =
            PropertiesRuleBuilder(graph)
                .should()
                .bePublic()
                .andShould()
                .beVal()
        val assertAnd = ruleAndShould.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAnd(contexts[0], contexts, v1)
        assertTrue(v1.isEmpty())

        val ruleOrShould =
            PropertiesRuleBuilder(graph)
                .should()
                .bePublic()
                .orShould()
                .bePrivate()
        val assertOr = ruleOrShould.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertOr(contexts[0], contexts, v2)
        assertTrue(v2.isEmpty())
        assertOr(contexts[1], contexts, v2)
        assertTrue(v2.isEmpty())

        val ruleNotShould = PropertiesRuleBuilder(graph).notShould().bePrivate()
        val assertNot = ruleNotShould.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNot(contexts[0], contexts, v3)
        assertTrue(v3.isEmpty())
        assertNot(contexts[1], contexts, v3)
        assertEquals(1, v3.size)
    }

    @Test
    fun `test properties rule builder multi-parameter rules`() {
        val pMulti =
            PropertyDeclaration(
                name = "multiProp",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.Double",
                isVal = true,
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                kdocText = null,
            )

        val graph = ProjectGraph(emptyMap())
        val context = PropertyDeclarationContext(pMulti, "com.example", null, ":app", "/src/MyService.kt")

        // 1. haveAllAnnotationsOf & haveAnyAnnotationOf in PropertiesThat
        val ruleAllAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A2")
        assertTrue(ruleAllAnno.getThatPredicate()!!(context))

        val ruleAnyAnno = PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf("A2", "A3")
        assertTrue(ruleAnyAnno.getThatPredicate()!!(context))

        val ruleNotAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A3")
        assertFalse(ruleNotAnno.getThatPredicate()!!(context))

        // 2. haveAllModifiers & haveAnyModifier in PropertiesThat
        val ruleAllMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(Modifier.CONST)
        assertTrue(ruleAllMod.getThatPredicate()!!(context))

        val ruleAnyMod = PropertiesRuleBuilder(graph).that().haveAnyModifier(Modifier.CONST, Modifier.LATEINIT)
        assertTrue(ruleAnyMod.getThatPredicate()!!(context))

        val ruleNotMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(Modifier.CONST, Modifier.LATEINIT)
        assertFalse(ruleNotMod.getThatPredicate()!!(context))

        // 3. haveAnyVisibility in PropertiesThat
        val ruleAnyVis = PropertiesRuleBuilder(graph).that().haveAnyVisibility(Visibility.INTERNAL, Visibility.PRIVATE)
        assertTrue(ruleAnyVis.getThatPredicate()!!(context))

        // 4. haveType in PropertiesThat
        val ruleAnyType = PropertiesRuleBuilder(graph).that().haveType("kotlin.Double", "kotlin.Int")
        assertTrue(ruleAnyType.getThatPredicate()!!(context))

        val ruleNotType = PropertiesRuleBuilder(graph).that().haveType("kotlin.String", "kotlin.Int")
        assertFalse(ruleNotType.getThatPredicate()!!(context))

        // 5. Assertions in PropertiesShould
        val assertAllAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("A1", "A2").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertNotAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("A1", "A3").getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotAnno(context, emptyList(), v2)
        assertEquals(1, v2.size)

        val assertAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.INTERNAL).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyVis(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertNotVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotVis(context, emptyList(), v4)
        assertEquals(1, v4.size)

        val assertAnyType =
            PropertiesRuleBuilder(
                graph,
            ).should().haveType("kotlin.Double", "kotlin.Int").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAnyType(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertNotType = PropertiesRuleBuilder(graph).should().haveType("kotlin.String").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotType(context, emptyList(), v6)
        assertEquals(1, v6.size)
    }

    @Test
    fun `test properties rule builder list-parameter rules`() {
        val pMulti =
            PropertyDeclaration(
                name = "multiProp",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.Double",
                isVal = true,
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                kdocText = null,
            )

        val graph = ProjectGraph(emptyMap())
        val context = PropertyDeclarationContext(pMulti, "com.example", null, ":app", "/src/MyService.kt")

        // 1. haveAllAnnotationsOf & haveAnyAnnotationOf in PropertiesThat
        val ruleAllAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf(listOf("A1", "A2"))
        assertTrue(ruleAllAnno.getThatPredicate()!!(context))

        val ruleAnyAnno = PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf(listOf("A2", "A3"))
        assertTrue(ruleAnyAnno.getThatPredicate()!!(context))

        // 2. haveAllModifiers & haveAnyModifier in PropertiesThat
        val ruleAllMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(listOf(Modifier.CONST))
        assertTrue(ruleAllMod.getThatPredicate()!!(context))

        val ruleAnyMod = PropertiesRuleBuilder(graph).that().haveAnyModifier(listOf(Modifier.CONST, Modifier.LATEINIT))
        assertTrue(ruleAnyMod.getThatPredicate()!!(context))

        // 3. haveAnyVisibility in PropertiesThat
        val ruleAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.INTERNAL, Visibility.PRIVATE))
        assertTrue(ruleAnyVis.getThatPredicate()!!(context))

        // 4. haveType in PropertiesThat
        val ruleAnyType = PropertiesRuleBuilder(graph).that().haveType(listOf("kotlin.Double", "kotlin.Int"))
        assertTrue(ruleAnyType.getThatPredicate()!!(context))

        // 5. Assertions in PropertiesShould
        val assertAllAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf(listOf("A1", "A2")).getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(listOf(Visibility.INTERNAL)).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyVis(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertAnyType =
            PropertiesRuleBuilder(
                graph,
            ).should().haveType(listOf("kotlin.Double", "kotlin.Int")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAnyType(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertAllMod =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllModifiers(listOf(Modifier.CONST)).getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertAllMod(context, emptyList(), v7)
        assertTrue(v7.isEmpty())

        val assertAnyMod =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyModifier(listOf(Modifier.CONST, Modifier.LATEINIT)).getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertAnyMod(context, emptyList(), v8)
        assertTrue(v8.isEmpty())
    }

    @Test
    fun `test properties rule builder additional filters and gates`() {
        val prop =
            PropertyDeclaration(
                name = "myVar",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.OPEN),
                type = "kotlin.String",
                isVal = false, // var
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.example",
                classes = emptyList(),
                topLevelProperties = listOf(prop),
                filePath = "/src/Sample.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context = PropertyDeclarationContext(prop, "com.example", null, ":app", "/src/Sample.kt")

        // resideInAPackage(predicate)
        assertTrue(
            PropertiesRuleBuilder(graph).that().resideInAPackage { it == "com.example" }.getThatPredicate()!!(context),
        )
        assertFalse(
            PropertiesRuleBuilder(graph).that().resideInAPackage { it == "other" }.getThatPredicate()!!(context),
        )

        // haveModifier, haveVisibility, haveType in PropertiesThat
        assertTrue(PropertiesRuleBuilder(graph).that().haveModifier(Modifier.OPEN).getThatPredicate()!!(context))
        assertTrue(
            PropertiesRuleBuilder(graph).that().haveVisibility(Visibility.PROTECTED).getThatPredicate()!!(context),
        )
        assertTrue(PropertiesRuleBuilder(graph).that().haveType("kotlin.String").getThatPredicate()!!(context))

        // resideInAPackage(packagePattern) and resideInAPackage(predicate) in PropertiesShould
        val shouldPackPattern = PropertiesRuleBuilder(graph).should().resideInAPackage("other").getShouldAssertion()!!
        val vPack1 = mutableListOf<String>()
        shouldPackPattern(context, emptyList(), vPack1)
        assertEquals(1, vPack1.size)
        assertTrue(vPack1[0].contains("should reside in package 'other'"))

        val shouldPackPred =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage { it == "other" }.getShouldAssertion()!!
        val vPack2 = mutableListOf<String>()
        shouldPackPred(context, emptyList(), vPack2)
        assertEquals(1, vPack2.size)
        assertTrue(vPack2[0].contains("should reside in package matching predicate"))

        // beVar, haveModifier, haveVisibility, satisfy in PropertiesShould
        val assertionsRule =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .andShould()
                .haveModifier(Modifier.OPEN)
                .andShould()
                .haveVisibility(Visibility.PROTECTED)
                .andShould()
                .satisfy { it.declaration.name == "myVar" }
                .andShould()
                .satisfy { p, violations -> if (p.declaration.name != "myVar") violations.add("oops") }

        val vAssert = mutableListOf<String>()
        assertionsRule.getShouldAssertion()!!(context, emptyList(), vAssert)
        assertTrue(vAssert.isEmpty(), "Expected no violations but got: $vAssert")

        // check failures
        val assertFailRule =
            PropertiesRuleBuilder(graph)
                .should()
                .haveModifier(Modifier.CONST)
                .andShould()
                .haveVisibility(Visibility.PUBLIC)
                .andShould()
                .satisfy { false }

        val vAssertFail = mutableListOf<String>()
        assertFailRule.getShouldAssertion()!!(context, emptyList(), vAssertFail)
        assertEquals(3, vAssertFail.size)

        // Logical AND/OR/NOT in builder filters
        val ruleAndNot =
            PropertiesRuleBuilder(graph)
                .that()
                .resideInAPackage("com.example")
                .not()
                .beMember()
        assertTrue(ruleAndNot.getThatPredicate()!!(context))

        val ruleOr =
            PropertiesRuleBuilder(graph)
                .that()
                .haveNameStartingWith("other")
                .or()
                .beTopLevel()
        assertTrue(ruleOr.getThatPredicate()!!(context))

        // xorShould in assertions
        val ruleXorPass =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .xorShould()
                .bePrivate()
        val vXorPass = mutableListOf<String>()
        ruleXorPass.getShouldAssertion()!!(context, emptyList(), vXorPass)
        assertTrue(vXorPass.isEmpty())

        val ruleXorFail =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .xorShould()
                .haveType("kotlin.String")
        val vXorFail = mutableListOf<String>()
        ruleXorFail.getShouldAssertion()!!(context, emptyList(), vXorFail)
        assertEquals(1, vXorFail.size)
        assertTrue(vXorFail[0].contains("should satisfy exactly one of the XOR assertions"))

        // check() method success/fail
        val passingBuilder =
            PropertiesRuleBuilder(graph)
                .that()
                .beTopLevel()
                .should()
                .beVar()
        passingBuilder.check() // should not throw

        val failingBuilder =
            PropertiesRuleBuilder(graph)
                .that()
                .beTopLevel()
                .should()
                .beVal()
        assertThrows(AssertionError::class.java) {
            failingBuilder.check()
        }
    }
}

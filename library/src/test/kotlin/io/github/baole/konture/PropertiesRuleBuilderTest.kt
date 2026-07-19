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
    fun `test properties rule builder overloads`() {
        val prop =
            PropertyDeclaration(
                name = "myVal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedType = "kotlin.String",
            )
        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.example.service",
                classes = emptyList(),
                topLevelProperties = listOf(prop),
                filePath = "/src/Sample.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":service",
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context = PropertyDeclarationContext(prop, "com.example.service", null, ":service", "/src/Sample.kt")

        // 1. resideInAPackage(vararg)
        val resideRule1 = PropertiesRuleBuilder(graph).should().resideInAPackage("com.example..", "other..")
        val vReside1 = mutableListOf<String>()
        resideRule1.getShouldAssertion()!!(context, emptyList(), vReside1)
        assertTrue(vReside1.isEmpty())

        val resideRule2 = PropertiesRuleBuilder(graph).should().resideInAPackage("other..", "another..")
        val vReside2 = mutableListOf<String>()
        resideRule2.getShouldAssertion()!!(context, emptyList(), vReside2)
        assertEquals(1, vReside2.size)

        // 2. haveType and haveTypeOf overloads
        val typeRule1 = PropertiesRuleBuilder(graph).should().haveType(String::class)
        val vType1 = mutableListOf<String>()
        typeRule1.getShouldAssertion()!!(context, emptyList(), vType1)
        assertTrue(vType1.isEmpty())

        val typeRule2 = PropertiesRuleBuilder(graph).should().haveType(Int::class)
        val vType2 = mutableListOf<String>()
        typeRule2.getShouldAssertion()!!(context, emptyList(), vType2)
        assertEquals(1, vType2.size)

        val typeRule3 = PropertiesRuleBuilder(graph).should().haveTypeOf<String>()
        val vType3 = mutableListOf<String>()
        typeRule3.getShouldAssertion()!!(context, emptyList(), vType3)
        assertTrue(vType3.isEmpty())
    }
}

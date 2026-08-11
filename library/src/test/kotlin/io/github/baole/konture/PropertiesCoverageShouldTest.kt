/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertiesCoverageShouldTest : KontureScopeTestFixture() {
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

    @Test
    fun `test PropertiesShould import and reference assertions`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val propCtx = createPropCtx(name = "myVal", isVal = true)

        val v1 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().notCall<String>().getShouldAssertion()!!(propCtx, listOf(propCtx), v1)

        val v2 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notReferenceClass<String>().getShouldAssertion()!!(propCtx, listOf(propCtx), v2)

        val v3 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveImportOf("com.example.Type").getShouldAssertion()!!(propCtx, listOf(propCtx), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().notHaveImportOf("com.example.Type").getShouldAssertion()!!(propCtx, listOf(propCtx), v4)
        assertEquals(0, v4.size)

        val v5 = mutableListOf<String>()
        PropertiesRuleBuilder(
            graph,
        ).should().haveNoWildcardImports().getShouldAssertion()!!(propCtx, listOf(propCtx), v5)
        assertEquals(0, v5.size)

        val v6 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().anyOf({ beVal() }).getShouldAssertion()!!(propCtx, listOf(propCtx), v6)
        assertEquals(0, v6.size)

        val v7 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().allOf({ beVal() }).getShouldAssertion()!!(propCtx, listOf(propCtx), v7)
        assertEquals(0, v7.size)

        val v8 = mutableListOf<String>()
        PropertiesRuleBuilder(graph).should().noneOf({ beVar() }).getShouldAssertion()!!(propCtx, listOf(propCtx), v8)
        assertEquals(0, v8.size)
    }

    @Test
    fun `test PropertiesShould type and modifier alias assertions`() {
        val propCtx = createPropCtx(name = "myProp", className = "ClassA", packageName = "com.example")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        fun assertPass(builder: PropertiesRuleBuilder) {
            val v = mutableListOf<String>()
            builder.getShouldAssertion()!!(propCtx, listOf(propCtx), v)
            assertTrue(v.isEmpty())
        }

        assertPass(PropertiesRuleBuilder(graph).should().resideInAPackage("com.example"))
        assertPass(PropertiesRuleBuilder(graph).should().notResideInAPackage("com.other"))
        assertPass(PropertiesRuleBuilder(graph).should().resideInAModule(":app"))
        assertPass(PropertiesRuleBuilder(graph).should().notResideInAModule(":forbidden"))

        assertPass(PropertiesRuleBuilder(graph).should().haveName(listOf("myProp")))
        assertPass(PropertiesRuleBuilder(graph).should().haveName("myProp", "otherProp"))
        assertPass(PropertiesRuleBuilder(graph).should().haveName { it.startsWith("my") })

        assertPass(PropertiesRuleBuilder(graph).should().notHaveName(listOf("forbiddenProp")))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveName("forbiddenProp", "otherProp"))

        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameMatching("forbidden*"))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameMatching(listOf("forbidden*")))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameMatching("forbidden*", "other*"))

        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameStartingWith("forbidden"))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("forbidden")))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameStartingWith("forbidden", "other"))

        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameEndingWith("Forbidden"))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameEndingWith(listOf("Forbidden")))
        assertPass(PropertiesRuleBuilder(graph).should().notHaveNameEndingWith("Forbidden", "Other"))

        assertPass(PropertiesRuleBuilder(graph).should().haveType(String::class))
        assertPass(PropertiesRuleBuilder(graph).should().haveType(listOf("String")))
        assertPass(PropertiesRuleBuilder(graph).should().haveType("String", "Int"))
        assertPass(PropertiesRuleBuilder(graph).should().haveTypeIn(listOf("String")))
        assertPass(PropertiesRuleBuilder(graph).should().haveTypeIn("String", "Int"))
    }
}

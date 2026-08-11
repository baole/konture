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

internal class FunctionsCoverageExtendedTest : KontureScopeTestFixture() {
    private fun createFuncCtx(
        name: String,
        className: String,
        packageName: String,
        modulePath: String = ":app",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        returnType: String = "Unit",
        parameters: List<ParameterDeclaration> = emptyList(),
        annotations: List<AnnotationDeclaration> = emptyList(),
        isExtension: Boolean = false,
        extensionReceiverType: String? = null,
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                parameters = parameters,
                annotations = annotations,
                kdocText = null,
                isExtension = isExtension,
                receiverType = extensionReceiverType,
            )
        val cls =
            ClassDeclaration(
                name = className,
                fqName = "$packageName.$className",
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/$className.kt",
            )
        val file = FileDeclaration("$className.kt", packageName, classes = listOf(cls))
        return FunctionDeclarationContext(decl, packageName, className, modulePath, file.filePath)
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
        FunctionsRuleBuilder(graph).should().resideInAPackage { false }.getShouldAssertion()!!(funcCtx, listOf(funcCtx), v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notResideInAModule(":app").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notResideInAModule(listOf(":app")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Wrong").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Wrong").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameMatching("wrong*").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveNameMatching(listOf("wrong*")).getShouldAssertion()!!(funcCtx, listOf(funcCtx), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnnotationOf("MissingAnnotation").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf("MissingAnnotation").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf("MissingAnnotation").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOperator().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInfix().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v17)
        assertEquals(1, v17.size)

        val v18 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInline().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v18)
        assertEquals(1, v18.size)

        val v19 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beSuspend().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v19)
        assertEquals(1, v19.size)

        val v20 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v20)
        assertEquals(1, v20.size)

        val v21 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().bePublic().getShouldAssertion()!!(
            createFuncCtx("f", "C", "com.example", visibility = Visibility.PRIVATE),
            listOf(funcCtx),
            v21,
        )
        assertEquals(1, v21.size)

        val v22 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v22)
        assertEquals(1, v22.size)

        val v23 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v23)
        assertEquals(1, v23.size)

        val v24 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v24)
        assertEquals(1, v24.size)

        val v25 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveReturnType("String").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v25)
        assertEquals(1, v25.size)

        val v27 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().notBeExtension().getShouldAssertion()!!(
            createFuncCtx("f", "C", "com.example", isExtension = true, extensionReceiverType = "String"),
            listOf(funcCtx),
            v27,
        )
        assertEquals(1, v27.size)

        val v28 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().beExtension().getShouldAssertion()!!(funcCtx, listOf(funcCtx), v28)
        assertEquals(1, v28.size)

        val v30 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().haveParameterTypes("Int").getShouldAssertion()!!(funcCtx, listOf(funcCtx), v30)
        assertEquals(1, v30.size)
    }

    @Test
    fun `test FunctionsThat module exclusions`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val funcCtx = createFuncCtx(name = "myFunc", className = "ClassA", packageName = "com.example")

        val p1 = FunctionsRuleBuilder(graph).that().notResideInAModule(":app").getThatPredicate()!!
        assertFalse(p1(funcCtx))

        val p2 = FunctionsRuleBuilder(graph).that().notResideInAModule(listOf(":app")).getThatPredicate()!!
        assertFalse(p2(funcCtx))

        val p3 = FunctionsRuleBuilder(graph).that().notResideInAModule(":app", ":lib").getThatPredicate()!!
        assertFalse(p3(funcCtx))
    }

    @Test
    fun `test FunctionsThat name patterns and globs`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val funcCtx = createFuncCtx(name = "myFunc", className = "ClassA", packageName = "com.example")

        val p1 = FunctionsRuleBuilder(graph).that().notHaveName("myFunc").getThatPredicate()!!
        assertFalse(p1(funcCtx))

        val p2 = FunctionsRuleBuilder(graph).that().notHaveName(listOf("myFunc")).getThatPredicate()!!
        assertFalse(p2(funcCtx))

        val p3 = FunctionsRuleBuilder(graph).that().notHaveName("myFunc", "other").getThatPredicate()!!
        assertFalse(p3(funcCtx))

        val p4 = FunctionsRuleBuilder(graph).that().notHaveNameMatching("my*").getThatPredicate()!!
        assertFalse(p4(funcCtx))

        val p5 = FunctionsRuleBuilder(graph).that().notHaveNameMatching(listOf("my*")).getThatPredicate()!!
        assertFalse(p5(funcCtx))

        val p6 = FunctionsRuleBuilder(graph).that().notHaveNameMatching("my*", "other*").getThatPredicate()!!
        assertFalse(p6(funcCtx))

        val p7 = FunctionsRuleBuilder(graph).that().notHaveNameStartingWith("my").getThatPredicate()!!
        assertFalse(p7(funcCtx))

        val p8 = FunctionsRuleBuilder(graph).that().notHaveNameStartingWith(listOf("my")).getThatPredicate()!!
        assertFalse(p8(funcCtx))

        val p9 = FunctionsRuleBuilder(graph).that().notHaveNameStartingWith("my", "other").getThatPredicate()!!
        assertFalse(p9(funcCtx))

        val p10 = FunctionsRuleBuilder(graph).that().notHaveNameEndingWith("Func").getThatPredicate()!!
        assertFalse(p10(funcCtx))

        val p11 = FunctionsRuleBuilder(graph).that().notHaveNameEndingWith(listOf("Func")).getThatPredicate()!!
        assertFalse(p11(funcCtx))

        val p12 = FunctionsRuleBuilder(graph).that().notHaveNameEndingWith("Func", "Other").getThatPredicate()!!
        assertFalse(p12(funcCtx))
    }

    @Test
    fun `test FunctionsThat visibilities`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val publicCtx =
            createFuncCtx("fPub", "C", "com.example", visibility = Visibility.PUBLIC)
        val privateCtx =
            createFuncCtx("fPriv", "C", "com.example", visibility = Visibility.PRIVATE)

        val p1 = FunctionsRuleBuilder(graph).that().arePublic().getThatPredicate()!!
        assertTrue(p1(publicCtx))
        assertFalse(p1(privateCtx))

        val p2 = FunctionsRuleBuilder(graph).that().areInternal().getThatPredicate()!!
        assertFalse(p2(publicCtx))

        val p3 = FunctionsRuleBuilder(graph).that().areProtected().getThatPredicate()!!
        assertFalse(p3(publicCtx))

        val p4 = FunctionsRuleBuilder(graph).that().arePrivate().getThatPredicate()!!
        assertFalse(p4(publicCtx))
        assertTrue(p4(privateCtx))
    }

    @Test
    fun `test FunctionsThat parameters and receiver types`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val publicCtx =
            createFuncCtx("fPub", "C", "com.example", parameters = emptyList())
        val privateCtx =
            createFuncCtx(
                "fPriv",
                "C",
                "com.example",
                parameters = listOf(ParameterDeclaration("x", "Int", false, emptyList())),
            )

        var b = FunctionsRuleBuilder(graph)
        b.that().haveAnyParameterType(Int::class)
        assertFalse(b.getThatPredicate()!!(publicCtx))
        assertTrue(b.getThatPredicate()!!(privateCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().haveAnyParameterTypeOf<Int>()
        assertFalse(b.getThatPredicate()!!(publicCtx))
        assertTrue(b.getThatPredicate()!!(privateCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().beInfix()
        assertFalse(b.getThatPredicate()!!(publicCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().haveReturnType(Int::class)
        assertFalse(b.getThatPredicate()!!(publicCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().haveReturnTypeOf<Int>()
        assertFalse(b.getThatPredicate()!!(publicCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().haveExtensionReceiver(Int::class)
        assertFalse(b.getThatPredicate()!!(publicCtx))

        b = FunctionsRuleBuilder(graph)
        b.that().haveExtensionReceiver<Int>()
        assertFalse(b.getThatPredicate()!!(publicCtx))
    }

    @Test
    fun `test FunctionsThat logical combinators`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val publicCtx =
            createFuncCtx("fPub", "C", "com.example", visibility = Visibility.PUBLIC)

        val p1 =
            FunctionsRuleBuilder(
                graph,
            ).that().satisfy { it.declaration.name.startsWith("f") }.getThatPredicate()!!
        assertTrue(p1(publicCtx))

        val p3 =
            FunctionsRuleBuilder(graph).that().anyOf(
                { haveName("fPub") },
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(p3(publicCtx))

        val p4 =
            FunctionsRuleBuilder(graph).that().allOf(
                { haveName("fPub") },
                { resideInAPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(p4(publicCtx))

        val p5 =
            FunctionsRuleBuilder(graph).that().noneOf(
                { haveName("other") },
            ).getThatPredicate()!!
        assertTrue(p5(publicCtx))
    }
}

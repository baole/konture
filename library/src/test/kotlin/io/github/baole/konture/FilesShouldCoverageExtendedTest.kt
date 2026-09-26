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

internal class FilesShouldCoverageExtendedTest : KontureScopeTestFixture() {
    @Test
    fun `test FilesShould failure messages`() {
        val file = FileDeclaration("MyFile.kt", "com.example")
        val fileCtx = FileDeclarationContext(file, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )

        val v1 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().inPackage("wrong.pkg").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().inPackage(listOf("wrong.pkg")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().inPackage("wrong.pkg", "other").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inPackage { false }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notInPackage("com.example").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notInPackage(listOf("com.example")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameEndsWith("Wrong").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameEndsWith(listOf("Wrong")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameStartsWith("Wrong").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameStartsWith(listOf("Wrong")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameMatches("wrong*").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().nameMatches(listOf("wrong*")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().containClass("MissingClass").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().containClass(listOf("MissingClass")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveImportOf("MissingImport").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveImportOf(listOf("MissingImport")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().containClassesWithAnnotation(
            "MissingAnnotation",
        ).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v17)
        assertEquals(1, v17.size)
    }

    @Test
    fun `test FilesShould usages, matching, and composite assertions`() {
        val usageCall =
            SourceUsage(
                UsageKind.CALL,
                "com.example.Foo.bar",
                "Test.kt",
                10,
                5,
                rawExpression = "Foo.bar()",
                unresolvedPossibleUsage = true,
            )
        val usageRef =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "com.example.TargetClass",
                "Test.kt",
                12,
                5,
                rawExpression = "TargetClass::class",
            )
        val fileWithUsages =
            FileDeclaration(
                "Test.kt",
                "com.example",
                classes = listOf(classA, classB),
                usages = listOf(usageCall, usageRef),
                imports = listOf("com.wrong.*"),
            )
        val fileCtx = FileDeclarationContext(fileWithUsages, ":app")
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsages))),
                ),
            )

        val vCallStr = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall("com.example.Foo.bar").getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallStr)
        assertEquals(1, vCallStr.size)

        val vCallKClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall(String::class).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallKClass)
        assertTrue(vCallKClass.isEmpty())

        val vCallReified = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall<String>().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallReified)
        assertTrue(vCallReified.isEmpty())

        val vRefStr = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass(
            "com.example.TargetClass",
        ).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefStr)
        assertEquals(1, vRefStr.size)

        val vRefKClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass(String::class).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefKClass)
        assertTrue(vRefKClass.isEmpty())

        val vRefReified = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass<String>().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefReified)
        assertTrue(vRefReified.isEmpty())

        val vWildcard = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notHaveWildcardImports().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vWildcard)
        assertEquals(1, vWildcard.size)

        val vOneClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveOnlyOneClassPerFile().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vOneClass)
        assertEquals(1, vOneClass.size)

        val vMatchClsName = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameMatchingClassName().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vMatchClsName)
        assertEquals(1, vMatchClsName.size)

        val vKdoc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().beDocumentedWithKDoc().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vKdoc)
        assertEquals(1, vKdoc.size)

        val vSatisfy1 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().satisfy {
            it.declaration.name == "Test.kt"
        }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), vSatisfy1)
        assertTrue(vSatisfy1.isEmpty())

        val vSatisfy2 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().satisfy {
                _,
                v,
            ->
            v.add("error")
        }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), vSatisfy2)
        assertEquals(1, vSatisfy2.size)

        val vAnyOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().anyOf({
            named("Test.kt")
        }, { named("Wrong.kt") }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAnyOfPass)
        assertTrue(vAnyOfPass.isEmpty())

        val vAnyOfFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().anyOf({
            named("Wrong1.kt")
        }, { named("Wrong2.kt") }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAnyOfFail)
        assertEquals(1, vAnyOfFail.size)

        val vAllOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().allOf({
            named("Test.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAllOfPass)
        assertTrue(vAllOfPass.isEmpty())

        val vNoneOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().noneOf({
            named("Wrong.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNoneOfPass)
        assertTrue(vNoneOfPass.isEmpty())

        val vNoneOfFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().noneOf({
            named("Test.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNoneOfFail)
        assertEquals(1, vNoneOfFail.size)

        val fileCtxImp =
            FileDeclarationContext(
                FileDeclaration("Imp.kt", "com.example", imports = listOf("com.prohibited.Feature")),
                ":app",
            )
        val vOnlyPkg = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().onlyDependOnPackages("com.allowed").getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyPkg)
        assertEquals(1, vOnlyPkg.size)

        val vNotPkg = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notDependOnPackages("com.prohibited").getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotPkg)
        assertEquals(1, vNotPkg.size)

        val otherMod = Module(":", ":other", "other", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val graphWithModules =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsages)), otherMod),
                ),
            )
        val fileWithOtherModUsage =
            FileDeclaration(
                "Usage.kt",
                "com.example",
                usages = listOf(SourceUsage(UsageKind.CLASS_REFERENCE, "com.example.ClassA", "Usage.kt", 1, 1)),
            )
        val fileCtxOtherUsage = FileDeclarationContext(fileWithOtherModUsage, ":other")

        val vOnlyMod = mutableListOf<String>()
        FilesRuleBuilder(
            graphWithModules,
        ).should().onlyDependOnModules(
            ":allowed",
        ).getShouldAssertion()!!(fileCtxOtherUsage, listOf(fileCtxOtherUsage), vOnlyMod)
        assertEquals(1, vOnlyMod.size)

        val vNotMod = mutableListOf<String>()
        FilesRuleBuilder(
            graphWithModules,
        ).should().notDependOnModules(
            ":app",
        ).getShouldAssertion()!!(fileCtxOtherUsage, listOf(fileCtxOtherUsage), vNotMod)
        assertEquals(1, vNotMod.size)
    }

    @Test
    fun `test FilesShould path and content alias assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        fun assertPass(builder: FilesRuleBuilder) {
            val v = mutableListOf<String>()
            builder.getShouldAssertion()!!(fileCtx, listOf(fileCtx), v)
            assertTrue(v.isEmpty())
        }

        assertPass(FilesRuleBuilder(graph).should().inPackage("com.example"))
        assertPass(FilesRuleBuilder(graph).should().inPackage(listOf("com.example")))
        assertPass(FilesRuleBuilder(graph).should().inPackage("com.example", "com.other"))
        assertPass(FilesRuleBuilder(graph).should().inPackage { it.startsWith("com") })

        assertPass(FilesRuleBuilder(graph).should().notInPackage("com.other"))
        assertPass(FilesRuleBuilder(graph).should().notInPackage(listOf("com.other")))
        assertPass(FilesRuleBuilder(graph).should().notInPackage("com.other", "com.forbidden"))

        assertPass(FilesRuleBuilder(graph).should().inModule(":app"))
        assertPass(FilesRuleBuilder(graph).should().inModules(listOf(":app")))
        assertPass(FilesRuleBuilder(graph).should().inModules(":app", ":core"))

        assertPass(FilesRuleBuilder(graph).should().notInModule(":forbidden"))
        assertPass(FilesRuleBuilder(graph).should().notInModules(listOf(":forbidden")))
        assertPass(FilesRuleBuilder(graph).should().notInModules(":forbidden", ":other"))

        assertPass(FilesRuleBuilder(graph).should().named(listOf("ClassA.kt")))
        assertPass(FilesRuleBuilder(graph).should().named("ClassA.kt", "FileB.kt"))
        assertPass(FilesRuleBuilder(graph).should().named { it.startsWith("ClassA") })

        assertPass(FilesRuleBuilder(graph).should().notNamed(listOf("Forbidden.kt")))
        assertPass(FilesRuleBuilder(graph).should().notNamed("Forbidden.kt", "Other.kt"))

        assertPass(FilesRuleBuilder(graph).should().notNameMatches("Forbidden*"))
        assertPass(FilesRuleBuilder(graph).should().notNameMatches(listOf("Forbidden*")))
        assertPass(FilesRuleBuilder(graph).should().notNameMatches("Forbidden*", "Other*"))

        assertPass(FilesRuleBuilder(graph).should().notNameStartsWith("Forbidden"))
        assertPass(FilesRuleBuilder(graph).should().notNameStartsWith(listOf("Forbidden")))
        assertPass(FilesRuleBuilder(graph).should().notNameStartsWith("Forbidden", "Other"))

        assertPass(FilesRuleBuilder(graph).should().notNameEndsWith("Forbidden.kt"))
        assertPass(FilesRuleBuilder(graph).should().notNameEndsWith(listOf("Forbidden.kt")))
        assertPass(FilesRuleBuilder(graph).should().notNameEndsWith("Forbidden.kt", "Other.kt"))

        assertPass(FilesRuleBuilder(graph).should().haveOnlyOneClassPerFile())
        assertPass(FilesRuleBuilder(graph).should().haveNameMatchingClassName())
        assertPass(FilesRuleBuilder(graph).should().haveNoWildcardImports())

        FilesRuleBuilder(graph).should().haveTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())

        FilesRuleBuilder(graph).should().notHaveTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())

        FilesRuleBuilder(graph).should().haveTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())

        FilesRuleBuilder(graph).should().notHaveTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())

        FilesRuleBuilder(graph).should().haveClasses()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())

        FilesRuleBuilder(graph).should().notHaveClasses()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), mutableListOf())
    }
}

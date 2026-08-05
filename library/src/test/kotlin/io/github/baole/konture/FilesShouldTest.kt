/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FilesShouldTest : KontureScopeTestFixture() {
    private lateinit var projectGraph: ProjectGraph

    @BeforeEach
    fun initGraph() {
        val module = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), listOf(fileA, fileB, fileC))
        projectGraph = ProjectGraph(mapOf(":" to listOf(module)))
        ProjectGraph.setDefault(projectGraph)
    }

    @Test
    fun `test FilesShould notCall and notReferenceClass overloads`() {
        val fileWithUsages =
            FileDeclaration(
                name = "TestFile.kt",
                packageName = "com.example",
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "com.example.Target.someFunc",
                            rawExpression = "someFunc()",
                            filePath = "/src/TestFile.kt",
                            line = 10,
                            column = 5,
                        ),
                        SourceUsage(
                            kind = UsageKind.CLASS_REFERENCE,
                            targetFqName = "com.example.TargetClass",
                            rawExpression = "TargetClass",
                            filePath = "/src/TestFile.kt",
                            line = 12,
                            column = 5,
                        ),
                    ),
                filePath = "/src/TestFile.kt",
            )
        val fileCtx = FileDeclarationContext(fileWithUsages, ":app")

        val ruleCall = FilesRuleBuilder(projectGraph).should().notCall("com.example.Target.someFunc")
        val violationsCall = mutableListOf<String>()
        ruleCall.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsCall)
        assertEquals(1, violationsCall.size)
        assertTrue(violationsCall[0].contains("someFunc()"))

        val ruleCallClass = FilesRuleBuilder(projectGraph).should().notCall(String::class)
        val violationsCallClass = mutableListOf<String>()
        ruleCallClass.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsCallClass)
        assertTrue(violationsCallClass.isEmpty())

        val ruleRef = FilesRuleBuilder(projectGraph).should().notReferenceClass("com.example.TargetClass")
        val violationsRef = mutableListOf<String>()
        ruleRef.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsRef)
        assertEquals(1, violationsRef.size)

        val ruleRefClass = FilesRuleBuilder(projectGraph).should().notReferenceClass(String::class)
        val violationsRefClass = mutableListOf<String>()
        ruleRefClass.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsRefClass)
        assertTrue(violationsRefClass.isEmpty())
    }

    @Test
    fun `test FilesShould package assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")

        // resideInAPackage single, list, vararg, predicate
        val ruleSingle = FilesRuleBuilder(projectGraph).should().resideInAPackage("com.example")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = FilesRuleBuilder(projectGraph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVararg = FilesRuleBuilder(projectGraph).should().resideInAPackage("com.other", "com.none")
        val violationsVararg = mutableListOf<String>()
        ruleVararg.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsVararg)
        assertEquals(1, violationsVararg.size)

        val rulePred = FilesRuleBuilder(projectGraph).should().resideInAPackage { it.contains("example") }
        val violationsPred = mutableListOf<String>()
        rulePred.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsPred)
        assertTrue(violationsPred.isEmpty())
    }

    @Test
    fun `test FilesShould name assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")

        // haveNameEndingWith
        val ruleEnding = FilesRuleBuilder(projectGraph).should().haveNameEndingWith(".kt")
        val violationsEnding = mutableListOf<String>()
        ruleEnding.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsEnding)
        assertTrue(violationsEnding.isEmpty())

        val ruleEndingList = FilesRuleBuilder(projectGraph).should().haveNameEndingWith(listOf(".kt", ".java"))
        val violationsEndingList = mutableListOf<String>()
        ruleEndingList.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsEndingList)
        assertTrue(violationsEndingList.isEmpty())

        val ruleEndingVararg = FilesRuleBuilder(projectGraph).should().haveNameEndingWith(".java", ".txt")
        val violationsEndingVararg = mutableListOf<String>()
        ruleEndingVararg.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsEndingVararg)
        assertEquals(1, violationsEndingVararg.size)

        // haveNameStartingWith
        val ruleStarting = FilesRuleBuilder(projectGraph).should().haveNameStartingWith("Class")
        val violationsStarting = mutableListOf<String>()
        ruleStarting.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsStarting)
        assertTrue(violationsStarting.isEmpty())

        val ruleStartingList = FilesRuleBuilder(projectGraph).should().haveNameStartingWith(listOf("Class", "File"))
        val violationsStartingList = mutableListOf<String>()
        ruleStartingList.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsStartingList)
        assertTrue(violationsStartingList.isEmpty())

        val ruleStartingVararg = FilesRuleBuilder(projectGraph).should().haveNameStartingWith("Foo", "Bar")
        val violationsStartingVararg = mutableListOf<String>()
        ruleStartingVararg.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsStartingVararg)
        assertEquals(1, violationsStartingVararg.size)

        // haveNameMatching
        val ruleMatch = FilesRuleBuilder(projectGraph).should().haveNameMatching("Class*.kt")
        val violationsMatch = mutableListOf<String>()
        ruleMatch.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsMatch)
        assertTrue(violationsMatch.isEmpty())

        val ruleMatchList = FilesRuleBuilder(projectGraph).should().haveNameMatching(listOf("Class*.kt", "File*.kt"))
        val violationsMatchList = mutableListOf<String>()
        ruleMatchList.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsMatchList)
        assertTrue(violationsMatchList.isEmpty())

        val ruleMatchVararg = FilesRuleBuilder(projectGraph).should().haveNameMatching("Foo*", "*Bar")
        val violationsMatchVararg = mutableListOf<String>()
        ruleMatchVararg.getShouldAssertion()!!(fileCtx, listOf(fileCtx), violationsMatchVararg)
        assertEquals(1, violationsMatchVararg.size)
    }

    @Test
    fun `test FilesShould structural and documentation assertions`() {
        val fileWithWildcard = FileDeclaration("Wild.kt", "com.example", imports = listOf("com.example.*"))
        val fileCtxWild = FileDeclarationContext(fileWithWildcard, ":app")

        val ruleWildcard = FilesRuleBuilder(projectGraph).should().notHaveWildcardImports()
        val violationsWild = mutableListOf<String>()
        ruleWildcard.getShouldAssertion()!!(fileCtxWild, listOf(fileCtxWild), violationsWild)
        assertEquals(1, violationsWild.size)

        val multiClassFile = FileDeclaration("Multi.kt", "com.example", classes = listOf(classA, classB))
        val fileCtxMulti = FileDeclarationContext(multiClassFile, ":app")

        val ruleMulti = FilesRuleBuilder(projectGraph).should().haveOnlyOneClassPerFile()
        val violationsMulti = mutableListOf<String>()
        ruleMulti.getShouldAssertion()!!(fileCtxMulti, listOf(fileCtxMulti), violationsMulti)
        assertEquals(1, violationsMulti.size)

        // haveNameMatchingClassName
        val ruleNameMatchClass = FilesRuleBuilder(projectGraph).should().haveNameMatchingClassName()
        val fileCtxA = FileDeclarationContext(fileA, ":app")
        val violationsNameClass = mutableListOf<String>()
        ruleNameMatchClass.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsNameClass)
        assertTrue(violationsNameClass.isEmpty())

        val mismatchedFile = FileDeclaration("Mismatched.kt", "com.example", classes = listOf(classA))
        val fileCtxMismatched = FileDeclarationContext(mismatchedFile, ":app")
        val violationsMismatch = mutableListOf<String>()
        ruleNameMatchClass.getShouldAssertion()!!(fileCtxMismatched, listOf(fileCtxMismatched), violationsMismatch)
        assertEquals(1, violationsMismatch.size)

        // beDocumentedWithKDoc
        val docFile = FileDeclaration("Doc.kt", "com.example", kdocText = "/** KDoc */")
        val fileCtxDoc = FileDeclarationContext(docFile, ":app")
        val ruleDoc = FilesRuleBuilder(projectGraph).should().beDocumentedWithKDoc()

        val violationsDocOk = mutableListOf<String>()
        ruleDoc.getShouldAssertion()!!(fileCtxDoc, listOf(fileCtxDoc), violationsDocOk)
        assertTrue(violationsDocOk.isEmpty())

        val violationsDocFail = mutableListOf<String>()
        ruleDoc.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsDocFail)
        assertEquals(1, violationsDocFail.size)
    }

    @Test
    fun `test FilesShould composite assertions satisfy anyOf allOf noneOf`() {
        val fileCtxA = FileDeclarationContext(fileA, ":app")

        val ruleSatisfyPred = FilesRuleBuilder(projectGraph).should().satisfy { it.declaration.name.endsWith(".kt") }
        val violationsSatisfy = mutableListOf<String>()
        ruleSatisfyPred.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsSatisfy)
        assertTrue(violationsSatisfy.isEmpty())

        val ruleSatisfyLambda =
            FilesRuleBuilder(projectGraph).should().satisfy { file, violations ->
                if (!file.declaration.name.startsWith("Class")) {
                    violations.add("Custom failure")
                }
            }
        val violationsLambda = mutableListOf<String>()
        ruleSatisfyLambda.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsLambda)
        assertTrue(violationsLambda.isEmpty())

        // anyOf
        val ruleAny =
            FilesRuleBuilder(projectGraph).should().anyOf(
                { resideInAPackage("com.example") },
                { resideInAPackage("com.other") },
            )
        val violationsAny = mutableListOf<String>()
        ruleAny.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsAny)
        assertTrue(violationsAny.isEmpty())

        // allOf
        val ruleAll =
            FilesRuleBuilder(projectGraph).should().allOf(
                { resideInAPackage("com.example") },
                { haveNameEndingWith(".kt") },
            )
        val violationsAll = mutableListOf<String>()
        ruleAll.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsAll)
        assertTrue(violationsAll.isEmpty())

        // noneOf
        val ruleNone =
            FilesRuleBuilder(projectGraph).should().noneOf(
                { resideInAPackage("com.other") },
                { haveNameStartingWith("Wrong") },
            )
        val violationsNone = mutableListOf<String>()
        ruleNone.getShouldAssertion()!!(fileCtxA, listOf(fileCtxA), violationsNone)
        assertTrue(violationsNone.isEmpty())
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.suppression

import io.github.baole.konture.AnnotationArgumentDeclaration
import io.github.baole.konture.AnnotationDeclaration
import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.FunctionDeclarationContext
import io.github.baole.konture.Module
import io.github.baole.konture.ProgrammaticSuppression
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.PropertyDeclarationContext
import io.github.baole.konture.Visibility
import io.github.baole.konture.core.model.SuppressionKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SuppressionEvaluatorDeepCoverageTest {
    @Test
    fun `test isSuppressAnnotation variations`() {
        assertTrue(SuppressionEvaluator.isSuppressAnnotation(AnnotationDeclaration("Suppress", "kotlin.Suppress")))
        assertTrue(
            SuppressionEvaluator.isSuppressAnnotation(
                AnnotationDeclaration("SuppressWarnings", "java.lang.SuppressWarnings"),
            ),
        )
        assertTrue(
            SuppressionEvaluator.isSuppressAnnotation(AnnotationDeclaration("custom.Suppress", "custom.Suppress")),
        )
        assertTrue(
            SuppressionEvaluator.isSuppressAnnotation(
                AnnotationDeclaration("custom.SuppressWarnings", "custom.SuppressWarnings"),
            ),
        )
        assertFalse(SuppressionEvaluator.isSuppressAnnotation(AnnotationDeclaration("Deprecated", "kotlin.Deprecated")))
    }

    @Test
    fun `test matchesRule all tokens and formats`() {
        assertTrue(SuppressionEvaluator.matchesRule("*", "rule.id"))
        assertTrue(SuppressionEvaluator.matchesRule("all", "rule.id"))
        assertTrue(SuppressionEvaluator.matchesRule("konture", "rule.id"))
        assertTrue(SuppressionEvaluator.matchesRule("konture:", "rule.id"))
        assertTrue(SuppressionEvaluator.matchesRule("konture:*", "rule.id"))
        assertTrue(SuppressionEvaluator.matchesRule("konture:all", "rule.id"))

        assertTrue(SuppressionEvaluator.matchesRule("konture:arch.*", "arch.layer"))
        assertTrue(SuppressionEvaluator.matchesRule("konture:arch.*", "arch"))
        assertFalse(SuppressionEvaluator.matchesRule("konture:arch.*", "other.layer"))

        assertTrue(SuppressionEvaluator.matchesRule("konture:arch.layer", "arch.layer"))
        assertTrue(SuppressionEvaluator.matchesRule("konture:arch.*layer", "arch.sublayer"))
        assertFalse(SuppressionEvaluator.matchesRule("konture:arch.layer", "arch.other"))

        assertTrue(SuppressionEvaluator.matchesRule("pkg.*", "pkg.subpkg"))
        assertTrue(SuppressionEvaluator.matchesRule("pkg.*", "pkg"))
        assertTrue(SuppressionEvaluator.matchesRule("rule-1", "rule-1"))
        assertTrue(SuppressionEvaluator.matchesRule("rule-*", "rule-123"))
        assertFalse(SuppressionEvaluator.matchesRule("rule-1", "rule-2"))
    }

    @Test
    fun `test checkInSourceSuppression extraction formats`() {
        val annQuotes =
            AnnotationDeclaration(
                name = "Suppress",
                fqName = "kotlin.Suppress",
                arguments = listOf(AnnotationArgumentDeclaration("names", "\"rule.id\", 'konture:pkg.*'")),
            )
        assertNotNull(SuppressionEvaluator.checkInSourceSuppression("rule.id", listOf(annQuotes)))
        assertNotNull(SuppressionEvaluator.checkInSourceSuppression("pkg.sub", listOf(annQuotes)))

        val annNoQuotes =
            AnnotationDeclaration(
                name = "Suppress",
                fqName = "kotlin.Suppress",
                arguments = listOf(AnnotationArgumentDeclaration("names", "[rule.id, other.rule]")),
            )
        assertNotNull(SuppressionEvaluator.checkInSourceSuppression("rule.id", listOf(annNoQuotes)))
        assertNull(SuppressionEvaluator.checkInSourceSuppression("missing.rule", listOf(annNoQuotes)))

        val annNonSuppress =
            AnnotationDeclaration(
                name = "Custom",
                fqName = "com.example.Custom",
                arguments = listOf(AnnotationArgumentDeclaration("value", "\"rule.id\"")),
            )
        assertNull(SuppressionEvaluator.checkInSourceSuppression("rule.id", listOf(annNonSuppress)))
    }

    @Test
    fun `test evaluateClassSuppression all sources and programmatic kinds`() {
        val suppressAnn =
            AnnotationDeclaration(
                "Suppress",
                "kotlin.Suppress",
                listOf(AnnotationArgumentDeclaration("v", "\"class-rule\"")),
            )
        val fileSuppressAnn =
            AnnotationDeclaration(
                "Suppress",
                "kotlin.Suppress",
                listOf(AnnotationArgumentDeclaration("v", "\"class-rule\"")),
            )

        val baseClass =
            ClassDeclaration("MyClass", "com.example.MyClass", "com.example", false, false, emptyList(), emptyList(), emptySet(), "/src/MyClass.kt", sourceLine = 10)
        val file =
            FileDeclaration("MyClass.kt", "com.example", classes = listOf(baseClass), filePath = "/src/MyClass.kt")

        // 1. File-level in-source
        val fileWithAnn = file.copy(annotations = listOf(fileSuppressAnn))
        val resFile = SuppressionEvaluator.evaluateClassSuppression("class-rule", baseClass, file = fileWithAnn)
        assertNotNull(resFile)
        assertEquals(SuppressionKind.IN_SOURCE, resFile?.kind)

        // 2. Enclosing class in-source
        val encClassWithAnn =
            baseClass.copy(
                name = "Outer",
                fqName = "com.example.Outer",
                annotations = listOf(suppressAnn),
            )
        val resEnc =
            SuppressionEvaluator.evaluateClassSuppression(
                "class-rule",
                baseClass,
                file = file,
                enclosingClass = encClassWithAnn,
            )
        assertNotNull(resEnc)
        assertEquals(SuppressionKind.IN_SOURCE, resEnc?.kind)

        // 3. Class-level in-source
        val classWithAnn = baseClass.copy(annotations = listOf(suppressAnn))
        val resCls = SuppressionEvaluator.evaluateClassSuppression("class-rule", classWithAnn, file = file)
        assertNotNull(resCls)
        assertEquals(SuppressionKind.IN_SOURCE, resCls?.kind)

        // 4. Programmatic: ClassFqName, ClassPredicate, FilePath, FilePredicate, other
        val suppFq = ProgrammaticSuppression.ClassFqName("com.example.MyClass", "reason fq")
        val resFq =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule",
                baseClass,
                programmaticSuppressions = listOf(suppFq),
            )
        assertEquals("reason fq", resFq?.reason)

        val suppPred = ProgrammaticSuppression.ClassPredicate({ it.name == "MyClass" }, "reason pred")
        val resPred =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule",
                baseClass,
                programmaticSuppressions = listOf(suppPred),
            )
        assertEquals("reason pred", resPred?.reason)

        val suppFilePath = ProgrammaticSuppression.FilePath("/src/MyClass.kt", "reason file")
        val resFilePath =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule",
                baseClass,
                file = file,
                programmaticSuppressions = listOf(suppFilePath),
            )
        assertEquals("reason file", resFilePath?.reason)

        val suppFilePred = ProgrammaticSuppression.FilePredicate({ it.name == "MyClass.kt" }, "reason file pred")
        val resFilePred =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule",
                baseClass,
                file = file,
                programmaticSuppressions = listOf(suppFilePred),
            )
        assertEquals("reason file pred", resFilePred?.reason)

        val suppOther = ProgrammaticSuppression.SliceKey("someKey", "reason")
        val resNone =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule",
                baseClass,
                programmaticSuppressions = listOf(suppOther),
            )
        assertNull(resNone)
    }

    @Test
    fun `test evaluateFunctionSuppression all sources and programmatic kinds`() {
        val suppressAnn =
            AnnotationDeclaration(
                "Suppress",
                "kotlin.Suppress",
                listOf(AnnotationArgumentDeclaration("v", "\"fn-rule\"")),
            )
        val funcDecl =
            FunctionDeclaration("myFn", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false, sourceLine = 20)
        val file =
            FileDeclaration(
                "MyFn.kt",
                "com.example",
                emptyList(),
                topLevelFunctions = listOf(funcDecl),
                filePath = "/src/MyFn.kt",
            )
        val funcCtx = FunctionDeclarationContext(funcDecl, "com.example", null, ":app", "/src/MyFn.kt")

        // 1. File-level in source
        val fileWithAnn = file.copy(annotations = listOf(suppressAnn))
        val resFile = SuppressionEvaluator.evaluateFunctionSuppression("fn-rule", funcCtx, file = fileWithAnn)
        assertNotNull(resFile)
        assertEquals(SuppressionKind.IN_SOURCE, resFile?.kind)

        // 2. Enclosing class in source
        val encCls =
            ClassDeclaration(
                "Outer", "com.example.Outer", "com.example", false, false,
                listOf(
                    suppressAnn,
                ),
                emptyList(), emptySet(), "/src/MyFn.kt", sourceLine = 5,
            )
        val resEnc =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "fn-rule",
                funcCtx,
                file = file,
                enclosingClass = encCls,
            )
        assertNotNull(resEnc)
        assertEquals(SuppressionKind.IN_SOURCE, resEnc?.kind)

        // 3. Function-level in source
        val funcDeclWithAnn = funcDecl.copy(annotations = listOf(suppressAnn))
        val funcCtxWithAnn = FunctionDeclarationContext(funcDeclWithAnn, "com.example", null, ":app", "/src/MyFn.kt")
        val resFn = SuppressionEvaluator.evaluateFunctionSuppression("fn-rule", funcCtxWithAnn, file = file)
        assertNotNull(resFn)
        assertEquals(SuppressionKind.IN_SOURCE, resFn?.kind)

        // 4. Programmatic: FunctionName, FunctionPredicate, FilePath, FilePredicate, other
        val suppName = ProgrammaticSuppression.FunctionName("myFn", "reason fn name")
        val resName =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "rule",
                funcCtx,
                programmaticSuppressions = listOf(suppName),
            )
        assertEquals("reason fn name", resName?.reason)

        val suppPred = ProgrammaticSuppression.FunctionPredicate({ it.declaration.name == "myFn" }, "reason fn pred")
        val resPred =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "rule",
                funcCtx,
                programmaticSuppressions = listOf(suppPred),
            )
        assertEquals("reason fn pred", resPred?.reason)

        val suppFilePath = ProgrammaticSuppression.FilePath("*.kt", "reason file path")
        val resFilePath =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "rule",
                funcCtx,
                file = file,
                programmaticSuppressions = listOf(suppFilePath),
            )
        assertEquals("reason file path", resFilePath?.reason)

        val suppFilePred =
            ProgrammaticSuppression.FilePredicate(
                { it.packageName == "com.example" },
                "reason file pred",
            )
        val resFilePred =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "rule",
                funcCtx,
                file = file,
                programmaticSuppressions = listOf(suppFilePred),
            )
        assertEquals("reason file pred", resFilePred?.reason)

        val suppOther = ProgrammaticSuppression.ModulePath(":other", "reason")
        val resNone =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "rule",
                funcCtx,
                programmaticSuppressions = listOf(suppOther),
            )
        assertNull(resNone)
    }

    @Test
    fun `test evaluatePropertySuppression all sources and programmatic kinds`() {
        val suppressAnn =
            AnnotationDeclaration(
                "Suppress",
                "kotlin.Suppress",
                listOf(AnnotationArgumentDeclaration("v", "\"prop-rule\"")),
            )
        val propDecl =
            PropertyDeclaration(
                "myProp",
                Visibility.PUBLIC,
                emptySet(),
                "String",
                true,
                emptyList(),
                null,
                sourceLine = 25,
            )
        val file =
            FileDeclaration(
                "MyProp.kt",
                "com.example",
                emptyList(),
                topLevelProperties = listOf(propDecl),
                filePath = "/src/MyProp.kt",
            )
        val propCtx = PropertyDeclarationContext(propDecl, "com.example", null, ":app", "/src/MyProp.kt")

        // 1. File-level in source
        val fileWithAnn = file.copy(annotations = listOf(suppressAnn))
        val resFile = SuppressionEvaluator.evaluatePropertySuppression("prop-rule", propCtx, file = fileWithAnn)
        assertNotNull(resFile)
        assertEquals(SuppressionKind.IN_SOURCE, resFile?.kind)

        // 2. Enclosing class in source
        val encCls =
            ClassDeclaration(
                "Outer", "com.example.Outer", "com.example", false, false,
                listOf(
                    suppressAnn,
                ),
                emptyList(), emptySet(), "/src/MyProp.kt", sourceLine = 5,
            )
        val resEnc =
            SuppressionEvaluator.evaluatePropertySuppression(
                "prop-rule",
                propCtx,
                file = file,
                enclosingClass = encCls,
            )
        assertNotNull(resEnc)
        assertEquals(SuppressionKind.IN_SOURCE, resEnc?.kind)

        // 3. Property-level in source
        val propDeclWithAnn = propDecl.copy(annotations = listOf(suppressAnn))
        val propCtxWithAnn = PropertyDeclarationContext(propDeclWithAnn, "com.example", null, ":app", "/src/MyProp.kt")
        val resProp = SuppressionEvaluator.evaluatePropertySuppression("prop-rule", propCtxWithAnn, file = file)
        assertNotNull(resProp)
        assertEquals(SuppressionKind.IN_SOURCE, resProp?.kind)

        // 4. Programmatic: PropertyName, PropertyPredicate, FilePath, FilePredicate, other
        val suppName = ProgrammaticSuppression.PropertyName("myProp", "reason prop name")
        val resName =
            SuppressionEvaluator.evaluatePropertySuppression(
                "rule",
                propCtx,
                programmaticSuppressions = listOf(suppName),
            )
        assertEquals("reason prop name", resName?.reason)

        val suppPred =
            ProgrammaticSuppression.PropertyPredicate(
                { it.declaration.name == "myProp" },
                "reason prop pred",
            )
        val resPred =
            SuppressionEvaluator.evaluatePropertySuppression(
                "rule",
                propCtx,
                programmaticSuppressions = listOf(suppPred),
            )
        assertEquals("reason prop pred", resPred?.reason)

        val suppFilePath = ProgrammaticSuppression.FilePath("MyProp.kt", "reason file path")
        val resFilePath =
            SuppressionEvaluator.evaluatePropertySuppression(
                "rule",
                propCtx,
                file = file,
                programmaticSuppressions = listOf(suppFilePath),
            )
        assertEquals("reason file path", resFilePath?.reason)

        val suppFilePred =
            ProgrammaticSuppression.FilePredicate(
                { it.packageName == "com.example" },
                "reason file pred",
            )
        val resFilePred =
            SuppressionEvaluator.evaluatePropertySuppression(
                "rule",
                propCtx,
                file = file,
                programmaticSuppressions = listOf(suppFilePred),
            )
        assertEquals("reason file pred", resFilePred?.reason)

        val suppOther = ProgrammaticSuppression.ModulePath(":other", "reason")
        val resNone =
            SuppressionEvaluator.evaluatePropertySuppression(
                "rule",
                propCtx,
                programmaticSuppressions = listOf(suppOther),
            )
        assertNull(resNone)
    }

    @Test
    fun `test evaluateFileSuppression evaluateModuleSuppression and evaluateSliceSuppression`() {
        val suppressAnn =
            AnnotationDeclaration(
                "Suppress",
                "kotlin.Suppress",
                listOf(AnnotationArgumentDeclaration("v", "\"file-rule\"")),
            )
        val file = FileDeclaration("FileA.kt", "com.example", emptyList(), filePath = "/src/FileA.kt")

        // File in-source
        val fileWithAnn = file.copy(annotations = listOf(suppressAnn))
        assertNotNull(SuppressionEvaluator.evaluateFileSuppression("file-rule", fileWithAnn))

        // File programmatic
        val suppFilePath = ProgrammaticSuppression.FilePath("/src/FileA.kt", "reason path")
        assertEquals(
            "reason path",
            SuppressionEvaluator.evaluateFileSuppression("rule", file, listOf(suppFilePath))?.reason,
        )

        val suppFilePred = ProgrammaticSuppression.FilePredicate({ it.name == "FileA.kt" }, "reason pred")
        assertEquals(
            "reason pred",
            SuppressionEvaluator.evaluateFileSuppression("rule", file, listOf(suppFilePred))?.reason,
        )

        val suppFileOther = ProgrammaticSuppression.ModulePath(":app", "reason")
        assertNull(SuppressionEvaluator.evaluateFileSuppression("rule", file, listOf(suppFileOther)))

        // Module programmatic
        val module = Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(file))
        val suppModPath = ProgrammaticSuppression.ModulePath(":app", "reason mod")
        assertEquals(
            "reason mod",
            SuppressionEvaluator.evaluateModuleSuppression("rule", module, listOf(suppModPath))?.reason,
        )

        val suppModPred = ProgrammaticSuppression.ModulePredicate({ it.path == ":app" }, "reason mod pred")
        assertEquals(
            "reason mod pred",
            SuppressionEvaluator.evaluateModuleSuppression("rule", module, listOf(suppModPred))?.reason,
        )

        val suppModOther = ProgrammaticSuppression.SliceKey("slice1", "reason")
        assertNull(SuppressionEvaluator.evaluateModuleSuppression("rule", module, listOf(suppModOther)))

        // Slice programmatic
        val suppSlice = ProgrammaticSuppression.SliceKey("slice1", "reason slice")
        assertEquals(
            "reason slice",
            SuppressionEvaluator.evaluateSliceSuppression(
                "rule",
                "slice1",
                listOf("alias1"),
                listOf(suppSlice),
            )?.reason,
        )
        assertEquals(
            "reason slice",
            SuppressionEvaluator.evaluateSliceSuppression("rule", "other", listOf("slice1"), listOf(suppSlice))?.reason,
        )
        assertNull(
            SuppressionEvaluator.evaluateSliceSuppression("rule", "other", listOf("otherAlias"), listOf(suppSlice)),
        )
    }
}

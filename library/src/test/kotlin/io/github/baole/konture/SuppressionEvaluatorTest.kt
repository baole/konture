/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.SuppressionKind
import io.github.baole.konture.impl.suppression.SuppressionEvaluator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SuppressionEvaluatorTest {
    @Test
    fun `test evaluateClassSuppression with in-source exact and wildcard tokens`() {
        val exactAnnotClass =
            ClassDeclaration(
                name = "ExactClass",
                fqName = "com.example.ExactClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:exact.rule\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ExactClass.kt",
            )
        val metaExact = SuppressionEvaluator.evaluateClassSuppression("exact.rule", exactAnnotClass)
        assertNotNull(metaExact)
        assertEquals(SuppressionKind.IN_SOURCE, metaExact?.kind)
        assertTrue(metaExact?.reason?.contains("konture:exact.rule") == true)

        val wildcardAnnotClass =
            ClassDeclaration(
                name = "WildcardClass",
                fqName = "com.example.WildcardClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "SuppressWarnings",
                            fqName = "java.lang.SuppressWarnings",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:*\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/WildcardClass.kt",
            )
        val metaWildcard = SuppressionEvaluator.evaluateClassSuppression("any.other.rule", wildcardAnnotClass)
        assertNotNull(metaWildcard)
        assertEquals(SuppressionKind.IN_SOURCE, metaWildcard?.kind)
    }

    @Test
    fun `test evaluateClassSuppression with programmatic matchers`() {
        val cls =
            ClassDeclaration(
                name = "TestClass",
                fqName = "com.example.TestClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TestClass.kt",
            )
        val file = FileDeclaration("TestClass.kt", "com.example", filePath = "/src/TestClass.kt", classes = listOf(cls))

        // ClassFqName glob pattern
        val metaFqGlob =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule.one",
                cls,
                file = file,
                programmaticSuppressions = listOf(ProgrammaticSuppression.ClassFqName("com.example.*", "Glob reason")),
            )
        assertNotNull(metaFqGlob)
        assertEquals("Glob reason", metaFqGlob?.reason)

        // ClassPredicate
        val metaPred =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule.one",
                cls,
                file = file,
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.ClassPredicate(
                            { it.name == "TestClass" },
                            "Class pred reason",
                        ),
                    ),
            )
        assertNotNull(metaPred)
        assertEquals("Class pred reason", metaPred?.reason)

        // FilePath pattern
        val metaFilePath =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule.one",
                cls,
                file = file,
                programmaticSuppressions = listOf(ProgrammaticSuppression.FilePath("*.kt", "File pattern reason")),
            )
        assertNotNull(metaFilePath)
        assertEquals("File pattern reason", metaFilePath?.reason)

        // FilePredicate
        val metaFilePred =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule.one",
                cls,
                file = file,
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.FilePredicate(
                            { it.packageName == "com.example" },
                            "File pred reason",
                        ),
                    ),
            )
        assertNotNull(metaFilePred)
        assertEquals("File pred reason", metaFilePred?.reason)

        // No match returns null
        val metaNull =
            SuppressionEvaluator.evaluateClassSuppression(
                "rule.one",
                cls,
                file = file,
                programmaticSuppressions = listOf(ProgrammaticSuppression.ClassFqName("com.other.*", "Other reason")),
            )
        assertNull(metaNull)
    }

    @Test
    fun `test evaluateFileSuppression with in-source and programmatic matchers`() {
        val fileWithAnnot =
            FileDeclaration(
                name = "MyFile.kt",
                packageName = "com.example",
                filePath = "/src/MyFile.kt",
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:file.rule\"")),
                        ),
                    ),
            )
        val metaFileAnnot = SuppressionEvaluator.evaluateFileSuppression("file.rule", fileWithAnnot)
        assertNotNull(metaFileAnnot)
        assertEquals(SuppressionKind.IN_SOURCE, metaFileAnnot?.kind)

        val cleanFile = FileDeclaration("Clean.kt", "com.example", filePath = "/src/Clean.kt")
        val metaFilePath =
            SuppressionEvaluator.evaluateFileSuppression(
                "file.rule",
                cleanFile,
                listOf(ProgrammaticSuppression.FilePath("/src/Clean.kt", "Path reason")),
            )
        assertNotNull(metaFilePath)
        assertEquals("Path reason", metaFilePath?.reason)

        val metaFilePred =
            SuppressionEvaluator.evaluateFileSuppression(
                "file.rule",
                cleanFile,
                listOf(ProgrammaticSuppression.FilePredicate({ it.name == "Clean.kt" }, "Predicate reason")),
            )
        assertNotNull(metaFilePred)
        assertEquals("Predicate reason", metaFilePred?.reason)

        val metaNull =
            SuppressionEvaluator.evaluateFileSuppression(
                "file.rule",
                cleanFile,
                listOf(ProgrammaticSuppression.FilePath("Other.kt", "Other reason")),
            )
        assertNull(metaNull)
    }

    @Test
    fun `test evaluateFunctionSuppression across scope hierarchy and matchers`() {
        val func =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:fn.rule\"")),
                        ),
                    ),
                kdocText = null,
                isExtension = false,
                sourceStartOffset = 0,
                sourceEndOffset = 10,
                resolvedReturnType = null,
                sourceLine = 5,
            )
        val ctx =
            FunctionDeclarationContext(
                declaration = func,
                packageName = "com.example",
                className = "Worker",
                modulePath = ":app",
                filePath = "/src/Worker.kt",
            )
        val metaDirect = SuppressionEvaluator.evaluateFunctionSuppression("fn.rule", ctx)
        assertNotNull(metaDirect)
        assertEquals(SuppressionKind.IN_SOURCE, metaDirect?.kind)

        val cleanFunc =
            FunctionDeclaration(
                name = "cleanFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                sourceStartOffset = 0,
                sourceEndOffset = 10,
                resolvedReturnType = null,
                sourceLine = 15,
            )
        val cleanCtx =
            FunctionDeclarationContext(
                declaration = cleanFunc,
                packageName = "com.example",
                className = "Worker",
                modulePath = ":app",
                filePath = "/src/Worker.kt",
            )

        val classWithAnnot =
            ClassDeclaration(
                name = "Worker",
                fqName = "com.example.Worker",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:fn.rule\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Worker.kt",
            )
        val metaFromClass =
            SuppressionEvaluator.evaluateFunctionSuppression("fn.rule", cleanCtx, enclosingClass = classWithAnnot)
        assertNotNull(metaFromClass)
        assertEquals(SuppressionKind.IN_SOURCE, metaFromClass?.kind)

        val fileWithAnnot =
            FileDeclaration(
                name = "Worker.kt",
                packageName = "com.example",
                filePath = "/src/Worker.kt",
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:fn.rule\"")),
                        ),
                    ),
            )
        val metaFromFile =
            SuppressionEvaluator.evaluateFunctionSuppression("fn.rule", cleanCtx, file = fileWithAnnot)
        assertNotNull(metaFromFile)
        assertEquals(SuppressionKind.IN_SOURCE, metaFromFile?.kind)

        // Programmatic function suppression
        val metaProgName =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "fn.rule",
                cleanCtx,
                programmaticSuppressions = listOf(ProgrammaticSuppression.FunctionName("cleanFunc", "Fn name reason")),
            )
        assertNotNull(metaProgName)
        assertEquals("Fn name reason", metaProgName?.reason)

        val metaProgPred =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "fn.rule",
                cleanCtx,
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.FunctionPredicate(
                            { it.declaration.name == "cleanFunc" },
                            "Fn pred reason",
                        ),
                    ),
            )
        assertNotNull(metaProgPred)
        assertEquals("Fn pred reason", metaProgPred?.reason)

        val metaProgFile =
            SuppressionEvaluator.evaluateFunctionSuppression(
                "fn.rule",
                cleanCtx,
                file = FileDeclaration("Worker.kt", "com.example", filePath = "/src/Worker.kt"),
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.FilePath("Worker.kt", "File path reason"),
                        ProgrammaticSuppression.FilePredicate({ it.name.endsWith(".kt") }, "File pred reason"),
                    ),
            )
        assertNotNull(metaProgFile)
        assertEquals("File path reason", metaProgFile?.reason)
    }

    @Test
    fun `test evaluatePropertySuppression across scope hierarchy and matchers`() {
        val prop =
            PropertyDeclaration(
                name = "myProp",
                type = "String",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:prop.rule\"")),
                        ),
                    ),
                kdocText = null,
                isExtension = false,
                resolvedType = null,
                sourceLine = 5,
            )
        val ctx =
            PropertyDeclarationContext(
                declaration = prop,
                packageName = "com.example",
                className = "Config",
                modulePath = ":app",
                filePath = "/src/Config.kt",
            )
        val metaDirect = SuppressionEvaluator.evaluatePropertySuppression("prop.rule", ctx)
        assertNotNull(metaDirect)
        assertEquals(SuppressionKind.IN_SOURCE, metaDirect?.kind)

        val cleanProp =
            PropertyDeclaration(
                name = "cleanProp",
                type = "Int",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedType = null,
                sourceLine = 12,
            )
        val cleanCtx =
            PropertyDeclarationContext(
                declaration = cleanProp,
                packageName = "com.example",
                className = "Config",
                modulePath = ":app",
                filePath = "/src/Config.kt",
            )

        val classWithAnnot =
            ClassDeclaration(
                name = "Config",
                fqName = "com.example.Config",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:prop.rule\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Config.kt",
            )
        val metaFromClass =
            SuppressionEvaluator.evaluatePropertySuppression("prop.rule", cleanCtx, enclosingClass = classWithAnnot)
        assertNotNull(metaFromClass)
        assertEquals(SuppressionKind.IN_SOURCE, metaFromClass?.kind)

        val fileWithAnnot =
            FileDeclaration(
                name = "Config.kt",
                packageName = "com.example",
                filePath = "/src/Config.kt",
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:prop.rule\"")),
                        ),
                    ),
            )
        val metaFromFile =
            SuppressionEvaluator.evaluatePropertySuppression("prop.rule", cleanCtx, file = fileWithAnnot)
        assertNotNull(metaFromFile)
        assertEquals(SuppressionKind.IN_SOURCE, metaFromFile?.kind)

        val metaProgName =
            SuppressionEvaluator.evaluatePropertySuppression(
                "prop.rule",
                cleanCtx,
                programmaticSuppressions = listOf(ProgrammaticSuppression.PropertyName("cleanProp", "Prop reason")),
            )
        assertNotNull(metaProgName)
        assertEquals("Prop reason", metaProgName?.reason)

        val metaProgPred =
            SuppressionEvaluator.evaluatePropertySuppression(
                "prop.rule",
                cleanCtx,
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.PropertyPredicate(
                            { it.declaration.name == "cleanProp" },
                            "Prop pred reason",
                        ),
                    ),
            )
        assertNotNull(metaProgPred)
        assertEquals("Prop pred reason", metaProgPred?.reason)

        val metaProgFile =
            SuppressionEvaluator.evaluatePropertySuppression(
                "prop.rule",
                cleanCtx,
                file = FileDeclaration("Config.kt", "com.example", filePath = "/src/Config.kt"),
                programmaticSuppressions =
                    listOf(
                        ProgrammaticSuppression.FilePath("Config.kt", "File path reason"),
                        ProgrammaticSuppression.FilePredicate({ it.name.startsWith("Con") }, "File pred reason"),
                    ),
            )
        assertNotNull(metaProgFile)
        assertEquals("File path reason", metaProgFile?.reason)
    }

    @Test
    fun `test evaluateModuleSuppression and evaluateSliceSuppression`() {
        val mod =
            Module(
                buildId = ":",
                path = ":feature:orders",
                projectDir = "feature/orders",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )

        val metaModPath =
            SuppressionEvaluator.evaluateModuleSuppression(
                "module.rule",
                mod,
                listOf(ProgrammaticSuppression.ModulePath(":feature:*", "Module glob reason")),
            )
        assertNotNull(metaModPath)
        assertEquals("Module glob reason", metaModPath?.reason)

        val metaModPred =
            SuppressionEvaluator.evaluateModuleSuppression(
                "module.rule",
                mod,
                listOf(ProgrammaticSuppression.ModulePredicate({ it.path.contains("orders") }, "Module pred reason")),
            )
        assertNotNull(metaModPred)
        assertEquals("Module pred reason", metaModPred?.reason)

        val metaSlice =
            SuppressionEvaluator.evaluateSliceSuppression(
                ruleId = "slice.rule",
                sliceKey = "com.example.(*)..",
                candidateSliceKeys = listOf("orders"),
                programmaticSuppressions = listOf(ProgrammaticSuppression.SliceKey("orders", "Slice reason")),
            )
        assertNotNull(metaSlice)
        assertEquals("Slice reason", metaSlice?.reason)

        assertNull(
            SuppressionEvaluator.evaluateModuleSuppression(
                ruleId = "module.rule",
                module = mod,
                programmaticSuppressions = listOf(ProgrammaticSuppression.ModulePath(":other:*", "Other")),
            ),
        )
        assertNull(
            SuppressionEvaluator.evaluateSliceSuppression(
                ruleId = "slice.rule",
                sliceKey = "orders",
                programmaticSuppressions = listOf(ProgrammaticSuppression.SliceKey("other", "Other")),
            ),
        )
    }
}

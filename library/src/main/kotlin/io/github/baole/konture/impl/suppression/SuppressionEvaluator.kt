/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.suppression

import io.github.baole.konture.AnnotationDeclaration
import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclarationContext
import io.github.baole.konture.Module
import io.github.baole.konture.ProgrammaticSuppression
import io.github.baole.konture.PropertyDeclarationContext
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.SuppressionKind
import io.github.baole.konture.core.model.SuppressionMetadata
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

internal object SuppressionEvaluator {
    private val STRING_LITERAL_REGEX = Regex("\"([^\"]*)\"|'([^']*)'")

    fun isSuppressAnnotation(annotation: AnnotationDeclaration): Boolean {
        val name = annotation.name
        val fqName = annotation.fqName
        return name == "Suppress" || name == "SuppressWarnings" ||
            fqName == "kotlin.Suppress" || fqName == "java.lang.SuppressWarnings" ||
            name.endsWith(".Suppress") || name.endsWith(".SuppressWarnings")
    }

    private fun extractTokens(raw: String): List<String> {
        val matches =
            STRING_LITERAL_REGEX.findAll(raw).mapNotNull {
                it.groups[1]?.value ?: it.groups[2]?.value
            }.toList()
        return if (matches.isNotEmpty()) {
            matches
        } else {
            raw.trim('[', ']', '(', ')', '{', '}', ' ', '"', '\'')
                .split(',', ';')
                .map { it.trim(' ', '"', '\'', '\t', '\n') }
                .filter { it.isNotBlank() }
        }
    }

    fun matchesRule(
        token: String,
        ruleId: String,
    ): Boolean {
        val trimmed = token.trim()
        when (trimmed) {
            "*", "all", "konture", "konture:", "konture:*", "konture:all" -> return true
        }

        if (trimmed.startsWith("konture:")) {
            val target = trimmed.removePrefix("konture:")
            if (target == "*" || target == "all" || target.isEmpty()) return true
            if (target.endsWith(".*")) {
                val prefix = target.removeSuffix(".*")
                return ruleId.startsWith("$prefix.") || ruleId == prefix
            }
            return target == ruleId || PatternMatchers.matchesSimpleGlob(target, ruleId)
        }
        if (trimmed.endsWith(".*")) {
            val prefix = trimmed.removeSuffix(".*")
            return ruleId.startsWith("$prefix.") || ruleId == prefix
        }
        return trimmed == ruleId || PatternMatchers.matchesSimpleGlob(trimmed, ruleId)
    }

    fun checkInSourceSuppression(
        ruleId: String,
        annotations: List<AnnotationDeclaration>,
    ): String? {
        for (ann in annotations) {
            if (!isSuppressAnnotation(ann)) continue
            for (arg in ann.arguments) {
                val tokens = extractTokens(arg.value)
                for (token in tokens) {
                    if (matchesRule(token, ruleId)) {
                        return "In-source suppression: @${ann.name}(\"$token\")"
                    }
                }
            }
        }
        return null
    }

    fun evaluateClassSuppression(
        ruleId: String,
        cls: ClassDeclaration,
        file: FileDeclaration? = null,
        enclosingClass: ClassDeclaration? = null,
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        // 1. File-level in-source suppression
        if (file != null) {
            val fileReason = checkInSourceSuppression(ruleId, file.annotations)
            if (fileReason != null) {
                logSuppressed(ruleId, cls.fqName, fileReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = fileReason,
                    location = SourceLocation(filePath = file.filePath, line = 1),
                )
            }
        }

        // 2. Enclosing class in-source suppression (if nested)
        if (enclosingClass != null) {
            val encReason = checkInSourceSuppression(ruleId, enclosingClass.annotations)
            if (encReason != null) {
                logSuppressed(ruleId, cls.fqName, encReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = encReason,
                    location = SourceLocation(filePath = enclosingClass.filePath, line = enclosingClass.sourceLine),
                )
            }
        }

        // 3. Class-level in-source suppression
        val classReason = checkInSourceSuppression(ruleId, cls.annotations)
        if (classReason != null) {
            logSuppressed(ruleId, cls.fqName, classReason)
            return SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = classReason,
                location = SourceLocation(filePath = cls.filePath, line = cls.sourceLine),
            )
        }

        // 4. Programmatic suppressions
        for (suppression in programmaticSuppressions) {
            when (suppression) {
                is ProgrammaticSuppression.ClassFqName -> {
                    if (cls.fqName == suppression.pattern ||
                        cls.name == suppression.pattern ||
                        PatternMatchers.matchesSimpleGlob(suppression.pattern, cls.fqName) ||
                        PatternMatchers.matchesSimpleGlob(suppression.pattern, cls.name)
                    ) {
                        logSuppressed(ruleId, cls.fqName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = cls.filePath, line = cls.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.ClassPredicate -> {
                    if (suppression.predicate(cls)) {
                        logSuppressed(ruleId, cls.fqName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = cls.filePath, line = cls.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePath -> {
                    if (matchesFilePattern(file, suppression.pattern)) {
                        logSuppressed(ruleId, cls.fqName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file?.filePath ?: cls.filePath, line = 1),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePredicate -> {
                    if (file != null && suppression.predicate(file)) {
                        logSuppressed(ruleId, cls.fqName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file.filePath, line = 1),
                        )
                    }
                }

                else -> {}
            }
        }

        return null
    }

    fun evaluateFunctionSuppression(
        ruleId: String,
        func: FunctionDeclarationContext,
        file: FileDeclaration? = null,
        enclosingClass: ClassDeclaration? = null,
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        // 1. File-level in-source suppression
        if (file != null) {
            val fileReason = checkInSourceSuppression(ruleId, file.annotations)
            if (fileReason != null) {
                logSuppressed(ruleId, func.qualifiedName, fileReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = fileReason,
                    location = SourceLocation(filePath = file.filePath, line = 1),
                )
            }
        }

        // 2. Enclosing class in-source suppression
        if (enclosingClass != null) {
            val encReason = checkInSourceSuppression(ruleId, enclosingClass.annotations)
            if (encReason != null) {
                logSuppressed(ruleId, func.qualifiedName, encReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = encReason,
                    location = SourceLocation(filePath = enclosingClass.filePath, line = enclosingClass.sourceLine),
                )
            }
        }

        // 3. Function-level in-source suppression
        val funcReason = checkInSourceSuppression(ruleId, func.declaration.annotations)
        if (funcReason != null) {
            logSuppressed(ruleId, func.qualifiedName, funcReason)
            return SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = funcReason,
                location = SourceLocation(filePath = func.filePath, line = func.declaration.sourceLine),
            )
        }

        // 4. Programmatic suppressions
        for (suppression in programmaticSuppressions) {
            when (suppression) {
                is ProgrammaticSuppression.FunctionName -> {
                    if (func.declaration.name == suppression.nameOrFqName ||
                        func.qualifiedName == suppression.nameOrFqName ||
                        PatternMatchers.matchesSimpleGlob(suppression.nameOrFqName, func.declaration.name) ||
                        PatternMatchers.matchesSimpleGlob(suppression.nameOrFqName, func.qualifiedName)
                    ) {
                        logSuppressed(ruleId, func.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = func.filePath, line = func.declaration.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.FunctionPredicate -> {
                    if (suppression.predicate(func)) {
                        logSuppressed(ruleId, func.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = func.filePath, line = func.declaration.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePath -> {
                    if (matchesFilePattern(file, suppression.pattern)) {
                        logSuppressed(ruleId, func.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file?.filePath ?: func.filePath, line = 1),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePredicate -> {
                    if (file != null && suppression.predicate(file)) {
                        logSuppressed(ruleId, func.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file.filePath, line = 1),
                        )
                    }
                }

                else -> {}
            }
        }

        return null
    }

    fun evaluatePropertySuppression(
        ruleId: String,
        prop: PropertyDeclarationContext,
        file: FileDeclaration? = null,
        enclosingClass: ClassDeclaration? = null,
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        // 1. File-level in-source suppression
        if (file != null) {
            val fileReason = checkInSourceSuppression(ruleId, file.annotations)
            if (fileReason != null) {
                logSuppressed(ruleId, prop.qualifiedName, fileReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = fileReason,
                    location = SourceLocation(filePath = file.filePath, line = 1),
                )
            }
        }

        // 2. Enclosing class in-source suppression
        if (enclosingClass != null) {
            val encReason = checkInSourceSuppression(ruleId, enclosingClass.annotations)
            if (encReason != null) {
                logSuppressed(ruleId, prop.qualifiedName, encReason)
                return SuppressionMetadata(
                    kind = SuppressionKind.IN_SOURCE,
                    reason = encReason,
                    location = SourceLocation(filePath = enclosingClass.filePath, line = enclosingClass.sourceLine),
                )
            }
        }

        // 3. Property-level in-source suppression
        val propReason = checkInSourceSuppression(ruleId, prop.declaration.annotations)
        if (propReason != null) {
            logSuppressed(ruleId, prop.qualifiedName, propReason)
            return SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = propReason,
                location = SourceLocation(filePath = prop.filePath, line = prop.declaration.sourceLine),
            )
        }

        // 4. Programmatic suppressions
        for (suppression in programmaticSuppressions) {
            when (suppression) {
                is ProgrammaticSuppression.PropertyName -> {
                    if (prop.declaration.name == suppression.nameOrFqName ||
                        prop.qualifiedName == suppression.nameOrFqName ||
                        PatternMatchers.matchesSimpleGlob(suppression.nameOrFqName, prop.declaration.name) ||
                        PatternMatchers.matchesSimpleGlob(suppression.nameOrFqName, prop.qualifiedName)
                    ) {
                        logSuppressed(ruleId, prop.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = prop.filePath, line = prop.declaration.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.PropertyPredicate -> {
                    if (suppression.predicate(prop)) {
                        logSuppressed(ruleId, prop.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = prop.filePath, line = prop.declaration.sourceLine),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePath -> {
                    if (matchesFilePattern(file, suppression.pattern)) {
                        logSuppressed(ruleId, prop.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file?.filePath ?: prop.filePath, line = 1),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePredicate -> {
                    if (file != null && suppression.predicate(file)) {
                        logSuppressed(ruleId, prop.qualifiedName, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file.filePath, line = 1),
                        )
                    }
                }

                else -> {}
            }
        }

        return null
    }

    fun evaluateFileSuppression(
        ruleId: String,
        file: FileDeclaration,
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        val fileReason = checkInSourceSuppression(ruleId, file.annotations)
        if (fileReason != null) {
            logSuppressed(ruleId, file.name, fileReason)
            return SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = fileReason,
                location = SourceLocation(filePath = file.filePath, line = 1),
            )
        }

        for (suppression in programmaticSuppressions) {
            when (suppression) {
                is ProgrammaticSuppression.FilePath -> {
                    if (file.filePath == suppression.pattern || file.name == suppression.pattern ||
                        PatternMatchers.matchesSimpleGlob(suppression.pattern, file.filePath) ||
                        PatternMatchers.matchesSimpleGlob(suppression.pattern, file.name)
                    ) {
                        logSuppressed(ruleId, file.name, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file.filePath, line = 1),
                        )
                    }
                }

                is ProgrammaticSuppression.FilePredicate -> {
                    if (suppression.predicate(file)) {
                        logSuppressed(ruleId, file.name, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                            location = SourceLocation(filePath = file.filePath, line = 1),
                        )
                    }
                }

                else -> {}
            }
        }

        return null
    }

    fun evaluateModuleSuppression(
        ruleId: String,
        module: Module,
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        for (suppression in programmaticSuppressions) {
            when (suppression) {
                is ProgrammaticSuppression.ModulePath -> {
                    if (module.path == suppression.modulePath ||
                        PatternMatchers.matchesModuleGlob(suppression.modulePath, module.path)
                    ) {
                        logSuppressed(ruleId, module.path, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                        )
                    }
                }

                is ProgrammaticSuppression.ModulePredicate -> {
                    if (suppression.predicate(module)) {
                        logSuppressed(ruleId, module.path, suppression.reason)
                        return SuppressionMetadata(
                            kind = SuppressionKind.PROGRAMMATIC,
                            reason = suppression.reason,
                        )
                    }
                }

                else -> {}
            }
        }
        return null
    }

    fun evaluateSliceSuppression(
        ruleId: String,
        sliceKey: String,
        candidateSliceKeys: List<String> = emptyList(),
        programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    ): SuppressionMetadata? {
        val keys = (listOf(sliceKey) + candidateSliceKeys).toSet()
        for (suppression in programmaticSuppressions) {
            if (suppression is ProgrammaticSuppression.SliceKey) {
                val matches =
                    keys.any { key ->
                        key == suppression.sliceKey ||
                            PatternMatchers.matchesSimpleGlob(suppression.sliceKey, key)
                    }
                if (matches) {
                    logSuppressed(ruleId, suppression.sliceKey, suppression.reason)
                    return SuppressionMetadata(
                        kind = SuppressionKind.PROGRAMMATIC,
                        reason = suppression.reason,
                    )
                }
            }
        }
        return null
    }

    private fun matchesFilePattern(
        file: FileDeclaration?,
        pattern: String,
    ): Boolean {
        if (file == null) return false
        return file.filePath == pattern ||
            file.name == pattern ||
            PatternMatchers.matchesSimpleGlob(pattern, file.filePath) ||
            PatternMatchers.matchesSimpleGlob(pattern, file.name)
    }

    private fun logSuppressed(
        ruleId: String,
        target: String,
        reason: String,
    ) {
        KontureLogger.log(
            LogLevel.DEBUG,
            getMessage("suppression.logged.debug", ruleId, target, reason),
        )
    }
}

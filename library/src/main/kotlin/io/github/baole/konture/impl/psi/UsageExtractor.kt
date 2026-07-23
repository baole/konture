/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import io.github.baole.konture.ResolutionConfidence
import io.github.baole.konture.SourceUsage
import io.github.baole.konture.UsageKind
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.parents

/** Extracts resolved and conservatively possible source usages from a Kotlin PSI file. */
internal object UsageExtractor {
    fun extract(
        file: KtFile,
        content: String,
        packageName: String,
        imports: List<String>,
        importAliases: Map<String, String>,
        filePath: String,
        isClassDeclared: (String) -> Boolean,
    ): List<SourceUsage> {
        val usages = mutableListOf<SourceUsage>()
        val seen = mutableSetOf<String>()

        fun location(element: KtElement): Pair<Int, Int> {
            val offset = element.textRange.startOffset
            val line = content.substring(0, offset).count { it == '\n' } + 1
            val previousBreak = content.lastIndexOf('\n', startIndex = (offset - 1).coerceAtLeast(0))
            return line to offset - previousBreak
        }

        fun enclosing(element: KtElement): Triple<KtNamedFunction?, String?, String?> =
            Triple(
                element.parents.filterIsInstance<KtNamedFunction>().firstOrNull(),
                element.parents.filterIsInstance<KtClassOrObject>().firstOrNull()?.fqName?.asString(),
                element.parents.filterIsInstance<KtProperty>().firstOrNull()?.name,
            )

        fun add(
            kind: UsageKind,
            target: String,
            element: KtElement,
            raw: String,
            possible: List<String> = emptyList(),
            unresolved: Boolean = false,
        ) {
            val (line, column) = location(element)
            val key = "$kind:$target:${element.textRange.startOffset}:$unresolved"
            if (!seen.add(key)) return
            val (function, clazz, property) = enclosing(element)
            usages +=
                SourceUsage(
                    kind = kind,
                    targetFqName = target,
                    filePath = filePath,
                    line = line,
                    column = column,
                    enclosingFunction = function?.name,
                    enclosingClass = clazz,
                    enclosingProperty = property,
                    rawExpression = raw,
                    possibleTargetFqNames = possible,
                    unresolvedPossibleUsage = unresolved,
                    confidence =
                        if (unresolved) ResolutionConfidence.POSSIBLE else ResolutionConfidence.RESOLVED,
                    sourceStartOffset = element.textRange.startOffset,
                    sourceEndOffset = element.textRange.endOffset,
                    enclosingFunctionStartOffset = function?.textRange?.startOffset ?: -1,
                    enclosingFunctionEndOffset = function?.textRange?.endOffset ?: -1,
                )
        }

        @Suppress("ReturnCount")
        fun resolve(
            raw: String,
            element: KtElement,
        ): Pair<String?, List<String>> {
            if (raw.contains('.')) return raw to emptyList()
            if (element.parents.filterIsInstance<KtNamedFunction>().any { it.name == raw }) return null to emptyList()
            importAliases[raw]?.let { return it to emptyList() }

            val explicit = imports.filter { !it.endsWith(".*") && it.substringAfterLast('.') == raw }
            if (explicit.size == 1) return explicit.single() to emptyList()
            if (explicit.size > 1) return null to explicit

            val samePackageFqName = if (packageName.isNotEmpty()) "$packageName.$raw" else raw
            if (isClassDeclared(samePackageFqName)) return samePackageFqName to emptyList()

            KotlinDefaultTypes.bySimpleName[raw]?.let { return it to emptyList() }

            val wildcard = imports.filter { it.endsWith(".*") }.map { "${it.removeSuffix(".*")}.$raw" }
            val declaredWildcardMatches = wildcard.filter(isClassDeclared)
            if (declaredWildcardMatches.isNotEmpty()) {
                if (declaredWildcardMatches.size == 1) return declaredWildcardMatches.single() to emptyList()
                return null to declaredWildcardMatches
            }

            if (wildcard.size == 1) return wildcard.single() to emptyList()
            if (wildcard.size > 1) return null to wildcard
            return null to emptyList()
        }

        file.accept(
            object : KtTreeVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    super.visitCallExpression(expression)
                    val raw = expression.calleeExpression?.text ?: return
                    val (target, possible) = resolve(raw, expression)
                    if (raw.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
                        if (target != null) add(UsageKind.CLASS_REFERENCE, target, expression, raw)
                    } else if (target != null) {
                        add(UsageKind.CALL, target, expression, raw)
                    } else if (possible.isNotEmpty()) {
                        add(UsageKind.CALL, raw, expression, raw, possible, unresolved = true)
                    }
                }

                override fun visitTypeReference(typeReference: KtTypeReference) {
                    super.visitTypeReference(typeReference)
                    Regex("[A-Za-z_][A-Za-z0-9_.]*").findAll(typeReference.text).forEach { match ->
                        val raw = match.value
                        if (raw.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
                            resolve(raw, typeReference).first?.let {
                                add(UsageKind.CLASS_REFERENCE, it, typeReference, raw)
                            }
                        }
                    }
                }

                override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
                    super.visitAnnotationEntry(annotationEntry)
                    val raw = annotationEntry.typeReference?.text ?: return
                    resolve(raw, annotationEntry).first?.let {
                        add(UsageKind.CLASS_REFERENCE, it, annotationEntry, raw)
                    }
                }

                override fun visitClassLiteralExpression(expression: KtClassLiteralExpression) {
                    super.visitClassLiteralExpression(expression)
                    val raw = expression.receiverExpression?.text ?: return
                    resolve(raw, expression).first?.let { add(UsageKind.CLASS_REFERENCE, it, expression, raw) }
                }

                override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                    super.visitDotQualifiedExpression(expression)
                    val raw = expression.receiverExpression.text
                    if (raw.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
                        resolve(raw, expression).first?.let { add(UsageKind.CLASS_REFERENCE, it, expression, raw) }
                    }
                }
            },
        )
        return usages
    }
}

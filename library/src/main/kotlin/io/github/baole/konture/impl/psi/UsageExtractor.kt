/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
        val collector = UsageCollector(content, filePath)
        val resolver = SymbolResolver(packageName, imports, importAliases, isClassDeclared)

        file.accept(UsageVisitor(collector, resolver))
        return collector.usages
    }
}

private class SymbolResolver(
    private val packageName: String,
    private val imports: List<String>,
    private val importAliases: Map<String, String>,
    private val isClassDeclared: (String) -> Boolean,
) {
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

    fun findVariableType(
        receiver: String,
        element: KtElement,
    ): String? {
        if (receiver.firstOrNull()?.isLowerCase() != true) return null
        for (parent in element.parents) {
            when (parent) {
                is KtNamedFunction -> {
                    parent.valueParameters.find { it.name == receiver }?.typeReference?.text?.let { return it }
                }
                is KtClassOrObject -> {
                    parent.primaryConstructorParameters.find { it.name == receiver }?.typeReference?.text?.let { return it }
                    parent.declarations.filterIsInstance<KtProperty>().find { it.name == receiver }?.typeReference?.text?.let { return it }
                }
                is KtFile -> {
                    parent.declarations.filterIsInstance<KtProperty>().find { it.name == receiver }?.typeReference?.text?.let { return it }
                }
            }
        }
        return null
    }
}

private class UsageCollector(
    private val content: String,
    private val filePath: String,
) {
    val usages = mutableListOf<SourceUsage>()
    private val seen = mutableSetOf<String>()

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

    private fun location(element: KtElement): Pair<Int, Int> {
        val offset = element.textRange.startOffset
        val line = content.substring(0, offset).count { it == '\n' } + 1
        val previousBreak = content.lastIndexOf('\n', startIndex = (offset - 1).coerceAtLeast(0))
        return line to offset - previousBreak
    }

    private fun enclosing(element: KtElement): Triple<KtNamedFunction?, String?, String?> =
        Triple(
            element.parents.filterIsInstance<KtNamedFunction>().firstOrNull(),
            element.parents.filterIsInstance<KtClassOrObject>().firstOrNull()?.fqName?.asString(),
            element.parents.filterIsInstance<KtProperty>().firstOrNull()?.name,
        )
}

private class UsageVisitor(
    private val collector: UsageCollector,
    private val resolver: SymbolResolver,
) : KtTreeVisitorVoid() {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        val parentDot = expression.parent as? KtDotQualifiedExpression
        val receiverText = if (parentDot?.selectorExpression == expression) parentDot.receiverExpression.text else null

        if (receiverText != null) {
            val fullRaw = "$receiverText.$callee"
            val typeOrClass =
                if (receiverText.firstOrNull()?.isLowerCase() == true) {
                    resolver.findVariableType(receiverText, expression) ?: receiverText
                } else {
                    receiverText
                }

            val (resolvedReceiver, _) = resolver.resolve(typeOrClass, expression)
            val qualifiedTarget = if (resolvedReceiver != null) "$resolvedReceiver.$callee" else fullRaw
            val possibleTargets = listOf(callee, fullRaw, qualifiedTarget).distinct()

            collector.add(UsageKind.CALL, qualifiedTarget, expression, fullRaw, possible = possibleTargets)
        } else {
            val (target, possible) = resolver.resolve(callee, expression)
            if (callee.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
                if (target != null) collector.add(UsageKind.CLASS_REFERENCE, target, expression, callee)
            } else if (target != null) {
                collector.add(UsageKind.CALL, target, expression, callee, possible = possible)
            } else {
                collector.add(
                    UsageKind.CALL,
                    callee,
                    expression,
                    callee,
                    possible = possible.ifEmpty { listOf(callee) },
                    unresolved = possible.isEmpty(),
                )
            }
        }
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        super.visitTypeReference(typeReference)
        Regex("[A-Za-z_][A-Za-z0-9_.]*").findAll(typeReference.text).forEach { match ->
            val raw = match.value
            if (raw.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
                resolver.resolve(raw, typeReference).first?.let {
                    collector.add(UsageKind.CLASS_REFERENCE, it, typeReference, raw)
                }
            }
        }
    }

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        super.visitAnnotationEntry(annotationEntry)
        val raw = annotationEntry.typeReference?.text ?: return
        resolver.resolve(raw, annotationEntry).first?.let {
            collector.add(UsageKind.CLASS_REFERENCE, it, annotationEntry, raw)
        }
    }

    override fun visitClassLiteralExpression(expression: KtClassLiteralExpression) {
        super.visitClassLiteralExpression(expression)
        val raw = expression.receiverExpression?.text ?: return
        resolver.resolve(raw, expression).first?.let { collector.add(UsageKind.CLASS_REFERENCE, it, expression, raw) }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val raw = expression.receiverExpression.text
        if (raw.substringAfterLast('.').firstOrNull()?.isUpperCase() == true) {
            resolver.resolve(raw, expression).first?.let { collector.add(UsageKind.CLASS_REFERENCE, it, expression, raw) }
        }
    }
}

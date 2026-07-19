/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile

internal object TypeAliasScanner {
    fun scan(
        ktFile: KtFile,
        sourceText: String,
    ): Map<String, TypeAliasDefinition> {
        val packageName = ktFile.packageFqName.asString()
        val imports = ktFile.importDirectives.mapNotNull { it.importPath?.toString() }
        val importAliases =
            ktFile.importDirectives
                .mapNotNull { directive ->
                    val aliasName = directive.aliasName
                    val fqName = directive.importedFqName?.asString()
                    if (aliasName != null && fqName != null) aliasName to fqName else null
                }.toMap()
        val declaredClasses = DeclaredClassScanner.collectFqNames(ktFile)

        val aliases = mutableMapOf<String, TypeAliasDefinition>()
        scanSourceTypeAliases(sourceText) { aliasName, typeParameters, rawUnderlyingType, offset ->
            if (!isCodeOffset(ktFile, offset)) return@scanSourceTypeAliases
            val enclosingClassScopes = enclosingClassScopes(ktFile, offset)
            val fqName =
                enclosingClassScopes.firstOrNull()?.let { "$it.$aliasName" }
                    ?: listOf(packageName, aliasName).filter(String::isNotEmpty).joinToString(".")
            val underlyingType =
                rawUnderlyingType
                    .canonicalizeEscapedKotlinIdentifiers()
                    .qualifyNestedAliasTarget(enclosingClassScopes, declaredClasses)
            aliases[fqName] =
                TypeAliasDefinition(
                    underlyingType = underlyingType,
                    typeParameters = typeParameters,
                    packageName = packageName,
                    imports = imports,
                    importAliases = importAliases,
                    enclosingClassScopes = enclosingClassScopes,
                )
        }
        return aliases
    }

    private fun isCodeOffset(
        ktFile: KtFile,
        offset: Int,
    ): Boolean {
        var element = ktFile.findElementAt(offset)
        while (element != null) {
            if (
                element is org.jetbrains.kotlin.com.intellij.psi.PsiComment ||
                element is org.jetbrains.kotlin.psi.KtStringTemplateExpression
            ) {
                return false
            }
            element = element.parent
        }
        return true
    }

    private fun String.qualifyNestedAliasTarget(
        enclosingClassScopes: List<String>,
        declaredClasses: Set<String>,
    ): String {
        val firstSegment = trim().substringBefore('.').substringBefore('<').trim()
        if (firstSegment.isEmpty() || '.' in trim().substringBefore('<')) return this
        val nestedScope = enclosingClassScopes.firstOrNull { "$it.$firstSegment" in declaredClasses } ?: return this
        return "$nestedScope.$firstSegment" + trim().removePrefix(firstSegment)
    }

    private fun scanSourceTypeAliases(
        sourceText: String,
        onAlias: (name: String, typeParameters: List<String>, underlyingType: String, offset: Int) -> Unit,
    ) {
        val typeAliasName =
            Regex(
                """\btypealias\s+((?:[A-Za-z_][A-Za-z0-9_]*)|(?:`[^`]+`))""",
            )
        typeAliasName.findAll(sourceText).forEach { match ->
            val header = sourceText.readTypeAliasHeader(match.range.last + 1) ?: return@forEach
            val underlyingType = sourceText.readTypeAliasUnderlyingType(header.underlyingTypeStart)
            if (underlyingType.isEmpty()) return@forEach
            onAlias(
                match.groupValues[1].canonicalizeEscapedKotlinIdentifiers(),
                header.typeParameters,
                underlyingType,
                match.range.first,
            )
        }
    }

    private data class TypeAliasHeader(
        val typeParameters: List<String>,
        val underlyingTypeStart: Int,
    )

    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    private fun String.readTypeAliasHeader(startIndex: Int): TypeAliasHeader? {
        var index = skipWhitespaceAndComments(startIndex) ?: return null
        val typeParameterText =
            if (getOrNull(index) == '<') {
                var depth = 1
                val parameters = StringBuilder()
                index++
                while (index < length && depth > 0) {
                    if (startsWith("//", index)) {
                        index = indexOf('\n', index).takeIf { it != -1 } ?: length
                        continue
                    }
                    if (startsWith("/*", index)) {
                        val commentEnd = nestedBlockCommentEnd(index) ?: return null
                        if (parameters.needsCommentSeparator(commentEnd, this)) parameters.append(' ')
                        index = commentEnd
                        continue
                    }

                    when (this[index]) {
                        '<' -> {
                            depth++
                            parameters.append('<')
                        }
                        '>' -> {
                            if (index == 0 || this[index - 1] != '-') depth--
                            if (depth > 0) parameters.append('>')
                        }
                        else -> parameters.append(this[index])
                    }
                    index++
                }
                if (depth != 0) return null
                parameters.toString()
            } else {
                ""
            }
        index = skipWhitespaceAndComments(index) ?: return null
        if (getOrNull(index) != '=') return null
        return TypeAliasHeader(typeParameterText.typeAliasParameterNames(), index + 1)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun String.skipWhitespaceAndComments(startIndex: Int): Int? {
        var index = startIndex
        while (index < length) {
            while (index < length && this[index].isWhitespace()) index++
            if (startsWith("//", index)) {
                index = indexOf('\n', index).takeIf { it != -1 } ?: length
                continue
            }
            if (startsWith("/*", index)) {
                index = nestedBlockCommentEnd(index) ?: return null
                continue
            }
            break
        }
        return index
    }

    private fun String.typeAliasParameterNames(): List<String> =
        splitTopLevelTypeParameters()
            .map { parameter ->
                parameter
                    .trim()
                    .substringBefore(':')
                    .removePrefix("out ")
                    .removePrefix("in ")
                    .trim()
                    .canonicalizeEscapedKotlinIdentifiers()
            }
            .filter(String::isNotEmpty)

    private fun String.splitTopLevelTypeParameters(): List<String> {
        if (isBlank()) return emptyList()
        val parameters = mutableListOf<String>()
        var parameterStart = 0
        var angleBracketDepth = 0
        var parenthesesDepth = 0
        var squareBracketDepth = 0
        for (index in indices) {
            when (this[index]) {
                '<' -> angleBracketDepth++
                '>' -> angleBracketDepth = (angleBracketDepth - 1).coerceAtLeast(0)
                '(' -> parenthesesDepth++
                ')' -> parenthesesDepth = (parenthesesDepth - 1).coerceAtLeast(0)
                '[' -> squareBracketDepth++
                ']' -> squareBracketDepth = (squareBracketDepth - 1).coerceAtLeast(0)
                ',' -> {
                    if (angleBracketDepth == 0 && parenthesesDepth == 0 && squareBracketDepth == 0) {
                        parameters.add(substring(parameterStart, index))
                        parameterStart = index + 1
                    }
                }
            }
        }
        parameters.add(substring(parameterStart))
        return parameters
    }

    @Suppress("ComplexCondition", "LoopWithTooManyJumpStatements")
    private fun String.readTypeAliasUnderlyingType(startIndex: Int): String {
        var index = startIndex
        val underlyingType = StringBuilder()
        var previousNonWhitespace: Char? = null
        var lastNonWhitespace: Char? = null
        var parenthesesDepth = 0
        var angleBracketDepth = 0
        var squareBracketDepth = 0

        while (index < length) {
            if (startsWith("//", index)) {
                if (parenthesesDepth == 0 && angleBracketDepth == 0 && squareBracketDepth == 0) {
                    return underlyingType.toString().trim()
                }
                index = indexOf('\n', index).takeIf { it != -1 } ?: length
                continue
            }
            if (startsWith("/*", index)) {
                val commentEnd = nestedBlockCommentEnd(index) ?: return underlyingType.toString().trim()
                if (underlyingType.needsCommentSeparator(commentEnd, this)) underlyingType.append(' ')
                index = commentEnd
                continue
            }

            val character = this[index]
            when (character) {
                '(' -> parenthesesDepth++
                ')' -> parenthesesDepth = (parenthesesDepth - 1).coerceAtLeast(0)
                '<' -> angleBracketDepth++
                '>' -> angleBracketDepth = (angleBracketDepth - 1).coerceAtLeast(0)
                '[' -> squareBracketDepth++
                ']' -> squareBracketDepth = (squareBracketDepth - 1).coerceAtLeast(0)
                ';' -> {
                    if (parenthesesDepth == 0 && angleBracketDepth == 0 && squareBracketDepth == 0) {
                        return underlyingType.toString().trim()
                    }
                }
                '\n', '\r' -> {
                    if (
                        parenthesesDepth == 0 &&
                        angleBracketDepth == 0 &&
                        squareBracketDepth == 0 &&
                        lastNonWhitespace != null &&
                        !hasTypeAliasContinuation(lastNonWhitespace, previousNonWhitespace, index + 1, underlyingType)
                    ) {
                        return underlyingType.toString().trim()
                    }
                }
            }
            underlyingType.append(character)
            if (!character.isWhitespace()) {
                previousNonWhitespace = lastNonWhitespace
                lastNonWhitespace = character
            }
            index++
        }
        return underlyingType.toString().trim()
    }

    private fun String.nestedBlockCommentEnd(startIndex: Int): Int? {
        var index = startIndex + 2
        var depth = 1
        while (index < length - 1) {
            when {
                startsWith("/*", index) -> {
                    depth++
                    index += 2
                }
                startsWith("*/", index) -> {
                    depth--
                    index += 2
                    if (depth == 0) return index
                }
                else -> index++
            }
        }
        return null
    }

    private fun StringBuilder.needsCommentSeparator(
        commentEnd: Int,
        sourceText: String,
    ): Boolean {
        if (isEmpty() || last().isWhitespace()) return false
        val nextCharacter = sourceText.getOrNull(commentEnd) ?: return false
        return last().isKotlinIdentifierCharacter() && nextCharacter.isKotlinIdentifierCharacter()
    }

    private fun Char.isKotlinIdentifierCharacter(): Boolean = isLetterOrDigit() || this == '_' || this == '`'

    private fun String.hasTypeAliasContinuation(
        lastNonWhitespace: Char,
        previousNonWhitespace: Char?,
        nextIndex: Int,
        capturedType: CharSequence,
    ): Boolean {
        if (lastNonWhitespace in "<(,[.") return true
        if (lastNonWhitespace == '>' && previousNonWhitespace == '-') return true

        var next = nextIndex
        while (next < length && this[next].isWhitespace()) next++
        if (capturedType.endsWithSuspendModifier() && getOrNull(next) == '(') return true
        return next < length && (this[next] in ".?&" || startsWith("->", next))
    }

    private fun CharSequence.endsWithSuspendModifier(): Boolean {
        var end = length - 1
        while (end >= 0 && this[end].isWhitespace()) end--
        val modifier = "suspend"
        if (end + 1 < modifier.length) return false
        val start = end - modifier.length + 1
        if (subSequence(start, end + 1).toString() != modifier) return false
        return start == 0 || !this[start - 1].isKotlinIdentifierCharacter()
    }

    private fun enclosingClassScopes(
        ktFile: KtFile,
        offset: Int,
    ): List<String> {
        val scopes = mutableListOf<String>()
        var element = ktFile.findElementAt(offset)
        while (element != null) {
            if (element is KtClassOrObject) {
                element.fqName?.asString()?.let(scopes::add)
            }
            element = element.parent
        }
        return scopes
    }
}

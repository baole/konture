/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

internal object TypeResolver {
    @Suppress("CyclomaticComplexMethod", "ReturnCount", "LongMethod")
    fun resolveRawType(
        typeText: String,
        context: TypeResolutionContext,
        resolvingAliases: Set<String> = emptySet(),
    ): String? {
        val normalizedType = typeText.trim().removeSuffix("?")
        normalizedType.functionTypeArity()?.let { return "kotlin.Function$it" }
        val rawType = normalizedType.substringBefore('<').trim().canonicalizeEscapedKotlinIdentifiers()
        if (rawType.isEmpty()) return null
        val firstSegment = rawType.substringBefore('.')
        val remainingSegments = rawType.removePrefix(firstSegment)
        val typeArguments = normalizedType.topLevelTypeArguments()

        fun resolveAlias(
            aliasFqName: String,
            memberSuffix: String = remainingSegments,
        ): String? {
            val definition = context.resolveTypeAlias(aliasFqName) ?: return null
            if (aliasFqName in resolvingAliases) return null
            if (definition.typeParameters.size != typeArguments.size) return null
            val expandedType =
                definition.typeParameters.zip(typeArguments).fold(definition.underlyingType) { type, (parameter, argument) ->
                    type.replace(Regex("\\b${Regex.escape(parameter)}\\b"), argument)
                }
            val aliasContext =
                TypeResolutionContext(
                    packageName = definition.packageName,
                    imports = definition.imports,
                    importAliases = definition.importAliases,
                    isClassDeclared = { requestedFqName ->
                        context.isClassDeclared(requestedFqName) ||
                            classScopedCandidates(requestedFqName, definition.packageName, definition.enclosingClassScopes)
                                .any(context.isClassDeclared)
                    },
                    resolveTypeAlias = { requestedFqName ->
                        classScopedCandidates(requestedFqName, definition.packageName, definition.enclosingClassScopes)
                            .firstNotNullOfOrNull(context.resolveTypeAlias) ?: context.resolveTypeAlias(requestedFqName)
                    },
                    resolveClassInScope = { requestedFqName ->
                        classScopedCandidates(requestedFqName, definition.packageName, definition.enclosingClassScopes)
                            .firstOrNull(context.isClassDeclared) ?: context.resolveClassInScope(requestedFqName)
                    },
                )
            return resolveRawType(
                expandedType + memberSuffix,
                aliasContext,
                resolvingAliases + aliasFqName,
            )
        }

        fun resolveCandidate(candidateFqName: String): String? =
            if (context.resolveTypeAlias(candidateFqName) != null) {
                resolveAlias(candidateFqName)
            } else {
                candidateFqName + remainingSegments
            }

        // Fully qualified aliases can be referenced without an import.
        if (context.resolveTypeAlias(rawType) != null) return resolveAlias(rawType, memberSuffix = "")
        if ('.' in rawType && context.packageName.isNotEmpty()) {
            val samePackageAlias = "${context.packageName}.$rawType"
            if (context.resolveTypeAlias(samePackageAlias) != null) {
                return resolveAlias(samePackageAlias, memberSuffix = "")
            }
        }

        val samePackageFqName = if (context.packageName.isNotEmpty()) "${context.packageName}.$firstSegment" else firstSegment
        if (context.resolveTypeAlias(samePackageFqName) != null) return resolveAlias(samePackageFqName)
        context.resolveClassInScope(samePackageFqName)?.let { return it + remainingSegments }

        // 1. Import Aliases
        context.importAliases[firstSegment]?.let { aliasTarget ->
            return resolveCandidate(aliasTarget)
        }

        // 2. Explicit Imports
        val explicitImports = context.imports.filter { !it.endsWith(".*") && it.substringAfterLast('.') == firstSegment }
        if (explicitImports.size == 1) {
            val importedType = explicitImports.single()
            return resolveCandidate(importedType)
        }
        if (explicitImports.size > 1) return null

        // 3. Local/Same Package Check (declared in package/project)
        if (context.isClassDeclared(samePackageFqName)) {
            return samePackageFqName + remainingSegments
        }

        // 4. Default Imports Check
        KotlinDefaultTypes.bySimpleName[firstSegment]?.let { return it + remainingSegments }

        // 5. Lowercase check (variable or primitive/generic)
        if (firstSegment.isNotEmpty() && firstSegment.first().isLowerCase()) return rawType

        // 6. Wildcard Imports Matching Declared Classes
        val wildcardImports = context.imports.filter { it.endsWith(".*") }.map { "${it.removeSuffix(".*")}.$firstSegment" }
        if (wildcardImports.isNotEmpty()) {
            val declaredWildcardMatches = wildcardImports.filter { context.isClassDeclared(it) || context.resolveTypeAlias(it) != null }
            if (declaredWildcardMatches.size == 1) {
                val wildcardType = declaredWildcardMatches.single()
                return resolveCandidate(wildcardType)
            }
            if (declaredWildcardMatches.size > 1) return null
            if (wildcardImports.size == 1) return wildcardImports.single() + remainingSegments
            return null
        }

        // 7. Identity cannot be established without a visible declaration.
        return null
    }

    private fun String.functionTypeArity(): Int? {
        val trimmedType = trim()
        val isSuspendFunction =
            trimmedType.startsWith("suspend") && trimmedType.getOrNull("suspend".length)?.isWhitespace() == true
        val type =
            if (isSuspendFunction) {
                trimmedType.removePrefix("suspend").trimStart()
            } else {
                trimmedType
            }
        val arrowIndex = type.topLevelFunctionArrowIndex()
        if (arrowIndex == -1) return null

        val parameters = type.substring(0, arrowIndex).trim()
        if (!parameters.endsWith(')')) return null
        val parameterStart = parameters.matchingOpeningParenthesis() ?: return null
        val parameterText = parameters.substring(parameterStart + 1, parameters.length - 1).trim()
        val declaredParameterCount = if (parameterText.isEmpty()) 0 else parameterText.topLevelParameterCount()
        val receiverPrefix = parameters.substring(0, parameterStart).trim()
        return declaredParameterCount +
            (if (receiverPrefix.endsWith('.')) 1 else 0) +
            (if (isSuspendFunction) 1 else 0)
    }

    private fun String.matchingOpeningParenthesis(): Int? {
        var depth = 0
        for (index in lastIndex downTo 0) {
            when (this[index]) {
                ')' -> depth++
                '(' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
        }
        return null
    }

    @Suppress("ComplexCondition")
    private fun String.topLevelFunctionArrowIndex(): Int {
        var parenthesesDepth = 0
        var angleBracketDepth = 0
        var squareBracketDepth = 0
        for (index in 0 until lastIndex) {
            when (this[index]) {
                '(' -> parenthesesDepth++
                ')' -> parenthesesDepth = (parenthesesDepth - 1).coerceAtLeast(0)
                '<' -> angleBracketDepth++
                '>' -> angleBracketDepth = (angleBracketDepth - 1).coerceAtLeast(0)
                '[' -> squareBracketDepth++
                ']' -> squareBracketDepth = (squareBracketDepth - 1).coerceAtLeast(0)
                '-' -> {
                    if (
                        this[index + 1] == '>' &&
                        parenthesesDepth == 0 &&
                        angleBracketDepth == 0 &&
                        squareBracketDepth == 0
                    ) {
                        return index
                    }
                }
            }
        }
        return -1
    }

    @Suppress("NestedBlockDepth")
    private fun String.topLevelParameterCount(): Int {
        var parenthesesDepth = 0
        var angleBracketDepth = 0
        var squareBracketDepth = 0
        var parameterCount = 0
        var containsParameterText = false
        for (character in this) {
            when (character) {
                '(' -> {
                    parenthesesDepth++
                    containsParameterText = true
                }
                ')' -> {
                    parenthesesDepth = (parenthesesDepth - 1).coerceAtLeast(0)
                    containsParameterText = true
                }
                '<' -> {
                    angleBracketDepth++
                    containsParameterText = true
                }
                '>' -> {
                    angleBracketDepth = (angleBracketDepth - 1).coerceAtLeast(0)
                    containsParameterText = true
                }
                '[' -> {
                    squareBracketDepth++
                    containsParameterText = true
                }
                ']' -> {
                    squareBracketDepth = (squareBracketDepth - 1).coerceAtLeast(0)
                    containsParameterText = true
                }
                ',' -> {
                    if (parenthesesDepth == 0 && angleBracketDepth == 0 && squareBracketDepth == 0) {
                        if (containsParameterText) parameterCount++
                        containsParameterText = false
                    }
                }
                else -> if (!character.isWhitespace()) containsParameterText = true
            }
        }
        return if (containsParameterText) parameterCount + 1 else parameterCount
    }

    private fun String.topLevelTypeArguments(): List<String> {
        val start = indexOf('<')
        if (start == -1) return emptyList()
        val end = lastIndexOf('>')
        if (end <= start) return emptyList()
        val arguments = mutableListOf<String>()
        var depth = 0
        var argumentStart = start + 1
        for (index in start + 1 until end) {
            when (this[index]) {
                '<' -> depth++
                '>' -> depth--
                ',' -> {
                    if (depth == 0) {
                        arguments.add(substring(argumentStart, index).trim())
                        argumentStart = index + 1
                    }
                }
            }
        }
        arguments.add(substring(argumentStart, end).trim())
        return arguments.filter(String::isNotEmpty)
    }
}

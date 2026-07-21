/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

internal class TypeResolutionContext(
    val packageName: String,
    val imports: List<String>,
    val importAliases: Map<String, String>,
    val isClassDeclared: (String) -> Boolean,
    val resolveTypeAlias: (String) -> TypeAliasDefinition?,
    val resolveClassInScope: (String) -> String? = { null },
) {
    fun withClassScope(classFqName: String): TypeResolutionContext {
        val classScopes = enclosingClassScopes(classFqName, packageName)

        val newIsClassDeclared = { requestedFqName: String ->
            isClassDeclared(requestedFqName) ||
                classScopedCandidates(requestedFqName, packageName, classScopes).any(isClassDeclared)
        }

        val newResolveClassInScope = { requestedFqName: String ->
            classScopedCandidates(requestedFqName, packageName, classScopes).firstOrNull(isClassDeclared)
                ?: resolveClassInScope(requestedFqName)
        }

        val newResolveTypeAlias = { requestedFqName: String ->
            classScopedCandidates(requestedFqName, packageName, classScopes)
                .firstNotNullOfOrNull(resolveTypeAlias) ?: resolveTypeAlias(requestedFqName)
        }

        return TypeResolutionContext(
            packageName = packageName,
            imports = imports,
            importAliases = importAliases,
            isClassDeclared = newIsClassDeclared,
            resolveTypeAlias = newResolveTypeAlias,
            resolveClassInScope = newResolveClassInScope,
        )
    }
}

internal fun enclosingClassScopes(
    classFqName: String,
    packageName: String,
): List<String> {
    val prefix = if (packageName.isEmpty()) "" else "$packageName."
    val classNames = classFqName.removePrefix(prefix).split('.').filter(String::isNotEmpty)
    return classNames.indices
        .reversed()
        .map { index -> (listOf(packageName).filter(String::isNotEmpty) + classNames.take(index + 1)).joinToString(".") }
}

internal fun classScopedCandidates(
    fqName: String,
    packageName: String,
    classScopes: List<String>,
): List<String> {
    if (fqName.substringBeforeLast('.', missingDelimiterValue = "") != packageName) return emptyList()
    val simpleName = fqName.substringAfterLast('.')
    return classScopes.map { "$it.$simpleName" }
}

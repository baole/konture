/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

internal class MapSymbolLookup(
    private val declaredClasses: Set<String>,
    private val typeAliases: Map<String, TypeAliasDefinition> = emptyMap(),
) : SymbolLookup {
    override fun isClassDeclared(fqName: String): Boolean = declaredClasses.contains(fqName)

    override fun resolveTypeAlias(fqName: String): TypeAliasDefinition? = typeAliases[fqName]
}

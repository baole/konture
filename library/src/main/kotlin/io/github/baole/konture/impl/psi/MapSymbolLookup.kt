/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

internal class MapSymbolLookup(
    private val declaredClasses: Set<String>,
    private val typeAliases: Map<String, TypeAliasDefinition> = emptyMap(),
) : SymbolLookup {
    override fun isClassDeclared(fqName: String): Boolean = declaredClasses.contains(fqName)

    override fun resolveTypeAlias(fqName: String): TypeAliasDefinition? = typeAliases[fqName]

    override fun lookupKey(): String = "${declaredClasses.hashCode()}_${typeAliases.hashCode()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapSymbolLookup) return false
        return declaredClasses == other.declaredClasses && typeAliases == other.typeAliases
    }

    override fun hashCode(): Int {
        var result = declaredClasses.hashCode()
        result = 31 * result + typeAliases.hashCode()
        return result
    }
}


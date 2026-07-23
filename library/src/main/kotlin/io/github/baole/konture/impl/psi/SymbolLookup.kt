/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

/** A lookup interface for symbols declared in the project. */
internal interface SymbolLookup {
    fun isClassDeclared(fqName: String): Boolean

    fun resolveTypeAlias(fqName: String): TypeAliasDefinition?
}

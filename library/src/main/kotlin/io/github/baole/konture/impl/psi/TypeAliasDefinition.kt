/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

internal data class TypeAliasDefinition(
    val underlyingType: String,
    val typeParameters: List<String>,
    val packageName: String,
    val imports: List<String>,
    val importAliases: Map<String, String>,
    val enclosingClassScopes: List<String>,
)

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

data class FileDeclaration(
    val name: String,
    val packageName: String,
    val imports: List<String> = emptyList(),
    val classes: List<ClassDeclaration> = emptyList(),
    val topLevelFunctions: List<FunctionDeclaration> = emptyList(),
    val topLevelProperties: List<PropertyDeclaration> = emptyList(),
    val kdocText: String? = null,
    val filePath: String = "",
    val importAliases: Map<String, String> = emptyMap(),
)

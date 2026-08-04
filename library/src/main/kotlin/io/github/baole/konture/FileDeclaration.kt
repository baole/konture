/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
    val sourceSets: List<SourceSetId> = emptyList(),
    val usages: List<SourceUsage> = emptyList(),
    val annotations: List<AnnotationDeclaration> = emptyList(),
)

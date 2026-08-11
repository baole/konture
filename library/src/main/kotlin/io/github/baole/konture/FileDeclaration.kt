/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Represents a source file declaration containing classes, functions, properties, and imports. */
public data class FileDeclaration(
    /** Filter or assertion criteria for name. */
    val name: String,
    /** Filter or assertion criteria for package name. */
    val packageName: String,
    /** Filter or assertion criteria for imports. */
    val imports: List<String> = emptyList(),
    /** Filter or assertion criteria for classes. */
    val classes: List<ClassDeclaration> = emptyList(),
    /** Filter or assertion criteria for top level functions. */
    val topLevelFunctions: List<FunctionDeclaration> = emptyList(),
    /** Filter or assertion criteria for top level properties. */
    val topLevelProperties: List<PropertyDeclaration> = emptyList(),
    /** Filter or assertion criteria for kdoc text. */
    val kdocText: String? = null,
    /** Filter or assertion criteria for file path. */
    val filePath: String = "",
    /** Filter or assertion criteria for import aliases. */
    val importAliases: Map<String, String> = emptyMap(),
    /** Filter or assertion criteria for source sets. */
    val sourceSets: List<SourceSetId> = emptyList(),
    /** Filter or assertion criteria for usages. */
    val usages: List<SourceUsage> = emptyList(),
)

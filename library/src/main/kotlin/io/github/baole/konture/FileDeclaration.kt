/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlinx.serialization.Serializable

/** Represents a source file declaration containing classes, functions, properties, and imports. */
@Serializable
public data class FileDeclaration(
    /** Filter or assertion criteria for name. */
    public val name: String,
    /** Filter or assertion criteria for package name. */
    public val packageName: String,
    /** Filter or assertion criteria for imports. */
    public val imports: List<String> = emptyList(),
    /** Filter or assertion criteria for classes. */
    public val classes: List<ClassDeclaration> = emptyList(),
    /** Filter or assertion criteria for top level functions. */
    public val topLevelFunctions: List<FunctionDeclaration> = emptyList(),
    /** Filter or assertion criteria for top level properties. */
    public val topLevelProperties: List<PropertyDeclaration> = emptyList(),
    /** Filter or assertion criteria for kdoc text. */
    public val kdocText: String? = null,
    /** Filter or assertion criteria for file path. */
    public val filePath: String = "",
    /** Filter or assertion criteria for import aliases. */
    public val importAliases: Map<String, String> = emptyMap(),
    /** Filter or assertion criteria for source sets. */
    public val sourceSets: List<SourceSetId> = emptyList(),
    /** Filter or assertion criteria for usages. */
    public val usages: List<SourceUsage> = emptyList(),
    /** Filter or assertion criteria for file-level annotations. */
    public val annotations: List<AnnotationDeclaration> = emptyList(),
)

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed Kotlin source file.
 *
 * @property name The filename (e.g., `UserService.kt`).
 * @property packageName The package declaration name in the file.
 * @property imports List of exact import directives in this file.
 * @property classes List of top-level classes, interfaces, or objects declared in this file.
 * @property topLevelFunctions List of top-level functions declared in this file.
 * @property topLevelProperties List of top-level properties declared in this file.
 * @property kdocText Raw text content of file-level KDoc, or null if un-documented.
 * @property filePath Absolute file system path of this source file.
 * @property importAliases Map of local import aliases to their fully-qualified names.
 * @property sourceSets Source set identifiers this file belongs to.
 * @property usages List of code usages (calls and references) originating within this file.
 * @property annotations List of file-level annotations declared on this file.
 */
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

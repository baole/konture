/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Context wrapper for verifying class declarations.
 *
 * Provides both the target [declaration] and architectural metadata to easily query scope.
 *
 * @property declaration The underlying [ClassDeclaration] AST model representing the class.
 * @property packageName The fully-qualified name of the package containing this class.
 * @property modulePath The module subdirectory/path containing this class.
 * @property filePath The project relative path to the source file defining this class.
 * @property sourceSet The source set this class was selected from, or null if unknown.
 * @property usages Source usages originating within this class declaration.
 */
data class ClassDeclarationContext(
    val declaration: ClassDeclaration,
    val packageName: String,
    val modulePath: String,
    val filePath: String,
    val sourceSet: SourceSetId? = null,
    val usages: List<SourceUsage> = emptyList(),
) {
    /**
     * The simple name of the class (delegated to [ClassDeclaration.name]).
     */
    val name: String
        get() = declaration.name

    /**
     * The fully-qualified name of the class (delegated to [ClassDeclaration.fqName]).
     */
    val fqName: String
        get() = declaration.fqName

    /**
     * Set of simple/qualified types referenced within this class body.
     */
    val referencedTypes: Set<String>
        get() = declaration.referencedTypes

    /**
     * List of import directives in the file defining this class.
     */
    val imports: List<String>
        get() = declaration.imports

    /**
     * List of supertypes implemented or extended by this class.
     */
    val supertypes: List<String>
        get() = declaration.supertypes

    /**
     * Annotations declared directly on this class.
     */
    val annotations: List<AnnotationDeclaration>
        get() = declaration.annotations

    /**
     * Source line number where this class is defined.
     */
    val sourceLine: Int
        get() = declaration.sourceLine

    /**
     * Helper to check if this class depends on another class.
     */
    fun dependsOn(target: ClassDeclarationContext): Boolean = declaration.dependsOn(target.declaration)

    /**
     * Helper to check if this class depends on target [ClassDeclaration].
     */
    fun dependsOn(target: ClassDeclaration): Boolean = declaration.dependsOn(target)
}

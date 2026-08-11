/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Context wrapper for verifying source file declarations.
 *
 * Provides both the target [declaration] and architectural metadata to easily query scope.
 *
 * @property declaration The underlying [FileDeclaration] AST model representing the source file.
 * @property modulePath The module subdirectory/path containing this file.
 */
data class FileDeclarationContext(
    /** Filter or assertion criteria for declaration. */
    val declaration: FileDeclaration,
    /** Filter or assertion criteria for module path. */
    val modulePath: String,
    /** Filter or assertion criteria for source set. */
    val sourceSet: SourceSetId? = null,
)

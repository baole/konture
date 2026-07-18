/*
 * Copyright 2026 Bao Le Duc
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
    val declaration: FileDeclaration,
    val modulePath: String,
    val sourceSet: SourceSetId? = null,
)

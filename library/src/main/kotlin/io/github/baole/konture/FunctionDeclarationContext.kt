/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Context wrapper for verifying function declarations.
 *
 * Provides both the target [declaration] and architectural metadata to easily query scope.
 *
 * @property declaration The underlying [FunctionDeclaration] AST model representing the function.
 * @property packageName The fully-qualified name of the package containing this function.
 * @property className The name of the surrounding class if this function is a member/nested function, or null if it's top-level.
 * @property modulePath The module subdirectory/path containing this function.
 * @property filePath The project relative path to the source file defining this function.
 */
data class FunctionDeclarationContext(
    val declaration: FunctionDeclaration,
    val packageName: String,
    val className: String?,
    val modulePath: String,
    val filePath: String,
    val sourceSet: SourceSetId? = null,
    val usages: List<SourceUsage> = emptyList(),
)

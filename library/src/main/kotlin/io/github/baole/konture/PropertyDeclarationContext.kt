/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Context wrapper for verifying property declarations.
 *
 * Provides both the target [declaration] and architectural metadata to easily query scope.
 *
 * @property declaration The underlying [PropertyDeclaration] AST model representing the property.
 * @property packageName The fully-qualified name of the package containing this property.
 * @property className The name of the surrounding class if this property is a member/nested property, or null if it's top-level.
 * @property modulePath The module subdirectory/path containing this property.
 * @property filePath The project relative path to the source file defining this property.
 */
data class PropertyDeclarationContext(
    val declaration: PropertyDeclaration,
    val packageName: String,
    val className: String?,
    val modulePath: String,
    val filePath: String,
)

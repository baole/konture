/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
 * @property sourceSet The source set this function was selected from, or null if unknown.
 * @property usages List of code usages (calls and references) originating within this function body.
 */
data class FunctionDeclarationContext(
    val declaration: FunctionDeclaration,
    val packageName: String,
    val className: String?,
    val modulePath: String,
    val filePath: String,
    val sourceSet: SourceSetId? = null,
    val usages: List<SourceUsage> = emptyList(),
) {
    /**
     * The fully-qualified name of the function, combining package, enclosing class (if any), and
     * simple name (e.g. `com.acme.UserService.getUser`), used to identify it unambiguously in
     * violation messages.
     */
    val qualifiedName: String
        get() = listOfNotNull(packageName.ifEmpty { null }, className, declaration.name).joinToString(".")
}

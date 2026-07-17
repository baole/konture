/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed Kotlin annotation declared on a class.
 *
 * @property name The simple name of the annotation (e.g., `UseCase`).
 * @property fqName The fully qualified name of the annotation if resolvable, or its simple name (e.g., `com.acme.annotations.UseCase`).
 * @property arguments List of arguments declared on the annotation.
 */
data class AnnotationDeclaration(
    val name: String,
    val fqName: String,
    val arguments: List<AnnotationArgumentDeclaration> = emptyList(),
)

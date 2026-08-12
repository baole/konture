/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
public data class AnnotationDeclaration(
    /** Filter or assertion criteria for name. */
    public val name: String,
    /** Filter or assertion criteria for fq name. */
    public val fqName: String,
    /** Filter or assertion criteria for arguments. */
    public val arguments: List<AnnotationArgumentDeclaration> = emptyList(),
)

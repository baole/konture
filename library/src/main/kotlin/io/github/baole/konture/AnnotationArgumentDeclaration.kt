/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Represents an argument passed to a Kotlin annotation. */
public data class AnnotationArgumentDeclaration(
    /** Filter or assertion criteria for name. */
    val name: String?,
    /** Filter or assertion criteria for value. */
    val value: String,
)

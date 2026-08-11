/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain), Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * A group of packages sharing the same slice key, derived from a slice pattern's capture group.
 *
 * Slices are the unit over which package-level relationships such as cycle-freedom and mutual
 * isolation are asserted. For the pattern `com.acme.(*)..`, the packages `com.acme.payment` and
 * `com.acme.payment.api` both produce the key `payment` and therefore belong to the same slice.
 *
 * @property key The captured slice key that identifies this slice.
 * @property packages The package names that belong to this slice.
 * @property classes The classes contained in this slice's packages.
 */
data class Slice(
    /** Filter or assertion criteria for key. */
    val key: String,
    /** Filter or assertion criteria for packages. */
    val packages: Set<String>,
    /** Filter or assertion criteria for classes. */
    val classes: List<ClassDeclaration>,
)

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Top-level entry point for defining and running architecture rule blocks.
 *
 * This function is an ergonomic shorthand for [Konture.architecture].
 *
 * @param block DSL configuration block scoped to [KontureContext].
 */
public fun architecture(block: KontureContext.() -> Unit) {
    Konture.architecture(block)
}

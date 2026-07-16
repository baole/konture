/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Top-level entry point for defining and running architecture rule blocks.
 *
 * This serves as a top-level compatibility and quickstart facade.
 */
fun architecture(block: KontureContext.() -> Unit) {
    Konture.architecture(block)
}

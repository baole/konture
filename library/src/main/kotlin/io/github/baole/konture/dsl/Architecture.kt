package io.github.baole.konture.dsl

import io.github.baole.konture.Konture
import io.github.baole.konture.KontureContext
import io.github.baole.konture.architecture

/**
 * Top-level entry point for defining and running architecture rule blocks.
 *
 * This serves as a top-level compatibility and quickstart facade.
 */
fun architecture(block: KontureContext.() -> Unit) {
    Konture.architecture(block)
}

package io.github.baole.konture

/**
 * DSL Marker for Konture DSL structures.
 *
 * Ensures proper scoping and blocks nested builders from implicitly accessing parent builders.
 */
@DslMarker
annotation class KontureDsl

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Type-safe, nested DSL for defining and verifying layered architecture rules.
 *
 * ### Example Usage:
 * ```kotlin
 * Konture.layered {
 *     val presentation = layer("presentation") definedBy "io.github.baole.konture.presentation.."
 *     val domain = layer("domain") definedBy "io.github.baole.konture.domain.."
 *     val data = layer("data") definedBy "io.github.baole.konture.data.."
 *
 *     where(presentation) {
 *         mayOnlyAccessLayers(domain)
 *     }
 *     where(domain) {
 *         mayNotBeAccessedByAnyLayer()
 *     }
 * }
 * ```
 */
@KontureDsl
class LayeredArchitectureDsl(
    private val projectGraph: ProjectGraph,
) {
    private val builder = LayeredArchitectureBuilder(projectGraph)

    /**
     * Represents a defined architectural layer.
     *
     * @property name The human-readable name of the layer.
     */
    class Layer(
        val name: String,
    )

    /**
     * Starts defining an architectural layer with the specified name.
     */
    fun layer(name: String): LayerSpec = LayerSpec(name)

    /**
     * Specification helper for associating a layer definition with a package/module pattern.
     */
    inner class LayerSpec(
        val name: String,
    ) {
        /**
         * Defines the package or module pattern that belongs to this layer.
         *
         * @param pattern An ant-style or package wildcard pattern (e.g. `com.example.service..`).
         * @return The completed [Layer] reference.
         */
        infix fun definedBy(pattern: String): Layer {
            builder.layer(name).definedBy(pattern)
            return Layer(name)
        }
    }

    /**
     * Specifies validation constraints for a given layer.
     */
    fun where(
        layer: Layer,
        block: ConstraintSpec.() -> Unit,
    ) {
        ConstraintSpec(layer.name).apply(block)
    }

    /**
     * DSL helper for specifying directional accessibility constraints on a layer.
     */
    inner class ConstraintSpec(
        val layerName: String,
    ) {
        /**
         * Asserts that no other defined layer in the system is allowed to access this layer.
         */
        fun mayNotBeAccessedByAnyLayer() {
            builder.whereLayer(layerName).mayNotBeAccessedByAnyLayer()
        }

        /**
         * Asserts that this layer can only be accessed by the specified subset of layers.
         */
        fun mayOnlyBeAccessedByLayers(allowedLayers: List<Layer>) {
            val names = allowedLayers.map { it.name }
            builder.whereLayer(layerName).mayOnlyBeAccessedByLayers(names)
        }

        /**
         * Asserts that this layer can only be accessed by the specified subset of layers.
         */
        fun mayOnlyBeAccessedByLayers(vararg allowedLayers: Layer) {
            mayOnlyBeAccessedByLayers(allowedLayers.toList())
        }

        /**
         * Asserts that this layer is only allowed to access the specified subset of layers.
         */
        fun mayOnlyAccessLayers(allowedLayers: List<Layer>) {
            val names = allowedLayers.map { it.name }
            builder.whereLayer(layerName).mayOnlyAccessLayers(names)
        }

        /**
         * Asserts that this layer is only allowed to access the specified subset of layers.
         */
        fun mayOnlyAccessLayers(vararg allowedLayers: Layer) {
            mayOnlyAccessLayers(allowedLayers.toList())
        }
    }

    internal fun verify() {
        builder.check()
    }
}

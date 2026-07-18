/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.LayerConstraint
import io.github.baole.konture.impl.LayerDefinition
import io.github.baole.konture.impl.PatternMatchers

/**
 * A builder class implementing the Layered Architecture DSL.
 */
class LayeredArchitectureBuilder(
    private val graph: ProjectGraph = Konture.projectGraph,
) {
    private val layers = mutableMapOf<String, LayerDefinition>()
    private val constraints = mutableListOf<LayerConstraint>()

    fun layer(name: String): LayerSpec = LayerSpec(this, name)

    class LayerSpec internal constructor(
        private val builder: LayeredArchitectureBuilder,
        private val name: String,
    ) {
        infix fun definedBy(packagePatterns: List<String>): LayeredArchitectureBuilder {
            builder.layers[name] = LayerDefinition(name, packagePatterns)
            return builder
        }

        fun definedBy(vararg packagePatterns: String): LayeredArchitectureBuilder = definedBy(packagePatterns.toList())

        infix fun definedBy(packagePattern: String): LayeredArchitectureBuilder = definedBy(listOf(packagePattern))
    }

    fun whereLayer(name: String): ConstraintSpec = ConstraintSpec(this, name)

    class ConstraintSpec internal constructor(
        private val builder: LayeredArchitectureBuilder,
        private val name: String,
    ) {
        fun mayNotBeAccessedByAnyLayer(): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        val layerDef = layers[name] ?: return
                        // Find all classes in this layer
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        // For each class in this layer, find other classes in other defined layers that access it
                        for (targetCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == targetCls.fqName) continue
                                // Is otherCls in any of our defined layers?
                                val otherLayer =
                                    layers.values.find { def ->
                                        def.packagePatterns.any { pattern ->
                                            PatternMatchers.matchesPackage(pattern, otherCls.packageName)
                                        }
                                    }
                                if (otherLayer != null && otherLayer.name != name) {
                                    if (otherCls.dependsOn(targetCls)) {
                                        violations.add(
                                            "Layer '$name' may not be accessed by any layer, but class ${otherCls.fqName} in layer '${otherLayer.name}' depends on ${targetCls.fqName}",
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
            )
            return builder
        }

        infix fun mayOnlyBeAccessedByLayers(allowedLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        val layerDef = layers[name] ?: return
                        val allowedSet = allowedLayerNames.toSet()
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (targetCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == targetCls.fqName) continue
                                val otherLayer =
                                    layers.values.find { def ->
                                        def.packagePatterns.any { pattern ->
                                            PatternMatchers.matchesPackage(pattern, otherCls.packageName)
                                        }
                                    }
                                if (otherLayer != null && otherLayer.name != name) {
                                    if (!allowedSet.contains(otherLayer.name)) {
                                        if (otherCls.dependsOn(targetCls)) {
                                            violations.add(
                                                "Layer '$name' may only be accessed by layers [${allowedLayerNames.joinToString()}], but class ${otherCls.fqName} in layer '${otherLayer.name}' depends on ${targetCls.fqName}",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            )
            return builder
        }

        fun mayOnlyBeAccessedByLayers(vararg allowedLayerNames: String): LayeredArchitectureBuilder =
            mayOnlyBeAccessedByLayers(allowedLayerNames.toList())

        infix fun mayOnlyBeAccessedByLayers(allowedLayerName: String): LayeredArchitectureBuilder =
            mayOnlyBeAccessedByLayers(listOf(allowedLayerName))

        infix fun mayOnlyAccessLayers(allowedLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        val layerDef = layers[name] ?: return
                        val allowedSet = allowedLayerNames.toSet()
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (sourceCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == sourceCls.fqName) continue
                                val otherLayer =
                                    layers.values.find { def ->
                                        def.packagePatterns.any { pattern ->
                                            PatternMatchers.matchesPackage(pattern, otherCls.packageName)
                                        }
                                    }
                                if (otherLayer != null && otherLayer.name != name) {
                                    if (!allowedSet.contains(otherLayer.name)) {
                                        if (sourceCls.dependsOn(otherCls)) {
                                            violations.add(
                                                "Layer '$name' may only access layers [${allowedLayerNames.joinToString()}], but class ${sourceCls.fqName} depends on ${otherCls.fqName} in layer '${otherLayer.name}' (at ${sourceCls.filePath})",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            )
            return builder
        }

        fun mayOnlyAccessLayers(vararg allowedLayerNames: String): LayeredArchitectureBuilder = mayOnlyAccessLayers(allowedLayerNames.toList())

        infix fun mayOnlyAccessLayers(allowedLayerName: String): LayeredArchitectureBuilder = mayOnlyAccessLayers(listOf(allowedLayerName))

        infix fun mayNotAccessLayers(forbiddenLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        val layerDef = layers[name] ?: return
                        val forbiddenSet = forbiddenLayerNames.toSet()
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (sourceCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == sourceCls.fqName) continue
                                val otherLayer =
                                    layers.values.find { def ->
                                        def.packagePatterns.any { pattern ->
                                            PatternMatchers.matchesPackage(pattern, otherCls.packageName)
                                        }
                                    }
                                if (otherLayer != null && otherLayer.name != name) {
                                    if (forbiddenSet.contains(otherLayer.name)) {
                                        if (sourceCls.dependsOn(otherCls)) {
                                            violations.add(
                                                "Layer '$name' may not access layers [${forbiddenLayerNames.joinToString()}], but class ${sourceCls.fqName} depends on ${otherCls.fqName} in layer '${otherLayer.name}' (at ${sourceCls.filePath})",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            )
            return builder
        }

        fun mayNotAccessLayers(vararg forbiddenLayerNames: String): LayeredArchitectureBuilder = mayNotAccessLayers(forbiddenLayerNames.toList())

        infix fun mayNotAccessLayers(forbiddenLayerName: String): LayeredArchitectureBuilder = mayNotAccessLayers(listOf(forbiddenLayerName))

        infix fun mayNotBeAccessedByLayers(forbiddenLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        val layerDef = layers[name] ?: return
                        val forbiddenSet = forbiddenLayerNames.toSet()
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (targetCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == targetCls.fqName) continue
                                val otherLayer =
                                    layers.values.find { def ->
                                        def.packagePatterns.any { pattern ->
                                            PatternMatchers.matchesPackage(pattern, otherCls.packageName)
                                        }
                                    }
                                if (otherLayer != null && otherLayer.name != name) {
                                    if (forbiddenSet.contains(otherLayer.name)) {
                                        if (otherCls.dependsOn(targetCls)) {
                                            violations.add(
                                                "Layer '$name' may not be accessed by layers [${forbiddenLayerNames.joinToString()}], but class ${otherCls.fqName} in layer '${otherLayer.name}' depends on ${targetCls.fqName} (at ${otherCls.filePath})",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            )
            return builder
        }

        fun mayNotBeAccessedByLayers(vararg forbiddenLayerNames: String): LayeredArchitectureBuilder =
            mayNotBeAccessedByLayers(forbiddenLayerNames.toList())

        infix fun mayNotBeAccessedByLayers(forbiddenLayerName: String): LayeredArchitectureBuilder =
            mayNotBeAccessedByLayers(listOf(forbiddenLayerName))
    }

    fun check(g: ProjectGraph = graph) {
        val allClasses = g.getAllModules().flatMap { it.classes }
        val violations = mutableListOf<String>()

        for (constraint in constraints) {
            constraint.verify(layers, allClasses, violations)
        }

        if (violations.isNotEmpty()) {
            BaselineManager.handleViolations(
                violations,
                "Layered architecture violation(s) detected:",
            )
        }
    }
}

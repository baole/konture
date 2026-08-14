/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.LayerConstraint
import io.github.baole.konture.impl.LayerDefinition
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation

/**
 * A builder class implementing the Layered Architecture DSL.
 */
public class LayeredArchitectureBuilder(
    private val graph: ProjectGraph = Konture.projectGraph,
) {
    private val layers = mutableMapOf<String, LayerDefinition>()
    private val constraints = mutableListOf<LayerConstraint>()

    /** Defines a new architectural layer with [name]. */
    public fun layer(name: String): LayerSpec = LayerSpec(this, name)

    /** Specification for defining package boundaries of an architectural layer. */
    public class LayerSpec internal constructor(
        private val builder: LayeredArchitectureBuilder,
        private val name: String,
    ) {
        /** Defines package boundaries for this layer using a list of package patterns. */
        public infix fun definedBy(packagePatterns: List<String>): LayeredArchitectureBuilder {
            builder.layers[name] = LayerDefinition(name, packagePatterns)
            return builder
        }

        /** Defines package boundaries for this layer using vararg package patterns. */
        public fun definedBy(vararg packagePatterns: String): LayeredArchitectureBuilder =
            definedBy(packagePatterns.toList())

        /** Defines package boundaries for this layer using a single package pattern. */
        public infix fun definedBy(packagePattern: String): LayeredArchitectureBuilder =
            definedBy(listOf(packagePattern))
    }

    /** Specifies constraints for the layer identified by [name]. */
    public fun whereLayer(name: String): ConstraintSpec = ConstraintSpec(this, name)

    /** Specification for defining dependency constraints on an architectural layer. */
    public class ConstraintSpec internal constructor(
        private val builder: LayeredArchitectureBuilder,
        private val name: String,
    ) {
        /** Constrains this layer so it may not be accessed by any other layer. */
        public fun mayNotBeAccessedByAnyLayer(): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        /** Filter or assertion criteria for layer def. */
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
                                            io.github.baole.konture.i18n.getMessage(
                                                "layered.architecture.mayNotBeAccessed",
                                                name,
                                                otherCls.fqName,
                                                otherLayer.name,
                                                targetCls.fqName,
                                                ViolationLocation.format(otherCls),
                                            ),
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

        /** Constrains this layer so it may only be accessed by layers in [allowedLayerNames]. */
        public infix fun mayOnlyBeAccessedByLayers(allowedLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        /** Filter or assertion criteria for layer def. */
                        val layerDef = layers[name] ?: return

                        /** Filter or assertion criteria for allowed set. */
                        val allowedSet = allowedLayerNames.toSet()

                        /** Filter or assertion criteria for layer classes. */
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (targetCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == targetCls.fqName) continue
                                /** Filter or assertion criteria for other layer. */
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
                                                io.github.baole.konture.i18n.getMessage(
                                                    "layered.architecture.mayOnlyBeAccessed",
                                                    name,
                                                    allowedLayerNames.joinToString(),
                                                    otherCls.fqName,
                                                    otherLayer.name,
                                                    targetCls.fqName,
                                                    ViolationLocation.format(otherCls),
                                                ),
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

        /** Constrains this layer so it may only be accessed by vararg layers [allowedLayerNames]. */
        public fun mayOnlyBeAccessedByLayers(vararg allowedLayerNames: String): LayeredArchitectureBuilder =
            mayOnlyBeAccessedByLayers(allowedLayerNames.toList())

        /** Constrains this layer so it may only be accessed by layer [allowedLayerName]. */
        public infix fun mayOnlyBeAccessedByLayers(allowedLayerName: String): LayeredArchitectureBuilder =
            mayOnlyBeAccessedByLayers(listOf(allowedLayerName))

        /** Constrains this layer so it may only access layers in [allowedLayerNames]. */
        public infix fun mayOnlyAccessLayers(allowedLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        /** Filter or assertion criteria for layer def. */
                        val layerDef = layers[name] ?: return

                        /** Filter or assertion criteria for allowed set. */
                        val allowedSet = allowedLayerNames.toSet()

                        /** Filter or assertion criteria for layer classes. */
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (sourceCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == sourceCls.fqName) continue
                                /** Filter or assertion criteria for other layer. */
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
                                                io.github.baole.konture.i18n.getMessage(
                                                    "layered.architecture.mayOnlyAccess",
                                                    name,
                                                    allowedLayerNames.joinToString(),
                                                    sourceCls.fqName,
                                                    otherCls.fqName,
                                                    otherLayer.name,
                                                    ViolationLocation.format(sourceCls),
                                                ),
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

        /** Constrains this layer so it may only access vararg layers [allowedLayerNames]. */
        public fun mayOnlyAccessLayers(vararg allowedLayerNames: String): LayeredArchitectureBuilder =
            mayOnlyAccessLayers(
                allowedLayerNames.toList(),
            )

        /** Constrains this layer so it may only access layer [allowedLayerName]. */
        public infix fun mayOnlyAccessLayers(allowedLayerName: String): LayeredArchitectureBuilder =
            mayOnlyAccessLayers(
                listOf(allowedLayerName),
            )

        /** Constrains this layer so it may not access layers in [forbiddenLayerNames]. */
        public infix fun mayNotAccessLayers(forbiddenLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        /** Filter or assertion criteria for layer def. */
                        val layerDef = layers[name] ?: return

                        /** Filter or assertion criteria for forbidden set. */
                        val forbiddenSet = forbiddenLayerNames.toSet()

                        /** Filter or assertion criteria for layer classes. */
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (sourceCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == sourceCls.fqName) continue
                                /** Filter or assertion criteria for other layer. */
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
                                                io.github.baole.konture.i18n.getMessage(
                                                    "layered.architecture.mayNotAccess",
                                                    name,
                                                    forbiddenLayerNames.joinToString(),
                                                    sourceCls.fqName,
                                                    otherCls.fqName,
                                                    otherLayer.name,
                                                    ViolationLocation.format(sourceCls),
                                                ),
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

        /** Constrains this layer so it may not access vararg layers [forbiddenLayerNames]. */
        public fun mayNotAccessLayers(vararg forbiddenLayerNames: String): LayeredArchitectureBuilder =
            mayNotAccessLayers(
                forbiddenLayerNames.toList(),
            )

        /** Constrains this layer so it may not access layer [forbiddenLayerName]. */
        public infix fun mayNotAccessLayers(forbiddenLayerName: String): LayeredArchitectureBuilder =
            mayNotAccessLayers(
                listOf(forbiddenLayerName),
            )

        /** Constrains this layer so it may not be accessed by layers in [forbiddenLayerNames]. */
        public infix fun mayNotBeAccessedByLayers(forbiddenLayerNames: List<String>): LayeredArchitectureBuilder {
            builder.constraints.add(
                object : LayerConstraint {
                    override fun verify(
                        layers: Map<String, LayerDefinition>,
                        allClasses: List<ClassDeclaration>,
                        violations: MutableList<String>,
                    ) {
                        /** Filter or assertion criteria for layer def. */
                        val layerDef = layers[name] ?: return

                        /** Filter or assertion criteria for forbidden set. */
                        val forbiddenSet = forbiddenLayerNames.toSet()

                        /** Filter or assertion criteria for layer classes. */
                        val layerClasses =
                            allClasses.filter { cls ->
                                layerDef.packagePatterns.any { pattern ->
                                    PatternMatchers.matchesPackage(pattern, cls.packageName)
                                }
                            }
                        for (targetCls in layerClasses) {
                            for (otherCls in allClasses) {
                                if (otherCls.fqName == targetCls.fqName) continue
                                /** Filter or assertion criteria for other layer. */
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
                                                io.github.baole.konture.i18n.getMessage(
                                                    "layered.architecture.mayNotBeAccessedBy",
                                                    name,
                                                    forbiddenLayerNames.joinToString(),
                                                    otherCls.fqName,
                                                    otherLayer.name,
                                                    targetCls.fqName,
                                                    ViolationLocation.format(otherCls),
                                                ),
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

        /** Constrains this layer so it may not be accessed by vararg layers [forbiddenLayerNames]. */
        public fun mayNotBeAccessedByLayers(vararg forbiddenLayerNames: String): LayeredArchitectureBuilder =
            mayNotBeAccessedByLayers(forbiddenLayerNames.toList())

        /** Constrains this layer so it may not be accessed by layer [forbiddenLayerName]. */
        public infix fun mayNotBeAccessedByLayers(forbiddenLayerName: String): LayeredArchitectureBuilder =
            mayNotBeAccessedByLayers(listOf(forbiddenLayerName))
    }

    /** Checks all configured layered architecture rules against [g]. */
    public fun check(g: ProjectGraph = graph) {
        /** Filter or assertion criteria for all classes. */
        val allClasses = g.getAllModules().flatMap { it.classes }

        /** Filter or assertion criteria for run check. */
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        val activeHeader =
            currentMeta?.description
                ?: io.github.baole.konture.i18n.getMessage("layered.architecture.violationHeader")

        val runCheck = { list: MutableList<String> ->
            for (constraint in constraints) {
                constraint.verify(layers, allClasses, list)
            }
        }

        BaselineManager.checkRule(
            activeHeader,
            runCheck,
        )
    }
}

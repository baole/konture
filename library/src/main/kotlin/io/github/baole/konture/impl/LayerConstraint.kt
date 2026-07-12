package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration

/**
 * Interface representing a dependency constraint on a layer.
 */
internal interface LayerConstraint {
    fun verify(
        layers: Map<String, LayerDefinition>,
        allClasses: List<ClassDeclaration>,
        violations: MutableList<String>,
    )
}

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents the resolved external/transitive dependency graph for the entire project.
 * Stored in `dependencies.json` and lazy-loaded by the runtime library.
 *
 * @property schemaVersion Integer version of the dependency graph schema.
 * @property modules Map of Gradle module paths (e.g. ":app", ":library") to their resolved external dependencies.
 */
@Serializable
data class DependencyGraphModel(
    val schemaVersion: Int = 1,
    val modules: Map<String, List<ResolvedDependencyModel>> = emptyMap(),
)

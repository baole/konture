package io.github.baole.konture.impl

/**
 * Unique key identifying a module inside a composite build setup.
 *
 * @property buildId The ID of the build (e.g., ":").
 * @property path The unique Gradle project path (e.g., ":domain").
 */
internal data class ModuleKey(
    val buildId: String,
    val path: String,
)

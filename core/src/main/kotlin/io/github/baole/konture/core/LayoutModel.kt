/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * The top-level root data model serialized into `layout.json` by the Gradle plugin.
 * This represents the entire ground-truth project topology and acts as the schema contract
 * between the build-time Gradle plugin and the test-time runtime.
 *
 * @property schemaVersion Integer version of the layout schema, used to prevent version mismatch between plugin
 * and runtime.
 * @property builds List of builds captured in this project (supports multi-project and included/composite builds).
 * @property exclusions Optional exclusion rules configured at build-time.
 */
@Serializable
data class LayoutModel(
    val schemaVersion: Int,
    val builds: List<BuildModel>,
    val exclusions: ExclusionsModel = ExclusionsModel(),
    val logLevel: String = "INFO",
) {
    companion object {
        /** Current version of the serialized Konture layout schema. */
        const val CURRENT_SCHEMA_VERSION: Int = 2
    }
}

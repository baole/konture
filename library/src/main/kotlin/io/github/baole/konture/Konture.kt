/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.ProjectGraphLoader

/**
 * Main entry point for Konture. All architecture assertion builders, scoping builders,
 * and graph configurations are extended from or accessible through this object.
 */
object Konture {
    /**
     * Lazily and thread-safely loads the [ProjectGraph] from the default resource path on first use,
     * or retrieves the default graph if already initialized.
     */
    val projectGraph: ProjectGraph
        get() =
            if (ProjectGraph.isDefaultInitialized()) {
                ProjectGraph.getDefault()
            } else {
                lazyGraph
            }

    private val lazyGraph: ProjectGraph by lazy {
        ProjectGraphLoader.loadFromResource()
    }
}

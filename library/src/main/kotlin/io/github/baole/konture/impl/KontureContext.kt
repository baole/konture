/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ProjectGraph

internal class KontureContext(
    val baselinePath: String = "konture-baseline.json",
    val generateBaseline: Boolean = false,
    val projectGraph: ProjectGraph? = null,
    val baselineManager: BaselineManager = BaselineManager(),
) {
    val projectGraphLoader: ProjectGraphLoader = ProjectGraphLoader()

    fun copy(
        baselinePath: String = this.baselinePath,
        generateBaseline: Boolean = this.generateBaseline,
        projectGraph: ProjectGraph? = this.projectGraph,
    ): KontureContext {
        return KontureContext(
            baselinePath = baselinePath,
            generateBaseline = generateBaseline,
            projectGraph = projectGraph,
            baselineManager = this.baselineManager,
        )
    }
}

internal object KontureContextProvider {
    private val threadLocalContext = ThreadLocal.withInitial { KontureContext() }

    var currentContext: KontureContext
        get() = threadLocalContext.get()
        set(value) {
            threadLocalContext.set(value)
        }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun reset() {
        try {
            threadLocalContext.get()?.baselineManager?.resetForTest()
        } catch (e: Exception) {
            // Ignore
        }
        threadLocalContext.set(KontureContext())
    }

    inline fun <T> runWithContext(
        context: KontureContext,
        block: () -> T,
    ): T {
        val previous = currentContext
        currentContext = context
        try {
            return block()
        } finally {
            currentContext = previous
        }
    }
}

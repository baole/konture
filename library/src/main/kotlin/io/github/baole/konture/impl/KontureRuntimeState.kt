/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ProjectGraph
import java.util.Locale

@Suppress("LongParameterList")
internal class KontureRuntimeState(
    val baselinePath: String = "konture-baseline.json",
    val generateBaseline: Boolean = false,
    val projectGraph: ProjectGraph? = null,
    val baselineManager: BaselineManager = BaselineManager(),
    val locale: Locale = Locale.ENGLISH,
    val isLocaleOverridden: Boolean = false,
) {
    val projectGraphLoader: ProjectGraphLoader = ProjectGraphLoader()

    fun copy(
        baselinePath: String = this.baselinePath,
        generateBaseline: Boolean = this.generateBaseline,
        projectGraph: ProjectGraph? = this.projectGraph,
        locale: Locale = this.locale,
        isLocaleOverridden: Boolean = this.isLocaleOverridden,
    ): KontureRuntimeState {
        return KontureRuntimeState(
            baselinePath = baselinePath,
            generateBaseline = generateBaseline,
            projectGraph = projectGraph,
            baselineManager = this.baselineManager,
            locale = locale,
            isLocaleOverridden = isLocaleOverridden,
        )
    }
}

internal object KontureRuntimeStateProvider {
    private val threadLocalState = ThreadLocal.withInitial { KontureRuntimeState() }

    var currentState: KontureRuntimeState
        get() = threadLocalState.get()
        set(value) {
            threadLocalState.set(value)
        }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun reset() {
        try {
            threadLocalState.get()?.baselineManager?.resetForTest()
        } catch (e: Exception) {
            // Ignore
        }
        threadLocalState.set(KontureRuntimeState())
    }

    inline fun <T> runWithState(
        state: KontureRuntimeState,
        block: () -> T,
    ): T {
        val previous = currentState
        currentState = state
        try {
            return block()
        } finally {
            currentState = previous
        }
    }
}

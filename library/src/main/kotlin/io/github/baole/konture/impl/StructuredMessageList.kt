/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject

/**
 * Custom [ArrayList] that associates active [RuleMetadata], [Subject] target, and dependency paths with added raw message entries.
 */
internal class StructuredMessageList : ArrayList<String>() {
    val messageMetadataMap: MutableMap<Int, RuleMetadata> = mutableMapOf()
    val messageSourceLocationMap: MutableMap<Int, SourceLocation> = mutableMapOf()
    val messageTargetMap: MutableMap<Int, Subject> = mutableMapOf()
    val messageDependencyPathMap: MutableMap<Int, List<Subject>> = mutableMapOf()

    override fun add(element: String): Boolean {
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        if (currentMeta != null) {
            messageMetadataMap[size] = currentMeta
        }
        return super.add(element)
    }

    fun add(
        element: String,
        target: Subject? = null,
        dependencyPath: List<Subject> = emptyList(),
        sourceLocation: SourceLocation? = null,
    ): Boolean {
        val idx = size
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        if (currentMeta != null) {
            messageMetadataMap[idx] = currentMeta
        }
        if (target != null) {
            messageTargetMap[idx] = target
        }
        if (dependencyPath.isNotEmpty()) {
            messageDependencyPathMap[idx] = dependencyPath
        }
        if (sourceLocation != null) {
            messageSourceLocationMap[idx] = sourceLocation
        }
        return super.add(element)
    }

    override fun addAll(elements: Collection<String>): Boolean {
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        if (currentMeta != null) {
            val start = size
            elements.forEachIndexed { i, _ ->
                messageMetadataMap[start + i] = currentMeta
            }
        }
        return super.addAll(elements)
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.SourceLocation

/**
 * Custom [ArrayList] that associates active [RuleMetadata] with added raw message entries.
 */
internal class StructuredMessageList : ArrayList<String>() {
    val messageMetadataMap: MutableMap<Int, RuleMetadata> = mutableMapOf()
    val messageSourceLocationMap: MutableMap<Int, SourceLocation> = mutableMapOf()

    override fun add(element: String): Boolean {
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        if (currentMeta != null) {
            messageMetadataMap[size] = currentMeta
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

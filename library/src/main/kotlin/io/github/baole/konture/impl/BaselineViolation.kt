/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class BaselineViolation(
    val location: String? = null,
    val message: String,
) : Comparable<BaselineViolation> {
    override fun compareTo(other: BaselineViolation): Int {
        val locComp = (location ?: "").compareTo(other.location ?: "")
        if (locComp != 0) return locComp
        return message.compareTo(other.message)
    }
}

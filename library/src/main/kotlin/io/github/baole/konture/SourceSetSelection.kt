/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/** A source set as exposed to architecture rules. */
data class SourceSetId(
    val modulePath: String,
    val name: String,
    val kind: SourceSetKind,
    val role: SourceSetRole,
)

enum class SourceSetKind { JVM, ANDROID, KMP }

enum class SourceSetRole { PRODUCTION, TEST }

/** Immutable selector used by source-backed Konture entry points. */
class SourceSetSelector internal constructor(
    private val matchesSourceSet: (SourceSetId) -> Boolean,
) {
    internal fun matches(sourceSet: SourceSetId): Boolean = matchesSourceSet(sourceSet)

    infix fun and(other: SourceSetSelector): SourceSetSelector = SourceSetSelector { matches(it) && other.matches(it) }

    infix fun or(other: SourceSetSelector): SourceSetSelector = SourceSetSelector { matches(it) || other.matches(it) }

    operator fun not(): SourceSetSelector = SourceSetSelector { !matches(it) }
}

/** Factory methods for selecting captured Kotlin source sets. */
object SourceSets {
    fun named(vararg names: String): SourceSetSelector {
        val acceptedNames = names.toSet()
        return SourceSetSelector { it.name in acceptedNames }
    }

    fun matchingName(pattern: String): SourceSetSelector = SourceSetSelector { PatternMatchers.matchesSimpleGlob(pattern, it.name) }

    fun of(
        role: SourceSetRole? = null,
        kind: SourceSetKind? = null,
    ): SourceSetSelector =
        SourceSetSelector { sourceSet ->
            (role == null || sourceSet.role == role) && (kind == null || sourceSet.kind == kind)
        }

    fun tests(): SourceSetSelector = of(role = SourceSetRole.TEST)

    fun production(): SourceSetSelector = of(role = SourceSetRole.PRODUCTION)

    fun inModule(modulePath: String): SourceSetSelector = SourceSetSelector { it.modulePath == modulePath }
}

internal fun FileDeclaration.membershipsFor(modulePath: String): List<SourceSetId> =
    sourceSets.ifEmpty { listOf(SourceSetId(modulePath, "main", SourceSetKind.JVM, SourceSetRole.PRODUCTION)) }

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/** A source set as exposed to architecture rules. */
public data class SourceSetId(
    /** Filter or assertion criteria for module path. */
    public val modulePath: String,
    /** Filter or assertion criteria for name. */
    public val name: String,
    /** Filter or assertion criteria for kind. */
    public val kind: SourceSetKind,
    /** Filter or assertion criteria for role. */
    public val role: SourceSetRole,
)

/** Broad technology stack classification of a source set. */
public enum class SourceSetKind {
    /** JVM source set. */
    JVM,

    /** Android variant source set. */
    ANDROID,

    /** Kotlin Multiplatform source set. */
    KMP,

    ;

    /** Companion factory and parsing methods for [SourceSetKind]. */
    public companion object {
        /**
         * Resolves a [SourceSetKind] from a string name (case-insensitive),
         * supporting standard names ("KOTLIN_JVM", "ANDROID_VARIANT", "KMP", "JVM", "ANDROID").
         */
        public fun fromString(value: String): SourceSetKind =
            when (value.trim().uppercase()) {
                "KMP" -> KMP
                "ANDROID", "ANDROID_VARIANT" -> ANDROID
                "JVM", "KOTLIN_JVM" -> JVM
                else -> JVM
            }

        /**
         * Converts a core [io.github.baole.konture.core.SourceSetKind] to this public [SourceSetKind].
         */
        public fun fromCoreKind(coreKind: io.github.baole.konture.core.SourceSetKind): SourceSetKind =
            when (coreKind) {
                io.github.baole.konture.core.SourceSetKind.KOTLIN_JVM -> JVM
                io.github.baole.konture.core.SourceSetKind.ANDROID_VARIANT -> ANDROID
                io.github.baole.konture.core.SourceSetKind.KMP -> KMP
            }
    }
}

/** Indicates whether a source set contains production or test code. */
public enum class SourceSetRole {
    /** Production application or library source set. */
    PRODUCTION,

    /** Test source set. */
    TEST,
}

/** Immutable selector used by source-backed Konture entry points. */
public class SourceSetSelector internal constructor(
    private val matchesSourceSet: (SourceSetId) -> Boolean,
) {
    internal fun matches(sourceSet: SourceSetId): Boolean = matchesSourceSet(sourceSet)

    /** Combines two selectors with logical AND. */
    public infix fun and(other: SourceSetSelector): SourceSetSelector =
        SourceSetSelector { matches(it) && other.matches(it) }

    /** Combines two selectors with logical OR. */
    public infix fun or(other: SourceSetSelector): SourceSetSelector =
        SourceSetSelector { matches(it) || other.matches(it) }

    /** Negates this selector with logical NOT. */
    public operator fun not(): SourceSetSelector = SourceSetSelector { !matches(it) }
}

/** Factory methods for selecting captured Kotlin source sets. */
public object SourceSets {
    /** Selects source sets matching exact names. */
    public fun named(vararg names: String): SourceSetSelector {
        /** Filter or assertion criteria for accepted names. */
        val acceptedNames = names.toSet()
        return SourceSetSelector { it.name in acceptedNames }
    }

    /** Selects source sets matching a glob pattern. */
    public fun matchingName(pattern: String): SourceSetSelector =
        SourceSetSelector {
            PatternMatchers.matchesSimpleGlob(pattern, it.name)
        }

    /** Selects source sets matching role and/or kind filters. */
    public fun of(
        role: SourceSetRole? = null,
        kind: SourceSetKind? = null,
    ): SourceSetSelector =
        SourceSetSelector { sourceSet ->
            (role == null || sourceSet.role == role) && (kind == null || sourceSet.kind == kind)
        }

    /** Selects test source sets. */
    public fun tests(): SourceSetSelector = of(role = SourceSetRole.TEST)

    /** Selects production source sets. */
    public fun production(): SourceSetSelector = of(role = SourceSetRole.PRODUCTION)

    /** Selects source sets located in a specific module path. */
    public fun inModule(modulePath: String): SourceSetSelector = SourceSetSelector { it.modulePath == modulePath }
}

internal fun FileDeclaration.membershipsFor(modulePath: String): List<SourceSetId> =
    sourceSets.ifEmpty { listOf(SourceSetId(modulePath, "main", SourceSetKind.JVM, SourceSetRole.PRODUCTION)) }

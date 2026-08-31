/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlinx.serialization.Serializable

/** Category of source code usage (e.g. function call or class reference). */
@Serializable
public enum class UsageKind {
    /** Direct function or constructor call. */
    CALL,

    /** Class or type reference. */
    CLASS_REFERENCE,
}

/** Confidence level of a resolved symbol usage in source code analysis. */
@Serializable
public enum class ResolutionConfidence {
    /** Symbol usage was deterministically resolved. */
    RESOLVED,

    /** Symbol usage is conservatively inferred as possible. */
    POSSIBLE,

    /** Symbol usage could not be resolved. */
    UNRESOLVED,
}

/** A resolved (or conservatively possible) Kotlin source usage. */
@Serializable
public data class SourceUsage(
    /** Filter or assertion criteria for kind. */
    public val kind: UsageKind,
    /** Filter or assertion criteria for target fq name. */
    public val targetFqName: String,
    /** Filter or assertion criteria for file path. */
    public val filePath: String,
    /** Filter or assertion criteria for line. */
    public val line: Int,
    /** Filter or assertion criteria for column. */
    public val column: Int,
    /** Filter or assertion criteria for enclosing function. */
    public val enclosingFunction: String? = null,
    /** Filter or assertion criteria for enclosing class. */
    public val enclosingClass: String? = null,
    /** Filter or assertion criteria for enclosing property. */
    public val enclosingProperty: String? = null,
    /** Filter or assertion criteria for raw expression. */
    public val rawExpression: String = targetFqName,
    /** Filter or assertion criteria for source sets. */
    public val sourceSets: List<SourceSetId> = emptyList(),
    /** Filter or assertion criteria for possible target fq names. */
    public val possibleTargetFqNames: List<String> = emptyList(),
    /** Filter or assertion criteria for unresolved possible usage. */
    public val unresolvedPossibleUsage: Boolean = false,
    /** Filter or assertion criteria for confidence. */
    public val confidence: ResolutionConfidence =
        if (unresolvedPossibleUsage) {
            ResolutionConfidence.POSSIBLE
        } else {
            ResolutionConfidence.RESOLVED
        },
    /** Filter or assertion criteria for source start offset. */
    public val sourceStartOffset: Int = -1,
    /** Filter or assertion criteria for source end offset. */
    public val sourceEndOffset: Int = -1,
    /** Filter or assertion criteria for enclosing function start offset. */
    public val enclosingFunctionStartOffset: Int = -1,
    /** Filter or assertion criteria for enclosing function end offset. */
    public val enclosingFunctionEndOffset: Int = -1,
)

/** Returns true if this usage is enclosed in the class specified by [classFqName] and optional [className]. */
public fun SourceUsage.isEnclosedInClass(
    classFqName: String,
    className: String? = null,
): Boolean =
    enclosingClass == classFqName ||
        (className != null && enclosingClass == className) ||
        enclosingClass == null ||
        (enclosingClass.startsWith("$classFqName."))

/** Returns true if this usage is enclosed in the property specified by [propertyName] and optional class contexts. */
public fun SourceUsage.isEnclosedInProperty(
    propertyName: String,
    classFqName: String? = null,
    className: String? = null,
): Boolean =
    enclosingProperty == propertyName &&
        (
            className == null ||
                enclosingClass == className ||
                (enclosingClass != null && classFqName != null && enclosingClass == classFqName) ||
                (enclosingClass != null && enclosingClass.endsWith(".$className"))
        )

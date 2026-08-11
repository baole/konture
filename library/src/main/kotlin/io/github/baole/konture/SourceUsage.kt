/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Category of source code usage (e.g. function call or class reference). */
public enum class UsageKind {
    /** Direct function or constructor call. */
    CALL,

    /** Class or type reference. */
    CLASS_REFERENCE,
}

/** Confidence level of a resolved symbol usage in source code analysis. */
public enum class ResolutionConfidence {
    /** Symbol usage was deterministically resolved. */
    RESOLVED,

    /** Symbol usage is conservatively inferred as possible. */
    POSSIBLE,

    /** Symbol usage could not be resolved. */
    UNRESOLVED,
}

/** A resolved (or conservatively possible) Kotlin source usage. */
data class SourceUsage(
    val kind: UsageKind,
    val targetFqName: String,
    val filePath: String,
    val line: Int,
    val column: Int,
    val enclosingFunction: String? = null,
    val enclosingClass: String? = null,
    val enclosingProperty: String? = null,
    val rawExpression: String = targetFqName,
    val sourceSets: List<SourceSetId> = emptyList(),
    val possibleTargetFqNames: List<String> = emptyList(),
    val unresolvedPossibleUsage: Boolean = false,
    val confidence: ResolutionConfidence =
        if (unresolvedPossibleUsage) {
            ResolutionConfidence.POSSIBLE
        } else {
            ResolutionConfidence.RESOLVED
        },
    val sourceStartOffset: Int = -1,
    val sourceEndOffset: Int = -1,
    val enclosingFunctionStartOffset: Int = -1,
    val enclosingFunctionEndOffset: Int = -1,
)

/** Returns true if this usage is enclosed in the class specified by [classFqName] and optional [className]. */
fun SourceUsage.isEnclosedInClass(
    classFqName: String,
    className: String? = null,
): Boolean =
    enclosingClass == classFqName ||
        (className != null && enclosingClass == className) ||
        enclosingClass == null ||
        (enclosingClass.startsWith("$classFqName."))

/** Returns true if this usage is enclosed in the property specified by [propertyName] and optional class contexts. */
fun SourceUsage.isEnclosedInProperty(
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

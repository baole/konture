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
    /** Filter or assertion criteria for kind. */
    val kind: UsageKind,
    /** Filter or assertion criteria for target fq name. */
    val targetFqName: String,
    /** Filter or assertion criteria for file path. */
    val filePath: String,
    /** Filter or assertion criteria for line. */
    val line: Int,
    /** Filter or assertion criteria for column. */
    val column: Int,
    /** Filter or assertion criteria for enclosing function. */
    val enclosingFunction: String? = null,
    /** Filter or assertion criteria for enclosing class. */
    val enclosingClass: String? = null,
    /** Filter or assertion criteria for enclosing property. */
    val enclosingProperty: String? = null,
    /** Filter or assertion criteria for raw expression. */
    val rawExpression: String = targetFqName,
    /** Filter or assertion criteria for source sets. */
    val sourceSets: List<SourceSetId> = emptyList(),
    /** Filter or assertion criteria for possible target fq names. */
    val possibleTargetFqNames: List<String> = emptyList(),
    /** Filter or assertion criteria for unresolved possible usage. */
    val unresolvedPossibleUsage: Boolean = false,
    /** Filter or assertion criteria for confidence. */
    val confidence: ResolutionConfidence =
        if (unresolvedPossibleUsage) {
            ResolutionConfidence.POSSIBLE
        } else {
            ResolutionConfidence.RESOLVED
        },
    /** Filter or assertion criteria for source start offset. */
    val sourceStartOffset: Int = -1,
    /** Filter or assertion criteria for source end offset. */
    val sourceEndOffset: Int = -1,
    /** Filter or assertion criteria for enclosing function start offset. */
    val enclosingFunctionStartOffset: Int = -1,
    /** Filter or assertion criteria for enclosing function end offset. */
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

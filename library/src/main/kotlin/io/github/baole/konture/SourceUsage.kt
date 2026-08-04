/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Category of a Kotlin code usage.
 */
enum class UsageKind {
    /** Invocations of functions, properties, or constructor calls. */
    CALL,

    /** References to classes or types (such as parameter types, property types, or type checks). */
    CLASS_REFERENCE,
}

/**
 * Resolution confidence level for a source usage.
 */
enum class ResolutionConfidence {
    /** High confidence resolved target FQ name. */
    RESOLVED,

    /** Conservative candidate match (e.g. imported type method call). */
    POSSIBLE,

    /** Target symbol could not be resolved. */
    UNRESOLVED,
}

/**
 * A resolved (or conservatively possible) Kotlin source usage.
 *
 * @property kind The category of usage ([UsageKind.CALL] or [UsageKind.CLASS_REFERENCE]).
 * @property targetFqName The target fully qualified name of the symbol being accessed or called.
 * @property filePath Relative project file path where the usage occurs.
 * @property line 1-indexed line number where the usage occurs.
 * @property column 1-indexed column number where the usage occurs.
 * @property enclosingFunction Name of enclosing function if usage is inside a function body.
 * @property enclosingClass Fully qualified name of enclosing class if usage is inside a class body.
 * @property enclosingProperty Name of enclosing property if usage is inside a property initializer.
 * @property rawExpression Raw source text string representing the call expression.
 * @property sourceSets Source set identifiers where this usage was detected.
 * @property possibleTargetFqNames Candidate fully qualified names when exact target is ambiguous.
 * @property unresolvedPossibleUsage True if usage represents a candidate resolution that could not be verified 100%.
 * @property confidence Resolution confidence level ([ResolutionConfidence]).
 * @property sourceStartOffset Start character offset in source file.
 * @property sourceEndOffset End character offset in source file.
 * @property enclosingFunctionStartOffset Character start offset of enclosing function, or -1.
 * @property enclosingFunctionEndOffset Character end offset of enclosing function, or -1.
 */
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
    val confidence: ResolutionConfidence = if (unresolvedPossibleUsage) ResolutionConfidence.POSSIBLE else ResolutionConfidence.RESOLVED,
    val sourceStartOffset: Int = -1,
    val sourceEndOffset: Int = -1,
    val enclosingFunctionStartOffset: Int = -1,
    val enclosingFunctionEndOffset: Int = -1,
)

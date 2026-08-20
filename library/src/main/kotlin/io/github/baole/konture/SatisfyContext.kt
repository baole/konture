/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Receiver context provided inside `satisfy` custom predicate assertion blocks.
 *
 * @param T The type of subject being evaluated (e.g. [ClassDeclaration], [FileDeclarationContext]).
 */
@KontureDsl
public interface SatisfyContext<T> {
    /** The subject currently being evaluated. */
    public val subject: T

    /** The stable rule identifier assigned to this satisfy block. */
    public val id: String

    /** Optional human-readable description explaining this satisfy assertion. */
    public val description: String?

    /** The project dependency graph context. */
    public val graph: ProjectGraph

    /**
     * Records a custom violation message for the current subject.
     *
     * @param message Detailed explanation of the architectural violation.
     */
    public fun addViolation(message: String)
}

/**
 * Concrete internal implementation of [SatisfyContext].
 */
internal class SatisfyContextImpl<T>(
    override val subject: T,
    override val id: String,
    override val description: String?,
    override val graph: ProjectGraph,
    private val rawMessages: MutableList<String>,
) : SatisfyContext<T> {
    override fun addViolation(message: String) {
        rawMessages.add(message)
    }
}

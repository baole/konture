/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.impl.LogicalOperator

/**
 * Context wrapper for verifying source file declarations.
 *
 * Provides both the target [declaration] and architectural metadata to easily query scope.
 *
 * @property declaration The underlying [FileDeclaration] AST model representing the source file.
 * @property modulePath The module subdirectory/path containing this file.
 */
data class FileDeclarationContext(
    val declaration: FileDeclaration,
    val modulePath: String,
)

/**
 * A builder for compiling and verifying architectural rules on Kotlin source files.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all source files in the project.
 */
@KontureDsl
class FilesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
) {
    private var thatPredicate: ((FileDeclarationContext) -> Boolean)? = null
    private var shouldAssertion: (
        (
            FileDeclarationContext,
            List<FileDeclarationContext>,
            MutableList<String>,
        ) -> Unit
    )? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Configures this builder to allow empty selections (i.e. if no files match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    fun allowEmpty(): FilesRuleBuilder {
        allowEmpty = true
        return this
    }

    internal fun getThatPredicate(): ((FileDeclarationContext) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): (
        (FileDeclarationContext, List<FileDeclarationContext>, MutableList<String>) -> Unit
    )? =
        shouldAssertion

    /**
     * Starts adding filtering conditions to select which files to verify.
     */
    fun that(): FilesThat = FilesThat(this)

    /**
     * Starts adding assertion rules that the selected files must satisfy.
     */
    fun should(): FilesShould = FilesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    fun and(): FilesThat {
        activeOperator = LogicalOperator.AND
        return FilesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    fun or(): FilesThat {
        activeOperator = LogicalOperator.OR
        return FilesThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    fun xor(): FilesThat {
        activeOperator = LogicalOperator.XOR
        return FilesThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    fun not(): FilesThat {
        negateNext = true
        return FilesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    fun andShould(): FilesShould {
        activeShouldOperator = LogicalOperator.AND
        return FilesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    fun orShould(): FilesShould {
        activeShouldOperator = LogicalOperator.OR
        return FilesShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    fun xorShould(): FilesShould {
        activeShouldOperator = LogicalOperator.XOR
        return FilesShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    fun notShould(): FilesShould {
        negateNextShould = true
        return FilesShould(this)
    }

    internal fun setThat(predicate: (FileDeclarationContext) -> Boolean) {
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                val p = { f: FileDeclarationContext -> !predicate(f) }
                p
            } else {
                predicate
            }

        val current = thatPredicate
        if (current == null) {
            thatPredicate = actualPredicate
        } else {
            val op = activeOperator
            thatPredicate =
                when (op) {
                    LogicalOperator.OR -> {
                        { current(it) || actualPredicate(it) }
                    }

                    LogicalOperator.XOR -> {
                        { current(it) xor actualPredicate(it) }
                    }

                    LogicalOperator.AND -> {
                        { current(it) && actualPredicate(it) }
                    }
                }
            activeOperator = LogicalOperator.AND
        }
    }

    internal fun setShould(assertion: (FileDeclarationContext, List<FileDeclarationContext>, MutableList<String>) -> Unit) {
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                val a = {
                        file: FileDeclarationContext,
                        allFiles: List<FileDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    val tempViolations = mutableListOf<String>()
                    assertion(file, allFiles, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add("File ${file.declaration.name} should not satisfy the negated condition.")
                    }
                }
                a
            } else {
                assertion
            }

        val current = shouldAssertion
        if (current == null) {
            shouldAssertion = actualAssertion
        } else {
            val op = activeShouldOperator
            if (op == LogicalOperator.OR) {
                shouldAssertion = { file, allFiles, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(file, allFiles, temp1)
                    actualAssertion(file, allFiles, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            "File ${file.declaration.name} should satisfy either: (${temp1.joinToString()}) OR (${temp2.joinToString()})",
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { file, allFiles, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(file, allFiles, temp1)
                    actualAssertion(file, allFiles, temp2)
                    val ok1 = temp1.isEmpty()
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            "File ${file.declaration.name} should satisfy exactly one of the XOR assertions.",
                        )
                    }
                }
            } else {
                shouldAssertion = { file, allFiles, violations ->
                    current(file, allFiles, violations)
                    actualAssertion(file, allFiles, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the compiled file rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    fun check(g: ProjectGraph = graph) {
        val allFiles =
            g.getAllModules().flatMap { module ->
                module.files.map { file ->
                    FileDeclarationContext(file, module.path)
                }
            }
        val filesToCheck = allFiles.filter { thatPredicate?.invoke(it) ?: true }

        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Files Rules: found ${allFiles.size} files total. Selected ${filesToCheck.size} files to verify.",
        )
        if (filesToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    "No files matched the filter criteria in 'that()'. " +
                        "If this is expected, use '.allowEmpty()' or 'allowEmpty = true' on the rule builder to allow empty selections.",
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No files matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }

        val assertion =
            shouldAssertion ?: throw AssertionError(
                "Files rule has no assertion ('should()'). You must specify at least one assertion condition.",
            )

        val violations = mutableListOf<String>()

        for (file in filesToCheck) {
            assertion(file, allFiles, violations)
        }

        if (violations.isNotEmpty()) {
            throw AssertionError("File architecture violation(s) detected:\n" + violations.joinToString("\n"))
        }
    }
}

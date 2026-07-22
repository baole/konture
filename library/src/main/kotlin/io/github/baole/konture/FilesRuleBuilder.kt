/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.LogicalOperator
import io.github.baole.konture.impl.ViolationLocation

/**
 * A builder for compiling and verifying architectural rules on Kotlin source files.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all source files in the project.
 */
@KontureDsl
class FilesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    private val sourceSets: SourceSetSelector = SourceSets.production(),
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
                        violations.add(getMessage("files.rule.negatedSatisfied", file.declaration.name))
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
                            getMessage("files.rule.eitherOr", file.declaration.name, temp1.joinToString(), temp2.joinToString()),
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
                            getMessage("files.rule.xor", file.declaration.name),
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
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).map { sourceSet ->
                        FileDeclarationContext(file, module.path, sourceSet)
                    }
                }
            }
        val filesToCheck = allFiles.filter { thatPredicate?.invoke(it) ?: true }

        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Files Rules: found ${allFiles.size} files total. Selected ${filesToCheck.size} files to verify.",
        )
        if (filesToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(getMessage("files.rule.emptySelect"))
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No files matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }

        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("files.rule.noAssertion"),
            )

        val runCheck = { list: MutableList<String> ->
            for (file in filesToCheck) {
                val startIdx = list.size
                assertion(file, allFiles, list)
                for (i in startIdx until list.size) {
                    list[i] = "${list[i]} (at ${ViolationLocation.of(file.modulePath, file.sourceSet?.name, file.declaration.filePath)})"
                }
            }
        }

        BaselineManager.checkRule(
            getMessage("files.rule.violationHeader"),
            runCheck,
        )
    }
}

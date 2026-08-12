/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
public class FilesRuleBuilder(
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
     * Debugging helper that prints information about all files matched by the `that()` filter.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printMatchedFiles(
        logger: (FileDeclarationContext) -> Unit = {
            println(
                getMessage(
                    "debug.files.matched",
                    it.declaration.name,
                    ViolationLocation.format(it.declaration, it.modulePath, it.sourceSet?.name),
                    it.declaration.packageName,
                ),
            )
        },
    ): FilesRuleBuilder =
        this.apply {
            setShould { file, _, _ ->
                logger(file)
            }
        }

    /**
     * Debugging helper that prints information about all discovered files in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printAllFiles(
        logger: (FileDeclarationContext) -> Unit = {
            println(
                getMessage(
                    "debug.files.discovered",
                    it.declaration.name,
                    ViolationLocation.format(it.declaration, it.modulePath, it.sourceSet?.name),
                    it.declaration.packageName,
                ),
            )
        },
    ): FilesRuleBuilder =
        this.apply {
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).map { sourceSet ->
                        FileDeclarationContext(file, module.path, sourceSet)
                    }
                }
            }.distinctBy {
                Pair(
                    it.modulePath,
                    it.declaration.filePath.ifEmpty { it.declaration.name },
                )
            }.forEach(logger)
        }

    private val ignoredPredicates = mutableListOf<(FileDeclarationContext) -> Boolean>()

    /**
     * Configures this builder to allow empty selections (i.e. if no files match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    public fun allowEmpty(): FilesRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for files satisfying the given predicate.
     */
    public fun ignoreFailuresIn(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for files matching any of the specified names or patterns.
     */
    public fun ignoreFailuresIn(vararg fileNames: String): FilesRuleBuilder {
        ignoredPredicates.add { ctx ->
            fileNames.any { name ->
                ctx.declaration.name == name || ctx.declaration.filePath == name || io.github.baole.konture.impl.PatternMatchers.matchesSimpleGlob(name, ctx.declaration.name)
            }
        }
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
    public fun that(): FilesThat = FilesThat(this)

    /**
     * Starts adding assertion rules that the selected files must satisfy.
     */
    public fun should(): FilesShould = FilesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    public fun and(): FilesThat {
        activeOperator = LogicalOperator.AND
        return FilesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    public fun or(): FilesThat {
        activeOperator = LogicalOperator.OR
        return FilesThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    public fun xor(): FilesThat {
        activeOperator = LogicalOperator.XOR
        return FilesThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    public fun not(): FilesThat {
        negateNext = true
        return FilesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    public fun andShould(): FilesShould {
        activeShouldOperator = LogicalOperator.AND
        return FilesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    public fun orShould(): FilesShould {
        activeShouldOperator = LogicalOperator.OR
        return FilesShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    public fun xorShould(): FilesShould {
        activeShouldOperator = LogicalOperator.XOR
        return FilesShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    public fun notShould(): FilesShould {
        negateNextShould = true
        return FilesShould(this)
    }

    internal fun setThat(predicate: (FileDeclarationContext) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for p. */
                val p = { f: FileDeclarationContext -> !predicate(f) }
                p
            } else {
                predicate
            }

        /** Filter or assertion criteria for current. */
        val current = thatPredicate
        if (current == null) {
            thatPredicate = actualPredicate
        } else {
            /** Filter or assertion criteria for op. */
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

    internal fun setShould(
        assertion: (FileDeclarationContext, List<FileDeclarationContext>, MutableList<String>) -> Unit,
    ) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for a. */
                val a = {
                        file: FileDeclarationContext,
                        allFiles: List<FileDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    /** Filter or assertion criteria for temp violations. */
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

        /** Filter or assertion criteria for current. */
        val current = shouldAssertion
        if (current == null) {
            shouldAssertion = actualAssertion
        } else {
            /** Filter or assertion criteria for op. */
            val op = activeShouldOperator
            if (op == LogicalOperator.OR) {
                shouldAssertion = { file, allFiles, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(file, allFiles, temp1)
                    actualAssertion(file, allFiles, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("files.rule.eitherOr", file.declaration.name, temp1.joinToString("; "), temp2.joinToString("; ")),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { file, allFiles, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(file, allFiles, temp1)
                    actualAssertion(file, allFiles, temp2)
                    /** Filter or assertion criteria for ok1. */
                    val ok1 = temp1.isEmpty()

                    /** Filter or assertion criteria for ok2. */
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
    public fun check(g: ProjectGraph = graph) {
        /** Filter or assertion criteria for all files. */
        val allFiles =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).map { sourceSet ->
                        FileDeclarationContext(file, module.path, sourceSet)
                    }
                }
            }

        /** Filter or assertion criteria for files to check. */
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

        /** Filter or assertion criteria for assertion. */
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("files.rule.noAssertion"),
            )

        /** Filter or assertion criteria for run check. */
        val runCheck = { list: MutableList<String> ->
            for (file in filesToCheck) {
                if (ignoredPredicates.any { it(file) }) continue
                /** Filter or assertion criteria for start idx. */
                val startIdx = list.size
                assertion(file, allFiles, list)
                for (i in startIdx until list.size) {
                    if (!list[i].contains(" (at ")) {
                        list[i] = "${list[i]} (at ${ViolationLocation.format(file.declaration, file.modulePath, file.sourceSet?.name)})"
                    }
                }
            }
        }

        BaselineManager.checkRule(
            getMessage("files.rule.violationHeader"),
            runCheck,
        )
    }
}

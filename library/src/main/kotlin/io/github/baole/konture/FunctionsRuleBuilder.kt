/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.LogicalOperator
import io.github.baole.konture.impl.StructuredMessageList
import io.github.baole.konture.impl.ViolationLocation
import io.github.baole.konture.impl.suppression.SuppressionEvaluator

/**
 * A builder for compiling and verifying architectural rules on Kotlin function declarations.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all functions in the project (both member/class functions and top-level functions).
 */
@KontureDsl
public class FunctionsRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    private val sourceSets: SourceSetSelector = SourceSets.production(),
) {
    private var thatPredicate: ((FunctionDeclarationContext) -> Boolean)? = null
    private var shouldAssertion: (
        (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit
    )? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Debugging helper that prints information about all functions matched by the `that()` filter.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printMatchedFunctions(
        logger: (FunctionDeclarationContext) -> Unit = {
            println(getMessage("debug.functions.matched", it.qualifiedName, ViolationLocation.format(it)))
        },
    ): FunctionsRuleBuilder =
        this.apply {
            setShould { func, _, _ ->
                logger(func)
            }
        }

    /**
     * Debugging helper that prints information about all discovered functions in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printAllFunctions(
        logger: (FunctionDeclarationContext) -> Unit = {
            println(getMessage("debug.functions.discovered", it.qualifiedName, ViolationLocation.format(it)))
        },
    ): FunctionsRuleBuilder =
        this.apply {
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    /** Filter or assertion criteria for top level. */
                    val topLevel =
                        file.topLevelFunctions.map { func ->
                            FunctionDeclarationContext(func, file.packageName, null, module.path, file.filePath, null)
                        }

                    /** Filter or assertion criteria for members. */
                    val members =
                        file.classes.flatMap { cls ->
                            cls.functions.map { func ->
                                FunctionDeclarationContext(
                                    func,
                                    file.packageName,
                                    cls.name,
                                    module.path,
                                    file.filePath,
                                    null,
                                )
                            }
                        }
                    topLevel + members
                }
            }.distinctBy {
                listOf(
                    it.modulePath,
                    it.className,
                    it.declaration.name,
                    it.declaration.parameters.map {
                            p ->
                        p.type
                    },
                )
            }
                .forEach(logger)
        }

    private val ignoredPredicates = mutableListOf<(FunctionDeclarationContext) -> Boolean>()
    private val programmaticSuppressions = mutableListOf<ProgrammaticSuppression>()

    /**
     * Configures programmatic violation suppressions for this function rule suite with mandatory audit rationale.
     */
    public fun suppress(block: RuleSuppressionBuilder.() -> Unit): FunctionsRuleBuilder {
        val builder = RuleSuppressionBuilder().apply(block)
        programmaticSuppressions.addAll(builder.suppressions)
        return this
    }

    /**
     * Configures this builder to allow empty selections (i.e. if no functions match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    public fun allowEmpty(): FunctionsRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for functions satisfying the given predicate.
     */
    @Deprecated(
        message = "Use suppress { ... } with mandatory audit reason instead",
        replaceWith = ReplaceWith("suppress { ... }"),
    )
    public fun ignoreFailuresIn(predicate: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for functions matching any of the specified names or patterns.
     */
    @Deprecated(
        message = "Use suppress { ... } with mandatory audit reason instead",
        replaceWith = ReplaceWith("suppress { ... }"),
    )
    public fun ignoreFailuresIn(vararg functionNames: String): FunctionsRuleBuilder {
        ignoredPredicates.add { ctx ->
            functionNames.any { name ->
                ctx.declaration.name == name || ctx.qualifiedName == name || io.github.baole.konture.impl.PatternMatchers.matchesSimpleGlob(name, ctx.declaration.name)
            }
        }
        return this
    }

    internal fun getThatPredicate(): ((FunctionDeclarationContext) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): (
        (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit
    )? =
        shouldAssertion

    /**
     * Starts adding filtering conditions to select which functions to verify.
     */
    public fun that(): FunctionsThat = FunctionsThat(this)

    /**
     * Starts adding assertion rules that the selected functions must satisfy.
     */
    public fun should(): FunctionsShould = FunctionsShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    public fun and(): FunctionsThat {
        activeOperator = LogicalOperator.AND
        return FunctionsThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    public fun or(): FunctionsThat {
        activeOperator = LogicalOperator.OR
        return FunctionsThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    public fun xor(): FunctionsThat {
        activeOperator = LogicalOperator.XOR
        return FunctionsThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    public fun not(): FunctionsThat {
        negateNext = true
        return FunctionsThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    public fun andShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.AND
        return FunctionsShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    public fun orShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.OR
        return FunctionsShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    public fun xorShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.XOR
        return FunctionsShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    public fun notShould(): FunctionsShould {
        negateNextShould = true
        return FunctionsShould(this)
    }

    internal fun setThat(predicate: (FunctionDeclarationContext) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for p. */
                val p = { f: FunctionDeclarationContext -> !predicate(f) }
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
        assertion: (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit,
    ) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for a. */
                val a = {
                        func: FunctionDeclarationContext,
                        allFuncs: List<FunctionDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    assertion(func, allFuncs, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(
                            getMessage("functions.rule.negatedSatisfied", func.qualifiedName),
                        )
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
                shouldAssertion = { func, allFuncs, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(func, allFuncs, temp1)
                    actualAssertion(func, allFuncs, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("functions.rule.eitherOr", func.qualifiedName, temp1.joinToString("; "), temp2.joinToString("; ")),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { func, allFuncs, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(func, allFuncs, temp1)
                    actualAssertion(func, allFuncs, temp2)
                    /** Filter or assertion criteria for ok1. */
                    val ok1 = temp1.isEmpty()

                    /** Filter or assertion criteria for ok2. */
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            getMessage("functions.rule.xor", func.qualifiedName),
                        )
                    }
                }
            } else {
                shouldAssertion = { func, allFuncs, violations ->
                    current(func, allFuncs, violations)
                    actualAssertion(func, allFuncs, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the compiled function rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    public fun check(g: ProjectGraph = graph): ViolationReport {
        /** Filter or assertion criteria for all functions. */
        val allFunctions =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).flatMap { sourceSet ->
                        /** Filter or assertion criteria for top level. */
                        val topLevel =
                            file.topLevelFunctions.map { func ->
                                FunctionDeclarationContext(
                                    func,
                                    file.packageName,
                                    null,
                                    module.path,
                                    file.filePath,
                                    sourceSet,
                                    file.usages.filter {
                                        it.enclosingFunctionStartOffset == func.sourceStartOffset &&
                                            it.enclosingFunctionEndOffset == func.sourceEndOffset
                                    },
                                )
                            }

                        /** Filter or assertion criteria for members. */
                        val members =
                            file.classes.flatMap { cls ->
                                cls.functions.map { func ->
                                    FunctionDeclarationContext(
                                        func,
                                        file.packageName,
                                        cls.name,
                                        module.path,
                                        file.filePath,
                                        sourceSet,
                                        file.usages.filter {
                                            it.enclosingFunctionStartOffset == func.sourceStartOffset &&
                                                it.enclosingFunctionEndOffset == func.sourceEndOffset
                                        },
                                    )
                                }
                            }
                        topLevel + members
                    }
                }
            }

        /** Filter or assertion criteria for functions to check. */
        val functionsToCheck = allFunctions.filter { thatPredicate?.invoke(it) ?: true }
        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Functions Rules: found ${allFunctions.size} functions total. Selected ${functionsToCheck.size} functions to verify.",
        )
        if (functionsToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    getMessage("functions.rule.emptySelect"),
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No functions matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }
        /** Filter or assertion criteria for assertion. */
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("functions.rule.noAssertion"),
            )

        /** Filter or assertion criteria for run check. */
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        val activeRuleId = currentMeta?.id ?: "functions.rule"
        val activeSeverity = currentMeta?.severity ?: Severity.ERROR
        val allProgrammatic =
            KontureRuntimeStateProvider.currentState.activeProgrammaticSuppressions + programmaticSuppressions
        val fileMap = graph.fileMap
        val classMap = graph.classMap

        val runCheckReport = { list: MutableList<Violation> ->
            for (func in functionsToCheck) {
                if (ignoredPredicates.any { it(func) }) continue
                val rawMessages = StructuredMessageList()
                assertion(func, allFunctions, rawMessages)
                for ((index, rawMsg) in rawMessages.withIndex()) {
                    val msgMeta = rawMessages.messageMetadataMap[index] ?: currentMeta
                    val ruleIdToUse = msgMeta?.id ?: activeRuleId
                    val severityToUse = msgMeta?.severity ?: activeSeverity
                    val fullMsg =
                        if (!rawMsg.contains(" (at ")) {
                            "$rawMsg (at ${ViolationLocation.format(func)})"
                        } else {
                            rawMsg
                        }
                    val fqName =
                        func.className?.let { "${func.packageName}.$it.${func.declaration.name}" }
                            ?: "${func.packageName}.${func.declaration.name}"
                    val subject =
                        Subject.FunctionSubject(
                            fqName = fqName,
                            location = SourceLocation(filePath = func.filePath, line = func.declaration.sourceLine),
                        )
                    val enclosingClass = func.className?.let { classMap["${func.packageName}.$it"] }
                    val suppression =
                        SuppressionEvaluator.evaluateFunctionSuppression(
                            ruleId = ruleIdToUse,
                            func = func,
                            file = fileMap[func.filePath],
                            enclosingClass = enclosingClass,
                            programmaticSuppressions = allProgrammatic,
                        )
                    list.add(
                        Violation(
                            ruleId = ruleIdToUse,
                            subject = subject,
                            message = fullMsg,
                            severity = severityToUse,
                            metadata = msgMeta,
                            isSuppressed = suppression != null,
                            suppression = suppression,
                        ),
                    )
                }
            }
        }

        return BaselineManager.checkRuleReport(
            ruleId = activeRuleId,
            violationHeader = currentMeta?.description ?: getMessage("functions.rule.violationHeader"),
            runCheckReport = runCheckReport,
        )
    }
}

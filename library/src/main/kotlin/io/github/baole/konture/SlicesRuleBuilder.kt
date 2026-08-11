/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain), Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.LogicalOperator
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector
import io.github.baole.konture.impl.SliceGraph

/**
 * A builder for compiling and verifying architectural rules on slices — groups of packages derived
 * from a package pattern with a capture group.
 *
 * A slice pattern such as `com.acme.(*)..` partitions the matched packages into slices keyed by the
 * captured segment, and the assertions on [SlicesShould] verify relationships between those slices
 * (for example, that they are free of cyclic dependencies).
 */
@KontureDsl
class SlicesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    private val sourceSets: SourceSetSelector = SourceSets.production(),
) {
    private var pattern: String? = null
    private var thatPredicate: ((Slice) -> Boolean)? = null
    private val assertions = mutableListOf<(SliceGraph, MutableList<String>) -> Unit>()
    private var allowEmpty = false

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false

    /**
     * Sets the slice pattern. It must contain exactly one capture group: `(*)` for one package
     * segment or `(**)` for one or more segments. The captured text becomes the slice key.
     */
    fun matching(pattern: String): SlicesRuleBuilder {
        this.pattern = pattern
        return this
    }

    /**
     * Debugging helper that prints information about all slices derived from the slice pattern.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    fun printMatchedSlices(
        logger: (Slice) -> Unit = {
            println(getMessage("debug.slices.derived", it.key, it.packages, it.classes.size))
        },
    ): SlicesRuleBuilder =
        this.apply {
            addShouldAssertion { sliceGraph, _ ->
                sliceGraph.slices.forEach(logger)
            }
        }

    /**
     * Debugging helper that prints information about all derived slices in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    fun printAllSlices(
        logger: (Slice) -> Unit = {
            println(getMessage("debug.slices.derived", it.key, it.packages, it.classes.size))
        },
    ): SlicesRuleBuilder = printMatchedSlices(logger)

    internal fun getThatPredicate(): ((Slice) -> Boolean)? = thatPredicate

    internal fun checkRuleAssertions(
        sliceGraph: SliceGraph,
        violations: MutableList<String>,
    ) {
        assertions.forEach { it(sliceGraph, violations) }
    }

    private val ignoredPredicates = mutableListOf<(Slice) -> Boolean>()

    /**
     * Configures this builder to allow empty selections (if no packages match the slice pattern the
     * rule passes instead of throwing an AssertionError).
     */
    fun allowEmpty(): SlicesRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for slices satisfying the given predicate.
     */
    fun ignoreFailuresIn(predicate: (Slice) -> Boolean): SlicesRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for slices matching any of the specified slice keys or patterns.
     */
    fun ignoreFailuresIn(vararg sliceKeys: String): SlicesRuleBuilder {
        ignoredPredicates.add { slice ->
            sliceKeys.any { key ->
                slice.key == key || io.github.baole.konture.impl.PatternMatchers.matchesSimpleGlob(key, slice.key)
            }
        }
        return this
    }

    /** Starts filtering conditions to select which slices to verify. */
    fun that(): SlicesThat = SlicesThat(this)

    /** Starts adding assertion rules that the derived slices must satisfy. */
    fun should(): SlicesShould = SlicesShould(this)

    /** Logical AND operator for chaining filter conditions. */
    fun and(): SlicesThat {
        activeOperator = LogicalOperator.AND
        return SlicesThat(this)
    }

    /** Logical OR operator for chaining filter conditions. */
    fun or(): SlicesThat {
        activeOperator = LogicalOperator.OR
        return SlicesThat(this)
    }

    /** Logical XOR operator for chaining filter conditions. */
    fun xor(): SlicesThat {
        activeOperator = LogicalOperator.XOR
        return SlicesThat(this)
    }

    /** Negates the next filter condition. */
    fun not(): SlicesThat {
        negateNext = true
        return SlicesThat(this)
    }

    /** Logical AND operator for chaining assertion rules. */
    fun andShould(): SlicesShould {
        activeShouldOperator = LogicalOperator.AND
        return SlicesShould(this)
    }

    /** Logical OR operator for chaining assertion rules. */
    fun orShould(): SlicesShould {
        activeShouldOperator = LogicalOperator.OR
        return SlicesShould(this)
    }

    /** Logical XOR operator for chaining assertion rules. */
    fun xorShould(): SlicesShould {
        activeShouldOperator = LogicalOperator.XOR
        return SlicesShould(this)
    }

    /** Negates the next assertion rule. */
    fun notShould(): SlicesShould {
        negateNextShould = true
        return SlicesShould(this)
    }

    internal fun setThat(predicate: (Slice) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate: (Slice) -> Boolean =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for fn. */
                val fn: (Slice) -> Boolean = { s: Slice -> !predicate(s) }
                fn
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

    internal fun setShould(assertion: (SliceGraph, MutableList<String>) -> Unit) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion: (SliceGraph, MutableList<String>) -> Unit =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for fn. */
                val fn: (
                    SliceGraph,
                    MutableList<String>,
                ) -> Unit = { graph: SliceGraph, violations: MutableList<String> ->
                    /** Filter or assertion criteria for temp. */
                    val temp = mutableListOf<String>()
                    assertion(graph, temp)
                    if (temp.isEmpty()) {
                        violations.add("Slice rule negated assertion was satisfied")
                    }
                }
                fn
            } else {
                assertion
            }
        assertions.add(actualAssertion)
    }

    internal fun addShouldAssertion(assertion: (SliceGraph, MutableList<String>) -> Unit) {
        setShould(assertion)
    }

    /**
     * Executes the compiled slice rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    fun check(g: ProjectGraph = graph) {
        /** Filter or assertion criteria for slice pattern. */
        val slicePattern = pattern ?: throw AssertionError(getMessage("slices.rule.noPattern"))

        /** Filter or assertion criteria for all classes. */
        val allClasses =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    if (file.membershipsFor(module.path).any(sourceSets::matches)) file.classes else emptyList()
                }
            }.distinctBy { it.fqName to it.filePath }

        /** Filter or assertion criteria for package to slice. */
        val packageToSlice = mutableMapOf<String, String>()

        /** Filter or assertion criteria for classes by key. */
        val classesByKey = linkedMapOf<String, MutableList<ClassDeclaration>>()

        /** Filter or assertion criteria for packages by key. */
        val packagesByKey = linkedMapOf<String, MutableSet<String>>()
        for (cls in allClasses) {
            /** Filter or assertion criteria for key. */
            val key = PatternMatchers.sliceKeyFor(slicePattern, cls.packageName) ?: continue
            packageToSlice[cls.packageName] = key
            classesByKey.getOrPut(key) { mutableListOf() }.add(cls)
            packagesByKey.getOrPut(key) { mutableSetOf() }.add(cls.packageName)
        }
        /** Filter or assertion criteria for all slices. */
        val allSlices =
            classesByKey.keys.sorted().map {
                Slice(
                    it,
                    packagesByKey.getValue(it),
                    classesByKey.getValue(it),
                )
            }

        /** Filter or assertion criteria for slices. */
        val slices = allSlices.filter { thatPredicate?.invoke(it) ?: true }

        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Slices Rules: pattern '$slicePattern' produced ${slices.size} slice(s).",
        )
        if (slices.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(getMessage("slices.rule.emptySelect"))
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No packages matched the slice pattern 'matching()'. Rule silently succeeded as allowEmpty is enabled.",
                )
                return
            }
        }

        if (assertions.isEmpty()) throw AssertionError(getMessage("slices.rule.noAssertion"))
        /** Filter or assertion criteria for active slices. */
        val activeSlices = slices.filterNot { slice -> ignoredPredicates.any { it(slice) } }

        /** Filter or assertion criteria for slice graph. */
        val sliceGraph = SliceCycleDetector.buildGraph(activeSlices, packageToSlice, allClasses, slicePattern)

        /** Filter or assertion criteria for run check. */
        val runCheck = { list: MutableList<String> -> assertions.forEach { it(sliceGraph, list) } }
        BaselineManager.checkRule(getMessage("slices.rule.violationHeader"), runCheck)
    }
}

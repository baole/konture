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
    private val assertions = mutableListOf<(SliceGraph, MutableList<String>) -> Unit>()
    private var allowEmpty = false

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
     * Configures this builder to allow empty selections (if no packages match the slice pattern the
     * rule passes instead of throwing an AssertionError).
     */
    fun allowEmpty(): SlicesRuleBuilder {
        allowEmpty = true
        return this
    }

    /** Starts adding assertion rules that the derived slices must satisfy. */
    fun should(): SlicesShould = SlicesShould(this)

    internal fun addShouldAssertion(assertion: (SliceGraph, MutableList<String>) -> Unit) {
        assertions.add(assertion)
    }

    /**
     * Executes the compiled slice rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    fun check(g: ProjectGraph = graph) {
        val slicePattern = pattern ?: throw AssertionError(getMessage("slices.rule.noPattern"))

        val allClasses =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    if (file.membershipsFor(module.path).any(sourceSets::matches)) file.classes else emptyList()
                }
            }.distinctBy { it.fqName to it.filePath }

        val packageToSlice = mutableMapOf<String, String>()
        val classesByKey = linkedMapOf<String, MutableList<ClassDeclaration>>()
        val packagesByKey = linkedMapOf<String, MutableSet<String>>()
        for (cls in allClasses) {
            val key = PatternMatchers.sliceKeyFor(slicePattern, cls.packageName) ?: continue
            packageToSlice[cls.packageName] = key
            classesByKey.getOrPut(key) { mutableListOf() }.add(cls)
            packagesByKey.getOrPut(key) { mutableSetOf() }.add(cls.packageName)
        }
        val slices = classesByKey.keys.sorted().map { Slice(it, packagesByKey.getValue(it), classesByKey.getValue(it)) }

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
        val sliceGraph = SliceCycleDetector.buildGraph(slices, packageToSlice, allClasses, slicePattern)

        val runCheck = { list: MutableList<String> -> assertions.forEach { it(sliceGraph, list) } }
        BaselineManager.checkRule(getMessage("slices.rule.violationHeader"), runCheck)
    }
}

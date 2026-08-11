/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * DSL Context wrapper that allows defining and verifying multiple independent rule suites.
 *
 * This context lets you specify module, class, function, property, and file assertions together,
 * and verify all of them in a single batch operation via [Konture.architecture].
 * All declared suites are executed even when earlier suites fail; violations are aggregated.
 */
@KontureDsl
public class KontureContext(
    private val projectGraph: ProjectGraph,
) {
    private data class RuleSuite(
        /** Filter or assertion criteria for label. */
        val label: String,
        /** Filter or assertion criteria for run. */
        val run: () -> Unit,
    )

    private val ruleSuites = mutableListOf<RuleSuite>()

    private fun addSuite(
        label: String,
        block: () -> Unit,
    ) {
        /** Filter or assertion criteria for duplicate count. */
        val duplicateCount = ruleSuites.count { it.label == label }

        /** Filter or assertion criteria for resolved label. */
        val resolvedLabel = if (duplicateCount > 0) "$label (${duplicateCount + 1})" else label
        ruleSuites.add(RuleSuite(resolvedLabel, block))
    }

    /**
     * Declares a suite of module structure/dependency rules inside this architecture validation context.
     */
    public fun modules(block: ModulesRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = ModulesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("modules") { builder.check() }
    }

    /**
     * Declares a suite of class structure/dependency rules inside this architecture validation context.
     */
    public fun classes(block: ClassesRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = ClassesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("classes") { builder.check() }
    }

    /**
     * Declares a suite of function structure/dependency rules inside this architecture validation context.
     */
    public fun functions(block: FunctionsRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = FunctionsRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("functions") { builder.check() }
    }

    /**
     * Declares a suite of property structure/dependency rules inside this architecture validation context.
     */
    public fun properties(block: PropertiesRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = PropertiesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("properties") { builder.check() }
    }

    /**
     * Declares a suite of file structure/dependency rules inside this architecture validation context.
     */
    public fun files(block: FilesRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = FilesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("files") { builder.check() }
    }

    /**
     * Declares a suite of slice rules inside this architecture validation context.
     */
    public fun slices(block: SlicesRuleBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = SlicesRuleBuilder(projectGraph)
        builder.apply(block)
        addSuite("slices") { builder.check() }
    }

    /**
     * Declares a suite of layered-architecture rules inside this architecture validation context.
     */
    public fun layeredArchitecture(block: LayeredArchitectureBuilder.() -> Unit) {
        /** Filter or assertion criteria for builder. */
        val builder = LayeredArchitectureBuilder(projectGraph)
        builder.apply(block)
        addSuite("layeredArchitecture") { builder.check() }
    }

    /**
     * Declares a nested, type-safe layered-architecture specification inside this architecture validation context.
     */
    public fun layered(block: LayeredArchitectureDsl.() -> Unit) {
        /** Filter or assertion criteria for dsl. */
        val dsl = LayeredArchitectureDsl(projectGraph)
        dsl.apply(block)
        addSuite("layered") { dsl.verify() }
    }

    internal fun verifyAll() {
        /** Filter or assertion criteria for failures. */
        val failures = mutableListOf<String>()
        for (suite in ruleSuites) {
            try {
                suite.run()
            } catch (e: AssertionError) {
                failures.add("[${suite.label}]\n${e.message}")
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(
                "Architecture validation failed in ${failures.size} suite(s):\n\n" +
                    failures.joinToString("\n\n"),
            )
        }
    }
}

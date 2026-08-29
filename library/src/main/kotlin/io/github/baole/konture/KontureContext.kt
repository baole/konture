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

    private val layerPolicies = mutableListOf<ArchitectureLayerPolicy>()

    private val sourceSetPolicies = mutableListOf<ArchitectureSourceSetPolicy>()

    /** Returns a [ClassSelector] for all classes in the project graph. */
    public val classes: ClassSelector get() = KontureScope.fromProject(projectGraph)

    /** Returns a [ModuleSelector] for all modules in the project graph. */
    public val modules: ModuleSelector get() = KontureModuleScope.fromProject(projectGraph)

    /** Returns a [FileSelector] for all files in the project graph. */
    public val files: FileSelector get() = KontureFileScope.fromProject(projectGraph)

    /** Returns a [FunctionSelector] for all functions in the project graph. */
    public val functions: FunctionSelector get() = KontureFunctionScope.fromProject(projectGraph)

    /** Returns a [PropertySelector] for all properties in the project graph. */
    public val properties: PropertySelector get() = KonturePropertyScope.fromProject(projectGraph)

    /** Returns a [SliceSelector] for packages matching [pattern] in the project graph. */
    public fun slices(pattern: String): SliceSelector = KontureSliceScope.fromProject(pattern, projectGraph)

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
     * Declares a first-class architectural layer with module/package selectors and
     * explicit dependency boundaries inside this architecture validation context.
     *
     * @param name The unique, human-readable name of the layer.
     * @param block Declarative layer policy scoped to [ArchitectureLayerPolicy].
     */
    public fun layer(
        name: String,
        block: ArchitectureLayerPolicy.() -> Unit,
    ) {
        val policy = ArchitectureLayerPolicy(name).apply(block)
        layerPolicies.add(policy)
    }

    /**
     * Declares a first-class source-set architecture policy for source sets matching [name].
     *
     * @param name The source set name (e.g., `"commonMain"`).
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        name: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        val policy = ArchitectureSourceSetPolicy(SourceSets.named(name), name).apply(block)
        sourceSetPolicies.add(policy)
    }

    /**
     * Declares a first-class source-set architecture policy for source sets matching any of [names].
     *
     * @param names The source set names.
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        vararg names: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        val selector = SourceSets.named(*names)
        val label = names.joinToString()
        val policy = ArchitectureSourceSetPolicy(selector, label).apply(block)
        sourceSetPolicies.add(policy)
    }

    /**
     * Declares a first-class source-set architecture policy using a custom [SourceSetSelector].
     *
     * @param selector The selector identifying targeted source sets.
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        selector: SourceSetSelector,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        val policy = ArchitectureSourceSetPolicy(selector, "custom").apply(block)
        sourceSetPolicies.add(policy)
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

    /**
     * Declares a named architecture rule with metadata and sub-rules inside this architecture validation context.
     */
    public fun rule(
        id: String,
        block: RuleBuilder.() -> Unit,
    ): RuleDefinition {
        val builder = RuleBuilder(id, projectGraph)
        builder.apply(block)
        val ruleDef =
            RuleDefinition(
                metadata = builder.buildMetadata(),
                executionSuites = builder.executionSuites,
                programmaticSuppressions = builder.programmaticSuppressions,
            )
        addSuite(id) { ruleDef.check() }
        return ruleDef
    }

    /**
     * Imports and executes a reusable [RuleSet] inside this architecture validation context.
     *
     * Each declaration captured by the rule set (via [architectureRules]) is replayed
     * into this context as if it had been declared inline. Multiple rule sets may be
     * applied within a single `architecture { ... }` block, and rule sets may be
     * combined with inline declarations and reused across separate blocks.
     *
     * Example:
     * ```kotlin
     * val cleanArchitecture = architectureRules {
     *     classes {
     *         that().resideInAPackage("com.example.domain..")
     *         should().satisfy { !it.isAbstract }
     *     }
     * }
     *
     * architecture {
     *     apply(cleanArchitecture)
     * }
     * ```
     *
     * @param ruleSet The reusable rule set to import and execute.
     */
    public fun apply(ruleSet: RuleSet) {
        for (declaration in ruleSet.declarations) {
            declaration()
        }
    }

    internal fun verifyAll() {
        /** Filter or assertion criteria for failures. */
        if (layerPolicies.isNotEmpty()) {
            addSuite("layers") { checkLayerPolicies(layerPolicies, projectGraph) }
        }
        if (sourceSetPolicies.isNotEmpty()) {
            addSuite("sourceSets") { checkSourceSetPolicies(sourceSetPolicies, projectGraph) }
        }
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

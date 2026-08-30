/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * DSL builder for configuring a reusable [RuleSet].
 *
 * Allows declaring module, class, function, property, file, slice, and layer rules,
 * importing existing rule sets, and writing custom extension functions.
 *
 * @property name Optional human-readable name or identifier for this rule set.
 */
@KontureDsl
public class RuleSetBuilder(
    public var name: String? = null,
) {
    /** Optional human-readable explanation or rationale for this rule set. */
    public var description: String? = null

    internal val actions = mutableListOf<(KontureContext) -> Unit>()

    /**
     * Imports and executes another [RuleSet] within this rule set.
     */
    public fun apply(ruleSet: RuleSet) {
        actions.add { context -> context.apply(ruleSet) }
    }

    /**
     * Imports and executes multiple [RuleSet] instances within this rule set.
     */
    public fun apply(vararg ruleSets: RuleSet) {
        ruleSets.forEach { apply(it) }
    }

    /**
     * Imports and executes a collection of [RuleSet] instances within this rule set.
     */
    public fun apply(ruleSets: Iterable<RuleSet>) {
        ruleSets.forEach { apply(it) }
    }

    /**
     * Imports and executes a [RuleDefinition] within this rule set.
     */
    public fun apply(rule: RuleDefinition) {
        actions.add { context -> context.apply(rule) }
    }

    /**
     * Imports and executes multiple [RuleDefinition] instances within this rule set.
     */
    public fun apply(vararg rules: RuleDefinition) {
        rules.forEach { apply(it) }
    }

    /**
     * Declares a named architecture rule with metadata and sub-rules inside this rule set.
     */
    public fun rule(
        id: String,
        block: RuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.rule(id, block) }
    }

    /**
     * Declares a suite of module structure/dependency rules inside this rule set.
     */
    public fun modules(block: ModulesRuleBuilder.() -> Unit) {
        actions.add { context -> context.modules(block) }
    }

    /**
     * Declares a suite of module structure/dependency rules scoped to specific source sets.
     */
    public fun modules(
        sourceSets: SourceSetSelector,
        block: ModulesRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.modules(sourceSets, block) }
    }

    /**
     * Declares a suite of class structure/dependency rules inside this rule set.
     */
    public fun classes(block: ClassesRuleBuilder.() -> Unit) {
        actions.add { context -> context.classes(block) }
    }

    /**
     * Declares a suite of class structure/dependency rules scoped to specific source sets.
     */
    public fun classes(
        sourceSets: SourceSetSelector,
        block: ClassesRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.classes(sourceSets, block) }
    }

    /**
     * Declares a suite of function structure/dependency rules inside this rule set.
     */
    public fun functions(block: FunctionsRuleBuilder.() -> Unit) {
        actions.add { context -> context.functions(block) }
    }

    /**
     * Declares a suite of function structure/dependency rules scoped to specific source sets.
     */
    public fun functions(
        sourceSets: SourceSetSelector,
        block: FunctionsRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.functions(sourceSets, block) }
    }

    /**
     * Declares a suite of property structure/dependency rules inside this rule set.
     */
    public fun properties(block: PropertiesRuleBuilder.() -> Unit) {
        actions.add { context -> context.properties(block) }
    }

    /**
     * Declares a suite of property structure/dependency rules scoped to specific source sets.
     */
    public fun properties(
        sourceSets: SourceSetSelector,
        block: PropertiesRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.properties(sourceSets, block) }
    }

    /**
     * Declares a suite of file structure/dependency rules inside this rule set.
     */
    public fun files(block: FilesRuleBuilder.() -> Unit) {
        actions.add { context -> context.files(block) }
    }

    /**
     * Declares a suite of file structure/dependency rules scoped to specific source sets.
     */
    public fun files(
        sourceSets: SourceSetSelector,
        block: FilesRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.files(sourceSets, block) }
    }

    /**
     * Declares a suite of slice rules inside this rule set.
     */
    public fun slices(block: SlicesRuleBuilder.() -> Unit) {
        actions.add { context -> context.slices(block) }
    }

    /**
     * Declares a suite of slice rules scoped to specific source sets.
     */
    public fun slices(
        sourceSets: SourceSetSelector,
        block: SlicesRuleBuilder.() -> Unit,
    ) {
        actions.add { context -> context.slices(sourceSets, block) }
    }

    /**
     * Declares a suite of layered-architecture rules inside this rule set.
     */
    public fun layeredArchitecture(block: LayeredArchitectureBuilder.() -> Unit) {
        actions.add { context -> context.layeredArchitecture(block) }
    }

    /**
     * Declares a nested, type-safe layered-architecture specification inside this rule set.
     */
    public fun layered(block: LayeredArchitectureDsl.() -> Unit) {
        actions.add { context -> context.layered(block) }
    }

    /**
     * Declares a first-class architectural layer with module/package selectors and
     * explicit dependency boundaries inside this rule set.
     */
    public fun layer(
        name: String,
        block: ArchitectureLayerPolicy.() -> Unit,
    ) {
        actions.add { context -> context.layer(name, block) }
    }

    /**
     * Declares a first-class source-set architecture policy for source sets matching [name].
     */
    public fun sourceSet(
        name: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        actions.add { context -> context.sourceSet(name, block) }
    }

    /**
     * Declares a first-class source-set architecture policy for source sets matching any of [names].
     */
    public fun sourceSet(
        vararg names: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        actions.add { context -> context.sourceSet(*names, block = block) }
    }

    /**
     * Declares a first-class source-set architecture policy using a custom [SourceSetSelector].
     */
    public fun sourceSet(
        selector: SourceSetSelector,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        actions.add { context -> context.sourceSet(selector, block) }
    }

    /**
     * Verifies that there are no module dependency cycles in the project.
     *
     * @param includeTestConfigurations if true, test-related dependency configurations will also be analyzed.
     */
    public fun noCycles(includeTestConfigurations: Boolean = false) {
        actions.add { context -> context.noCycles(includeTestConfigurations) }
    }

    /**
     * Alias for [noCycles].
     */
    public fun assertNoCycles(includeTestConfigurations: Boolean = false) {
        noCycles(includeTestConfigurations)
    }
}

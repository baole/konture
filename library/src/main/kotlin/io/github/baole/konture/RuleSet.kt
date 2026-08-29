/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * A reusable, packageable bundle of architecture rules.
 *
 * Created by [architectureRules], a [RuleSet] captures rule-suite declarations
 * (modules, classes, functions, properties, files, slices, layered architecture,
 * layers, source-set policies, and named rules) without binding them to a
 * specific [ProjectGraph]. The captured declarations are replayed into a
 * [KontureContext] when [KontureContext.apply] is called inside an
 * `architecture { ... }` block.
 *
 * Rule sets can be shared across projects, published as reusable Kotlin
 * libraries, and composed freely within a single architecture block. Custom
 * extension functions on [RuleSetBuilder] are the primary extension mechanism
 * for team- or organization-specific policy bundles.
 *
 * Example:
 * ```kotlin
 * val cleanArchitecture = architectureRules {
 *     classes {
 *         that().resideInAPackage("com.example.domain..")
 *         should().satisfy { !it.isAbstract }
 *     }
 *     layer("domain") {
 *         selector { packages("com.example.domain..") }
 *     }
 * }
 *
 * architecture {
 *     apply(cleanArchitecture)
 * }
 * ```
 *
 * @property declarations The captured declaration blocks, each scoped to a [KontureContext].
 */
public class RuleSet internal constructor(
    internal val declarations: List<KontureContext.() -> Unit>,
)

/**
 * Builder DSL for composing reusable [RuleSet] objects.
 *
 * Exposes the same suite-declaration surface as [KontureContext] (modules, classes,
 * functions, properties, files, slices, layered architecture, layers, source-set
 * policies, and named rules), capturing each declaration for later replay. This is
 * the receiver type for the [architectureRules] builder block and for custom
 * extension functions that bundle team-specific architecture policies.
 *
 * @see architectureRules
 */
@KontureDsl
public class RuleSetBuilder {
    private val declarations = mutableListOf<KontureContext.() -> Unit>()

    internal fun build(): RuleSet = RuleSet(declarations.toList())

    /**
     * Captures a suite of module structure/dependency rules for later replay.
     */
    public fun modules(block: ModulesRuleBuilder.() -> Unit) {
        declarations.add { modules(block) }
    }

    /**
     * Captures a suite of class structure/dependency rules for later replay.
     */
    public fun classes(block: ClassesRuleBuilder.() -> Unit) {
        declarations.add { classes(block) }
    }

    /**
     * Captures a suite of function structure/dependency rules for later replay.
     */
    public fun functions(block: FunctionsRuleBuilder.() -> Unit) {
        declarations.add { functions(block) }
    }

    /**
     * Captures a suite of property structure/dependency rules for later replay.
     */
    public fun properties(block: PropertiesRuleBuilder.() -> Unit) {
        declarations.add { properties(block) }
    }

    /**
     * Captures a suite of file structure/dependency rules for later replay.
     */
    public fun files(block: FilesRuleBuilder.() -> Unit) {
        declarations.add { files(block) }
    }

    /**
     * Captures a suite of slice rules for later replay.
     */
    public fun slices(block: SlicesRuleBuilder.() -> Unit) {
        declarations.add { slices(block) }
    }

    /**
     * Captures a suite of layered-architecture rules for later replay.
     */
    public fun layeredArchitecture(block: LayeredArchitectureBuilder.() -> Unit) {
        declarations.add { layeredArchitecture(block) }
    }

    /**
     * Captures a first-class architectural layer with module/package selectors and
     * explicit dependency boundaries for later replay.
     *
     * @param name The unique, human-readable name of the layer.
     * @param block Declarative layer policy scoped to [ArchitectureLayerPolicy].
     */
    public fun layer(
        name: String,
        block: ArchitectureLayerPolicy.() -> Unit,
    ) {
        declarations.add { layer(name, block) }
    }

    /**
     * Captures a first-class source-set architecture policy for source sets matching [name].
     *
     * @param name The source set name (e.g., `"commonMain"`).
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        name: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        declarations.add { sourceSet(name, block) }
    }

    /**
     * Captures a first-class source-set architecture policy for source sets matching any of [names].
     *
     * @param names The source set names.
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        vararg names: String,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        declarations.add { sourceSet(names = names, block = block) }
    }

    /**
     * Captures a first-class source-set architecture policy using a custom [SourceSetSelector].
     *
     * @param selector The selector identifying targeted source sets.
     * @param block Declarative source-set policy scoped to [ArchitectureSourceSetPolicy].
     */
    public fun sourceSet(
        selector: SourceSetSelector,
        block: ArchitectureSourceSetPolicy.() -> Unit,
    ) {
        declarations.add { sourceSet(selector, block) }
    }

    /**
     * Captures a nested, type-safe layered-architecture specification for later replay.
     */
    public fun layered(block: LayeredArchitectureDsl.() -> Unit) {
        declarations.add { layered(block) }
    }

    /**
     * Captures a named architecture rule with metadata and sub-rules for later replay.
     *
     * @param id Unique identifier for the rule.
     */
    public fun rule(
        id: String,
        block: RuleBuilder.() -> Unit,
    ) {
        declarations.add { rule(id, block) }
    }
}

/**
 * Creates a reusable [RuleSet] from the given [block].
 *
 * The block is scoped to [RuleSetBuilder], which mirrors the suite-declaration
 * surface of [KontureContext]. Declarations are captured (not executed) and
 * replayed later when the rule set is applied inside an `architecture { ... }`
 * block via [KontureContext.apply].
 *
 * Custom extension functions on [RuleSetBuilder] are the recommended mechanism
 * for bundling team- or organization-specific architecture policies into
 * shareable, publishable rule sets.
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
 * @param block DSL configuration block scoped to [RuleSetBuilder].
 * @return A reusable [RuleSet] capturing the declared rules.
 */
public fun architectureRules(block: RuleSetBuilder.() -> Unit): RuleSet {
    val builder = RuleSetBuilder()
    builder.apply(block)
    return builder.build()
}

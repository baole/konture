/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity

/**
 * DSL builder for configuring named architecture rules with metadata and sub-rule blocks.
 *
 * @property id Unique identifier for the rule.
 */
@KontureDsl
public class RuleBuilder(
    public val id: String,
    private val projectGraph: ProjectGraph = Konture.projectGraph,
) {
    /** Human-readable explanation or rationale for this rule. */
    public var description: String? = null

    /** Severity level associated with rule violations (defaults to [Severity.ERROR]). */
    public var severity: Severity = Severity.ERROR
    private val tags = mutableSetOf<String>()

    internal val executionSuites = mutableListOf<() -> Unit>()

    /**
     * Tag this rule with one or more arbitrary string labels.
     */
    public fun tag(vararg tagNames: String) {
        tags.addAll(tagNames)
    }

    /**
     * Constructs the [RuleMetadata] representation of this rule configuration.
     */
    public fun buildMetadata(): RuleMetadata =
        RuleMetadata(
            id = id,
            description = description,
            severity = severity,
            tags = tags.toSet(),
        )

    /**
     * Declares a class-level rule suite inside this named rule scope.
     */
    public fun classes(block: ClassesRuleBuilder.() -> Unit) {
        executionSuites.add {
            ClassesRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a class-level rule suite scoped to specific source sets.
     */
    public fun classes(
        sourceSets: SourceSetSelector,
        block: ClassesRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            ClassesRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a file-level rule suite inside this named rule scope.
     */
    public fun files(block: FilesRuleBuilder.() -> Unit) {
        executionSuites.add {
            FilesRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a file-level rule suite scoped to specific source sets.
     */
    public fun files(
        sourceSets: SourceSetSelector,
        block: FilesRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            FilesRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a function-level rule suite inside this named rule scope.
     */
    public fun functions(block: FunctionsRuleBuilder.() -> Unit) {
        executionSuites.add {
            FunctionsRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a function-level rule suite scoped to specific source sets.
     */
    public fun functions(
        sourceSets: SourceSetSelector,
        block: FunctionsRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            FunctionsRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a module-level rule suite inside this named rule scope.
     */
    public fun modules(block: ModulesRuleBuilder.() -> Unit) {
        executionSuites.add {
            ModulesRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a module-level rule suite scoped to specific source sets.
     */
    public fun modules(
        sourceSets: SourceSetSelector,
        block: ModulesRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            ModulesRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a property-level rule suite inside this named rule scope.
     */
    public fun properties(block: PropertiesRuleBuilder.() -> Unit) {
        executionSuites.add {
            PropertiesRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a property-level rule suite scoped to specific source sets.
     */
    public fun properties(
        sourceSets: SourceSetSelector,
        block: PropertiesRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            PropertiesRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a slice rule suite inside this named rule scope.
     */
    public fun slices(block: SlicesRuleBuilder.() -> Unit) {
        executionSuites.add {
            SlicesRuleBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a slice rule suite scoped to specific source sets.
     */
    public fun slices(
        sourceSets: SourceSetSelector,
        block: SlicesRuleBuilder.() -> Unit,
    ) {
        executionSuites.add {
            SlicesRuleBuilder(projectGraph, sourceSets).apply(block).check()
        }
    }

    /**
     * Declares a layered-architecture rule suite inside this named rule scope.
     */
    public fun layeredArchitecture(block: LayeredArchitectureBuilder.() -> Unit) {
        executionSuites.add {
            LayeredArchitectureBuilder(projectGraph).apply(block).check()
        }
    }

    /**
     * Declares a nested type-safe layered architecture specification inside this named rule scope.
     */
    public fun layered(block: LayeredArchitectureDsl.() -> Unit) {
        executionSuites.add {
            LayeredArchitectureDsl(projectGraph).apply(block).verify()
        }
    }
}

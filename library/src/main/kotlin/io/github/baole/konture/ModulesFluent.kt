/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

// ==========================================
// Modules Rule Builder Fluent DSL
// ==========================================

/**
 * Filters modules in this rule using a concise lambda predicate evaluated on each [Module].
 *
 * @param predicate The filter criteria block executed on the [Module].
 * @return This [ModulesRuleBuilder] with the filter condition applied.
 */
fun ModulesRuleBuilder.that(predicate: Module.() -> Boolean): ModulesRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on filtered modules using a lambda block that provides a [ModuleShouldContext] receiver.
 * Supports both imperative assertions and Boolean predicate matches.
 *
 * @param assertion The assertion block containing module validation rules or boolean predicate.
 * @return This [ModulesRuleBuilder] with the assertion block registered.
 */
fun ModulesRuleBuilder.should(assertion: ModuleShouldContext.() -> Any?): ModulesRuleBuilder =
    this.apply {
        setShould { module, graph, violations ->
            /** Filter or assertion criteria for context. */
            val context = ModuleShouldContext(module, graph, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(
                    io.github.baole.konture.i18n.getMessage("module.should.failedCustomAssertion", module.path),
                )
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [Module] element.
 * Provides easy access to all element properties and custom helper assertions.
 *
 * @property element The target [Module] being verified.
 * @property graph The overall analyzed Gradle project graph.
 * @property violations Mutable collection where assertion failure messages are appended.
 */
class ModuleShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    val element: Module,
    /** Filter or assertion criteria for graph. */
    val graph: ProjectGraph,
    /** Filter or assertion criteria for violations. */
    val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for build id. */
    val buildId get() = element.buildId

    /** Filter or assertion criteria for path. */
    val path get() = element.path

    /** Filter or assertion criteria for project dir. */
    val projectDir get() = element.projectDir

    /** Filter or assertion criteria for applied plugins. */
    val appliedPlugins get() = element.appliedPlugins

    /** Filter or assertion criteria for source sets. */
    val sourceSets get() = element.sourceSets

    /** Filter or assertion criteria for dependencies. */
    val dependencies get() = element.dependencies

    /** Filter or assertion criteria for files. */
    val files get() = element.files

    /** Filter or assertion criteria for classes. */
    val classes get() = element.classes

    /**
     * Appends a custom violation failure message to the assertion run.
     */
    fun addViolation(message: String) {
        violations.add(message)
    }

    /**
     * Asserts [condition] is true, recording a violation with [message] when false.
     * When [message] is omitted, a default message referencing [element] is used.
     */
    fun check(
        condition: Boolean,
        message: String? = null,
    ) {
        if (!condition) {
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("module.should.failedAssertion", path))
        }
    }
}

/** Filters modules that depend on [targetModulePath]. */
fun List<Module>.dependingOnModule(targetModulePath: String): List<Module> =
    filter { module ->
        module.dependencies.any { dep ->
            dep.targetPath == targetModulePath || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(targetModulePath, dep.targetPath)
        }
    }

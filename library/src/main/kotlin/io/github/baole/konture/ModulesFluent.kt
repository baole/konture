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
            val context = ModuleShouldContext(module, graph, violations)
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add("Module ${module.path} failed custom assertion")
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
    val element: Module,
    val graph: ProjectGraph,
    val violations: MutableList<String>,
) {
    val buildId get() = element.buildId
    val path get() = element.path
    val projectDir get() = element.projectDir
    val appliedPlugins get() = element.appliedPlugins
    val sourceSets get() = element.sourceSets
    val dependencies get() = element.dependencies
    val files get() = element.files
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
            addViolation(message ?: "Module $path failed assertion")
        }
    }
}

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.normalizeModulePath

/**
 * Fluent API for defining filtering conditions on Gradle modules.
 */
@KontureDsl
class ModulesThat internal constructor(
    private val builder: ModulesRuleBuilder,
) {
    infix fun haveNamePath(path: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(path)
        builder.setThat { it.path == normalized }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified list of paths.
     *
     * @param paths The list of Gradle paths of the module (e.g., ":core", ":app").
     */
    infix fun haveNamePath(paths: List<String>): ModulesRuleBuilder {
        val normalizedPaths = paths.map { normalizeModulePath(it) }
        builder.setThat { normalizedPaths.contains(it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified paths.
     *
     * @param paths The vararg list of Gradle paths of the module (e.g., ":core", ":app").
     */
    fun haveNamePath(vararg paths: String): ModulesRuleBuilder = haveNamePath(paths.asList())

    /**
     * Restricts the rules to modules with a Gradle path matching the given predicate.
     *
     * @param predicate The predicate to match the module path.
     */
    infix fun haveNamePath(predicate: (String) -> Boolean): ModulesRuleBuilder {
        builder.setThat { predicate(it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches the specified glob pattern.
     *
     * @param pattern Glob pattern (e.g., ":feature-*", ":core-**").
     */
    infix fun haveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setThat { PatternMatchers.matchesModuleGlob(pattern, it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    infix fun haveNameMatching(patterns: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> patterns.any { PatternMatchers.matchesModuleGlob(it, module.path) } }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    fun haveNameMatching(vararg patterns: String): ModulesRuleBuilder = haveNameMatching(patterns.toList())

    /**
     * Restricts the rules to modules matching the specified predicate.
     *
     * @param predicate The predicate to filter modules.
     */
    infix fun matching(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Satisfies an arbitrary custom predicate logic.
     */
    fun satisfy(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Matches if any of the nested condition blocks are satisfied.
     */
    fun anyOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /**
     * Matches if all of the nested condition blocks are satisfied.
     */
    fun allOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /**
     * Matches if none of the nested condition blocks are satisfied.
     */
    fun noneOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}

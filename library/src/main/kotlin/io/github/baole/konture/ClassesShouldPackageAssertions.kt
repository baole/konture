/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
@Suppress("ComplexInterface")
interface ClassesShouldPackageAssertions {
    val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes reside in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, cls.packageName)) {
                violations.add(
                    getMessage("class.should.resideInPackage", cls.fqName, packagePattern, cls.packageName),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns List of package matching patterns.
     */
    infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, cls.packageName) }
            if (!matches) {
                violations.add(
                    getMessage(
                        "class.should.resideInPackageAny",
                        cls.fqName,
                        packagePatterns.joinToString(),
                        cls.packageName,
                    ),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns Package matching patterns.
     */
    fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param predicate Predicate checking package name.
     */
    infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder =
        resideInAPackage("custom package predicate", predicate)

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking package name.
     */
    fun resideInAPackage(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!predicate(cls.packageName)) {
                violations.add(
                    getMessage("class.should.resideInPackageMatching", cls.fqName, description, cls.packageName),
                )
            }
        }
        return builder
    }

    infix fun notResideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, cls.packageName)) {
                violations.add(
                    getMessage("class.should.notResideInPackage", cls.fqName, packagePattern, cls.packageName),
                )
            }
        }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, cls.packageName) }
            if (matches) {
                violations.add(
                    getMessage(
                        "class.should.notResideInPackageAny",
                        cls.fqName,
                        packagePatterns.joinToString(),
                        cls.packageName,
                    ),
                )
            }
        }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    infix fun resideInAModule(modulePath: String): ClassesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { cls, _, violations ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            if (module?.path != normalized) {
                violations.add(
                    getMessage("class.should.resideInModule", cls.fqName, normalized, module?.path ?: "unknown"),
                )
            }
        }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { cls, _, violations ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            if (module == null || !normalizedPaths.contains(module.path)) {
                violations.add(
                    getMessage(
                        "class.should.resideInModule",
                        cls.fqName,
                        normalizedPaths.joinToString(),
                        module?.path ?: "unknown",
                    ),
                )
            }
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun resideInModule(modulePath: String): ClassesRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): ClassesRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): ClassesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { cls, _, violations ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            if (module?.path == normalized) {
                violations.add(getMessage("class.should.notResideInModule", cls.fqName, normalized))
            }
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { cls, _, violations ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            if (module != null && normalizedPaths.contains(module.path)) {
                violations.add(
                    getMessage("class.should.notResideInModuleAny", cls.fqName, normalizedPaths.joinToString()),
                )
            }
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): ClassesRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): ClassesRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())

    /**
     * Asserts that selected classes have simple names ending with the specified suffix.
     *
     * @param suffix The expected name suffix.
     */
    infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.name.endsWith(suffix)) {
                violations.add(getMessage("class.should.haveNameEndingWith", cls.fqName, suffix))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names ending with any of the specified suffixes.
     *
     * @param suffixes The expected name suffixes.
     */
    infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = suffixes.any { cls.name.endsWith(it) }
            if (!matches) {
                violations.add(getMessage("class.should.haveNameEndingWithAny", cls.fqName, suffixes.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names ending with any of the specified suffixes.
     *
     * @param suffixes The expected name suffixes.
     */
    fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Asserts that selected classes have simple names starting with the specified prefix.
     *
     * @param prefix The expected name prefix.
     */
    infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.name.startsWith(prefix)) {
                violations.add(getMessage("class.should.haveNameStartingWith", cls.fqName, prefix))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names starting with any of the specified prefixes.
     *
     * @param prefixes The expected name prefixes.
     */
    infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = prefixes.any { cls.name.startsWith(it) }
            if (!matches) {
                violations.add(getMessage("class.should.haveNameStartingWithAny", cls.fqName, prefixes.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names starting with any of the specified prefixes.
     *
     * @param prefixes The expected name prefixes.
     */
    fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param predicate Predicate checking class simple name.
     */
    infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder =
        haveName("custom name predicate", predicate)

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking class simple name.
     */
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!predicate(cls.name)) {
                violations.add(getMessage("class.should.haveNameMatching", cls.fqName, description, cls.name))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching the specified glob pattern.
     * Supports '*' wildcards.
     *
     * @param pattern Glob pattern (e.g. "*UseCase", "*Repository").
     */
    infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, cls.name)) {
                violations.add(getMessage("class.should.haveNameMatchingPattern", cls.fqName, pattern))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, cls.name) }
            if (!matches) {
                violations.add(
                    getMessage("class.should.haveNameMatchingPatternAny", cls.fqName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun haveName(name: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name != name) {
                violations.add(getMessage("class.should.haveNameMatchingPattern", cls.fqName, name))
            }
        }
        return builder
    }

    infix fun haveName(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!names.contains(cls.name)) {
                violations.add(getMessage("class.should.haveNameMatchingPatternAny", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    fun haveName(vararg names: String): ClassesRuleBuilder = haveName(names.toList())

    infix fun notHaveName(name: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name == name) {
                violations.add(getMessage("class.should.notHaveName", cls.fqName, name))
            }
        }
        return builder
    }

    infix fun notHaveName(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (names.contains(cls.name)) {
                violations.add(getMessage("class.should.notHaveNameIn", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    fun notHaveName(vararg names: String): ClassesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, cls.name)) {
                violations.add(getMessage("class.should.notHaveNameMatching", cls.fqName, pattern))
            }
        }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matching = patterns.filter { PatternMatchers.matchesSimpleGlob(it, cls.name) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameMatching", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notHaveNameMatching(patterns.toList())

    infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name.startsWith(prefix)) {
                violations.add(getMessage("class.should.notHaveNameStartingWith", cls.fqName, prefix))
            }
        }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matching = prefixes.filter { cls.name.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameStartingWith", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name.endsWith(suffix)) {
                violations.add(getMessage("class.should.notHaveNameEndingWith", cls.fqName, suffix))
            }
        }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matching = suffixes.filter { cls.name.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameEndingWith", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = notHaveNameEndingWith(suffixes.toList())
}

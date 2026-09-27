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
public interface ClassesShouldPackageAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes reside in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    public infix fun inPackage(packagePattern: String): ClassesRuleBuilder {
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
    public infix fun inPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matches. */
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
    public fun inPackage(vararg packagePatterns: String): ClassesRuleBuilder = inPackage(packagePatterns.toList())

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param predicate Predicate checking package name.
     */
    public infix fun inPackage(predicate: (String) -> Boolean): ClassesRuleBuilder =
        inPackage("custom package predicate", predicate)

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking package name.
     */
    public fun inPackage(
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

    /** Asserts that selected classes reside in the package of the specified type. */
    public infix fun inPackageOf(type: kotlin.reflect.KClass<*>): ClassesRuleBuilder =
        inPackage(type.toKonturePackageReference().packageName)

    /**
     * Asserts that selected classes do not reside in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    public infix fun notInPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, cls.packageName)) {
                violations.add(
                    getMessage("class.should.notResideInPackage", cls.fqName, packagePattern, cls.packageName),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes do not reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns List of package matching patterns.
     */
    public infix fun notInPackage(packagePatterns: List<String>): ClassesRuleBuilder {
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

    /**
     * Asserts that selected classes do not reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns Package matching patterns.
     */
    public fun notInPackage(vararg packagePatterns: String): ClassesRuleBuilder = notInPackage(packagePatterns.toList())

    /**
     * Asserts that selected classes reside in a module matching the specified path.
     *
     * @param modulePath Module path.
     */
    public infix fun inModule(modulePath: String): ClassesRuleBuilder {
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

    /**
     * Asserts that selected classes reside in any module matching the specified paths.
     *
     * @param modulePaths List of module paths.
     */
    public infix fun inModule(modulePaths: List<String>): ClassesRuleBuilder {
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

    /**
     * Asserts that selected classes reside in any module matching the specified paths.
     *
     * @param modulePaths Module paths.
     */
    public fun inModule(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /**
     * Asserts that selected classes reside in any module matching the specified paths.
     *
     * @param modulePaths List of module paths.
     */
    public infix fun inModules(modulePaths: List<String>): ClassesRuleBuilder = inModule(modulePaths)

    /**
     * Asserts that selected classes reside in any module matching the specified paths.
     *
     * @param modulePaths Module paths.
     */
    public fun inModules(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /**
     * Asserts that selected classes do not reside in a module matching the specified path.
     *
     * @param modulePath Module path.
     */
    public infix fun notInModule(modulePath: String): ClassesRuleBuilder {
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

    /**
     * Asserts that selected classes do not reside in any module matching the specified paths.
     *
     * @param modulePaths List of module paths.
     */
    public infix fun notInModule(modulePaths: List<String>): ClassesRuleBuilder {
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

    /**
     * Asserts that selected classes do not reside in any module matching the specified paths.
     *
     * @param modulePaths Module paths.
     */
    public fun notInModule(vararg modulePaths: String): ClassesRuleBuilder = notInModule(modulePaths.toList())

    /**
     * Asserts that selected classes do not reside in any module matching the specified paths.
     *
     * @param modulePaths List of module paths.
     */
    public infix fun notInModules(modulePaths: List<String>): ClassesRuleBuilder = notInModule(modulePaths)

    /**
     * Asserts that selected classes do not reside in any module matching the specified paths.
     *
     * @param modulePaths Module paths.
     */
    public fun notInModules(vararg modulePaths: String): ClassesRuleBuilder = notInModule(modulePaths.toList())

    /**
     * Asserts that selected classes have simple names ending with the specified suffix.
     *
     * @param suffix The expected name suffix.
     */
    public infix fun nameEndsWith(suffix: String): ClassesRuleBuilder {
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
    public infix fun nameEndsWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matches. */
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
    public fun nameEndsWith(vararg suffixes: String): ClassesRuleBuilder = nameEndsWith(suffixes.toList())

    /**
     * Asserts that selected classes have simple names starting with the specified prefix.
     *
     * @param prefix The expected name prefix.
     */
    public infix fun nameStartsWith(prefix: String): ClassesRuleBuilder {
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
    public infix fun nameStartsWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matches. */
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
    public fun nameStartsWith(vararg prefixes: String): ClassesRuleBuilder = nameStartsWith(prefixes.toList())

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param predicate Predicate checking class simple name.
     */
    public infix fun named(predicate: (String) -> Boolean): ClassesRuleBuilder =
        named("custom name predicate", predicate)

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking class simple name.
     */
    public fun named(
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
    public infix fun nameMatches(pattern: String): ClassesRuleBuilder {
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
    public infix fun nameMatches(patterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matches. */
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
    public fun nameMatches(vararg patterns: String): ClassesRuleBuilder = nameMatches(patterns.toList())

    /** Filter or assertion criteria for named. */
    public infix fun named(name: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name != name) {
                violations.add(getMessage("class.should.haveNameMatchingPattern", cls.fqName, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for simple named. */
    public infix fun simpleNamed(name: String): ClassesRuleBuilder = named(name)

    /** Filter or assertion criteria for named. */
    public infix fun named(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!names.contains(cls.name)) {
                violations.add(getMessage("class.should.haveNameMatchingPatternAny", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for named. */
    public fun named(vararg names: String): ClassesRuleBuilder = named(names.toList())

    /** Filter or assertion criteria for not named. */
    public infix fun notNamed(name: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name == name) {
                violations.add(getMessage("class.should.notHaveName", cls.fqName, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not named. */
    public infix fun notNamed(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (names.contains(cls.name)) {
                violations.add(getMessage("class.should.notHaveNameIn", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not named. */
    public fun notNamed(vararg names: String): ClassesRuleBuilder = notNamed(names.toList())

    /** Filter or assertion criteria for not name matches. */
    public infix fun notNameMatches(pattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, cls.name)) {
                violations.add(getMessage("class.should.notHaveNameMatching", cls.fqName, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name matches. */
    public infix fun notNameMatches(patterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = patterns.filter { PatternMatchers.matchesSimpleGlob(it, cls.name) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameMatching", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name matches. */
    public fun notNameMatches(vararg patterns: String): ClassesRuleBuilder = notNameMatches(patterns.toList())

    /** Filter or assertion criteria for not name starts with. */
    public infix fun notNameStartsWith(prefix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name.startsWith(prefix)) {
                violations.add(getMessage("class.should.notHaveNameStartingWith", cls.fqName, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name starts with. */
    public infix fun notNameStartsWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = prefixes.filter { cls.name.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameStartingWith", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name starts with. */
    public fun notNameStartsWith(vararg prefixes: String): ClassesRuleBuilder =
        notNameStartsWith(
            prefixes.toList(),
        )

    /** Filter or assertion criteria for not name ends with. */
    public infix fun notNameEndsWith(suffix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.name.endsWith(suffix)) {
                violations.add(getMessage("class.should.notHaveNameEndingWith", cls.fqName, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name ends with. */
    public infix fun notNameEndsWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = suffixes.filter { cls.name.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("class.should.notHaveNameEndingWith", cls.fqName, matching.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name ends with. */
    public fun notNameEndsWith(vararg suffixes: String): ClassesRuleBuilder = notNameEndsWith(suffixes.toList())
}

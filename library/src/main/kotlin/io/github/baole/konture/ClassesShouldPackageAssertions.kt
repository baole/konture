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

    /** Filter or assertion criteria for not in package. */
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

    /** Filter or assertion criteria for not in package. */
    public infix fun notInPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for not in package. */
    public fun notInPackage(vararg packagePatterns: String): ClassesRuleBuilder = notInPackage(packagePatterns.toList())

    /** Filter or assertion criteria for in module. */
    public infix fun inModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for module. */
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

    /** Filter or assertion criteria for in module. */
    public infix fun inModule(modulePaths: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for module. */
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

    /** Filter or assertion criteria for in module. */
    public fun inModule(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /** Filter or assertion criteria for in modules. */
    public infix fun inModules(modulePaths: List<String>): ClassesRuleBuilder = inModule(modulePaths)

    /** Filter or assertion criteria for in modules. */
    public fun inModules(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /** Filter or assertion criteria for not in module. */
    public infix fun notInModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for module. */
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

    /** Filter or assertion criteria for not in module. */
    public infix fun notInModule(modulePaths: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for module. */
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

    /** Filter or assertion criteria for not in module. */
    public fun notInModule(vararg modulePaths: String): ClassesRuleBuilder = notInModule(modulePaths.toList())

    /** Filter or assertion criteria for not in modules. */
    public infix fun notInModules(modulePaths: List<String>): ClassesRuleBuilder = notInModule(modulePaths)

    /** Filter or assertion criteria for not in modules. */
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

    // --- Legacy Deprecations ---

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePattern)"))
    public infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder = inPackage(packagePattern)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePatterns)"))
    public infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder = inPackage(packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(*packagePatterns)"))
    public fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder = inPackage(*packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(predicate)"))
    public infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder = inPackage(predicate)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(description, predicate)"))
    public fun resideInAPackage(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder = inPackage(description, predicate)

    /** Legacy resideInPackageOf method. */
    @Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf(type)"))
    public infix fun resideInPackageOf(type: kotlin.reflect.KClass<*>): ClassesRuleBuilder = inPackageOf(type)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePattern)"))
    public infix fun notResideInAPackage(packagePattern: String): ClassesRuleBuilder = notInPackage(packagePattern)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePatterns)"))
    public infix fun notResideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        notInPackage(packagePatterns)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(*packagePatterns)"))
    public fun notResideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder = notInPackage(*packagePatterns)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInAModule(modulePath: String): ClassesRuleBuilder = inModule(modulePath)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePaths)"))
    public infix fun resideInAModule(modulePaths: List<String>): ClassesRuleBuilder = inModule(modulePaths)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(*modulePaths)"))
    public fun resideInAModule(vararg modulePaths: String): ClassesRuleBuilder = inModule(*modulePaths)

    /** Legacy resideInModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInModule(modulePath: String): ClassesRuleBuilder = inModule(modulePath)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(modulePaths)"))
    public infix fun resideInModules(modulePaths: List<String>): ClassesRuleBuilder = inModules(modulePaths)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(*modulePaths)"))
    public fun resideInModules(vararg modulePaths: String): ClassesRuleBuilder = inModules(*modulePaths)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInAModule(modulePath: String): ClassesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePaths)"))
    public infix fun notResideInAModule(modulePaths: List<String>): ClassesRuleBuilder = notInModule(modulePaths)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(*modulePaths)"))
    public fun notResideInAModule(vararg modulePaths: String): ClassesRuleBuilder = notInModule(*modulePaths)

    /** Legacy notResideInModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInModule(modulePath: String): ClassesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(modulePaths)"))
    public infix fun notResideInModules(modulePaths: List<String>): ClassesRuleBuilder = notInModules(modulePaths)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(*modulePaths)"))
    public fun notResideInModules(vararg modulePaths: String): ClassesRuleBuilder = notInModules(*modulePaths)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffix)"))
    public infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder = nameEndsWith(suffix)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffixes)"))
    public infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder = nameEndsWith(suffixes)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(*suffixes)"))
    public fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = nameEndsWith(*suffixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefix)"))
    public infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder = nameStartsWith(prefix)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefixes)"))
    public infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder = nameStartsWith(prefixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(*prefixes)"))
    public fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = nameStartsWith(*prefixes)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(predicate)"))
    public infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder = named(predicate)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(description, predicate)"))
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder = named(description, predicate)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(pattern)"))
    public infix fun haveNameMatching(pattern: String): ClassesRuleBuilder = nameMatches(pattern)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(patterns)"))
    public infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder = nameMatches(patterns)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(*patterns)"))
    public fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = nameMatches(*patterns)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(name)"))
    public infix fun haveName(name: String): ClassesRuleBuilder = named(name)

    /** Legacy haveSimpleName method. */
    @Deprecated("Use simpleNamed instead.", ReplaceWith("simpleNamed(name)"))
    public infix fun haveSimpleName(name: String): ClassesRuleBuilder = simpleNamed(name)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(names)"))
    public infix fun haveName(names: List<String>): ClassesRuleBuilder = named(names)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(*names)"))
    public fun haveName(vararg names: String): ClassesRuleBuilder = named(*names)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(name)"))
    public infix fun notHaveName(name: String): ClassesRuleBuilder = notNamed(name)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(names)"))
    public infix fun notHaveName(names: List<String>): ClassesRuleBuilder = notNamed(names)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(*names)"))
    public fun notHaveName(vararg names: String): ClassesRuleBuilder = notNamed(*names)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(pattern)"))
    public infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder = notNameMatches(pattern)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(patterns)"))
    public infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder = notNameMatches(patterns)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(*patterns)"))
    public fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notNameMatches(*patterns)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefix)"))
    public infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder = notNameStartsWith(prefix)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefixes)"))
    public infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder = notNameStartsWith(prefixes)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(*prefixes)"))
    public fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = notNameStartsWith(*prefixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffix)"))
    public infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder = notNameEndsWith(suffix)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffixes)"))
    public infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder = notNameEndsWith(suffixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(*suffixes)"))
    public fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = notNameEndsWith(*suffixes)
}

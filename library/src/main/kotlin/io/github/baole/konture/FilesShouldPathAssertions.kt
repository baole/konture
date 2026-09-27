/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

/** Path and package assertions for file rules. */
@Suppress("ComplexInterface")
public interface FilesShouldPathAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FilesRuleBuilder

    /** Asserts that files reside in a package matching [packagePattern]. */
    public infix fun inPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage(
                        "file.should.resideInPackage",
                        file.declaration.name,
                        packagePattern,
                        file.declaration.packageName,
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that files reside in packages matching [packagePatterns]. */
    public infix fun inPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, file.declaration.packageName) }
            if (!matches) {
                violations.add(
                    getMessage(
                        "file.should.resideInPackageAny",
                        file.declaration.name,
                        packagePatterns.joinToString(),
                        file.declaration.packageName,
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that files reside in packages matching [packagePatterns]. */
    public fun inPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(packagePatterns.toList())

    /** Asserts that files reside in a package matching [predicate]. */
    public infix fun inPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.packageName)) {
                violations.add(
                    getMessage(
                        "file.should.resideInPackageMatching",
                        file.declaration.name,
                        file.declaration.packageName,
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that files do not reside in a package matching [packagePattern]. */
    public infix fun notInPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage("file.should.notResideInPackage", file.declaration.name, packagePattern),
                )
            }
        }
        return builder
    }

    /** Asserts that files do not reside in packages matching [packagePatterns]. */
    public infix fun notInPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (packagePatterns.any { PatternMatchers.matchesPackage(it, file.declaration.packageName) }) {
                violations.add(
                    getMessage(
                        "file.should.notResideInPackageAny",
                        file.declaration.name,
                        packagePatterns.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that files do not reside in packages matching [packagePatterns]. */
    public fun notInPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(packagePatterns.toList())

    /** Asserts that files reside in a module matching [modulePath]. */
    public infix fun inModule(modulePath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.modulePath != modulePath && !PatternMatchers.matchesModuleGlob(modulePath, file.modulePath)) {
                violations.add(
                    getMessage("file.should.resideInModule", file.declaration.name, modulePath, file.modulePath),
                )
            }
        }
        return builder
    }

    /** Asserts that files reside in modules matching [modulePaths]. */
    public infix fun inModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (modulePaths.none { file.modulePath == it || PatternMatchers.matchesModuleGlob(it, file.modulePath) }) {
                violations.add(
                    getMessage("file.should.resideInModuleAny", file.declaration.name, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that files reside in modules matching [modulePaths]. */
    public fun inModules(vararg modulePaths: String): FilesRuleBuilder = inModules(modulePaths.toList())

    /** Asserts that files do not reside in a module matching [modulePath]. */
    public infix fun notInModule(modulePath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.modulePath == modulePath || PatternMatchers.matchesModuleGlob(modulePath, file.modulePath)) {
                violations.add(
                    getMessage("file.should.notResideInModule", file.declaration.name, modulePath),
                )
            }
        }
        return builder
    }

    /** Asserts that files do not reside in modules matching [modulePaths]. */
    public infix fun notInModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (modulePaths.any { it == file.modulePath || PatternMatchers.matchesModuleGlob(it, file.modulePath) }) {
                violations.add(
                    getMessage("file.should.notResideInModuleAny", file.declaration.name, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that files do not reside in modules matching [modulePaths]. */
    public fun notInModules(vararg modulePaths: String): FilesRuleBuilder = notInModules(modulePaths.toList())

    /** Filter or assertion criteria for name matching. */
    public infix fun nameMatches(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name matching. */
    public infix fun nameMatches(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameMatchingAny", file.declaration.name, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name matching. */
    public fun nameMatches(vararg patterns: String): FilesRuleBuilder = nameMatches(patterns.toList())

    /** Filter or assertion criteria for name starting with. */
    public infix fun nameStartsWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.haveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name starting with. */
    public infix fun nameStartsWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = prefixes.any { file.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameStartingWithAny", file.declaration.name, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name starting with. */
    public fun nameStartsWith(vararg prefixes: String): FilesRuleBuilder = nameStartsWith(prefixes.toList())

    /** Filter or assertion criteria for name ending with. */
    public infix fun nameEndsWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.haveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name ending with. */
    public infix fun nameEndsWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = suffixes.any { file.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameEndingWithAny", file.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for name ending with. */
    public fun nameEndsWith(vararg suffixes: String): FilesRuleBuilder = nameEndsWith(suffixes.toList())

    /** Filter or assertion criteria for named. */
    public infix fun named(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name != name) {
                violations.add(getMessage("file.should.haveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for named. */
    public infix fun named(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for named. */
    public fun named(vararg names: String): FilesRuleBuilder = named(names.toList())

    /** Filter or assertion criteria for named. */
    public infix fun named(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for notNamed. */
    public infix fun notNamed(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name == name) {
                violations.add(getMessage("file.should.notHaveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for notNamed. */
    public infix fun notNamed(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for notNamed. */
    public fun notNamed(vararg names: String): FilesRuleBuilder = notNamed(names.toList())

    /** Filter or assertion criteria for not name matching. */
    public infix fun notNameMatches(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name matching. */
    public infix fun notNameMatches(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }) {
                violations.add(
                    getMessage("file.should.notHaveNameMatchingAny", file.declaration.name, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name matching. */
    public fun notNameMatches(vararg patterns: String): FilesRuleBuilder = notNameMatches(patterns.toList())

    /** Filter or assertion criteria for not name starting with. */
    public infix fun notNameStartsWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.notHaveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name starting with. */
    public infix fun notNameStartsWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (prefixes.any { file.declaration.name.startsWith(it) }) {
                violations.add(
                    getMessage(
                        "file.should.notHaveNameStartingWithAny",
                        file.declaration.name,
                        prefixes.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name starting with. */
    public fun notNameStartsWith(vararg prefixes: String): FilesRuleBuilder = notNameStartsWith(prefixes.toList())

    /** Filter or assertion criteria for not name ending with. */
    public infix fun notNameEndsWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.notHaveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name ending with. */
    public infix fun notNameEndsWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (suffixes.any { file.declaration.name.endsWith(it) }) {
                violations.add(
                    getMessage("file.should.notHaveNameEndingWithAny", file.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not name ending with. */
    public fun notNameEndsWith(vararg suffixes: String): FilesRuleBuilder = notNameEndsWith(suffixes.toList())
}

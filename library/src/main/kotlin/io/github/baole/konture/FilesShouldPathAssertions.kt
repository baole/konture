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
    val builder: FilesRuleBuilder

    /** Filter or assertion criteria for reside in package. */
    infix fun resideInPackage(packagePattern: String): FilesRuleBuilder {
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

    /** Filter or assertion criteria for reside in package. */
    infix fun resideInPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for reside in package. */
    fun resideInPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in package. */
    infix fun resideInPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
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

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder = resideInPackage(packagePattern)

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = resideInPackage(packagePatterns)

    /** Filter or assertion criteria for reside in a package. */
    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder = resideInPackage(predicate)

    /** Filter or assertion criteria for not reside in package. */
    infix fun notResideInPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage("file.should.notResideInPackage", file.declaration.name, packagePattern),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in package. */
    infix fun notResideInPackage(packagePatterns: List<String>): FilesRuleBuilder {
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

    /** Filter or assertion criteria for not reside in package. */
    fun notResideInPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for not reside in a package. */
    infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder = notResideInPackage(packagePattern)

    /** Filter or assertion criteria for not reside in a package. */
    infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = notResideInPackage(packagePatterns)

    /** Filter or assertion criteria for not reside in a package. */
    fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = notResideInPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in module. */
    infix fun resideInModule(modulePath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.modulePath != modulePath && !PatternMatchers.matchesModuleGlob(modulePath, file.modulePath)) {
                violations.add(
                    getMessage("file.should.resideInModule", file.declaration.name, modulePath, file.modulePath),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in modules. */
    infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (modulePaths.none { file.modulePath == it || PatternMatchers.matchesModuleGlob(it, file.modulePath) }) {
                violations.add(
                    getMessage("file.should.resideInModuleAny", file.declaration.name, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in modules. */
    fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = resideInModules(modulePaths.toList())

    /** Filter or assertion criteria for reside in a module. */
    infix fun resideInAModule(modulePath: String): FilesRuleBuilder = resideInModule(modulePath)

    /** Filter or assertion criteria for reside in a module. */
    infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder = resideInModules(modulePaths)

    /** Filter or assertion criteria for reside in a module. */
    fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInModules(*modulePaths)

    /** Filter or assertion criteria for not reside in module. */
    infix fun notResideInModule(modulePath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.modulePath == modulePath || PatternMatchers.matchesModuleGlob(modulePath, file.modulePath)) {
                violations.add(
                    getMessage("file.should.notResideInModule", file.declaration.name, modulePath),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in modules. */
    infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (modulePaths.any { it == file.modulePath || PatternMatchers.matchesModuleGlob(it, file.modulePath) }) {
                violations.add(
                    getMessage("file.should.notResideInModuleAny", file.declaration.name, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in modules. */
    fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder = notResideInModules(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    infix fun notResideInAModule(modulePath: String): FilesRuleBuilder = notResideInModule(modulePath)

    /** Filter or assertion criteria for not reside in a module. */
    infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder = notResideInModules(modulePaths)

    /** Filter or assertion criteria for not reside in a module. */
    fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notResideInModules(*modulePaths)

    /** Filter or assertion criteria for have name matching. */
    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
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

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for have name starting with. */
    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.haveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
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

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name ending with. */
    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.haveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
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

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name. */
    infix fun haveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name != name) {
                violations.add(getMessage("file.should.haveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    infix fun haveName(names: List<String>): FilesRuleBuilder = haveNameIn(names)

    /** Filter or assertion criteria for have name. */
    fun haveName(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    /** Filter or assertion criteria for have name. */
    infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name in. */
    infix fun haveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name in. */
    fun haveNameIn(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    /** Filter or assertion criteria for not have name. */
    infix fun notHaveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name == name) {
                violations.add(getMessage("file.should.notHaveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    infix fun notHaveName(names: List<String>): FilesRuleBuilder = notHaveNameIn(names)

    /** Filter or assertion criteria for not have name. */
    fun notHaveName(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    /** Filter or assertion criteria for not have name in. */
    infix fun notHaveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name in. */
    fun notHaveNameIn(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    /** Filter or assertion criteria for not have name matching. */
    infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }) {
                violations.add(
                    getMessage("file.should.notHaveNameMatchingAny", file.declaration.name, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name starting with. */
    infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.notHaveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
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

    /** Filter or assertion criteria for not have name starting with. */
    fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = notHaveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.notHaveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (suffixes.any { file.declaration.name.endsWith(it) }) {
                violations.add(
                    getMessage("file.should.notHaveNameEndingWithAny", file.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = notHaveNameEndingWith(suffixes.toList())
}

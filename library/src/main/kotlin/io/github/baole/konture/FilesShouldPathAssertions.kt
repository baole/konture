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

    /** Filter or assertion criteria for reside in package. */
    public infix fun resideInPackage(packagePattern: String): FilesRuleBuilder {
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
    public infix fun resideInPackage(packagePatterns: List<String>): FilesRuleBuilder {
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
    public fun resideInPackage(vararg packagePatterns: String): FilesRuleBuilder =
        resideInPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in package. */
    public infix fun resideInPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
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
    public infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder = resideInPackage(packagePattern)

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder =
        resideInPackage(packagePatterns)

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder = resideInPackage(predicate)

    /** Filter or assertion criteria for not reside in package. */
    public infix fun notResideInPackage(packagePattern: String): FilesRuleBuilder {
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
    public infix fun notResideInPackage(packagePatterns: List<String>): FilesRuleBuilder {
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
    public fun notResideInPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder = notResideInPackage(packagePattern)

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder =
        notResideInPackage(packagePatterns)

    /** Filter or assertion criteria for not reside in a package. */
    public fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): FilesRuleBuilder {
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
    public infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder {
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
    public fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = resideInModules(modulePaths.toList())

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePath: String): FilesRuleBuilder = resideInModule(modulePath)

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder = resideInModules(modulePaths)

    /** Filter or assertion criteria for reside in a module. */
    public fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInModules(*modulePaths)

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): FilesRuleBuilder {
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
    public infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder {
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
    public fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder =
        notResideInModules(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): FilesRuleBuilder = notResideInModule(modulePath)

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder = notResideInModules(modulePaths)

    /** Filter or assertion criteria for not reside in a module. */
    public fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notResideInModules(*modulePaths)

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
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
    public fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.haveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
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
    public fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.haveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
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
    public fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name != name) {
                violations.add(getMessage("file.should.haveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): FilesRuleBuilder = haveNameIn(names)

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name in. */
    public infix fun haveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name in. */
    public fun haveNameIn(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name == name) {
                violations.add(getMessage("file.should.notHaveName", file.declaration.name, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(names: List<String>): FilesRuleBuilder = notHaveNameIn(names)

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    /** Filter or assertion criteria for not have name in. */
    public infix fun notHaveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name in. */
    public fun notHaveNameIn(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder {
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
    public fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.notHaveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
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
    public fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.notHaveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
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
    public fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())
}

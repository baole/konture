/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

@Suppress("ComplexInterface")
interface FilesShouldPathAssertions {
    val builder: FilesRuleBuilder

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

    infix fun resideInPackage(packagePatterns: List<String>): FilesRuleBuilder {
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

    fun resideInPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInPackage(packagePatterns.toList())

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

    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder = resideInPackage(packagePattern)

    infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = resideInPackage(packagePatterns)

    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInPackage(*packagePatterns)

    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder = resideInPackage(predicate)

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

    fun notResideInPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInPackage(
            packagePatterns.toList(),
        )

    infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder = notResideInPackage(packagePattern)

    infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = notResideInPackage(packagePatterns)

    fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = notResideInPackage(*packagePatterns)

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

    fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = resideInModules(modulePaths.toList())

    infix fun resideInAModule(modulePath: String): FilesRuleBuilder = resideInModule(modulePath)

    infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder = resideInModules(modulePaths)

    fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInModules(*modulePaths)

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

    fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder = notResideInModules(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FilesRuleBuilder = notResideInModule(modulePath)

    infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder = notResideInModules(modulePaths)

    fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notResideInModules(*modulePaths)

    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameMatchingAny", file.declaration.name, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.haveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = prefixes.any { file.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameStartingWithAny", file.declaration.name, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.haveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = suffixes.any { file.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameEndingWithAny", file.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name != name) {
                violations.add(getMessage("file.should.haveName", file.declaration.name, name))
            }
        }
        return builder
    }

    infix fun haveName(names: List<String>): FilesRuleBuilder = haveNameIn(names)

    fun haveName(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameMatching", file.declaration.name, file.declaration.name))
            }
        }
        return builder
    }

    infix fun haveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.haveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    fun haveNameIn(vararg names: String): FilesRuleBuilder = haveNameIn(names.toList())

    infix fun notHaveName(name: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name == name) {
                violations.add(getMessage("file.should.notHaveName", file.declaration.name, name))
            }
        }
        return builder
    }

    infix fun notHaveName(names: List<String>): FilesRuleBuilder = notHaveNameIn(names)

    fun notHaveName(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    infix fun notHaveNameIn(names: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (names.contains(file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameIn", file.declaration.name, names.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameIn(vararg names: String): FilesRuleBuilder = notHaveNameIn(names.toList())

    infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(getMessage("file.should.notHaveNameMatching", file.declaration.name, pattern))
            }
        }
        return builder
    }

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

    fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notHaveNameMatching(patterns.toList())

    infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("file.should.notHaveNameStartingWith", file.declaration.name, prefix))
            }
        }
        return builder
    }

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

    fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = notHaveNameStartingWith(prefixes.toList())

    infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("file.should.notHaveNameEndingWith", file.declaration.name, suffix))
            }
        }
        return builder
    }

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

    fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = notHaveNameEndingWith(suffixes.toList())
}

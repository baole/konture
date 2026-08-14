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

    /** Filter or assertion criteria for reside in package. */
    public infix fun inPackage(packagePatterns: List<String>): FilesRuleBuilder {
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
    public fun inPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in package. */
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

    /** Legacy resideInPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePattern)"))
    public infix fun resideInPackage(packagePattern: String): FilesRuleBuilder = inPackage(packagePattern)

    /** Legacy resideInPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePatterns)"))
    public infix fun resideInPackage(packagePatterns: List<String>): FilesRuleBuilder = inPackage(packagePatterns)

    /** Legacy resideInPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(*packagePatterns)"))
    public fun resideInPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(*packagePatterns)

    /** Legacy resideInPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(predicate)"))
    public infix fun resideInPackage(predicate: (String) -> Boolean): FilesRuleBuilder = inPackage(predicate)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePattern)"))
    public infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder = inPackage(packagePattern)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePatterns)"))
    public infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = inPackage(packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(*packagePatterns)"))
    public fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(*packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(predicate)"))
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder = inPackage(predicate)

    /** Filter or assertion criteria for not reside in package. */
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

    /** Filter or assertion criteria for not reside in package. */
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

    /** Filter or assertion criteria for not reside in package. */
    public fun notInPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(packagePatterns.toList())

    /** Legacy notResideInPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePattern)"))
    public infix fun notResideInPackage(packagePattern: String): FilesRuleBuilder = notInPackage(packagePattern)

    /** Legacy notResideInPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePatterns)"))
    public infix fun notResideInPackage(packagePatterns: List<String>): FilesRuleBuilder = notInPackage(packagePatterns)

    /** Legacy notResideInPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(*packagePatterns)"))
    public fun notResideInPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(*packagePatterns)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePattern)"))
    public infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder = notInPackage(packagePattern)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePatterns)"))
    public infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder =
        notInPackage(packagePatterns)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(*packagePatterns)"))
    public fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in module. */
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

    /** Filter or assertion criteria for reside in modules. */
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

    /** Filter or assertion criteria for reside in modules. */
    public fun inModules(vararg modulePaths: String): FilesRuleBuilder = inModules(modulePaths.toList())

    /** Legacy resideInModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInModule(modulePath: String): FilesRuleBuilder = inModule(modulePath)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(modulePaths)"))
    public infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder = inModules(modulePaths)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(*modulePaths)"))
    public fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = inModules(*modulePaths)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInAModule(modulePath: String): FilesRuleBuilder = inModule(modulePath)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(modulePaths)"))
    public infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder = inModules(modulePaths)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(*modulePaths)"))
    public fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = inModules(*modulePaths)

    /** Filter or assertion criteria for not reside in module. */
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

    /** Filter or assertion criteria for not reside in modules. */
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

    /** Filter or assertion criteria for not reside in modules. */
    public fun notInModules(vararg modulePaths: String): FilesRuleBuilder = notInModules(modulePaths.toList())

    /** Legacy notResideInModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInModule(modulePath: String): FilesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(modulePaths)"))
    public infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder = notInModules(modulePaths)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(*modulePaths)"))
    public fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder = notInModules(*modulePaths)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInAModule(modulePath: String): FilesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(modulePaths)"))
    public infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder = notInModules(modulePaths)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(*modulePaths)"))
    public fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notInModules(*modulePaths)

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

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(pattern)"))
    public infix fun haveNameMatching(pattern: String): FilesRuleBuilder = nameMatches(pattern)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(patterns)"))
    public infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder = nameMatches(patterns)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(*patterns)"))
    public fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = nameMatches(*patterns)

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

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefix)"))
    public infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder = nameStartsWith(prefix)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefixes)"))
    public infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder = nameStartsWith(prefixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(*prefixes)"))
    public fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = nameStartsWith(*prefixes)

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

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffix)"))
    public infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder = nameEndsWith(suffix)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffixes)"))
    public infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder = nameEndsWith(suffixes)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(*suffixes)"))
    public fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = nameEndsWith(*suffixes)

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

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(name)"))
    public infix fun haveName(name: String): FilesRuleBuilder = named(name)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(names)"))
    public infix fun haveName(names: List<String>): FilesRuleBuilder = named(names)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(*names)"))
    public fun haveName(vararg names: String): FilesRuleBuilder = named(*names)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(predicate)"))
    public infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder = named(predicate)

    /** Legacy haveNameIn method. */
    @Deprecated("Use named instead.", ReplaceWith("named(names)"))
    public infix fun haveNameIn(names: List<String>): FilesRuleBuilder = named(names)

    /** Legacy haveNameIn method. */
    @Deprecated("Use named instead.", ReplaceWith("named(*names)"))
    public fun haveNameIn(vararg names: String): FilesRuleBuilder = named(*names)

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

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(name)"))
    public infix fun notHaveName(name: String): FilesRuleBuilder = notNamed(name)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(names)"))
    public infix fun notHaveName(names: List<String>): FilesRuleBuilder = notNamed(names)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(*names)"))
    public fun notHaveName(vararg names: String): FilesRuleBuilder = notNamed(*names)

    /** Legacy notHaveNameIn method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(names)"))
    public infix fun notHaveNameIn(names: List<String>): FilesRuleBuilder = notNamed(names)

    /** Legacy notHaveNameIn method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(*names)"))
    public fun notHaveNameIn(vararg names: String): FilesRuleBuilder = notNamed(*names)

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

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(pattern)"))
    public infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder = notNameMatches(pattern)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(patterns)"))
    public infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder = notNameMatches(patterns)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(*patterns)"))
    public fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notNameMatches(*patterns)

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

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefix)"))
    public infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder = notNameStartsWith(prefix)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefixes)"))
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder = notNameStartsWith(prefixes)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(*prefixes)"))
    public fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = notNameStartsWith(*prefixes)

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

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffix)"))
    public infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder = notNameEndsWith(suffix)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffixes)"))
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder = notNameEndsWith(suffixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(*suffixes)"))
    public fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = notNameEndsWith(*suffixes)
}

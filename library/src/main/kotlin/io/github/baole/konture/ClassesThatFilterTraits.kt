/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Common scope interface providing access to [ClassesRuleBuilder].
 */
public interface ClassesThatScope {
    /** Filter or assertion criteria for builder. */
    public val builder: ClassesRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatPackageFilter : ClassesThatScope {
    /** Specifies in package criteria. */
    public infix fun inPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies in package criteria. */
    public infix fun inPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for in package. */
    public fun inPackage(vararg packagePatterns: String): ClassesRuleBuilder = inPackage(packagePatterns.toList())

    /** Specifies in package criteria. */
    public infix fun inPackage(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Specifies in package of criteria. */
    public infix fun inPackageOf(type: KClass<*>): ClassesRuleBuilder =
        inPackage(type.toKonturePackageReference().packageName)

    /** Specifies not in package criteria. */
    public infix fun notInPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies not in package criteria. */
    public infix fun notInPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filter or assertion criteria for not in package. */
    public fun notInPackage(vararg packagePatterns: String): ClassesRuleBuilder = notInPackage(packagePatterns.toList())

    /** Specifies in module criteria. */
    public infix fun inModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module?.path == normalized
        }
        return builder
    }

    /** Specifies in module criteria. */
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
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module != null && normalizedPaths.contains(module.path)
        }
        return builder
    }

    /** Filter or assertion criteria for in module. */
    public fun inModule(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /** Specifies in modules criteria. */
    public infix fun inModules(modulePaths: List<String>): ClassesRuleBuilder = inModule(modulePaths)

    /** Filter or assertion criteria for in modules. */
    public fun inModules(vararg modulePaths: String): ClassesRuleBuilder = inModule(modulePaths.toList())

    /** Specifies not in module criteria. */
    public infix fun notInModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module == null || (module.path != normalized && !PatternMatchers.matchesModuleGlob(normalized, module.path))
        }
        return builder
    }

    /** Specifies not in module criteria. */
    public infix fun notInModule(modulePaths: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module == null ||
                normalized.none { target ->
                    module.path == target || PatternMatchers.matchesModuleGlob(target, module.path)
                }
        }
        return builder
    }

    /** Filter or assertion criteria for not in module. */
    public fun notInModule(vararg modulePaths: String): ClassesRuleBuilder = notInModule(modulePaths.toList())

    /** Specifies not in modules criteria. */
    public infix fun notInModules(modulePaths: List<String>): ClassesRuleBuilder = notInModule(modulePaths)

    /** Filter or assertion criteria for not in modules. */
    public fun notInModules(vararg modulePaths: String): ClassesRuleBuilder = notInModule(modulePaths.toList())

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

    /** Legacy resideInPackageOf method. */
    @Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf(type)"))
    public infix fun resideInPackageOf(type: KClass<*>): ClassesRuleBuilder = inPackageOf(type)

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
}

/**
 * Trait interface for name matching and naming pattern filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatNameFilter : ClassesThatScope {
    /** Specifies named criteria. */
    public infix fun named(name: String): ClassesRuleBuilder {
        builder.setThat { it.fqName == name || it.name == name }
        return builder
    }

    /** Specifies simple named criteria. */
    public infix fun simpleNamed(name: String): ClassesRuleBuilder {
        builder.setThat { it.name == name }
        return builder
    }

    /** Specifies named criteria. */
    public infix fun named(names: List<String>): ClassesRuleBuilder {
        builder.setThat { names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for named. */
    public fun named(vararg names: String): ClassesRuleBuilder = named(names.toList())

    /** Specifies not named criteria. */
    public infix fun notNamed(name: String): ClassesRuleBuilder {
        builder.setThat { it.name != name }
        return builder
    }

    /** Specifies not named criteria. */
    public infix fun notNamed(names: List<String>): ClassesRuleBuilder {
        builder.setThat { !names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for not named. */
    public fun notNamed(vararg names: String): ClassesRuleBuilder = notNamed(names.toList())

    /** Specifies not named criteria. */
    public infix fun notNamed(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { !predicate(it.name) }
        return builder
    }

    /** Specifies name ends with criteria. */
    public infix fun nameEndsWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies name ends with criteria. */
    public infix fun nameEndsWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for name ends with. */
    public fun nameEndsWith(vararg suffixes: String): ClassesRuleBuilder = nameEndsWith(suffixes.toList())

    /** Specifies not name ends with criteria. */
    public infix fun notNameEndsWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies not name ends with criteria. */
    public infix fun notNameEndsWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not name ends with. */
    public fun notNameEndsWith(vararg suffixes: String): ClassesRuleBuilder = notNameEndsWith(suffixes.toList())

    /** Specifies name starts with criteria. */
    public infix fun nameStartsWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies name starts with criteria. */
    public infix fun nameStartsWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for name starts with. */
    public fun nameStartsWith(vararg prefixes: String): ClassesRuleBuilder = nameStartsWith(prefixes.toList())

    /** Specifies not name starts with criteria. */
    public infix fun notNameStartsWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies not name starts with criteria. */
    public infix fun notNameStartsWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not name starts with. */
    public fun notNameStartsWith(vararg prefixes: String): ClassesRuleBuilder = notNameStartsWith(prefixes.toList())

    /** Specifies named criteria. */
    public infix fun named(predicate: (String) -> Boolean): ClassesRuleBuilder =
        named("custom name predicate", predicate)

    /** Filter or assertion criteria for named. */
    @Suppress("UnusedParameter")
    public fun named(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setThat { predicate(it.name) }
        return builder
    }

    /** Specifies name matches criteria. */
    public infix fun nameMatches(pattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies name matches criteria. */
    public infix fun nameMatches(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for name matches. */
    public fun nameMatches(vararg patterns: String): ClassesRuleBuilder = nameMatches(patterns.toList())

    /** Specifies not name matches criteria. */
    public infix fun notNameMatches(pattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies not name matches criteria. */
    public infix fun notNameMatches(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not name matches. */
    public fun notNameMatches(vararg patterns: String): ClassesRuleBuilder = notNameMatches(patterns.toList())

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

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(predicate)"))
    public infix fun notHaveName(predicate: (String) -> Boolean): ClassesRuleBuilder = notNamed(predicate)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffix)"))
    public infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder = nameEndsWith(suffix)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffixes)"))
    public infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder = nameEndsWith(suffixes)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(*suffixes)"))
    public fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = nameEndsWith(*suffixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffix)"))
    public infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder = notNameEndsWith(suffix)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffixes)"))
    public infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder = notNameEndsWith(suffixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(*suffixes)"))
    public fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = notNameEndsWith(*suffixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefix)"))
    public infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder = nameStartsWith(prefix)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefixes)"))
    public infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder = nameStartsWith(prefixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(*prefixes)"))
    public fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = nameStartsWith(*prefixes)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefix)"))
    public infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder = notNameStartsWith(prefix)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefixes)"))
    public infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder = notNameStartsWith(prefixes)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(*prefixes)"))
    public fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = notNameStartsWith(*prefixes)

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

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(pattern)"))
    public infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder = notNameMatches(pattern)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(patterns)"))
    public infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder = notNameMatches(patterns)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(*patterns)"))
    public fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notNameMatches(*patterns)
}

/**
 * Trait interface for structural, hierarchy, member and type assignability filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatStructureFilter : ClassesThatScope {
    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { it.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
        areAssignableTo(superType.kontureQualifiedName())

    /** Specifies be child of criteria. */
    public infix fun beChildOf(superType: String): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies be child of criteria. */
    public infix fun beChildOf(superType: KClass<*>): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies are assignable to any of criteria. */
    public infix fun areAssignableToAnyOf(superType: String): ClassesRuleBuilder =
        areAssignableToAnyOf(listOf(superType))

    /** Specifies are assignable to any of criteria. */
    public infix fun areAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.any { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to any of. */
    public fun areAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder =
        areAssignableToAnyOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to any of. */
    public fun areAssignableToAnyOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAnyOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable to all of criteria. */
    public infix fun areAssignableToAllOf(superType: String): ClassesRuleBuilder =
        areAssignableToAllOf(listOf(superType))

    /** Specifies are assignable to all of criteria. */
    public infix fun areAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to all of. */
    public fun areAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder =
        areAssignableToAllOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to all of. */
    public fun areAssignableToAllOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAllOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable from criteria. */
    public infix fun areAssignableFrom(subType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            /** Filter or assertion criteria for sub type decl. */
            val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
            if (subTypeDecl != null) {
                subTypeDecl.fqName == cls.fqName ||
                    subTypeDecl.isAssignableTo(cls.fqName, allClasses) ||
                    subTypeDecl.isAssignableTo(cls.name, allClasses)
            } else {
                subType == cls.fqName || subType == cls.name
            }
        }
        return builder
    }

    /** Specifies are assignable from criteria. */
    public infix fun areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
        areAssignableFrom(subType.kontureQualifiedName())

    /** Filter or assertion criteria for have companion object. */
    public fun haveCompanionObject(): ClassesRuleBuilder {
        builder.setThat { it.companionObject != null }
        return builder
    }

    /** Filter or assertion criteria for have no arg constructor. */
    public fun haveNoArgConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.parameters?.isEmpty() == true ||
                cls.secondaryConstructors.any { it.parameters.isEmpty() }
        }
        return builder
    }

    /** Filter or assertion criteria for have private primary constructor. */
    public fun havePrivatePrimaryConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.visibility == Visibility.PRIVATE
        }
        return builder
    }

    /** Specifies contain property criteria. */
    public infix fun containProperty(propertyName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.properties.any { it.name == propertyName } }
        return builder
    }

    /** Specifies contain property criteria. */
    public infix fun containProperty(propertyNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> propertyNames.all { prop -> cls.properties.any { it.name == prop } } }
        return builder
    }

    /** Filter or assertion criteria for contain property. */
    public fun containProperty(vararg propertyNames: String): ClassesRuleBuilder =
        containProperty(propertyNames.toList())

    /** Specifies contain properties criteria. */
    public infix fun containProperties(propertyNames: List<String>): ClassesRuleBuilder = containProperty(propertyNames)

    /** Filter or assertion criteria for contain properties. */
    public fun containProperties(vararg propertyNames: String): ClassesRuleBuilder =
        containProperty(propertyNames.toList())

    /** Specifies contain function criteria. */
    public infix fun containFunction(functionName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.functions.any { it.name == functionName } }
        return builder
    }

    /** Specifies contain function criteria. */
    public infix fun containFunction(functionNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> functionNames.all { func -> cls.functions.any { it.name == func } } }
        return builder
    }

    /** Filter or assertion criteria for contain function. */
    public fun containFunction(vararg functionNames: String): ClassesRuleBuilder =
        containFunction(functionNames.toList())

    /** Specifies contain functions criteria. */
    public infix fun containFunctions(functionNames: List<String>): ClassesRuleBuilder = containFunction(functionNames)

    /** Filter or assertion criteria for contain functions. */
    public fun containFunctions(vararg functionNames: String): ClassesRuleBuilder =
        containFunction(functionNames.toList())

    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to. */
    public fun areAssignableTo(vararg superTypes: String): ClassesRuleBuilder = areAssignableTo(superTypes.toList())

    /** Specifies are assignable from criteria. */
    public infix fun areAssignableFrom(subTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            subTypes.all { subType ->
                /** Filter or assertion criteria for sub type decl. */
                val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
                if (subTypeDecl != null) {
                    subTypeDecl.fqName == cls.fqName ||
                        subTypeDecl.isAssignableTo(cls.fqName, allClasses) ||
                        subTypeDecl.isAssignableTo(cls.name, allClasses)
                } else {
                    subType == cls.fqName || subType == cls.name
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for are assignable from. */
    public fun areAssignableFrom(vararg subTypes: String): ClassesRuleBuilder = areAssignableFrom(subTypes.toList())

    /** Specifies are not assignable to criteria. */
    public infix fun areNotAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> !cls.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are not assignable to criteria. */
    public infix fun areNotAssignableTo(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableTo(type.kontureQualifiedName())

    /** Specifies are not assignable from criteria. */
    public infix fun areNotAssignableFrom(subType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            /** Filter or assertion criteria for sub type decl. */
            val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
            if (subTypeDecl != null) {
                subTypeDecl.fqName != cls.fqName &&
                    !subTypeDecl.isAssignableTo(cls.fqName, allClasses) &&
                    !subTypeDecl.isAssignableTo(cls.name, allClasses)
            } else {
                subType != cls.fqName && subType != cls.name
            }
        }
        return builder
    }

    /** Specifies are not assignable from criteria. */
    public infix fun areNotAssignableFrom(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableFrom(type.kontureQualifiedName())
}

/**
 * Trait interface for metadata, annotations, modifiers and visibility filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatMetadataFilter : ClassesThatScope {
    /** Specifies annotated with criteria. */
    public infix fun annotatedWith(annotationFqName: String): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
        }
        return builder
    }

    /** Specifies annotated with criteria. */
    public infix fun annotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        annotatedWith(annotation.kontureQualifiedName())

    /** Specifies annotated with all of criteria. */
    public infix fun annotatedWithAllOf(name: String): ClassesRuleBuilder = annotatedWithAllOf(listOf(name))

    /** Specifies annotated with all of criteria. */
    public infix fun annotatedWithAllOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /** Filter or assertion criteria for annotated with all of. */
    public fun annotatedWithAllOf(vararg names: String): ClassesRuleBuilder = annotatedWithAllOf(names.asList())

    /** Specifies annotated with any of criteria. */
    public infix fun annotatedWithAnyOf(name: String): ClassesRuleBuilder = annotatedWithAnyOf(listOf(name))

    /** Specifies annotated with any of criteria. */
    public infix fun annotatedWithAnyOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /** Filter or assertion criteria for annotated with any of. */
    public fun annotatedWithAnyOf(vararg names: String): ClassesRuleBuilder = annotatedWithAnyOf(names.asList())

    /** Legacy haveAnnotationOf method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotationFqName)"))
    public infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder = annotatedWith(annotationFqName)

    /** Legacy haveAnnotationOf method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        annotatedWith(annotation)

    /** Legacy areAnnotatedWith method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotationFqName)"))
    public infix fun areAnnotatedWith(annotationFqName: String): ClassesRuleBuilder = annotatedWith(annotationFqName)

    /** Legacy areAnnotatedWith method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
    public infix fun areAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        annotatedWith(annotation)

    /** Legacy haveAllAnnotationsOf method. */
    @Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(name)"))
    public infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = annotatedWithAllOf(name)

    /** Legacy haveAllAnnotationsOf method. */
    @Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(names)"))
    public infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder = annotatedWithAllOf(names)

    /** Legacy haveAllAnnotationsOf method. */
    @Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(*names)"))
    public fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = annotatedWithAllOf(*names)

    /** Legacy haveAnyAnnotationOf method. */
    @Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(name)"))
    public infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = annotatedWithAnyOf(name)

    /** Legacy haveAnyAnnotationOf method. */
    @Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(names)"))
    public infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder = annotatedWithAnyOf(names)

    /** Legacy haveAnyAnnotationOf method. */
    @Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(*names)"))
    public fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = annotatedWithAnyOf(*names)

    /** Filter or assertion criteria for have annotation with argument. */
    public fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { ann ->
                (ann.name == annotationName || ann.fqName == annotationName) &&
                    ann.arguments.any { arg ->
                        (argName == null || arg.name == argName) &&
                            (
                                arg.value == argValue ||
                                    arg.value.removeSurrounding("\"") == argValue ||
                                    arg.value.removeSurrounding("'") == argValue
                            )
                    }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for are interfaces. */
    public fun areInterfaces(): ClassesRuleBuilder {
        builder.setThat { it.isInterface }
        return builder
    }

    /** Filter or assertion criteria for are enums. */
    public fun areEnums(): ClassesRuleBuilder {
        builder.setThat { it.isEnum }
        return builder
    }

    /** Filter or assertion criteria for are abstract. */
    public fun areAbstract(): ClassesRuleBuilder {
        builder.setThat { it.isAbstract || it.isInterface }
        return builder
    }

    /** Specifies have visibility criteria. */
    public infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setThat { it.visibility == visibility }
        return builder
    }

    /** Specifies have any visibility criteria. */
    public infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(listOf(visibility))

    /** Specifies have any visibility criteria. */
    public infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setThat { cls -> visibilities.contains(cls.visibility) }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    public fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filter or assertion criteria for be public. */
    public fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    /** Filter or assertion criteria for be internal. */
    public fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    /** Filter or assertion criteria for be private. */
    public fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    /** Filter or assertion criteria for be protected. */
    public fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /** Specifies have modifier criteria. */
    public infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(modifier) }
        return builder
    }

    /** Specifies have any modifier criteria. */
    public infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    /** Specifies have any modifier criteria. */
    public infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.any { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have any modifier. */
    public fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Specifies have all modifiers criteria. */
    public infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    /** Specifies have all modifiers criteria. */
    public infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.all { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    public fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filter or assertion criteria for be sealed. */
    public fun beSealed(): ClassesRuleBuilder = haveModifier(Modifier.SEALED)

    /** Filter or assertion criteria for be data. */
    public fun beData(): ClassesRuleBuilder = haveModifier(Modifier.DATA)

    /** Filter or assertion criteria for be inline. */
    public fun beInline(): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
        return builder
    }

    /** Filter or assertion criteria for are open. */
    public fun areOpen(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filter or assertion criteria for are override. */
    public fun areOverride(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filter or assertion criteria for are inner. */
    public fun areInner(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.INNER) }
        return builder
    }

    /** Filter or assertion criteria for are top level. */
    public fun areTopLevel(): ClassesRuleBuilder {
        builder.setThat { cls ->
            !cls.fqName.substringBeforeLast('.').contains('.') || cls.packageName == cls.fqName.substringBeforeLast('.')
        }
        return builder
    }

    /** Filter or assertion criteria for are nested. */
    public fun areNested(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.packageName != cls.fqName.substringBeforeLast('.') }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    public fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setThat { it.kdocText?.isNotBlank() == true }
        return builder
    }
}

/**
 * Trait interface for composite, logical and custom predicate filtering on classes.
 */
public interface ClassesThatCompositeFilter : ClassesThatScope {
    /** Filter or assertion criteria for not. */
    public fun not(): ClassesThat = builder.not()

    /** Specifies matching criteria. */
    public infix fun matching(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for satisfy. */
    public fun satisfy(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for any of. */
    public fun anyOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}

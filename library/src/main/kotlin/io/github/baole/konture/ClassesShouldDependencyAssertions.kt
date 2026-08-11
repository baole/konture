/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
public interface ClassesShouldDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes are free of dependency cycles.
     */
    fun beFreeOfCycles(): ClassesRuleBuilder {
        builder.setShould { _, allClasses, violations ->
            /** Filter or assertion criteria for adjacency. */
            val adjacency =
                allClasses.associate { cls ->
                    cls.fqName to cls.referencedTypes.toSet()
                }

            /** Filter or assertion criteria for cycles. */
            val cycles = io.github.baole.konture.impl.SliceCycleDetector.findCycles(adjacency)
            if (cycles.isNotEmpty()) {
                for (cycle in cycles) {
                    /** Filter or assertion criteria for rendered. */
                    val rendered = (cycle + cycle.first()).joinToString(" -> ")
                    violations.add(getMessage("class.should.beFreeOfCycles", rendered))
                }
            }
        }
        return builder
    }

    /** Asserts that selected classes do not depend on the specified [classes]. */
    fun notDependOnClasses(vararg classes: KClass<*>): ClassesRuleBuilder {
        classes.forEach { notReferenceClass(it) }
        return builder
    }

    /** Asserts that selected classes do not depend on packages matching [packagePattern]. */
    infix fun notDependOnPackages(packagePattern: String): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(packagePattern)

    /** Asserts that selected classes do not depend on packages matching [packagePatterns]. */
    fun notDependOnPackages(vararg packagePatterns: String): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(*packagePatterns)

    /** Asserts that selected classes do not depend on packages matching [packagePatterns]. */
    infix fun notDependOnPackages(packagePatterns: List<String>): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(packagePatterns)

    /** Asserts that selected classes only depend on packages matching [packagePattern]. */
    infix fun onlyDependOnPackages(packagePattern: String): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(packagePattern)

    /** Asserts that selected classes only depend on packages matching [packagePatterns]. */
    fun onlyDependOnPackages(vararg packagePatterns: String): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(*packagePatterns)

    /** Asserts that selected classes only depend on packages matching [packagePatterns]. */
    infix fun onlyDependOnPackages(packagePatterns: List<String>): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(packagePatterns)

    /**
     * Asserts that selected classes have KDoc documentation.
     */
    fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.kdocText?.isBlank() != false) {
                violations.add(getMessage("class.should.beDocumented", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing allowed accessing classes.
     */
    fun onlyBeAccessedByAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        builder.setShould { targetCls, allClasses, violations ->
            /** Filter or assertion criteria for accessing classes. */
            val accessingClasses =
                allClasses.filter { other ->
                    other.fqName != targetCls.fqName && other.dependsOn(targetCls)
                }
            for (accessor in accessingClasses) {
                /** Filter or assertion criteria for is allowed. */
                val isAllowed =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, accessor.packageName)
                    }
                if (!isAllowed) {
                    violations.add(
                        getMessage(
                            "class.should.notAccessForbiddenPackage",
                            targetCls.fqName,
                            accessor.fqName,
                            accessor.packageName,
                            packagePatterns.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified pattern.
     */
    infix fun onlyBeAccessedByAnyPackage(packagePattern: String): ClassesRuleBuilder =
        onlyBeAccessedByAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified patterns.
     */
    infix fun onlyBeAccessedByAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        onlyBeAccessedByAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes are not accessed by any class residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing forbidden accessing classes.
     */
    fun notBeAccessedByAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        builder.setShould { targetCls, allClasses, violations ->
            /** Filter or assertion criteria for accessing classes. */
            val accessingClasses =
                allClasses.filter { other ->
                    other.fqName != targetCls.fqName && other.dependsOn(targetCls)
                }
            for (accessor in accessingClasses) {
                /** Filter or assertion criteria for is forbidden. */
                val isForbidden =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, accessor.packageName)
                    }
                if (isForbidden) {
                    violations.add(
                        getMessage(
                            "class.should.beAccessedByForbiddenPackage",
                            targetCls.fqName,
                            accessor.fqName,
                            accessor.packageName,
                            packagePatterns.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are not accessed by any class residing in packages matching the specified pattern.
     */
    infix fun notBeAccessedByAnyPackage(packagePattern: String): ClassesRuleBuilder =
        notBeAccessedByAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes are not accessed by any class residing in packages matching the specified patterns.
     */
    infix fun notBeAccessedByAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        notBeAccessedByAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing allowed dependency packages.
     */
    fun onlyDependOnClassesInAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for standard exclusions. */
        val standardExclusions = listOf("java", "javax", "kotlin")
        builder.setShould { cls, allClasses, violations ->
            /** Filter or assertion criteria for extract package. */
            fun extractPackage(fqName: String): String? {
                /** Filter or assertion criteria for clean. */
                val clean = fqName.substringBefore("<").trim()
                if (!clean.contains('.')) return null

                /** Filter or assertion criteria for segments. */
                val segments = clean.split('.')

                /** Filter or assertion criteria for class index. */
                val classIndex = segments.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }
                return if (classIndex > 0) {
                    segments.take(classIndex).joinToString(".")
                } else if (classIndex == 0) {
                    null
                } else {
                    segments.dropLast(1).joinToString(".")
                }
            }

            /** Filter or assertion criteria for deps. */
            val deps = mutableListOf<Pair<String, String>>()

            // 1. Imports
            for (imp in cls.imports) {
                if (imp.endsWith(".*")) {
                    /** Filter or assertion criteria for pkg. */
                    val pkg = imp.removeSuffix(".*")
                    deps.add(Pair(imp, pkg))
                } else {
                    extractPackage(imp)?.let { pkg ->
                        deps.add(Pair(imp, pkg))
                    }
                }
            }

            // 2. Referenced types
            for (ref in cls.referencedTypes) {
                extractPackage(ref)?.let { pkg ->
                    deps.add(Pair(ref, pkg))
                }
            }

            // 3. Supertypes
            for (superType in cls.supertypes) {
                extractPackage(superType)?.let { pkg ->
                    deps.add(Pair(superType, pkg))
                }
            }

            // 4. Annotations
            for (ann in cls.annotations) {
                extractPackage(ann.fqName)?.let { pkg ->
                    deps.add(Pair(ann.fqName, pkg))
                }
            }

            // 5. Resolved internal classes
            for (other in allClasses) {
                if (other.fqName != cls.fqName && cls.dependsOn(other)) {
                    deps.add(Pair(other.fqName, other.packageName))
                }
            }

            /** Filter or assertion criteria for filtered deps. */
            val filteredDeps =
                deps.filter { (_, depPkg) ->
                    depPkg != cls.packageName && standardExclusions.none { depPkg == it || depPkg.startsWith("$it.") }
                }.distinctBy { it.first }

            for (dep in filteredDeps) {
                /** Filter or assertion criteria for is allowed. */
                val isAllowed =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, dep.second)
                    }
                if (!isAllowed) {
                    violations.add(
                        getMessage(
                            "class.should.notDependOnForbiddenPackage",
                            cls.fqName,
                            dep.first,
                            dep.second,
                            packagePatterns.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified pattern.
     */
    infix fun onlyDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified patterns.
     */
    infix fun onlyDependOnClassesInAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing forbidden dependency packages.
     */
    fun notDependOnClassesInAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for standard exclusions. */
        val standardExclusions = listOf("java", "javax", "kotlin")
        builder.setShould { cls, allClasses, violations ->
            /** Filter or assertion criteria for extract package. */
            fun extractPackage(fqName: String): String? {
                /** Filter or assertion criteria for clean. */
                val clean = fqName.substringBefore("<").trim()
                if (!clean.contains('.')) return null

                /** Filter or assertion criteria for segments. */
                val segments = clean.split('.')

                /** Filter or assertion criteria for class index. */
                val classIndex = segments.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }
                return if (classIndex > 0) {
                    segments.take(classIndex).joinToString(".")
                } else if (classIndex == 0) {
                    null
                } else {
                    segments.dropLast(1).joinToString(".")
                }
            }

            /** Filter or assertion criteria for deps. */
            val deps = mutableListOf<Pair<String, String>>()

            // 1. Imports
            for (imp in cls.imports) {
                if (imp.endsWith(".*")) {
                    /** Filter or assertion criteria for pkg. */
                    val pkg = imp.removeSuffix(".*")
                    deps.add(Pair(imp, pkg))
                } else {
                    extractPackage(imp)?.let { pkg ->
                        deps.add(Pair(imp, pkg))
                    }
                }
            }

            // 2. Referenced types
            for (ref in cls.referencedTypes) {
                extractPackage(ref)?.let { pkg ->
                    deps.add(Pair(ref, pkg))
                }
            }

            // 3. Supertypes
            for (superType in cls.supertypes) {
                extractPackage(superType)?.let { pkg ->
                    deps.add(Pair(superType, pkg))
                }
            }

            // 4. Annotations
            for (ann in cls.annotations) {
                extractPackage(ann.fqName)?.let { pkg ->
                    deps.add(Pair(ann.fqName, pkg))
                }
            }

            // 5. Resolved internal classes
            for (other in allClasses) {
                if (other.fqName != cls.fqName && cls.dependsOn(other)) {
                    deps.add(Pair(other.fqName, other.packageName))
                }
            }

            /** Filter or assertion criteria for filtered deps. */
            val filteredDeps =
                deps.filter { (_, depPkg) ->
                    depPkg != cls.packageName && standardExclusions.none { depPkg == it || depPkg.startsWith("$it.") }
                }.distinctBy { it.first }

            for (dep in filteredDeps) {
                /** Filter or assertion criteria for is forbidden. */
                val isForbidden =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, dep.second)
                    }
                if (isForbidden) {
                    violations.add(
                        getMessage(
                            "class.should.notDependOnForbiddenPackageExplicit",
                            cls.fqName,
                            dep.first,
                            dep.second,
                            packagePatterns.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified pattern.
     */
    infix fun notDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified patterns.
     */
    infix fun notDependOnClassesInAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(*packagePatterns.toTypedArray())

    /** Fails for every invocation of [fqName] in the selected class. */
    fun notCall(fqName: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for file usages. */
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == cls.filePath || file.classes.any { it.fqName == cls.fqName } }
                    ?.usages.orEmpty()

            fileUsages
                .filter {
                        usage ->
                    usage.isEnclosedInClass(cls.fqName, cls.name) && PatternMatchers.isCallUsageMatch(usage, fqName)
                }
                .forEach { usage ->
                    /** Filter or assertion criteria for unresolved. */
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column),
                    )
                }
        }
        return builder
    }

    /** Fails for every invocation of [kClass] in the selected class. */
    fun notCall(kClass: KClass<*>): ClassesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [fqName] in the selected class; imports alone do not match. */
    fun notReferenceClass(fqName: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            /** Filter or assertion criteria for file usages. */
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == cls.filePath || file.classes.any { it.fqName == cls.fqName } }
                    ?.usages.orEmpty()

            fileUsages
                .filter { usage ->
                    usage.kind == UsageKind.CLASS_REFERENCE &&
                        usage.isEnclosedInClass(cls.fqName, cls.name) &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName.endsWith("." + usage.targetFqName) || usage.rawExpression == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    violations.add(
                        getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column),
                    )
                }
        }
        return builder
    }

    /** Fails for every actual class/type use of [kClass] in the selected class; imports alone do not match. */
    fun notReferenceClass(kClass: KClass<*>): ClassesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())
}

/** Fails for every invocation of type [T] in the selected class. */
inline fun <reified T : Any> ClassesShould.notCall(): ClassesRuleBuilder = notCall(T::class)

/** Fails for every actual class/type use of type [T] in the selected class; imports alone do not match. */
inline fun <reified T : Any> ClassesShould.notReferenceClass(): ClassesRuleBuilder = notReferenceClass(T::class)

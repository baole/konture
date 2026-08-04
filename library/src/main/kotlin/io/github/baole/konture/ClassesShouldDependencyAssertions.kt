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
internal interface ClassesShouldDependencyAssertions {
    val builder: ClassesRuleBuilder

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
            val accessingClasses =
                allClasses.filter { other ->
                    other.fqName != targetCls.fqName && other.dependsOn(targetCls)
                }
            for (accessor in accessingClasses) {
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
    infix fun onlyBeAccessedByAnyPackage(packagePattern: String): ClassesRuleBuilder = onlyBeAccessedByAnyPackage(listOf(packagePattern))

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
            val accessingClasses =
                allClasses.filter { other ->
                    other.fqName != targetCls.fqName && other.dependsOn(targetCls)
                }
            for (accessor in accessingClasses) {
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
    infix fun notBeAccessedByAnyPackage(packagePattern: String): ClassesRuleBuilder = notBeAccessedByAnyPackage(listOf(packagePattern))

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
        val standardExclusions = listOf("java", "javax", "kotlin")
        builder.setShould { cls, allClasses, violations ->
            fun extractPackage(fqName: String): String? {
                val clean = fqName.substringBefore("<").trim()
                if (!clean.contains('.')) return null

                val segments = clean.split('.')
                val classIndex = segments.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }
                return if (classIndex > 0) {
                    segments.take(classIndex).joinToString(".")
                } else if (classIndex == 0) {
                    null
                } else {
                    segments.dropLast(1).joinToString(".")
                }
            }

            val deps = mutableListOf<Pair<String, String>>()

            // 1. Imports
            for (imp in cls.imports) {
                if (imp.endsWith(".*")) {
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

            val filteredDeps =
                deps.filter { (_, depPkg) ->
                    depPkg != cls.packageName && standardExclusions.none { depPkg == it || depPkg.startsWith("$it.") }
                }.distinctBy { it.first }

            for (dep in filteredDeps) {
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
    infix fun onlyDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder = onlyDependOnClassesInAnyPackage(listOf(packagePattern))

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
        val standardExclusions = listOf("java", "javax", "kotlin")
        builder.setShould { cls, allClasses, violations ->
            fun extractPackage(fqName: String): String? {
                val clean = fqName.substringBefore("<").trim()
                if (!clean.contains('.')) return null

                val segments = clean.split('.')
                val classIndex = segments.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }
                return if (classIndex > 0) {
                    segments.take(classIndex).joinToString(".")
                } else if (classIndex == 0) {
                    null
                } else {
                    segments.dropLast(1).joinToString(".")
                }
            }

            val deps = mutableListOf<Pair<String, String>>()

            // 1. Imports
            for (imp in cls.imports) {
                if (imp.endsWith(".*")) {
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

            val filteredDeps =
                deps.filter { (_, depPkg) ->
                    depPkg != cls.packageName && standardExclusions.none { depPkg == it || depPkg.startsWith("$it.") }
                }.distinctBy { it.first }

            for (dep in filteredDeps) {
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
    infix fun notDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder = notDependOnClassesInAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified patterns.
     */
    infix fun notDependOnClassesInAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(*packagePatterns.toTypedArray())

    /** Fails for every invocation of [fqName] in the selected class. */
    fun notCall(fqName: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val fileUsages = builder.graph.getAllModules()
                .flatMap { it.files }
                .find { file -> file.filePath == cls.filePath || file.classes.any { it.fqName == cls.fqName } }
                ?.usages.orEmpty()

            fileUsages
                .filter { usage ->
                    usage.kind == UsageKind.CALL &&
                        (usage.enclosingClass == cls.name || usage.enclosingClass == null) &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName in usage.possibleTargetFqNames || usage.rawExpression == fqName)
                }.forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column))
                }
        }
        return builder
    }

    /** Fails for every invocation of [kClass] in the selected class. */
    fun notCall(kClass: KClass<*>): ClassesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [fqName] in the selected class; imports alone do not match. */
    fun notReferenceClass(fqName: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val fileUsages = builder.graph.getAllModules()
                .flatMap { it.files }
                .find { file -> file.filePath == cls.filePath || file.classes.any { it.fqName == cls.fqName } }
                ?.usages.orEmpty()

            fileUsages
                .filter { usage ->
                    usage.kind == UsageKind.CLASS_REFERENCE &&
                        (usage.enclosingClass == cls.name || usage.enclosingClass == null) &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName.endsWith("." + usage.targetFqName) || usage.rawExpression == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    violations.add(getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column))
                }
        }
        return builder
    }


    /** Fails for every actual class/type use of [kClass] in the selected class; imports alone do not match. */
    fun notReferenceClass(kClass: KClass<*>): ClassesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

}


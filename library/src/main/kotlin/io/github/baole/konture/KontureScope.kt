/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a scope containing a set of classes for writing architecture rules in a Konsist-inspired fluent DSL.
 *
 * A scope acts as the starting point or container for filtering, querying, and running assertions against
 * Kotlin class declarations in your codebase.
 *
 * @property classes The list of [ClassDeclaration] structures included in this scope.
 */
class KontureScope(
    val classes: List<ClassDeclaration>,
) {
    companion object {
        /**
         * Creates a [KontureScope] representing the entire project structure from the given or default project graph.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @return A [KontureScope] containing all class declarations found in all modules in the project.
         */
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureScope {
            val classes =
                graph.getAllModules().flatMap { module ->
                    module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) }.flatMap { it.classes }
                }
            return KontureScope(classes)
        }

        /**
         * Creates a [KontureScope] for a specific Gradle module, identified by its path (e.g., ":core", ":app").
         *
         * @param path The Gradle module path.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @return A [KontureScope] containing classes defined in the specified module.
         * @throws IllegalArgumentException If the specified module path is not found in the project graph.
         */
        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureScope {
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")
            return KontureScope(module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) }.flatMap { it.classes })
        }

        /**
         * Creates a [KontureScope] containing classes within a specific Kotlin package or any of its subpackages.
         *
         * @param packageName The package FQN prefix (e.g., "io.github.baole.konture").
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @return A [KontureScope] containing classes matching the package or nested packages.
         */
        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureScope {
            val classes =
                graph
                    .getAllModules()
                    .flatMap { module -> module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) }.flatMap { it.classes } }
                    .filter { it.packageName == packageName || it.packageName.startsWith("$packageName.") }
            return KontureScope(classes)
        }
    }
}

operator fun KontureScope.plus(other: KontureScope): KontureScope = KontureScope(this.classes + other.classes)

operator fun KontureScope.minus(other: KontureScope): KontureScope {
    val otherFqNames = other.classes.map { it.fqName }.toSet()
    return KontureScope(this.classes.filterNot { it.fqName in otherFqNames })
}

// Filtering extensions on List<ClassDeclaration>

/**
 * Filters the list of class declarations to include only those whose names end with the specified suffix.
 */
fun List<ClassDeclaration>.withNameEndingWith(suffix: String): List<ClassDeclaration> = filter { it.name.endsWith(suffix) }

/**
 * Filters the list of class declarations to include only those whose names start with the specified prefix.
 */
fun List<ClassDeclaration>.withNameStartingWith(prefix: String): List<ClassDeclaration> = filter { it.name.startsWith(prefix) }

/**
 * Filters the list of class declarations to include only those annotated with the specified annotation.
 */
fun List<ClassDeclaration>.withAnnotationOf(annotationFqName: String): List<ClassDeclaration> =
    filter { cls -> cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName } }

/**
 * Filters the list of class declarations to exclude those annotated with the specified annotation.
 */
fun List<ClassDeclaration>.withoutAnnotationOf(annotationFqName: String): List<ClassDeclaration> =
    filterNot { cls -> cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName } }

/**
 * Filters the list of class declarations to include only interface definitions.
 */
fun List<ClassDeclaration>.interfaces(): List<ClassDeclaration> = filter { it.isInterface }

/**
 * Filters the list of class declarations to include only normal classes (excluding interface definitions).
 */
fun List<ClassDeclaration>.classes(): List<ClassDeclaration> = filter { !it.isInterface }

/**
 * Filters class declarations to those extending or implementing the specified parent type.
 */
fun List<ClassDeclaration>.withParentOf(fqName: String): List<ClassDeclaration> = filter { it.supertypes.contains(fqName) }

/**
 * Filters class declarations to those matching the specified visibility.
 */
fun List<ClassDeclaration>.withVisibility(visibility: Visibility): List<ClassDeclaration> = filter { it.visibility == visibility }

fun List<ClassDeclaration>.public(): List<ClassDeclaration> = withVisibility(Visibility.PUBLIC)

fun List<ClassDeclaration>.internal(): List<ClassDeclaration> = withVisibility(Visibility.INTERNAL)

fun List<ClassDeclaration>.private(): List<ClassDeclaration> = withVisibility(Visibility.PRIVATE)

fun List<ClassDeclaration>.protected(): List<ClassDeclaration> = withVisibility(Visibility.PROTECTED)

/**
 * Filters class declarations to those containing the specified modifier.
 */
fun List<ClassDeclaration>.withModifier(modifier: Modifier): List<ClassDeclaration> = filter { it.modifiers.contains(modifier) }

fun List<ClassDeclaration>.dataClasses(): List<ClassDeclaration> = withModifier(Modifier.DATA)

fun List<ClassDeclaration>.sealedClasses(): List<ClassDeclaration> = withModifier(Modifier.SEALED)

fun List<ClassDeclaration>.inlineClasses(): List<ClassDeclaration> =
    filter { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }

/**
 * Filters the list of class declarations to include only those residing in packages matching the specified pattern.
 * Supports '..' segment wildcards.
 */
fun List<ClassDeclaration>.withPackage(packagePattern: String): List<ClassDeclaration> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/**
 * Filters the list of class declarations to include only those whose simple names match the specified glob pattern.
 * Supports '*' wildcards.
 */
fun List<ClassDeclaration>.withNameMatching(pattern: String): List<ClassDeclaration> = filter { PatternMatchers.matchesSimpleGlob(pattern, it.name) }

// Scope-level delegation for KontureScope

fun KontureScope.withNameEndingWith(suffix: String) = KontureScope(classes.withNameEndingWith(suffix))

fun KontureScope.withNameStartingWith(prefix: String) = KontureScope(classes.withNameStartingWith(prefix))

fun KontureScope.withAnnotationOf(annotationFqName: String) = KontureScope(classes.withAnnotationOf(annotationFqName))

fun KontureScope.withoutAnnotationOf(annotationFqName: String) =
    KontureScope(
        classes.withoutAnnotationOf(annotationFqName),
    )

fun KontureScope.interfaces() = KontureScope(classes.interfaces())

fun KontureScope.classes() = KontureScope(classes.classes())

fun KontureScope.withParentOf(fqName: String) = KontureScope(classes.withParentOf(fqName))

fun KontureScope.withVisibility(visibility: Visibility) = KontureScope(classes.withVisibility(visibility))

fun KontureScope.public() = KontureScope(classes.public())

fun KontureScope.internal() = KontureScope(classes.internal())

fun KontureScope.private() = KontureScope(classes.private())

fun KontureScope.protected() = KontureScope(classes.protected())

fun KontureScope.withModifier(modifier: Modifier) = KontureScope(classes.withModifier(modifier))

fun KontureScope.dataClasses() = KontureScope(classes.dataClasses())

fun KontureScope.sealedClasses() = KontureScope(classes.sealedClasses())

fun KontureScope.inlineClasses() = KontureScope(classes.inlineClasses())

fun KontureScope.withPackage(packagePattern: String) = KontureScope(classes.withPackage(packagePattern))

fun KontureScope.withNameMatching(pattern: String) = KontureScope(classes.withNameMatching(pattern))

// Assertion extensions on List<ClassDeclaration>

@kotlin.jvm.JvmName("assertClassesTrue")
fun List<ClassDeclaration>.assertTrue(
    additionalMessage: String? = null,
    predicate: (ClassDeclaration) -> Boolean,
) {
    val violations = filterNot(predicate)
    if (violations.isNotEmpty()) {
        val message =
            buildString {
                appendLine("Assertion failed! The following classes do not meet the criteria:")
                if (additionalMessage != null) {
                    appendLine(additionalMessage)
                }
                violations.forEach {
                    appendLine("  - ${it.fqName} (at ${it.filePath})")
                }
            }
        throw AssertionError(message)
    }
}

@kotlin.jvm.JvmName("assertClassesHasKDoc")
fun List<ClassDeclaration>.assertHasKDoc(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { it.kdocText?.isNotBlank() == true }
}

fun KontureScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (ClassDeclaration) -> Boolean,
) {
    classes.assertTrue(additionalMessage, predicate)
}

fun KontureScope.assertHasKDoc(additionalMessage: String? = null) = classes.assertHasKDoc(additionalMessage)

// --- New high-level assertion extensions on List<ClassDeclaration> ---

/**
 * Asserts that all class declarations reside in packages matching any of the specified patterns.
 * Matches using standard Kotlin package wildcard matching (e.g. "..domain..").
 *
 * ### Example:
 * ```kotlin
 * classes.assertResideInAPackage("..domain..", "..data..")
 * ```
 *
 * @param packagePatterns The package wildcard patterns. At least one must match.
 * @throws AssertionError if any class does not reside in a matching package.
 */
fun List<ClassDeclaration>.assertResideInAPackage(vararg packagePatterns: String) {
    assertTrue("Classes must reside in any of these packages: ${packagePatterns.joinToString()}") { clazz ->
        packagePatterns.any { PatternMatchers.matchesPackage(it, clazz.packageName) }
    }
}

/**
 * Asserts that all class declarations have names ending with any of the specified suffixes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertNameEndingWith("Repository", "Repo")
 * ```
 *
 * @param suffixes The allowed suffixes. At least one must match.
 * @throws AssertionError if any class name does not end with any of the specified suffixes.
 */
fun List<ClassDeclaration>.assertNameEndingWith(vararg suffixes: String) {
    assertTrue("Classes must have names ending with any of: ${suffixes.joinToString()}") { clazz ->
        suffixes.any { clazz.name.endsWith(it) }
    }
}

/**
 * Asserts that all class declarations have names starting with any of the specified prefixes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertNameStartingWith("Get", "Fetch")
 * ```
 *
 * @param prefixes The allowed prefixes. At least one must match.
 * @throws AssertionError if any class name does not start with any of the specified prefixes.
 */
fun List<ClassDeclaration>.assertNameStartingWith(vararg prefixes: String) {
    assertTrue("Classes must have names starting with any of: ${prefixes.joinToString()}") { clazz ->
        prefixes.any { clazz.name.startsWith(it) }
    }
}

/**
 * Asserts that all class declarations have names matching any of the specified glob patterns.
 * Supports '*' wildcards.
 *
 * ### Example:
 * ```kotlin
 * classes.assertNameMatching("*Controller", "*Handler")
 * ```
 *
 * @param patterns The glob patterns. At least one must match.
 * @throws AssertionError if any class name does not match any of the specified glob patterns.
 */
fun List<ClassDeclaration>.assertNameMatching(vararg patterns: String) {
    assertTrue("Classes must have names matching any of the glob patterns: ${patterns.joinToString()}") { clazz ->
        patterns.any { PatternMatchers.matchesSimpleGlob(it, clazz.name) }
    }
}

/**
 * Asserts that all class declarations have at least one of the specified annotations.
 * Matches either the annotation's simple name or its fully qualified name (FQN).
 *
 * ### Example:
 * ```kotlin
 * classes.assertHaveAnnotationOf("org.springframework.stereotype.Repository", "Repository")
 * ```
 *
 * @param annotationFqNames The annotation names or fully qualified names. At least one must match.
 * @throws AssertionError if any class is not annotated with any of the specified annotations.
 */
fun List<ClassDeclaration>.assertHaveAnnotationOf(vararg annotationFqNames: String) {
    assertTrue("Classes must have any of the annotations: ${annotationFqNames.joinToString()}") { clazz ->
        clazz.annotations.any { ann ->
            annotationFqNames.any { fqName -> ann.fqName == fqName || ann.name == fqName }
        }
    }
}

/**
 * Asserts that all class declarations represent interfaces.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreInterfaces()
 * ```
 *
 * @throws AssertionError if any class is not an interface.
 */
fun List<ClassDeclaration>.assertAreInterfaces() {
    assertTrue("Classes must be interfaces") { it.isInterface }
}

/**
 * Asserts that all class declarations represent enum classes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreEnums()
 * ```
 *
 * @throws AssertionError if any class is not an enum class.
 */
fun List<ClassDeclaration>.assertAreEnums() {
    assertTrue("Classes must be enums") { it.isEnum }
}

/**
 * Asserts that all class declarations represent abstract classes or interfaces.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreAbstract()
 * ```
 *
 * @throws AssertionError if any class is not abstract.
 */
fun List<ClassDeclaration>.assertAreAbstract() {
    assertTrue("Classes must be abstract") { it.isAbstract || it.isInterface }
}

/**
 * Asserts that all class declarations are sealed classes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreSealed()
 * ```
 *
 * @throws AssertionError if any class is not marked with the 'sealed' modifier.
 */
fun List<ClassDeclaration>.assertAreSealed() {
    assertTrue("Classes must be sealed") { it.modifiers.contains(Modifier.SEALED) }
}

/**
 * Asserts that all class declarations are data classes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreData()
 * ```
 *
 * @throws AssertionError if any class is not marked with the 'data' modifier.
 */
fun List<ClassDeclaration>.assertAreData() {
    assertTrue("Classes must be data classes") { it.modifiers.contains(Modifier.DATA) }
}

/**
 * Asserts that all class declarations are inline/value classes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreInline()
 * ```
 *
 * @throws AssertionError if any class is not marked with the 'inline' or 'value' modifier.
 */
fun List<ClassDeclaration>.assertAreInline() {
    assertTrue("Classes must be inline classes") { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
}

/**
 * Asserts that all class declarations have the specified visibility.
 *
 * ### Example:
 * ```kotlin
 * classes.assertHaveVisibility(Visibility.PUBLIC)
 * ```
 *
 * @param visibility The required visibility level.
 * @throws AssertionError if any class does not have the specified visibility.
 */
fun List<ClassDeclaration>.assertHaveVisibility(visibility: Visibility) {
    assertTrue("Classes must have $visibility visibility") { it.visibility == visibility }
}

/**
 * Asserts that all class declarations are public.
 *
 * ### Example:
 * ```kotlin
 * classes.assertArePublic()
 * ```
 *
 * @throws AssertionError if any class is not public.
 */
fun List<ClassDeclaration>.assertArePublic() = assertHaveVisibility(Visibility.PUBLIC)

/**
 * Asserts that all class declarations are internal.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreInternal()
 * ```
 *
 * @throws AssertionError if any class is not internal.
 */
fun List<ClassDeclaration>.assertAreInternal() = assertHaveVisibility(Visibility.INTERNAL)

/**
 * Asserts that all class declarations are private.
 *
 * ### Example:
 * ```kotlin
 * classes.assertArePrivate()
 * ```
 *
 * @throws AssertionError if any class is not private.
 */
fun List<ClassDeclaration>.assertArePrivate() = assertHaveVisibility(Visibility.PRIVATE)

/**
 * Asserts that all class declarations are protected.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreProtected()
 * ```
 *
 * @throws AssertionError if any class is not protected.
 */
fun List<ClassDeclaration>.assertAreProtected() = assertHaveVisibility(Visibility.PROTECTED)

/**
 * Asserts that all class declarations extend or implement any of the specified supertypes.
 *
 * ### Example:
 * ```kotlin
 * classes.assertAreAssignableTo("BaseRepository", "com.example.Identifiable")
 * ```
 *
 * @param superTypes The allowed supertype names. At least one must match.
 * @throws AssertionError if any class is not assignable to any of the specified supertypes.
 */
fun List<ClassDeclaration>.assertAreAssignableTo(
    vararg superTypes: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) {
    assertTrue("Classes must be assignable to any of: ${superTypes.joinToString()}") { clazz ->
        superTypes.any { clazz.isAssignableTo(it, allClasses) }
    }
}

/**
 * Asserts that the selected classes are only accessed by classes residing in packages matching the specified patterns.
 *
 * ### Example:
 * ```kotlin
 * repositories.assertOnlyBeAccessedByAnyPackage("..domain..", "..data..")
 * ```
 *
 * @param packagePatterns Package wildcard patterns representing allowed accessing classes.
 * @param allClasses The complete collection of class declarations used for dependency resolution. Defaults to all classes in the loaded project graph.
 * @throws AssertionError if any unauthorized class accesses any of the target classes.
 */
fun List<ClassDeclaration>.assertOnlyBeAccessedByAnyPackage(
    vararg packagePatterns: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) {
    val violations = mutableListOf<String>()
    for (targetCls in this) {
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
                    "Class ${targetCls.fqName} is accessed by ${accessor.fqName} (in package ${accessor.packageName}), which is not allowed by package pattern(s): ${packagePatterns.joinToString()}",
                )
            }
        }
    }
    if (violations.isNotEmpty()) {
        throw AssertionError(
            buildString {
                appendLine("Assertion failed! The following classes violate access rules:")
                violations.forEach { appendLine("  - $it") }
            },
        )
    }
}

/**
 * Asserts that the selected classes depend only on classes residing in packages matching the specified patterns.
 *
 * ### Example:
 * ```kotlin
 * repositories.assertOnlyDependOnClassesInAnyPackage("..domain..", "..data..")
 * ```
 *
 * @param packagePatterns Package wildcard patterns representing allowed dependency packages.
 * @param allClasses The complete collection of class declarations used for dependency resolution. Defaults to all classes in the loaded project graph.
 * @throws AssertionError if any target class depends on a class outside the allowed packages.
 */
fun List<ClassDeclaration>.assertOnlyDependOnClassesInAnyPackage(
    vararg packagePatterns: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) {
    val violations = mutableListOf<String>()
    val standardExclusions = listOf("java", "javax", "kotlin")

    for (cls in this) {
        val depPackages =
            cls.collectDependencyPackages(allClasses).filter { depPkg ->
                depPkg != cls.packageName && standardExclusions.none { depPkg == it || depPkg.startsWith("$it.") }
            }
        for (depPkg in depPackages) {
            val isAllowed =
                packagePatterns.any { pattern ->
                    PatternMatchers.matchesPackage(pattern, depPkg)
                }
            if (!isAllowed) {
                violations.add(
                    "Class ${cls.fqName} depends on package $depPkg, which is not allowed by package pattern(s): ${packagePatterns.joinToString()}",
                )
            }
        }
    }
    if (violations.isNotEmpty()) {
        throw AssertionError(
            buildString {
                appendLine("Assertion failed! The following classes violate dependency rules:")
                violations.forEach { appendLine("  - $it") }
            },
        )
    }
}

// --- KontureScope delegation assertions ---

/**
 * Asserts that all class declarations in the scope reside in packages matching any of the specified patterns.
 * Matches using standard Kotlin package wildcard matching (e.g. "..domain..").
 *
 * @param packagePatterns The package wildcard patterns. At least one must match.
 * @throws AssertionError if any class does not reside in a matching package.
 */
fun KontureScope.assertResideInAPackage(vararg packagePatterns: String) = classes.assertResideInAPackage(*packagePatterns)

/**
 * Asserts that all class declarations in the scope have names ending with any of the specified suffixes.
 *
 * @param suffixes The allowed suffixes. At least one must match.
 * @throws AssertionError if any class name does not end with any of the specified suffixes.
 */
fun KontureScope.assertNameEndingWith(vararg suffixes: String) = classes.assertNameEndingWith(*suffixes)

/**
 * Asserts that all class declarations in the scope have names starting with any of the specified prefixes.
 *
 * @param prefixes The allowed prefixes. At least one must match.
 * @throws AssertionError if any class name does not start with any of the specified prefixes.
 */
fun KontureScope.assertNameStartingWith(vararg prefixes: String) = classes.assertNameStartingWith(*prefixes)

/**
 * Asserts that all class declarations in the scope have names matching any of the specified glob patterns.
 * Supports '*' wildcards.
 *
 * @param patterns The glob patterns. At least one must match.
 * @throws AssertionError if any class name does not match any of the specified glob patterns.
 */
fun KontureScope.assertNameMatching(vararg patterns: String) = classes.assertNameMatching(*patterns)

/**
 * Asserts that all class declarations in the scope have at least one of the specified annotations.
 * Matches either the annotation's simple name or its fully qualified name (FQN).
 *
 * @param annotationFqNames The annotation names or fully qualified names. At least one must match.
 * @throws AssertionError if any class is not annotated with any of the specified annotations.
 */
fun KontureScope.assertHaveAnnotationOf(vararg annotationFqNames: String) = classes.assertHaveAnnotationOf(*annotationFqNames)

/**
 * Asserts that all class declarations in the scope represent interfaces.
 *
 * @throws AssertionError if any class is not an interface.
 */
fun KontureScope.assertAreInterfaces() = classes.assertAreInterfaces()

/**
 * Asserts that all class declarations in the scope represent enum classes.
 *
 * @throws AssertionError if any class is not an enum class.
 */
fun KontureScope.assertAreEnums() = classes.assertAreEnums()

/**
 * Asserts that all class declarations in the scope represent abstract classes or interfaces.
 *
 * @throws AssertionError if any class is not abstract.
 */
fun KontureScope.assertAreAbstract() = classes.assertAreAbstract()

/**
 * Asserts that all class declarations in the scope are sealed classes.
 *
 * @throws AssertionError if any class is not marked with the 'sealed' modifier.
 */
fun KontureScope.assertAreSealed() = classes.assertAreSealed()

/**
 * Asserts that all class declarations in the scope are data classes.
 *
 * @throws AssertionError if any class is not marked with the 'data' modifier.
 */
fun KontureScope.assertAreData() = classes.assertAreData()

/**
 * Asserts that all class declarations in the scope are inline/value classes.
 *
 * @throws AssertionError if any class is not marked with the 'inline' or 'value' modifier.
 */
fun KontureScope.assertAreInline() = classes.assertAreInline()

/**
 * Asserts that all class declarations in the scope have the specified visibility.
 *
 * @param visibility The required visibility level.
 * @throws AssertionError if any class does not have the specified visibility.
 */
fun KontureScope.assertHaveVisibility(visibility: Visibility) = classes.assertHaveVisibility(visibility)

/**
 * Asserts that all class declarations in the scope are public.
 *
 * @throws AssertionError if any class is not public.
 */
fun KontureScope.assertArePublic() = classes.assertArePublic()

/**
 * Asserts that all class declarations in the scope are internal.
 *
 * @throws AssertionError if any class is not internal.
 */
fun KontureScope.assertAreInternal() = classes.assertAreInternal()

/**
 * Asserts that all class declarations in the scope are private.
 *
 * @throws AssertionError if any class is not private.
 */
fun KontureScope.assertArePrivate() = classes.assertArePrivate()

/**
 * Asserts that all class declarations in the scope are protected.
 *
 * @throws AssertionError if any class is not protected.
 */
fun KontureScope.assertAreProtected() = classes.assertAreProtected()

/**
 * Asserts that all class declarations in the scope extend or implement any of the specified supertypes.
 *
 * @param superTypes The allowed supertype names. At least one must match.
 * @throws AssertionError if any class is not assignable to any of the specified supertypes.
 */
fun KontureScope.assertAreAssignableTo(
    vararg superTypes: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = classes.assertAreAssignableTo(*superTypes, allClasses = allClasses)

/**
 * Asserts that the selected classes in the scope are only accessed by classes residing in packages matching the specified patterns.
 *
 * @param packagePatterns Package wildcard patterns representing allowed accessing classes.
 * @param allClasses The complete collection of class declarations used for dependency resolution. Defaults to all classes in the loaded project graph.
 * @throws AssertionError if any unauthorized class accesses any of the target classes.
 */
fun KontureScope.assertOnlyBeAccessedByAnyPackage(
    vararg packagePatterns: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = classes.assertOnlyBeAccessedByAnyPackage(*packagePatterns, allClasses = allClasses)

/**
 * Asserts that the selected classes in the scope depend only on classes residing in packages matching the specified patterns.
 *
 * @param packagePatterns Package wildcard patterns representing allowed dependency packages.
 * @param allClasses The complete collection of class declarations used for dependency resolution. Defaults to all classes in the loaded project graph.
 * @throws AssertionError if any target class depends on a class outside the allowed packages.
 */
fun KontureScope.assertOnlyDependOnClassesInAnyPackage(
    vararg packagePatterns: String,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = classes.assertOnlyDependOnClassesInAnyPackage(*packagePatterns, allClasses = allClasses)

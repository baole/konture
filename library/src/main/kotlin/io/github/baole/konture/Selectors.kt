/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector

/** Typealias for class scope selector. */
public typealias ClassSelector = KontureScope

/** Typealias for module scope selector. */
public typealias ModuleSelector = KontureModuleScope

/** Typealias for file scope selector. */
public typealias FileSelector = KontureFileScope

/** Typealias for function scope selector. */
public typealias FunctionSelector = KontureFunctionScope

/** Typealias for property scope selector. */
public typealias PropertySelector = KonturePropertyScope

/** Typealias for slice scope selector. */
public typealias SliceSelector = KontureSliceScope

// --- Selector Fluent Filtering Extensions ---

/**
 * Filters this class selector to include classes residing in packages matching the specified pattern.
 */
public fun ClassSelector.inPackage(packagePattern: String): ClassSelector = resideInAPackage(packagePattern)

/**
 * Filters this class selector to include classes whose simple names match the specified pattern.
 */
public fun ClassSelector.withName(pattern: String): ClassSelector = withNameMatching(pattern)

/**
 * Filters this module selector to include modules residing in packages matching the specified pattern.
 *
 * Retains a [Module] if at least one source file in that module resides in a package matching [packagePattern].
 */
public fun ModuleSelector.inPackage(packagePattern: String): ModuleSelector =
    ModuleSelector(
        modules.filter { module ->
            module.files.any { file ->
                PatternMatchers.matchesPackage(packagePattern, file.packageName)
            }
        },
    )

/**
 * Filters this module selector to include modules whose path matches the specified pattern.
 */
public fun ModuleSelector.withName(pattern: String): ModuleSelector = byPath(pattern)

/**
 * Filters this file selector to include files residing in packages matching the specified pattern.
 */
public fun FileSelector.inPackage(packagePattern: String): FileSelector = resideInAPackage(packagePattern)

/**
 * Filters this file selector to include files whose names match the specified pattern.
 */
public fun FileSelector.withName(pattern: String): FileSelector = withNameMatching(pattern)

/**
 * Filters this function selector to include functions residing in packages matching the specified pattern.
 */
public fun FunctionSelector.inPackage(packagePattern: String): FunctionSelector = resideInAPackage(packagePattern)

/**
 * Filters this function selector to include functions whose names match the specified pattern.
 */
public fun FunctionSelector.withName(pattern: String): FunctionSelector = withNameMatching(pattern)

/**
 * Filters this property selector to include properties residing in packages matching the specified pattern.
 */
public fun PropertySelector.inPackage(packagePattern: String): PropertySelector = resideInAPackage(packagePattern)

/**
 * Filters this property selector to include properties whose names match the specified pattern.
 */
public fun PropertySelector.withName(pattern: String): PropertySelector = withNameMatching(pattern)

/**
 * Filters this slice selector to include slices whose names match the specified pattern.
 */
public fun SliceSelector.withName(pattern: String): SliceSelector =
    SliceSelector(
        slices.filter { slice ->
            PatternMatchers.matchesSimpleGlob(pattern, slice.key)
        },
    )

// --- Sequential .should() Assertion Scopes ---

/**
 * Fluent assertion receiver for sequential assertions on a [ClassSelector].
 */
@KontureDsl
public class ClassSelectorShould internal constructor(
    public val selector: ClassSelector,
) {
    /** Asserts that all classes in the selector are interfaces. */
    public fun beInterfaces(): Unit = selector.assertAreInterfaces()

    /** Asserts that all classes in the selector are enum classes. */
    public fun beEnums(): Unit = selector.assertAreEnums()

    /** Asserts that all classes in the selector are abstract classes or interfaces. */
    public fun beAbstract(): Unit = selector.assertAreAbstract()

    /** Asserts that all classes in the selector are sealed classes. */
    public fun beSealed(): Unit = selector.assertAreSealed()

    /** Asserts that all classes in the selector are data classes. */
    public fun beData(): Unit = selector.assertAreData()

    /** Asserts that all classes in the selector are inline/value classes. */
    public fun beInline(): Unit = selector.assertAreInline()

    /** Asserts that all classes in the selector are public. */
    public fun bePublic(): Unit = selector.assertArePublic()

    /** Asserts that all classes in the selector are internal. */
    public fun beInternal(): Unit = selector.assertAreInternal()

    /** Asserts that all classes in the selector are private. */
    public fun bePrivate(): Unit = selector.assertArePrivate()

    /** Asserts that all classes in the selector are protected. */
    public fun beProtected(): Unit = selector.assertAreProtected()

    /** Asserts that all classes in the selector have the specified visibility. */
    public fun haveVisibility(visibility: Visibility): Unit = selector.assertHaveVisibility(visibility)

    /** Asserts that all classes in the selector do not depend on classes in packages matching any of the specified patterns. */
    public fun notDependOnPackages(vararg packagePatterns: String): Unit =
        selector.assertNotDependOnClassesInAnyPackage(*packagePatterns)

    /** Asserts that all classes in the selector depend only on classes in packages matching any of the specified patterns. */
    public fun onlyDependOnPackages(vararg packagePatterns: String): Unit =
        selector.assertOnlyDependOnClassesInAnyPackage(*packagePatterns)

    /** Asserts that classes in the selector are only accessed by packages matching the specified patterns. */
    public fun beAccessedBy(vararg packagePatterns: String): Unit =
        selector.assertOnlyBeAccessedByAnyPackage(*packagePatterns)

    /** Asserts that classes in the selector are only accessed by packages matching the specified patterns. */
    public fun onlyBeAccessedByAnyPackage(vararg packagePatterns: String): Unit =
        selector.assertOnlyBeAccessedByAnyPackage(*packagePatterns)

    /** Asserts that classes in the selector are not accessed by packages matching the specified patterns. */
    public fun notBeAccessedByAnyPackage(vararg packagePatterns: String): Unit =
        selector.assertNotBeAccessedByAnyPackage(*packagePatterns)

    /** Asserts that all classes in the selector have names ending with any of the specified suffixes. */
    public fun haveNameEndingWith(vararg suffixes: String): Unit = selector.assertNameEndingWith(*suffixes)

    /** Asserts that all classes in the selector have names starting with any of the specified prefixes. */
    public fun haveNameStartingWith(vararg prefixes: String): Unit = selector.assertNameStartingWith(*prefixes)

    /** Asserts that all classes in the selector have names matching any of the specified glob patterns. */
    public fun haveNameMatching(vararg patterns: String): Unit = selector.assertNameMatching(*patterns)

    /** Asserts that all classes in the selector have any of the specified annotations. */
    public fun haveAnnotationOf(vararg annotations: String): Unit = selector.assertHaveAnnotationOf(*annotations)

    /** Asserts that all classes in the selector extend or implement any of the specified supertypes. */
    public fun beAssignableTo(vararg superTypes: String): Unit = selector.assertAreAssignableTo(*superTypes)

    /** Asserts that all classes in the selector reside in packages matching any of the specified patterns. */
    public fun resideInAPackage(vararg packagePatterns: String): Unit =
        selector.assertResideInAPackage(*packagePatterns)
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [ClassSelector].
 */
public fun ClassSelector.should(): ClassSelectorShould = ClassSelectorShould(this)

/**
 * Fluent assertion receiver for sequential assertions on a [ModuleSelector].
 */
@KontureDsl
public class ModuleSelectorShould internal constructor(
    public val selector: ModuleSelector,
) {
    /** Asserts that no module in the selector depends on any of the specified module paths or glob patterns. */
    public fun notDependOnModules(vararg modulePatterns: String) {
        val failures = mutableListOf<String>()
        for (module in selector.modules) {
            val dependencies = module.dependencies.map { it.targetPath }
            val forbidden =
                dependencies.filter { depPath ->
                    modulePatterns.any { pattern -> PatternMatchers.matchesModuleGlob(pattern, depPath) }
                }
            if (forbidden.isNotEmpty()) {
                failures.add(
                    "Module ${module.path} depends on forbidden module(s): ${forbidden.distinct().joinToString()}",
                )
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n"))
        }
    }

    /** Asserts that no module in the selector depends on any of the specified module paths or glob patterns. */
    public fun notDependOnModule(vararg modulePatterns: String): Unit = notDependOnModules(*modulePatterns)

    /** Asserts that modules in the selector depend only on the specified module paths or glob patterns. */
    public fun onlyDependOnModules(vararg modulePatterns: String) {
        val failures = mutableListOf<String>()
        for (module in selector.modules) {
            val actual = module.dependencies.map { it.targetPath }
            val unauthorized =
                actual.filterNot { depPath ->
                    modulePatterns.any { pattern -> PatternMatchers.matchesModuleGlob(pattern, depPath) }
                }
            if (unauthorized.isNotEmpty()) {
                failures.add(
                    "Module ${module.path} depends on unauthorized module(s): ${unauthorized.distinct().joinToString()}",
                )
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n"))
        }
    }

    /** Asserts that all modules in the selector have the specified plugins applied. */
    public fun havePlugin(vararg pluginIds: String) {
        val failures = mutableListOf<String>()
        for (module in selector.modules) {
            val missing = pluginIds.filterNot { module.appliedPlugins.contains(it) }
            if (missing.isNotEmpty()) {
                failures.add("Module ${module.path} is missing required plugin(s): ${missing.joinToString()}")
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n"))
        }
    }

    /** Asserts that no modules in the selector have the specified plugins applied. */
    public fun notHavePlugin(vararg pluginIds: String) {
        val failures = mutableListOf<String>()
        for (module in selector.modules) {
            val forbidden = pluginIds.filter { module.appliedPlugins.contains(it) }
            if (forbidden.isNotEmpty()) {
                failures.add(
                    "Module ${module.path} unexpectedly applies forbidden plugin(s): ${forbidden.joinToString()}",
                )
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n"))
        }
    }
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [ModuleSelector].
 */
public fun ModuleSelector.should(): ModuleSelectorShould = ModuleSelectorShould(this)

/**
 * Fluent assertion receiver for sequential assertions on a [FileSelector].
 */
@KontureDsl
public class FileSelectorShould internal constructor(
    public val selector: FileSelector,
) {
    /** Asserts that all files reside in packages matching any of the specified patterns. */
    public fun resideInAPackage(vararg packagePatterns: String): Unit =
        selector.assertResideInAPackage(*packagePatterns)

    /** Asserts that all file names match any of the specified glob patterns. */
    public fun haveNameMatching(vararg patterns: String): Unit = selector.assertNameMatching(*patterns)

    /** Asserts that all file names end with any of the specified suffixes. */
    public fun haveNameEndingWith(vararg suffixes: String): Unit = selector.assertNameEndingWith(*suffixes)

    /** Asserts that all file names start with any of the specified prefixes. */
    public fun haveNameStartingWith(vararg prefixes: String): Unit = selector.assertNameStartingWith(*prefixes)

    /** Asserts that no files contain wildcard imports. */
    public fun haveNoWildcardImports(): Unit = selector.assertNoWildcardImports()

    /** Asserts that files contain at most one class declaration. */
    public fun haveOnlyOneClassPerFile(): Unit = selector.assertOnlyOneClassPerFile()
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [FileSelector].
 */
public fun FileSelector.should(): FileSelectorShould = FileSelectorShould(this)

/**
 * Fluent assertion receiver for sequential assertions on a [FunctionSelector].
 */
@KontureDsl
public class FunctionSelectorShould internal constructor(
    public val selector: FunctionSelector,
) {
    /** Asserts that all functions in the selector are public. */
    public fun bePublic(): Unit = selector.assertTrue("Functions must be public") { it.visibility == Visibility.PUBLIC }

    /** Asserts that all functions in the selector are internal. */
    public fun beInternal(): Unit =
        selector.assertTrue("Functions must be internal") {
            it.visibility == Visibility.INTERNAL
        }

    /** Asserts that all functions in the selector are private. */
    public fun bePrivate(): Unit =
        selector.assertTrue(
            "Functions must be private",
        ) { it.visibility == Visibility.PRIVATE }

    /** Asserts that all functions in the selector are protected. */
    public fun beProtected(): Unit =
        selector.assertTrue("Functions must be protected") {
            it.visibility == Visibility.PROTECTED
        }

    /** Asserts that all functions in the selector are marked inline. */
    public fun beInline(): Unit =
        selector.assertTrue("Functions must be inline") {
            it.declaration.modifiers.contains(Modifier.INLINE)
        }

    /** Asserts that all functions in the selector are operator functions. */
    public fun beOperator(): Unit =
        selector.assertTrue("Functions must be operator") {
            it.declaration.modifiers.contains(Modifier.OPERATOR)
        }

    /** Asserts that all functions in the selector are infix functions. */
    public fun beInfix(): Unit =
        selector.assertTrue("Functions must be infix") {
            it.declaration.modifiers.contains(Modifier.INFIX)
        }

    /** Asserts that all functions in the selector are suspend functions. */
    public fun beSuspend(): Unit =
        selector.assertTrue("Functions must be suspend") {
            it.declaration.modifiers.contains(Modifier.SUSPEND)
        }

    /** Asserts that all functions in the selector are override functions. */
    public fun beOverride(): Unit =
        selector.assertTrue("Functions must be override") {
            it.declaration.modifiers.contains(Modifier.OVERRIDE)
        }

    /** Asserts that function names match any of the specified glob patterns. */
    public fun haveNameMatching(vararg patterns: String): Unit =
        selector.assertTrue("Functions must match name pattern(s)") { func ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
        }

    /** Asserts that all functions have any of the specified annotations. */
    public fun haveAnnotationOf(vararg annotations: String): Unit =
        selector.assertTrue("Functions must have annotation(s)") { func ->
            annotations.any { func.hasAnnotation(it) }
        }
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [FunctionSelector].
 */
public fun FunctionSelector.should(): FunctionSelectorShould = FunctionSelectorShould(this)

/**
 * Fluent assertion receiver for sequential assertions on a [PropertySelector].
 */
@KontureDsl
public class PropertySelectorShould internal constructor(
    public val selector: PropertySelector,
) {
    /** Asserts that all properties in the selector are public. */
    public fun bePublic(): Unit =
        selector.assertTrue(
            "Properties must be public",
        ) { it.visibility == Visibility.PUBLIC }

    /** Asserts that all properties in the selector are internal. */
    public fun beInternal(): Unit =
        selector.assertTrue("Properties must be internal") {
            it.visibility == Visibility.INTERNAL
        }

    /** Asserts that all properties in the selector are private. */
    public fun bePrivate(): Unit =
        selector.assertTrue(
            "Properties must be private",
        ) { it.visibility == Visibility.PRIVATE }

    /** Asserts that all properties in the selector are protected. */
    public fun beProtected(): Unit =
        selector.assertTrue("Properties must be protected") {
            it.visibility == Visibility.PROTECTED
        }

    /** Asserts that all properties in the selector are read-only ('val'). */
    public fun beVal(): Unit = selector.assertTrue("Properties must be val") { it.declaration.isVal }

    /** Asserts that all properties in the selector are mutable ('var'). */
    public fun beVar(): Unit = selector.assertTrue("Properties must be var") { !it.declaration.isVal }

    /** Asserts that all properties in the selector are compile-time constants ('const val'). */
    public fun beConst(): Unit =
        selector.assertTrue("Properties must be const") {
            it.declaration.modifiers.contains(Modifier.CONST)
        }

    /** Asserts that all properties in the selector are marked 'lateinit'. */
    public fun beLateinit(): Unit =
        selector.assertTrue("Properties must be lateinit") {
            it.declaration.modifiers.contains(Modifier.LATEINIT)
        }

    /** Asserts that property names match any of the specified glob patterns. */
    public fun haveNameMatching(vararg patterns: String): Unit =
        selector.assertTrue("Properties must match name pattern(s)") { prop ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
        }

    /** Asserts that all properties have any of the specified annotations. */
    public fun haveAnnotationOf(vararg annotations: String): Unit =
        selector.assertTrue("Properties must have annotation(s)") { prop ->
            annotations.any { prop.hasAnnotation(it) }
        }
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [PropertySelector].
 */
public fun PropertySelector.should(): PropertySelectorShould = PropertySelectorShould(this)

/**
 * Fluent assertion receiver for sequential assertions on a [SliceSelector].
 */
@KontureDsl
public class SliceSelectorShould internal constructor(
    public val selector: SliceSelector,
) {
    /** Asserts that there are no cycle dependencies between slices in the selector. */
    public fun beFreeOfCycles() {
        val packageToSlice = mutableMapOf<String, String>()
        val allClasses = selector.slices.flatMap { it.classes }
        for (slice in selector.slices) {
            for (pkg in slice.packages) {
                packageToSlice[pkg] = slice.key
            }
        }
        val sliceGraph =
            SliceCycleDetector.buildGraph(
                selector.slices,
                packageToSlice,
                allClasses,
                "slice",
            )
        val cycles = SliceCycleDetector.findCycles(sliceGraph.adjacency)
        if (cycles.isNotEmpty()) {
            throw AssertionError("Cyclic dependencies detected between slices:\n" + cycles.joinToString("\n"))
        }
    }

    /** Asserts that there are no cycle dependencies between slices in the selector. */
    public fun notHaveCycles(): Unit = beFreeOfCycles()
}

/**
 * Returns a fluent assertion scope for sequential assertions on this [SliceSelector].
 */
public fun SliceSelector.should(): SliceSelectorShould = SliceSelectorShould(this)

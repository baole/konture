/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain), Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector

/**
 * Assertions that the slices derived by [SlicesRuleBuilder] must satisfy.
 */
@KontureDsl
public class SlicesShould(private val builder: SlicesRuleBuilder) {
    /**
     * Asserts that the slices have no cyclic dependencies between them. Each detected cycle is
     * reported as a path, e.g. `payment -> billing -> payment`.
     */
    public fun beFreeOfCycles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (cycle in SliceCycleDetector.findCycles(sliceGraph.adjacency)) {
                /** Filter or assertion criteria for rendered. */
                val rendered = (cycle + cycle.first()).joinToString(" -> ")
                violations.add(getMessage("slice.should.beFreeOfCycles", rendered))
            }
        }
        return builder
    }

    /**
     * Asserts that no slice depends on any other slice — enforcing complete isolation between them.
     * Every inter-slice dependency is reported.
     */
    public fun notDependOnEachOther(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                for (to in targets.sorted()) {
                    violations.add(getMessage("slice.should.notDependOnEachOther", from, to))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices depend only on the specified allowed slices.
     */
    public fun onlyDependOnSlices(allowedSliceKeys: List<String>): SlicesRuleBuilder =
        onlyDependOnSlices(*allowedSliceKeys.toTypedArray())

    /** Asserts that slices depend only on the specified allowed slices vararg. */
    public fun onlyDependOnSlices(vararg allowedSliceKeys: String): SlicesRuleBuilder {
        /** Filter or assertion criteria for allowed. */
        val allowed = allowedSliceKeys.toSet()
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                for (to in targets.sorted()) {
                    if (to !in allowed) {
                        violations.add(getMessage("slice.should.onlyDependOnSlices", from, to, allowed.joinToString()))
                    }
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices do not depend on the specified forbidden slice.
     */
    public infix fun notDependOnSlice(forbiddenSliceKey: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                if (forbiddenSliceKey in targets) {
                    violations.add(getMessage("slice.should.notDependOnSlice", from, forbiddenSliceKey))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices do not depend on any of the specified forbidden slices.
     */
    public infix fun notDependOnSlices(forbiddenSliceKeys: List<String>): SlicesRuleBuilder {
        forbiddenSliceKeys.forEach { notDependOnSlice(it) }
        return builder
    }

    /**
     * Asserts that slices do not depend on any of the specified forbidden slices.
     */
    public fun notDependOnSlices(vararg forbiddenSliceKeys: String): SlicesRuleBuilder =
        notDependOnSlices(forbiddenSliceKeys.toList())

    /**
     * Asserts that slices depend on the specified required slice.
     */
    public infix fun dependOnSlice(requiredSliceKey: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                if (from != requiredSliceKey && requiredSliceKey !in targets) {
                    violations.add(getMessage("slice.should.dependOnSlice", from, requiredSliceKey))
                }
            }
        }
        return builder
    }

    /** Asserts that slices depend on required slices in [requiredSliceKeys]. */
    public infix fun dependOnSlices(requiredSliceKeys: List<String>): SlicesRuleBuilder {
        requiredSliceKeys.forEach { dependOnSlice(it) }
        return builder
    }

    /**
     * Asserts that slices depend on all specified required slices.
     */
    public fun dependOnSlices(vararg requiredSliceKeys: String): SlicesRuleBuilder =
        dependOnSlices(requiredSliceKeys.toList())

    /** Asserts that slices contain classes. */
    public fun containClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isEmpty()) {
                    violations.add(getMessage("slice.should.containClasses", slice.key))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not contain classes. */
    public fun notContainClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isNotEmpty()) {
                    violations.add(getMessage("slice.should.notContainClasses", slice.key))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not contain class [fqName]. */
    public infix fun notContainClass(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { it.fqName == fqName || it.name == fqName }) {
                    violations.add(getMessage("slice.should.notContainClass", slice.key, fqName))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not contain class [type]. */
    public infix fun notContainClass(type: kotlin.reflect.KClass<*>): SlicesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    /** Asserts that slices contain classes in package matching [packagePattern]. */
    public infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches = slice.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
                if (!matches) {
                    violations.add(getMessage("slice.should.containClassesInPackage", slice.key, packagePattern))
                }
            }
        }
        return builder
    }

    /** Asserts that slices contain classes in packages matching [packagePatterns]. */
    public infix fun containClassesInPackage(packagePatterns: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches =
                    slice.classes.any {
                            cls ->
                        packagePatterns.any { PatternMatchers.matchesPackage(it, cls.packageName) }
                    }
                if (!matches) {
                    violations.add(
                        getMessage("slice.should.containClassesInPackage", slice.key, packagePatterns.joinToString()),
                    )
                }
            }
        }
        return builder
    }

    /** Asserts that slices contain classes in packages matching vararg [packagePatterns]. */
    public fun containClassesInPackage(vararg packagePatterns: String): SlicesRuleBuilder =
        containClassesInPackage(packagePatterns.toList())

    /** Asserts that slices do not contain classes in package matching [packagePattern]. */
    public infix fun notContainClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches = slice.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
                if (matches) {
                    violations.add(getMessage("slice.should.notContainClassesInPackage", slice.key, packagePattern))
                }
            }
        }
        return builder
    }

    /** Asserts that slices contain classes with annotation [annotationFqName]. */
    public infix fun containClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches =
                    slice.classes.any { cls ->
                        cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                    }
                if (!matches) {
                    violations.add(getMessage("slice.should.containClassesWithAnnotation", slice.key, annotationFqName))
                }
            }
        }
        return builder
    }

    /** Asserts that slices contain classes with annotations [annotationFqNames]. */
    public infix fun containClassesWithAnnotation(annotationFqNames: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches =
                    slice.classes.any { cls ->
                        cls.annotations.any { ann -> annotationFqNames.any { ann.name == it || ann.fqName == it } }
                    }
                if (!matches) {
                    violations.add(
                        getMessage(
                            "slice.should.containClassesWithAnnotation",
                            slice.key,
                            annotationFqNames.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /** Asserts that slices contain classes with vararg annotations [annotationFqNames]. */
    public fun containClassesWithAnnotation(vararg annotationFqNames: String): SlicesRuleBuilder =
        containClassesWithAnnotation(annotationFqNames.toList())

    /** Asserts that slices contain classes with annotation [annotation]. */
    public infix fun containClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): SlicesRuleBuilder = containClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Asserts that slices do not contain classes with annotation [annotationFqName]. */
    public infix fun notContainClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for matches. */
                val matches =
                    slice.classes.any { cls ->
                        cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                    }
                if (matches) {
                    violations.add(
                        getMessage("slice.should.notContainClassesWithAnnotation", slice.key, annotationFqName),
                    )
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not contain classes with annotation [annotation]. */
    public infix fun notContainClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): SlicesRuleBuilder = notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    /**
     * Custom assertion block executed against the derived [SliceGraph].
     */
    internal fun satisfy(
        assertion: (io.github.baole.konture.impl.SliceGraph, MutableList<String>) -> Unit,
    ): SlicesRuleBuilder {
        builder.addShouldAssertion(assertion)
        return builder
    }

    /**
     * Asserts that the derived slices satisfy a custom Boolean predicate.
     */
    internal fun satisfy(
        description: String,
        predicate: (io.github.baole.konture.impl.SliceGraph) -> Boolean,
    ): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            if (!predicate(sliceGraph)) {
                violations.add(getMessage("slice.should.satisfyCustomCondition", description))
            }
        }
        return builder
    }

    /** Asserts that slices satisfy custom description [description]. */
    public fun satisfy(description: String): SlicesRuleBuilder = satisfy(description) { true }

    /** Asserts that slices satisfy at least one assertion block in [blocks]. */
    public fun anyOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for temp builder. */
                    val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                    SlicesShould(tempBuilder).apply(block)
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                    tempViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add(getMessage("slice.should.satisfyAnyOf"))
            }
        }
        return builder
    }

    /** Asserts that slices satisfy all assertion blocks in [blocks]. */
    public fun allOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            blocks.forEach { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                SlicesShould(tempBuilder).apply(block)
                tempBuilder.checkRuleAssertions(sliceGraph, violations)
            }
        }
        return builder
    }

    /** Asserts that slices satisfy none of the assertion blocks in [blocks]. */
    public fun noneOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for temp builder. */
                    val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                    SlicesShould(tempBuilder).apply(block)
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                    tempViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add(getMessage("slice.should.satisfyNoneOf"))
            }
        }
        return builder
    }

    /** Asserts that slices contain files. */
    public fun containFiles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isEmpty()) {
                    violations.add(getMessage("slice.should.notBeEmpty", slice.key))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not contain files. */
    public fun notContainFiles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isNotEmpty()) {
                    violations.add(getMessage("slice.should.beEmpty", slice.key))
                }
            }
        }
        return builder
    }

    /** Asserts that slices have key matching [namePattern]. */
    public infix fun haveName(namePattern: String): SlicesRuleBuilder = dependOnSlice(namePattern)

    /** Asserts that slices do not have key matching [namePattern]. */
    public infix fun notHaveName(namePattern: String): SlicesRuleBuilder = notDependOnSlice(namePattern)

    /** Asserts that slices reside in module [modulePath]. */
    public infix fun resideInModule(modulePath: String): SlicesRuleBuilder {
        val cleanName = modulePath.removePrefix(":").removePrefix("/")
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (!slice.classes.any { cls ->
                        val normPath = cls.filePath.replace('\\', '/')
                        normPath.contains("/$cleanName/") || normPath.contains("$cleanName/") || normPath.contains(modulePath)
                    }
                ) {
                    violations.add(getMessage("slice.should.resideInModulePath", slice.key, modulePath))
                }
            }
        }
        return builder
    }

    /** Asserts that slices reside in modules [modulePaths]. */
    public infix fun resideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        val cleanNames = modulePaths.map { it.removePrefix(":").removePrefix("/") }
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (!slice.classes.any { cls ->
                        val normPath = cls.filePath.replace('\\', '/')
                        cleanNames.any { cleanName ->
                            normPath.contains("/$cleanName/") || normPath.contains("$cleanName/") || normPath.contains(cleanName)
                        }
                    }
                ) {
                    violations.add(getMessage("slice.should.resideInModulePaths", slice.key, modulePaths.toString()))
                }
            }
        }
        return builder
    }

    /** Asserts that slices reside in vararg modules [modulePaths]. */
    public fun resideInModules(vararg modulePaths: String): SlicesRuleBuilder = resideInModules(modulePaths.toList())

    /** Asserts that slices do not reside in module [modulePath]. */
    public infix fun notResideInModule(modulePath: String): SlicesRuleBuilder {
        val cleanName = modulePath.removePrefix(":").removePrefix("/")
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { cls ->
                        val normPath = cls.filePath.replace('\\', '/')
                        normPath.contains("/$cleanName/") || normPath.contains("$cleanName/") || normPath.contains(modulePath)
                    }
                ) {
                    violations.add(getMessage("slice.should.notResideInModulePath", slice.key, modulePath))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not reside in modules [modulePaths]. */
    public infix fun notResideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        val cleanNames = modulePaths.map { it.removePrefix(":").removePrefix("/") }
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { cls ->
                        val normPath = cls.filePath.replace('\\', '/')
                        cleanNames.any { cleanName ->
                            normPath.contains("/$cleanName/") || normPath.contains("$cleanName/") || normPath.contains(cleanName)
                        }
                    }
                ) {
                    violations.add(getMessage("slice.should.notResideInModulePaths", slice.key, modulePaths.toString()))
                }
            }
        }
        return builder
    }

    /** Asserts that slices do not reside in vararg modules [modulePaths]. */
    public fun notResideInModules(vararg modulePaths: String): SlicesRuleBuilder =
        notResideInModules(modulePaths.toList())

    /** Fails for every call usage matching [fqName] in any file contained in the selected slices. */
    public fun notCall(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            /** Filter or assertion criteria for all files. */
            val allFiles = builder.graph.getAllModules().flatMap { it.files }
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for slice files. */
                val sliceFiles =
                    allFiles.filter {
                            file ->
                        slice.packages.any {
                                pkg ->
                            PatternMatchers.matchesPackage(pkg, file.packageName) || file.packageName == pkg
                        }
                    }
                for (file in sliceFiles) {
                    /** Filter or assertion criteria for calls. */
                    val calls = file.usages.filter { PatternMatchers.isCallUsageMatch(it, fqName) }
                    for (usage in calls) {
                        violations.add(
                            "Slice '${slice.key}' file '${file.name}' calls prohibited target '$fqName' (expression: '${usage.rawExpression}')",
                        )
                    }
                }
            }
        }
        return builder
    }

    /** Fails for every call usage matching class [kClass] in any file contained in the selected slices. */
    public fun notCall(kClass: kotlin.reflect.KClass<*>): SlicesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every class reference usage matching [fqName] in any file contained in the selected slices. */
    public fun notReferenceClass(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            /** Filter or assertion criteria for all files. */
            val allFiles = builder.graph.getAllModules().flatMap { it.files }
            for (slice in sliceGraph.slices) {
                /** Filter or assertion criteria for slice files. */
                val sliceFiles =
                    allFiles.filter {
                            file ->
                        slice.packages.any {
                                pkg ->
                            PatternMatchers.matchesPackage(pkg, file.packageName) || file.packageName == pkg
                        }
                    }
                for (file in sliceFiles) {
                    /** Filter or assertion criteria for refs. */
                    val refs = file.usages.filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                    for (usage in refs) {
                        violations.add(
                            "Slice '${slice.key}' file '${file.name}' references prohibited class '$fqName'",
                        )
                    }
                }
            }
        }
        return builder
    }

    /** Fails for every class reference usage matching [kClass] in any file contained in the selected slices. */
    public fun notReferenceClass(kClass: kotlin.reflect.KClass<*>): SlicesRuleBuilder =
        notReferenceClass(
            kClass.kontureQualifiedName(),
        )
}

/** Asserts that slices contain classes with annotation type parameter [T]. */
public inline fun <reified T : Annotation> SlicesShould.containClassesWithAnnotation(): SlicesRuleBuilder =
    containClassesWithAnnotation(T::class)

/** Asserts that slices do not call members of type parameter [T]. */
public inline fun <reified T : Any> SlicesShould.notCall(): SlicesRuleBuilder = notCall(T::class)

/** Asserts that slices do not reference class type parameter [T]. */
public inline fun <reified T : Any> SlicesShould.notReferenceClass(): SlicesRuleBuilder = notReferenceClass(T::class)

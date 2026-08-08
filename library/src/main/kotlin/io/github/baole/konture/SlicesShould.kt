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
class SlicesShould(private val builder: SlicesRuleBuilder) {
    /**
     * Asserts that the slices have no cyclic dependencies between them. Each detected cycle is
     * reported as a path, e.g. `payment -> billing -> payment`.
     */
    fun beFreeOfCycles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (cycle in SliceCycleDetector.findCycles(sliceGraph.adjacency)) {
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
    fun notDependOnEachOther(): SlicesRuleBuilder {
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
    fun onlyDependOnSlices(allowedSliceKeys: List<String>): SlicesRuleBuilder =
        onlyDependOnSlices(*allowedSliceKeys.toTypedArray())

    fun onlyDependOnSlices(vararg allowedSliceKeys: String): SlicesRuleBuilder {
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
    infix fun notDependOnSlice(forbiddenSliceKey: String): SlicesRuleBuilder {
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
    infix fun notDependOnSlices(forbiddenSliceKeys: List<String>): SlicesRuleBuilder {
        forbiddenSliceKeys.forEach { notDependOnSlice(it) }
        return builder
    }

    /**
     * Asserts that slices do not depend on any of the specified forbidden slices.
     */
    fun notDependOnSlices(vararg forbiddenSliceKeys: String): SlicesRuleBuilder =
        notDependOnSlices(forbiddenSliceKeys.toList())

    /**
     * Asserts that slices depend on the specified required slice.
     */
    infix fun dependOnSlice(requiredSliceKey: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                if (from != requiredSliceKey && requiredSliceKey !in targets) {
                    violations.add(getMessage("slice.should.dependOnSlice", from, requiredSliceKey))
                }
            }
        }
        return builder
    }

    infix fun dependOnSlices(requiredSliceKeys: List<String>): SlicesRuleBuilder {
        requiredSliceKeys.forEach { dependOnSlice(it) }
        return builder
    }

    /**
     * Asserts that slices depend on all specified required slices.
     */
    fun dependOnSlices(vararg requiredSliceKeys: String): SlicesRuleBuilder = dependOnSlices(requiredSliceKeys.toList())

    fun containClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isEmpty()) {
                    violations.add(getMessage("slice.should.containClasses", slice.key))
                }
            }
        }
        return builder
    }

    fun notContainClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isNotEmpty()) {
                    violations.add(getMessage("slice.should.notContainClasses", slice.key))
                }
            }
        }
        return builder
    }

    infix fun notContainClass(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { it.fqName == fqName || it.name == fqName }) {
                    violations.add("Slice '${slice.key}' contains prohibited class '$fqName'")
                }
            }
        }
        return builder
    }

    infix fun notContainClass(type: kotlin.reflect.KClass<*>): SlicesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                val matches = slice.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
                if (!matches) {
                    violations.add(getMessage("slice.should.containClassesInPackage", slice.key, packagePattern))
                }
            }
        }
        return builder
    }

    infix fun containClassesInPackage(packagePatterns: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
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

    fun containClassesInPackage(vararg packagePatterns: String): SlicesRuleBuilder =
        containClassesInPackage(packagePatterns.toList())

    infix fun notContainClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                val matches = slice.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
                if (matches) {
                    violations.add(getMessage("slice.should.notContainClassesInPackage", slice.key, packagePattern))
                }
            }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
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

    infix fun containClassesWithAnnotation(annotationFqNames: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
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

    fun containClassesWithAnnotation(vararg annotationFqNames: String): SlicesRuleBuilder =
        containClassesWithAnnotation(annotationFqNames.toList())

    infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): SlicesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notContainClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
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

    infix fun notContainClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): SlicesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

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
                violations.add("Slice graph does not satisfy custom condition: $description")
            }
        }
        return builder
    }

    fun satisfy(description: String): SlicesRuleBuilder =
        satisfy(description) { true }


    fun anyOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {

        builder.addShouldAssertion { sliceGraph, violations ->
            val anyPassed =
                blocks.any { block ->
                    val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                    SlicesShould(tempBuilder).apply(block)
                    val tempViolations = mutableListOf<String>()
                    tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                    tempViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add("Slice graph does not satisfy any of the specified conditions")
            }
        }
        return builder
    }

    fun allOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            blocks.forEach { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                SlicesShould(tempBuilder).apply(block)
                tempBuilder.checkRuleAssertions(sliceGraph, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            val anyPassed =
                blocks.any { block ->
                    val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                    SlicesShould(tempBuilder).apply(block)
                    val tempViolations = mutableListOf<String>()
                    tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                    tempViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add("Slice graph satisfies one of the forbidden conditions")
            }
        }
        return builder
    }

    fun containFiles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isEmpty()) {
                    violations.add("Slice '${slice.key}' does not contain any files/classes")
                }
            }
        }
        return builder
    }

    fun notContainFiles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isNotEmpty()) {
                    violations.add("Slice '${slice.key}' should not contain any files/classes")
                }
            }
        }
        return builder
    }

    // Name aliases mirroring haveKey / notHaveKey
    infix fun haveName(namePattern: String): SlicesRuleBuilder = dependOnSlice(namePattern)

    infix fun notHaveName(namePattern: String): SlicesRuleBuilder = notDependOnSlice(namePattern)

    // Module assertions
    infix fun resideInModule(modulePath: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (!slice.classes.any { it.filePath.contains(modulePath) }) {
                    violations.add("Slice '${slice.key}' does not reside in module $modulePath")
                }
            }
        }
        return builder
    }

    infix fun resideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (!slice.classes.any { cls -> modulePaths.any { cls.filePath.contains(it) } }) {
                    violations.add("Slice '${slice.key}' does not reside in modules $modulePaths")
                }
            }
        }
        return builder
    }

    fun resideInModules(vararg modulePaths: String): SlicesRuleBuilder = resideInModules(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { it.filePath.contains(modulePath) }) {
                    violations.add("Slice '${slice.key}' resides in prohibited module $modulePath")
                }
            }
        }
        return builder
    }

    infix fun notResideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.any { cls -> modulePaths.any { cls.filePath.contains(it) } }) {
                    violations.add("Slice '${slice.key}' resides in prohibited modules $modulePaths")
                }
            }
        }
        return builder
    }

    fun notResideInModules(vararg modulePaths: String): SlicesRuleBuilder = notResideInModules(modulePaths.toList())

    /** Fails for every call usage matching [fqName] in any file contained in the selected slices. */
    fun notCall(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            val allFiles = builder.graph.getAllModules().flatMap { it.files }
            for (slice in sliceGraph.slices) {
                val sliceFiles =
                    allFiles.filter {
                            file ->
                        slice.packages.any {
                                pkg ->
                            PatternMatchers.matchesPackage(pkg, file.packageName) || file.packageName == pkg
                        }
                    }
                for (file in sliceFiles) {
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

    fun notCall(kClass: kotlin.reflect.KClass<*>): SlicesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every class reference usage matching [fqName] in any file contained in the selected slices. */
    fun notReferenceClass(fqName: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            val allFiles = builder.graph.getAllModules().flatMap { it.files }
            for (slice in sliceGraph.slices) {
                val sliceFiles =
                    allFiles.filter {
                            file ->
                        slice.packages.any {
                                pkg ->
                            PatternMatchers.matchesPackage(pkg, file.packageName) || file.packageName == pkg
                        }
                    }
                for (file in sliceFiles) {
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

    fun notReferenceClass(kClass: kotlin.reflect.KClass<*>): SlicesRuleBuilder =
        notReferenceClass(
            kClass.kontureQualifiedName(),
        )
}

inline fun <reified T : Annotation> SlicesShould.containClassesWithAnnotation(): SlicesRuleBuilder =
    containClassesWithAnnotation(T::class)

inline fun <reified T : Any> SlicesShould.notCall(): SlicesRuleBuilder = notCall(T::class)

inline fun <reified T : Any> SlicesShould.notReferenceClass(): SlicesRuleBuilder = notReferenceClass(T::class)

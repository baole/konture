/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Fluent API for filtering Kotlin classes in architecture rules.
 */
@KontureDsl
public class ClassesThat internal constructor(
    /** Filter or assertion criteria for builder. */
    override val builder: ClassesRuleBuilder,
) : ClassesThatPackageFilter,
    ClassesThatNameFilter,
    ClassesThatStructureFilter,
    ClassesThatMetadataFilter,
    ClassesThatCompositeFilter {
    /** Filters classes that are assignable to type parameter [T]. */
    public inline fun <reified T : Any> areAssignableTo(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).areAssignableTo(T::class)

    /** Filters classes that are assignable from type parameter [T]. */
    public inline fun <reified T : Any> areAssignableFrom(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).areAssignableFrom(T::class)

    /** Filters classes that are children of type parameter [T]. */
    public inline fun <reified T : Any> beChildOf(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).beChildOf(T::class)

    /** Filters classes annotated with annotation type parameter [T]. */
    public inline fun <reified T : Annotation> areAnnotatedWith(): ClassesRuleBuilder =
        (this as ClassesThatMetadataFilter).areAnnotatedWith(T::class)

    /** Filters classes having annotation type parameter [T]. */
    public inline fun <reified T : Annotation> haveAnnotationOf(): ClassesRuleBuilder =
        (this as ClassesThatMetadataFilter).haveAnnotationOf(T::class)
}

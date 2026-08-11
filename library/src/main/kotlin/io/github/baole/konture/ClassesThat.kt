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
class ClassesThat internal constructor(
    override val builder: ClassesRuleBuilder,
) : ClassesThatPackageFilter,
    ClassesThatNameFilter,
    ClassesThatStructureFilter,
    ClassesThatMetadataFilter,
    ClassesThatCompositeFilter {
    inline fun <reified T : Any> areAssignableTo(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).areAssignableTo(T::class)

    inline fun <reified T : Any> areAssignableFrom(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).areAssignableFrom(T::class)

    inline fun <reified T : Any> beChildOf(): ClassesRuleBuilder =
        (this as ClassesThatStructureFilter).beChildOf(T::class)

    inline fun <reified T : Annotation> areAnnotatedWith(): ClassesRuleBuilder =
        (this as ClassesThatMetadataFilter).areAnnotatedWith(T::class)

    inline fun <reified T : Annotation> haveAnnotationOf(): ClassesRuleBuilder =
        (this as ClassesThatMetadataFilter).haveAnnotationOf(T::class)
}

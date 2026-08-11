/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package io.github.baole.konture

import kotlin.reflect.KClass

/** Filters properties having an annotation [annotation] with argument [argName] equal to [argValue]. */
public fun PropertiesThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): PropertiesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Asserts that properties have an annotation [annotation] with argument [argName] equal to [argValue]. */
public fun PropertiesShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): PropertiesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Filters properties having the specified [annotation] class. */
public infix fun PropertiesThat.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Filters properties having annotation type parameter [T]. */
public inline fun <reified T : Annotation> PropertiesThat.haveAnnotationOfType(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

/** Filters properties having all of the specified [annotations]. */
public fun PropertiesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters properties having any of the specified [annotations]. */
public fun PropertiesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that properties have the specified [annotation] class. */
public infix fun PropertiesShould.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Asserts that properties have annotation type parameter [T]. */
public inline fun <reified T : Annotation> PropertiesShould.haveAnnotationOfType(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

/** Asserts that properties have all of the specified [annotations]. */
public fun PropertiesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that properties have any of the specified [annotations]. */
public fun PropertiesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters properties residing in the package of [type]. */
public infix fun PropertiesThat.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

/** Filters properties residing in the package of type parameter [T]. */
public inline fun <reified T : Any> PropertiesThat.resideInPackageOf(): PropertiesRuleBuilder =
    resideInPackageOf(T::class)

/** Asserts that properties reside in the package of [type]. */
public infix fun PropertiesShould.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

/** Asserts that properties reside in the package of type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.resideInPackageOf(): PropertiesRuleBuilder =
    resideInPackageOf(T::class)

/** Asserts that properties in this assertion scope have the specified type [type]. */
public fun PropertyAssertionScope.haveType(type: KClass<*>) {
    /** Filter or assertion criteria for expected type. */
    val expectedType = type.toKontureTypeReference()
    assertions.add { property, violations ->
        if (property.resolvedType?.let { matchesKotlinType(it, expectedType) } != true) {
            violations.add(
                io.github.baole.konture.i18n.getMessage(
                    "property.scope.haveType",
                    "'${type.kontureQualifiedName()}'",
                    property.type,
                ),
            )
        }
    }
}

/** Asserts that properties in this assertion scope have the type specified by [T]. */
public inline fun <reified T : Any> PropertyAssertionScope.haveTypeOf(): Unit = haveType(T::class)

/** Asserts that properties in this assertion scope have the specified [annotation] class. */
public fun PropertyAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) {
    haveAnnotationOf(annotation.kontureQualifiedName())
}

/** Asserts that properties in this assertion scope have the annotation specified by [T]. */
public inline fun <reified T : Annotation> PropertyAssertionScope.haveAnnotationOfType(): Unit =
    haveAnnotationOf(T::class)

/** Asserts that properties do not call members of type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.notCall(): PropertiesRuleBuilder = notCall(T::class)

/** Asserts that properties do not reference class type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.notReferenceClass(): PropertiesRuleBuilder =
    notReferenceClass(T::class)

/** Filters properties that have an import of type parameter [T]. */
public inline fun <reified T : Any> PropertiesThat.haveImportOf(): PropertiesRuleBuilder = haveImportOf(T::class)

/** Filters properties that do not have an import of type parameter [T]. */
public inline fun <reified T : Any> PropertiesThat.notHaveImportOf(): PropertiesRuleBuilder = notHaveImportOf(T::class)

/** Asserts that properties have an import of type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.haveImportOf(): PropertiesRuleBuilder = haveImportOf(T::class)

/** Asserts that properties do not have an import of type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.notHaveImportOf(): PropertiesRuleBuilder =
    notHaveImportOf(T::class)

/** Filters properties having annotation specified by annotation type parameter [T]. */
public inline fun <reified T : Annotation> PropertiesThat.haveAnnotationOf(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

/** Filters properties annotated with [annotation]. */
public infix fun PropertiesThat.areAnnotatedWith(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation)

/** Filters properties annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> PropertiesThat.areAnnotatedWith(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

/** Asserts that properties are annotated with [annotation]. */
public infix fun PropertiesShould.beAnnotatedWith(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Asserts that properties are annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> PropertiesShould.beAnnotatedWith(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

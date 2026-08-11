/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package io.github.baole.konture

import kotlin.reflect.KClass

/** Filters functions having an annotation [annotation] with an argument [argName] equal to [argValue]. */
public fun FunctionsThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): FunctionsRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Asserts that functions have an annotation [annotation] with an argument [argName] equal to [argValue]. */
public fun FunctionsShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): FunctionsRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Filters functions having the specified [annotation] class. */
public infix fun FunctionsThat.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Filters functions having the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FunctionsThat.haveAnnotationOfType(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

/** Filters functions having all of the specified [annotations]. */
public fun FunctionsThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters functions having any of the specified [annotations]. */
public fun FunctionsThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that functions have the specified [annotation] class. */
public infix fun FunctionsShould.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Asserts that functions have the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FunctionsShould.haveAnnotationOfType(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

/** Asserts that functions have all of the specified [annotations]. */
public fun FunctionsShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that functions have any of the specified [annotations]. */
public fun FunctionsShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters functions residing in the package of the specified [type]. */
public infix fun FunctionsThat.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

/** Filters functions residing in the package of the type parameter [T]. */
public inline fun <reified T : Any> FunctionsThat.resideInPackageOf(): FunctionsRuleBuilder =
    resideInPackageOf(T::class)

/** Asserts that functions reside in the package of the specified [type]. */
public infix fun FunctionsShould.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

/** Asserts that functions reside in the package of the type parameter [T]. */
public inline fun <reified T : Any> FunctionsShould.resideInPackageOf(): FunctionsRuleBuilder =
    resideInPackageOf(T::class)

/** Asserts that functions in this assertion scope have the specified return [type]. */
public fun FunctionAssertionScope.haveReturnType(type: KClass<*>) {
    /** Filter or assertion criteria for expected type. */
    val expectedType = type.toKontureTypeReference()
    assertions.add { function, violations ->
        if (function.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } != true) {
            violations.add(
                io.github.baole.konture.i18n.getMessage(
                    "function.scope.haveReturnType",
                    "'${type.kontureQualifiedName()}'",
                    function.returnType,
                ),
            )
        }
    }
}

/** Asserts that functions in this assertion scope have the return type specified by [T]. */
public inline fun <reified T : Any> FunctionAssertionScope.haveReturnTypeOf(): Unit = haveReturnType(T::class)

/** Asserts that functions in this assertion scope have the specified [annotation] class. */
public fun FunctionAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) {
    haveAnnotationOf(annotation.kontureQualifiedName())
}

/** Asserts that functions in this assertion scope have the annotation specified by [T]. */
public inline fun <reified T : Annotation> FunctionAssertionScope.haveAnnotationOfType(): Unit =
    haveAnnotationOf(T::class)

/** Filters functions having return type specified by type parameter [T]. */
public inline fun <reified T : Any> FunctionsThat.haveReturnType(): FunctionsRuleBuilder = haveReturnType(T::class)

/** Filters functions that do not have return type specified by type parameter [T]. */
public inline fun <reified T : Any> FunctionsThat.notHaveReturnType(): FunctionsRuleBuilder =
    notHaveReturnType(T::class)

/** Filters functions that do not have a parameter of type specified by type parameter [T]. */
public inline fun <reified T : Any> FunctionsThat.notHaveParameterOf(): FunctionsRuleBuilder =
    notHaveParameterOf(T::class)

/** Filters functions having annotation specified by annotation type parameter [T]. */
public inline fun <reified T : Annotation> FunctionsThat.haveAnnotationOf(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

/** Filters functions having parameters of the specified [types]. */
@JvmName("haveParameterOfKClasses")
public infix fun FunctionsThat.haveParameterOf(types: List<KClass<*>>): FunctionsRuleBuilder =
    haveParameterOf(types.map { it.qualifiedName ?: it.java.name })

/** Filters functions annotated with the specified [annotation] class. */
public infix fun FunctionsThat.areAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation)

/** Asserts that functions are annotated with the specified [annotation] class. */
public infix fun FunctionsShould.beAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

/** Asserts that functions are annotated with the annotation specified by type parameter [T]. */
public inline fun <reified T : Annotation> FunctionsShould.beAnnotatedWith(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

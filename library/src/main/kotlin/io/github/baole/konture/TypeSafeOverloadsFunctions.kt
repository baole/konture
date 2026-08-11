/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

fun FunctionsThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): FunctionsRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

fun FunctionsShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): FunctionsRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

infix fun FunctionsThat.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> FunctionsThat.haveAnnotationOfType(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

fun FunctionsThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun FunctionsThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun FunctionsShould.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> FunctionsShould.haveAnnotationOfType(): FunctionsRuleBuilder =
    haveAnnotationOf(T::class)

fun FunctionsShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun FunctionsShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun FunctionsThat.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FunctionsThat.resideInPackageOf(): FunctionsRuleBuilder = resideInPackageOf(T::class)

infix fun FunctionsShould.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FunctionsShould.resideInPackageOf(): FunctionsRuleBuilder = resideInPackageOf(T::class)

fun FunctionAssertionScope.haveReturnType(type: KClass<*>) {
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

inline fun <reified T : Any> FunctionAssertionScope.haveReturnTypeOf() = haveReturnType(T::class)

fun FunctionAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> FunctionAssertionScope.haveAnnotationOfType() = haveAnnotationOf(T::class)

inline fun <reified T : Any> FunctionsThat.haveReturnType(): FunctionsRuleBuilder = haveReturnType(T::class)

inline fun <reified T : Any> FunctionsThat.notHaveReturnType(): FunctionsRuleBuilder = notHaveReturnType(T::class)

inline fun <reified T : Any> FunctionsThat.notHaveParameterOf(): FunctionsRuleBuilder = notHaveParameterOf(T::class)

inline fun <reified T : Annotation> FunctionsThat.haveAnnotationOf(): FunctionsRuleBuilder = haveAnnotationOf(T::class)

@JvmName("haveParameterOfKClasses")
infix fun FunctionsThat.haveParameterOf(types: List<KClass<*>>): FunctionsRuleBuilder =
    haveParameterOf(types.map { it.qualifiedName ?: it.java.name })

infix fun FunctionsThat.areAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation)

infix fun FunctionsShould.beAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> FunctionsShould.beAnnotatedWith(): FunctionsRuleBuilder = haveAnnotationOf(T::class)

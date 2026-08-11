/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

fun PropertiesThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): PropertiesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

fun PropertiesShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): PropertiesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

infix fun PropertiesThat.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> PropertiesThat.haveAnnotationOfType(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

fun PropertiesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun PropertiesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun PropertiesShould.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> PropertiesShould.haveAnnotationOfType(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

fun PropertiesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun PropertiesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun PropertiesThat.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> PropertiesThat.resideInPackageOf(): PropertiesRuleBuilder = resideInPackageOf(T::class)

infix fun PropertiesShould.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> PropertiesShould.resideInPackageOf(): PropertiesRuleBuilder = resideInPackageOf(T::class)

fun PropertyAssertionScope.haveType(type: KClass<*>) {
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

inline fun <reified T : Any> PropertyAssertionScope.haveTypeOf() = haveType(T::class)

fun PropertyAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> PropertyAssertionScope.haveAnnotationOfType() = haveAnnotationOf(T::class)

inline fun <reified T : Any> PropertiesShould.notCall(): PropertiesRuleBuilder = notCall(T::class)

inline fun <reified T : Any> PropertiesShould.notReferenceClass(): PropertiesRuleBuilder = notReferenceClass(T::class)

inline fun <reified T : Any> PropertiesThat.haveImportOf(): PropertiesRuleBuilder = haveImportOf(T::class)

inline fun <reified T : Any> PropertiesThat.notHaveImportOf(): PropertiesRuleBuilder = notHaveImportOf(T::class)

inline fun <reified T : Any> PropertiesShould.haveImportOf(): PropertiesRuleBuilder = haveImportOf(T::class)

inline fun <reified T : Any> PropertiesShould.notHaveImportOf(): PropertiesRuleBuilder = notHaveImportOf(T::class)

inline fun <reified T : Annotation> PropertiesThat.haveAnnotationOf(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

infix fun PropertiesThat.areAnnotatedWith(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation)

inline fun <reified T : Annotation> PropertiesThat.areAnnotatedWith(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

infix fun PropertiesShould.beAnnotatedWith(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> PropertiesShould.beAnnotatedWith(): PropertiesRuleBuilder =
    haveAnnotationOf(T::class)

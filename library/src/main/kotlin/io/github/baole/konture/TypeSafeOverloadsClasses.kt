/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

infix fun ClassesThat.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> ClassesThat.haveAnnotationOf(): ClassesRuleBuilder = haveAnnotationOf(T::class)

fun ClassesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun ClassesShould.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> ClassesShould.haveAnnotationOf(): ClassesRuleBuilder = haveAnnotationOf(T::class)

fun ClassesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

fun ClassesShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

infix fun ClassesThat.areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
    areAssignableTo(superType.kontureQualifiedName())

inline fun <reified T : Any> ClassesThat.areAssignableTo(): ClassesRuleBuilder = areAssignableTo(T::class)

fun ClassesThat.areAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.areAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

infix fun ClassesShould.beAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
    beAssignableTo(superType.kontureQualifiedName())

inline fun <reified T : Any> ClassesShould.beAssignableTo(): ClassesRuleBuilder = beAssignableTo(T::class)

fun ClassesShould.beAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesShould.beAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

infix fun ClassesThat.areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
    areAssignableFrom(subType.kontureQualifiedName())

inline fun <reified T : Any> ClassesThat.areAssignableFrom(): ClassesRuleBuilder = areAssignableFrom(T::class)

infix fun ClassesShould.beAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
    beAssignableFrom(subType.kontureQualifiedName())

inline fun <reified T : Any> ClassesShould.beAssignableFrom(): ClassesRuleBuilder = beAssignableFrom(T::class)

fun List<ClassDeclaration>.withAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> List<ClassDeclaration>.withAnnotationOf(): List<ClassDeclaration> =
    withAnnotationOf(T::class)

fun List<ClassDeclaration>.withoutAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withoutAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> List<ClassDeclaration>.withoutAnnotationOf(): List<ClassDeclaration> =
    withoutAnnotationOf(T::class)

fun List<ClassDeclaration>.withParentOf(type: KClass<*>): List<ClassDeclaration> =
    withParentOf(type.kontureQualifiedName())

inline fun <reified T : Any> List<ClassDeclaration>.withParentOf(): List<ClassDeclaration> = withParentOf(T::class)

fun List<ClassDeclaration>.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>) =
    assertHaveAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun List<ClassDeclaration>.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(
    first.kontureQualifiedName(),
    *additional.map { it.kontureQualifiedName() }.toTypedArray(),
    allClasses = allClasses,
)

inline fun <reified T : Annotation> List<ClassDeclaration>.assertHaveAnnotationOfType() =
    assertHaveAnnotationOf(T::class)

inline fun <reified T : Any> List<ClassDeclaration>.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

fun KontureScope.withAnnotationOf(annotation: KClass<out Annotation>) =
    KontureScope(classes.withAnnotationOf(annotation))

inline fun <reified T : Annotation> KontureScope.withAnnotationOf() = KontureScope(classes.withAnnotationOf<T>())

fun KontureScope.withoutAnnotationOf(annotation: KClass<out Annotation>) =
    KontureScope(classes.withoutAnnotationOf(annotation))

inline fun <reified T : Annotation> KontureScope.withoutAnnotationOf() = KontureScope(classes.withoutAnnotationOf<T>())

fun KontureScope.withParentOf(type: KClass<*>) = KontureScope(classes.withParentOf(type))

inline fun <reified T : Any> KontureScope.withParentOf() = KontureScope(classes.withParentOf<T>())

fun KontureScope.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>) =
    classes.assertHaveAnnotationOf(*annotations)

fun KontureScope.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = classes.assertAreAssignableTo(first, *additional, allClasses = allClasses)

inline fun <reified T : Annotation> KontureScope.assertHaveAnnotationOfType() = assertHaveAnnotationOf(T::class)

inline fun <reified T : Any> KontureScope.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

infix fun ClassesThat.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

@JvmName("areAssignableToKClasses")
infix fun ClassesThat.areAssignableTo(superTypes: List<KClass<*>>): ClassesRuleBuilder =
    areAssignableTo(superTypes.map { it.qualifiedName ?: it.java.name })

inline fun <reified T : Any> ClassesThat.resideInPackageOf(): ClassesRuleBuilder = resideInPackageOf(T::class)

infix fun ClassesShould.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> ClassesShould.resideInPackageOf(): ClassesRuleBuilder = resideInPackageOf(T::class)

inline fun <reified T : Any> ClassesThat.areNotAssignableTo(): ClassesRuleBuilder = areNotAssignableTo(T::class)

inline fun <reified T : Any> ClassesThat.areNotAssignableFrom(): ClassesRuleBuilder = areNotAssignableFrom(T::class)

infix fun ClassesShould.beAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnnotationOf(annotation)

inline fun <reified T : Annotation> ClassesShould.beAnnotatedWith(): ClassesRuleBuilder = haveAnnotationOf(T::class)

inline fun <reified T : Any> ClassesShould.notDependOnClass(): ClassesRuleBuilder = notReferenceClass(T::class)

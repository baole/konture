/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

/** Filters files residing in the package of the specified [type]. */
public infix fun FilesThat.resideInPackageOf(type: KClass<*>): FilesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

/** Filters files residing in the package of the type parameter [T]. */
public inline fun <reified T : Any> FilesThat.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

/** Asserts that files reside in the package of the specified [type]. */
public infix fun FilesShould.resideInPackageOf(type: KClass<*>): FilesRuleBuilder =
    resideInPackage(type.toKonturePackageReference().packageName)

/** Asserts that files reside in the package of the type parameter [T]. */
public inline fun <reified T : Any> FilesShould.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

/** Filters files that do not contain a class matching type parameter [T]. */
public inline fun <reified T : Any> FilesThat.notContainClass(): FilesRuleBuilder = notContainClass(T::class)

/** Filters files that do not contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesThat.notContainClassesWithAnnotation(): FilesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

/** Filters files that do not have an import of the type parameter [T]. */
public inline fun <reified T : Any> FilesThat.notHaveImportOf(): FilesRuleBuilder = notHaveImportOf(T::class)

/** Filters files annotated with the specified [annotation] class. */
public infix fun FilesThat.areAnnotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    haveAnnotationOf(annotation)

/** Filters files annotated with the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesThat.haveAnnotationOf(): FilesRuleBuilder = haveAnnotationOf(T::class)

/** Filters files annotated with the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesThat.areAnnotatedWith(): FilesRuleBuilder = haveAnnotationOf(T::class)

/** Asserts that files are annotated with the specified [annotation] class. */
public infix fun FilesShould.beAnnotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    haveAnnotationOf(annotation)

/** Asserts that files are annotated with the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesShould.beAnnotatedWith(): FilesRuleBuilder = haveAnnotationOf(T::class)

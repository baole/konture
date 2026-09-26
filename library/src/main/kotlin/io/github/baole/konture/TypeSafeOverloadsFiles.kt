/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package io.github.baole.konture

import kotlin.reflect.KClass

/** Filters files in the package of the specified [type]. */
public infix fun FilesThat.inPackageOf(type: KClass<*>): FilesRuleBuilder =
    inPackage(type.toKonturePackageReference().packageName)

/** Filters files in the package of the type parameter [T]. */
public inline fun <reified T : Any> FilesThat.inPackageOf(): FilesRuleBuilder = inPackageOf(T::class)

/** Asserts that files are in the package of the specified [type]. */
public infix fun FilesShould.inPackageOf(type: KClass<*>): FilesRuleBuilder =
    inPackage(type.toKonturePackageReference().packageName)

/** Asserts that files are in the package of the type parameter [T]. */
public inline fun <reified T : Any> FilesShould.inPackageOf(): FilesRuleBuilder = inPackageOf(T::class)

/** Filters files that do not contain a class matching type parameter [T]. */
public inline fun <reified T : Any> FilesThat.notContainClass(): FilesRuleBuilder = notContainClass(T::class)

/** Filters files that do not contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesThat.notContainClassesWithAnnotation(): FilesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

/** Filters files that do not have an import of the type parameter [T]. */
public inline fun <reified T : Any> FilesThat.notHaveImportOf(): FilesRuleBuilder = notHaveImportOf(T::class)

/** Filters files annotated with the specified [annotation] class. */
public infix fun FilesThat.annotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    containClassesWithAnnotation(annotation)

/** Filters files annotated with the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesThat.annotatedWith(): FilesRuleBuilder =
    containClassesWithAnnotation(T::class)

/** Asserts that files are annotated with the specified [annotation] class. */
public infix fun FilesShould.annotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    containClassesWithAnnotation(annotation)

/** Asserts that files are annotated with the annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesShould.annotatedWith(): FilesRuleBuilder =
    containClassesWithAnnotation(T::class)

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

infix fun FilesThat.resideInPackageOf(type: KClass<*>): FilesRuleBuilder =
    resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FilesThat.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

infix fun FilesShould.resideInPackageOf(type: KClass<*>): FilesRuleBuilder =
    resideInPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FilesShould.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

inline fun <reified T : Any> FilesThat.notContainClass(): FilesRuleBuilder = notContainClass(T::class)

inline fun <reified T : Annotation> FilesThat.notContainClassesWithAnnotation(): FilesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

inline fun <reified T : Any> FilesThat.notHaveImportOf(): FilesRuleBuilder = notHaveImportOf(T::class)

infix fun FilesThat.areAnnotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    haveAnnotationOf(annotation)

inline fun <reified T : Annotation> FilesThat.haveAnnotationOf(): FilesRuleBuilder = haveAnnotationOf(T::class)

inline fun <reified T : Annotation> FilesThat.areAnnotatedWith(): FilesRuleBuilder = haveAnnotationOf(T::class)

infix fun FilesShould.beAnnotatedWith(annotation: KClass<out Annotation>): FilesRuleBuilder =
    haveAnnotationOf(annotation)

inline fun <reified T : Annotation> FilesShould.beAnnotatedWith(): FilesRuleBuilder = haveAnnotationOf(T::class)

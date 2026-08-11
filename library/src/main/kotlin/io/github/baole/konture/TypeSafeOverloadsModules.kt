/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

fun Konture.scopeFromPackageOf(type: KClass<*>) = scopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.scopeFromPackageOf() = scopeFromPackageOf(T::class)

fun Konture.fileScopeFromPackageOf(type: KClass<*>) = fileScopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.fileScopeFromPackageOf() = fileScopeFromPackageOf(T::class)

fun Konture.functionScopeFromPackageOf(type: KClass<*>) =
    functionScopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.functionScopeFromPackageOf() = functionScopeFromPackageOf(T::class)

fun Konture.propertyScopeFromPackageOf(type: KClass<*>) =
    propertyScopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.propertyScopeFromPackageOf() = propertyScopeFromPackageOf(T::class)

inline fun <reified T : Any> SlicesShould.notContainClass(): SlicesRuleBuilder = notContainClass(T::class)

inline fun <reified T : Annotation> SlicesShould.notContainClassesWithAnnotation(): SlicesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

inline fun <reified T : Any> ModulesThat.containClass(): ModulesRuleBuilder = containClass(T::class)

inline fun <reified T : Any> ModulesThat.notContainClass(): ModulesRuleBuilder = notContainClass(T::class)

inline fun <reified T : Annotation> ModulesThat.containClassesWithAnnotation(): ModulesRuleBuilder =
    containClassesWithAnnotation(T::class)

inline fun <reified T : Annotation> ModulesThat.notContainClassesWithAnnotation(): ModulesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

inline fun <reified T : Any> ModulesShould.containClass(): ModulesRuleBuilder = containClass(T::class)

inline fun <reified T : Any> ModulesShould.notContainClass(): ModulesRuleBuilder = notContainClass(T::class)

inline fun <reified T : Annotation> ModulesShould.containClassesWithAnnotation(): ModulesRuleBuilder =
    containClassesWithAnnotation(T::class)

inline fun <reified T : Annotation> ModulesShould.notContainClassesWithAnnotation(): ModulesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

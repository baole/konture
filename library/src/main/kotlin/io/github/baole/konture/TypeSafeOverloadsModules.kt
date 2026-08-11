/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

/** Obtains a scope containing all files and declarations in the package of [type]. */
public fun Konture.scopeFromPackageOf(type: KClass<*>): KontureScope =
    scopeFromPackage(type.toKonturePackageReference().packageName)

/** Obtains a scope containing all files and declarations in the package of type parameter [T]. */
public inline fun <reified T : Any> Konture.scopeFromPackageOf(): KontureScope = scopeFromPackageOf(T::class)

/** Obtains a file scope containing files in the package of [type]. */
public fun Konture.fileScopeFromPackageOf(type: KClass<*>): KontureFileScope =
    fileScopeFromPackage(type.toKonturePackageReference().packageName)

/** Obtains a file scope containing files in the package of type parameter [T]. */
public inline fun <reified T : Any> Konture.fileScopeFromPackageOf(): KontureFileScope =
    fileScopeFromPackageOf(T::class)

/** Obtains a function scope containing functions in the package of [type]. */
public fun Konture.functionScopeFromPackageOf(type: KClass<*>): KontureFunctionScope =
    functionScopeFromPackage(type.toKonturePackageReference().packageName)

/** Obtains a function scope containing functions in the package of type parameter [T]. */
public inline fun <reified T : Any> Konture.functionScopeFromPackageOf(): KontureFunctionScope =
    functionScopeFromPackageOf(T::class)

/** Obtains a property scope containing properties in the package of [type]. */
public fun Konture.propertyScopeFromPackageOf(type: KClass<*>): KonturePropertyScope =
    propertyScopeFromPackage(type.toKonturePackageReference().packageName)

/** Obtains a property scope containing properties in the package of type parameter [T]. */
public inline fun <reified T : Any> Konture.propertyScopeFromPackageOf(): KonturePropertyScope =
    propertyScopeFromPackageOf(T::class)

/** Asserts that slices do not contain the class specified by type parameter [T]. */
public inline fun <reified T : Any> SlicesShould.notContainClass(): SlicesRuleBuilder = notContainClass(T::class)

/** Asserts that slices do not contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> SlicesShould.notContainClassesWithAnnotation(): SlicesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

/** Filters modules containing the class specified by type parameter [T]. */
public inline fun <reified T : Any> ModulesThat.containClass(): ModulesRuleBuilder = containClass(T::class)

/** Filters modules that do not contain the class specified by type parameter [T]. */
public inline fun <reified T : Any> ModulesThat.notContainClass(): ModulesRuleBuilder = notContainClass(T::class)

/** Filters modules containing classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> ModulesThat.containClassesWithAnnotation(): ModulesRuleBuilder =
    containClassesWithAnnotation(T::class)

/** Filters modules that do not contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> ModulesThat.notContainClassesWithAnnotation(): ModulesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

/** Asserts that modules contain the class specified by type parameter [T]. */
public inline fun <reified T : Any> ModulesShould.containClass(): ModulesRuleBuilder = containClass(T::class)

/** Asserts that modules do not contain the class specified by type parameter [T]. */
public inline fun <reified T : Any> ModulesShould.notContainClass(): ModulesRuleBuilder = notContainClass(T::class)

/** Asserts that modules contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> ModulesShould.containClassesWithAnnotation(): ModulesRuleBuilder =
    containClassesWithAnnotation(T::class)

/** Asserts that modules do not contain classes annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> ModulesShould.notContainClassesWithAnnotation(): ModulesRuleBuilder =
    notContainClassesWithAnnotation(T::class)

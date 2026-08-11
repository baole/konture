/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Fluent API for filtering Kotlin functions in architecture rules.
 */
@KontureDsl
public class FunctionsThat internal constructor(
    /** Filter or assertion criteria for builder. */
    public override val builder: FunctionsRuleBuilder,
) : FunctionsThatPackageFilter,
    FunctionsThatNameFilter,
    FunctionsThatStructureFilter,
    FunctionsThatModifierFilter,
    FunctionsThatCompositeFilter {
    /** Filters functions having parameter of type parameter [T]. */
    public inline fun <reified T : Any> haveParameterOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveParameterOf(T::class)

    /** Filters functions having return type of type parameter [T]. */
    public inline fun <reified T : Any> haveReturnTypeOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveReturnType(T::class)

    /** Filters functions having extension receiver of type parameter [T]. */
    public inline fun <reified T : Any> haveExtensionReceiver(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveExtensionReceiver(T::class)

    /** Filters functions having any parameter type of type parameter [T]. */
    public inline fun <reified T : Any> haveAnyParameterTypeOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveAnyParameterType(T::class)

    /** Filters functions annotated with annotation type parameter [T]. */
    public inline fun <reified T : Annotation> areAnnotatedWith(): FunctionsRuleBuilder =
        (this as FunctionsThatModifierFilter).areAnnotatedWith(T::class)

    /** Filters functions having annotation of type parameter [T]. */
    public inline fun <reified T : Annotation> haveAnnotationOf(): FunctionsRuleBuilder =
        (this as FunctionsThatModifierFilter).haveAnnotationOf(T::class)
}

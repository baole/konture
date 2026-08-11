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
class FunctionsThat internal constructor(
    override val builder: FunctionsRuleBuilder,
) : FunctionsThatPackageFilter,
    FunctionsThatNameFilter,
    FunctionsThatStructureFilter,
    FunctionsThatModifierFilter,
    FunctionsThatCompositeFilter {
    inline fun <reified T : Any> haveParameterOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveParameterOf(T::class)

    inline fun <reified T : Any> haveReturnTypeOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveReturnType(T::class)

    inline fun <reified T : Any> haveExtensionReceiver(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveExtensionReceiver(T::class)

    inline fun <reified T : Any> haveAnyParameterTypeOf(): FunctionsRuleBuilder =
        (this as FunctionsThatStructureFilter).haveAnyParameterType(T::class)

    inline fun <reified T : Annotation> areAnnotatedWith(): FunctionsRuleBuilder =
        (this as FunctionsThatModifierFilter).areAnnotatedWith(T::class)

    inline fun <reified T : Annotation> haveAnnotationOf(): FunctionsRuleBuilder =
        (this as FunctionsThatModifierFilter).haveAnnotationOf(T::class)
}

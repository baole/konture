/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Assertion builder for checking rule expectations on Kotlin property declarations. */
@KontureDsl
public class PropertiesShould internal constructor(
    /** Filter or assertion criteria for builder. */
    public override val builder: PropertiesRuleBuilder,
) : PropertiesShouldModifierAssertions,
    PropertiesShouldTypeAssertions,
    PropertiesShouldDependencyAssertions,
    PropertiesShouldCompositeAssertions

/** Asserts that properties have type specified by type parameter [T]. */
public inline fun <reified T : Any> PropertiesShould.haveTypeOf(): PropertiesRuleBuilder = haveType(T::class)

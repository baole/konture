/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Fluent API for defining assertion rules on Gradle modules.
 */
@KontureDsl
public class ModulesShould internal constructor(
    /** Filter or assertion criteria for builder. */
    public override val builder: ModulesRuleBuilder,
) : ModulesShouldDependencyAssertions,
    ModulesShouldStructureAssertions,
    ModulesShouldCompositeAssertions

/** Asserts that modules do not call members of type parameter [T]. */
public inline fun <reified T : Any> ModulesShould.notCall(): ModulesRuleBuilder = notCall(T::class)

/** Asserts that modules do not reference class type parameter [T]. */
public inline fun <reified T : Any> ModulesShould.notReferenceClass(): ModulesRuleBuilder = notReferenceClass(T::class)

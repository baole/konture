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
class ModulesShould internal constructor(
    override val builder: ModulesRuleBuilder,
) : ModulesShouldDependencyAssertions,
    ModulesShouldStructureAssertions,
    ModulesShouldCompositeAssertions

inline fun <reified T : Any> ModulesShould.notCall(): ModulesRuleBuilder = notCall(T::class)

inline fun <reified T : Any> ModulesShould.notReferenceClass(): ModulesRuleBuilder = notReferenceClass(T::class)

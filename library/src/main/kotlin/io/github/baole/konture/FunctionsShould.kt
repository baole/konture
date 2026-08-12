/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Assertion builder for checking rule expectations on Kotlin function declarations. */
@KontureDsl
public class FunctionsShould internal constructor(
    /** Filter or assertion criteria for builder. */
    public override val builder: FunctionsRuleBuilder,
) : FunctionsShouldCallAssertions,
    FunctionsShouldNameAssertions,
    FunctionsShouldModifierAssertions,
    FunctionsShouldSignatureAssertions,
    FunctionsShouldCompositeAssertions

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Common scope interface providing access to [FunctionsRuleBuilder].
 */
public interface FunctionsThatScope {
    /** The underlying [FunctionsRuleBuilder] used to accumulate function filtering rules. */
    public val builder: FunctionsRuleBuilder
}

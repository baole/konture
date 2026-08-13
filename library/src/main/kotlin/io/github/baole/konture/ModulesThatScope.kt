/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Common scope interface providing access to [ModulesRuleBuilder].
 */
@KontureDsl
public interface ModulesThatScope {
    /** The underlying [ModulesRuleBuilder] used to accumulate module filtering rules. */
    public val builder: ModulesRuleBuilder
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Common scope interface providing access to [FilesRuleBuilder].
 */
@KontureDsl
public interface FilesThatScope {
    /** The underlying [FilesRuleBuilder] used to accumulate file filtering rules. */
    public val builder: FilesRuleBuilder
}

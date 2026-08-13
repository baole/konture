/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a location in source code associated with a rule violation or subject.
 *
 * @property filePath Build-root relative path to the file.
 * @property line Optional 1-based line number.
 * @property column Optional 1-based column number.
 */
@Serializable
public data class SourceLocation(
    val filePath: String,
    val line: Int? = null,
    val column: Int? = null,
)

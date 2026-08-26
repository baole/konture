/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.impl.StructuredMessageList

internal fun MutableList<String>.addViolationMessage(
    message: String,
    sourceLocation: SourceLocation? = null,
) {
    if (this is StructuredMessageList && sourceLocation != null) {
        messageSourceLocationMap[size] = sourceLocation
    }
    add(message)
}

internal fun toSourceLocation(
    filePath: String,
    line: Int,
    column: Int,
): SourceLocation =
    SourceLocation(
        filePath = filePath,
        line = line.takeIf { it > 0 },
        column = column.takeIf { it > 0 },
    )

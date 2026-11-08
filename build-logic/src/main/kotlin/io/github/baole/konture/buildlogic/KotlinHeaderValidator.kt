/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.buildlogic

import com.diffplug.spotless.FormatterFunc
import java.io.Serializable

internal class KotlinHeaderValidator : FormatterFunc, Serializable {
    override fun apply(input: String): String {
        require(HEADER_REGEX.containsMatchIn(input)) {
            "Kotlin files must start with a copyright and SPDX header."
        }
        return input
    }

    private companion object {
        val HEADER_REGEX =
            Regex(
                """\A/\*\R \* Copyright \d{4}(?:-\d{4})? .+\R(?: \* Contributors: .+\R)? \* SPDX-License-Identifier: Apache-2\.0\R \*/\R\R""",
            )
    }
}

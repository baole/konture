/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

/** Normalizes Kotlin type text before it is matched against source declarations. */
internal fun String.canonicalizeEscapedKotlinIdentifiers(): String = replace(ESCAPED_IDENTIFIER, "\$1")

private val ESCAPED_IDENTIFIER = Regex("""`([^`]+)`""")

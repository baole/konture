/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.utils

fun violationsFound(rule: () -> Unit): AssertionError? =
    try {
        rule()
        null
    } catch (e: AssertionError) {
        e
    }

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

/** Log verbosity levels for Konture execution logging. */
public enum class LogLevel {
    /** Fine-grained diagnostic trace messages. */
    TRACE,

    /** Detailed debugging messages. */
    DEBUG,

    /** General informational messages. */
    INFO,

    /** Warning messages indicating potential non-fatal issues. */
    WARNING,

    /** Error messages indicating failure states. */
    ERROR,
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KontureLoggerTest {
    @Test
    fun testKontureLoggerCustomHandler() {
        val logged = mutableListOf<Triple<LogLevel, String, Throwable?>>()
        val originalMinLevel = KontureLogger.minLevel
        val originalLogger = KontureLogger.logger

        try {
            KontureLogger.minLevel = LogLevel.DEBUG
            KontureLogger.logger = { level, message, throwable ->
                logged.add(Triple(level, message, throwable))
            }

            KontureLogger.log(LogLevel.INFO, "Info message")
            KontureLogger.log(LogLevel.WARNING, "Warning message", RuntimeException("test ex"))

            assertEquals(2, logged.size)
            assertEquals(LogLevel.INFO, logged[0].first)
            assertEquals("Info message", logged[0].second)

            assertEquals(LogLevel.WARNING, logged[1].first)
            assertEquals("Warning message", logged[1].second)
            assertTrue(logged[1].third is RuntimeException)
        } finally {
            KontureLogger.minLevel = originalMinLevel
            KontureLogger.logger = originalLogger
        }
    }

    @Test
    fun testDefaultLoggerExecution() {
        val originalMinLevel = KontureLogger.minLevel
        try {
            KontureLogger.minLevel = LogLevel.INFO
            // Test default output branch execution
            KontureLogger.log(LogLevel.WARNING, "Test default logger warning", IllegalArgumentException("test"))
            KontureLogger.log(LogLevel.ERROR, "Test default logger error")
            KontureLogger.log(LogLevel.INFO, "Test default logger info")
        } finally {
            KontureLogger.minLevel = originalMinLevel
        }
    }
}

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KontureLoggingTest : RuleBuildersTestBase() {
    private val loggedMessages = mutableListOf<Pair<LogLevel, String>>()
    private val originalLogger = KontureLogger.logger
    private val originalMinLevel = KontureLogger.minLevel

    @BeforeEach
    fun setUpLogging() {
        loggedMessages.clear()
        KontureLogger.minLevel = LogLevel.TRACE
        KontureLogger.logger = { level, message, _ ->
            loggedMessages.add(level to message)
        }
    }

    @AfterEach
    fun tearDownLogging() {
        KontureLogger.logger = originalLogger
        KontureLogger.minLevel = originalMinLevel
    }

    @Test
    fun `test ClassesRuleBuilder logs debug info and warns on empty matches`() {
        // Test with match (all classes match)
        ClassesRuleBuilder(projectGraph)
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG &&
                    msg.contains("Checking Classes Rules: found 3 classes total. Selected 3 classes to verify.")
            },
        )

        loggedMessages.clear()

        // Test with 0 matches
        ClassesRuleBuilder(projectGraph)
            .allowEmpty()
            .that()
            .haveNameStartingWith("NonExistentClass")
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG &&
                    msg.contains("Checking Classes Rules: found 3 classes total. Selected 0 classes to verify.")
            },
        )
        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("No classes matched the filter 'that()'")
            },
        )
    }

    @Test
    fun `test FilesRuleBuilder logs debug info and warns on empty matches`() {
        // Test with match
        FilesRuleBuilder(projectGraph)
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG && msg.contains("Checking Files Rules:") && msg.contains("files total")
            },
        )

        loggedMessages.clear()

        // Test with 0 matches
        FilesRuleBuilder(projectGraph)
            .allowEmpty()
            .that()
            .haveNameStartingWith("NonExistentFile")
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("No files matched the filter 'that()'")
            },
        )
    }

    @Test
    fun `test FunctionsRuleBuilder logs debug info and warns on empty matches`() {
        // Test with match
        FunctionsRuleBuilder(projectGraph)
            .allowEmpty()
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG && msg.contains("Checking Functions Rules:") && msg.contains("functions total")
            },
        )

        loggedMessages.clear()

        // Test with 0 matches
        FunctionsRuleBuilder(projectGraph)
            .allowEmpty()
            .that()
            .haveNameStartingWith("NonExistentFunction")
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("No functions matched the filter 'that()'")
            },
        )
    }

    @Test
    fun `test PropertiesRuleBuilder logs debug info and warns on empty matches`() {
        // Test with match
        PropertiesRuleBuilder(projectGraph)
            .allowEmpty()
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG && msg.contains("Checking Properties Rules:") &&
                    msg.contains("properties total")
            },
        )

        loggedMessages.clear()

        // Test with 0 matches
        PropertiesRuleBuilder(projectGraph)
            .allowEmpty()
            .that()
            .haveNameStartingWith("NonExistentProperty")
            .should()
            .satisfy { _, _ -> }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("No properties matched the filter 'that()'")
            },
        )
    }

    @Test
    fun `test ModulesRuleBuilder logs debug info and warns on empty matches`() {
        // Test with match
        ModulesRuleBuilder(projectGraph)
            .should()
            .satisfy { true }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.DEBUG && msg.contains("Checking Modules Rules:") && msg.contains("modules total")
            },
        )

        loggedMessages.clear()

        // Test with 0 matches
        ModulesRuleBuilder(projectGraph)
            .allowEmpty()
            .that()
            .haveNamePath(":nonExistentModule")
            .should()
            .satisfy { true }
            .check()

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("No modules matched the filter 'that()'")
            },
        )
    }
}

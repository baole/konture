/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PatternMatchersTest {
    @Test
    fun testModuleGlobMatching() {
        assertTrue(PatternMatchers.matchesModuleGlob(":feature:*", ":feature:profile"))
        assertFalse(PatternMatchers.matchesModuleGlob(":feature:*", ":feature:profile:detail"))

        assertTrue(PatternMatchers.matchesModuleGlob(":feature:**", ":feature:profile:detail"))
        assertTrue(PatternMatchers.matchesModuleGlob(":*-api", ":network-api"))
        assertTrue(PatternMatchers.matchesModuleGlob(":*-api", ":auth-api"))
        assertFalse(PatternMatchers.matchesModuleGlob(":*-api", ":network-api-impl"))

        // Special characters escaping test
        assertTrue(PatternMatchers.matchesModuleGlob(":feature+app.test", ":feature+app.test"))
        assertFalse(PatternMatchers.matchesModuleGlob(":feature+app.test", ":feature-app.test"))
    }

    @Test
    fun `haveNameMatching with double-star should not match the root project`() {
        // ":**" is the common pattern for "all modules". The root project path is just ":" and
        // should never be selected by this pattern, since it has no path segment after the colon.
        assertFalse(PatternMatchers.matchesModuleGlob(":**", ":"))

        // Real submodule paths must still match.
        assertTrue(PatternMatchers.matchesModuleGlob(":**", ":app"))
        assertTrue(PatternMatchers.matchesModuleGlob(":**", ":core"))
        assertTrue(PatternMatchers.matchesModuleGlob(":**", ":feature:login"))
        assertTrue(PatternMatchers.matchesModuleGlob(":**", ":a:b:c"))

        // Nested wildcard — root must not match, but nested paths must.
        assertFalse(PatternMatchers.matchesModuleGlob(":feature:**", ":feature:"))
        assertTrue(PatternMatchers.matchesModuleGlob(":feature:**", ":feature:profile"))
        assertTrue(PatternMatchers.matchesModuleGlob(":feature:**", ":feature:profile:detail"))
    }

    @Test
    fun testPackagePatternMatching() {
        // Double dot matches anywhere
        assertTrue(PatternMatchers.matchesPackage("..domain..", "domain"))
        assertTrue(PatternMatchers.matchesPackage("..domain..", "com.acme.domain"))
        assertTrue(PatternMatchers.matchesPackage("..domain..", "com.acme.domain.usecase"))
        assertFalse(PatternMatchers.matchesPackage("..domain..", "com.acme.domaineer"))

        // Prefix match
        assertTrue(PatternMatchers.matchesPackage("com.acme.domain..", "com.acme.domain"))
        assertTrue(PatternMatchers.matchesPackage("com.acme.domain..", "com.acme.domain.usecase"))
        assertFalse(PatternMatchers.matchesPackage("com.acme.domain..", "com.acme.data"))

        // Suffix match
        assertTrue(PatternMatchers.matchesPackage("..domain.usecase", "domain.usecase"))
        assertTrue(PatternMatchers.matchesPackage("..domain.usecase", "com.acme.domain.usecase"))
        assertFalse(PatternMatchers.matchesPackage("..domain.usecase", "com.acme.domain.usecase.impl"))

        // Middle match
        assertTrue(PatternMatchers.matchesPackage("com..usecase", "com.usecase"))
        assertTrue(PatternMatchers.matchesPackage("com..usecase", "com.acme.domain.usecase"))
        assertFalse(PatternMatchers.matchesPackage("com..usecase", "com.acme.domain.usecase.impl"))

        // Multiple consecutive wildcards or edge cases
        assertTrue(PatternMatchers.matchesPackage("..", "com.acme.domain"))
        assertTrue(PatternMatchers.matchesPackage("com..domain..usecase", "com.acme.domain.usecase"))
        assertTrue(PatternMatchers.matchesPackage("com..domain..usecase", "com.domain.usecase"))
    }

    @Test
    fun testSimpleGlobMatching() {
        assertTrue(PatternMatchers.matchesSimpleGlob("*UseCase", "GetUserUseCase"))
        assertTrue(PatternMatchers.matchesSimpleGlob("Get*", "GetUserUseCase"))
        assertTrue(PatternMatchers.matchesSimpleGlob("*User*", "GetUserUseCase"))
        assertTrue(PatternMatchers.matchesSimpleGlob("GetUserUseCase", "GetUserUseCase"))
        assertFalse(PatternMatchers.matchesSimpleGlob("*UseCase", "GetUserUseCaseImpl"))
        assertTrue(PatternMatchers.matchesSimpleGlob("*", "Anything"))
    }

    @Test
    fun testIsCallUsageMatchPackageMismatch() {
        val usage =
            io.github.baole.konture.SourceUsage(
                kind = io.github.baole.konture.UsageKind.CALL,
                targetFqName = "io.github.baole.konture.tests.BannedApi.legacyLog",
                rawExpression = "BannedApi.legacyLog",
                possibleTargetFqNames =
                    listOf(
                        "legacyLog",
                        "BannedApi.legacyLog",
                        "io.github.baole.konture.tests.BannedApi.legacyLog",
                    ),
                filePath = "/src/BannedApi.kt",
                line = 10,
                column = 5,
            )

        // Matching package should return true
        assertTrue(PatternMatchers.isCallUsageMatch(usage, "io.github.baole.konture.tests.BannedApi.legacyLog"))

        // Mismatched package should return false
        assertFalse(PatternMatchers.isCallUsageMatch(usage, "io.github.baole.konture.testsx.BannedApi.legacyLog"))
    }

    @Test
    fun testIsCallUsageMatchDoesNotTreatBareCalleeAsAnyQualifiedMethod() {
        val usage =
            io.github.baole.konture.SourceUsage(
                kind = io.github.baole.konture.UsageKind.CALL,
                targetFqName = "android.content.Context.getString",
                rawExpression = "context.getString",
                possibleTargetFqNames =
                    listOf(
                        "getString",
                        "context.getString",
                        "android.content.Context.getString",
                    ),
                filePath = "/src/Helper.kt",
                line = 26,
                column = 21,
                unresolvedPossibleUsage = true,
            )

        assertTrue(PatternMatchers.isCallUsageMatch(usage, "android.content.Context.getString"))
        assertFalse(PatternMatchers.isCallUsageMatch(usage, "org.jetbrains.compose.resources.getString"))
    }
}

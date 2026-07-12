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
}

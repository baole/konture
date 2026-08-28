/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class ModulesDependencyPolicyTest : RuleBuildersTestBase() {
    @Test
    fun `test mustNotDependOn and notDependOn overloads`() {
        val depB = Dependency("implementation", ":", ":feature:login")
        val depC = Dependency("api", ":", ":feature:profile")
        val modA = moduleA.copy(path = ":app", dependencies = listOf(depB, depC))
        val graph = ProjectGraph(mapOf(":" to listOf(modA, moduleB, moduleC)))

        // Single string / infix
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotDependOn(":feature:login")
            .getShouldAssertion()!!(modA, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("must not depend on modules matching [:feature:login]"))

        // Vararg with glob
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotDependOn(":feature:**", ":other")
            .getShouldAssertion()!!(modA, graph, v2)
        assertEquals(1, v2.size)

        // List
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotDependOn(listOf(":feature:profile"))
            .getShouldAssertion()!!(modA, graph, v3)
        assertEquals(1, v3.size)

        // Predicate with description
        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotDependOn("profile modules") { it.contains("profile") }
            .getShouldAssertion()!!(modA, graph, v4)
        assertEquals(1, v4.size)
        assertTrue(v4[0].contains("profile modules"))

        // Predicate without description
        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotDependOn { it.contains("login") }
            .getShouldAssertion()!!(modA, graph, v5)
        assertEquals(1, v5.size)

        // Shorthand notDependOn alias
        val v6 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOn(":feature:login")
            .getShouldAssertion()!!(modA, graph, v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOn(":feature:**", ":other")
            .getShouldAssertion()!!(modA, graph, v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOn(listOf(":feature:profile"))
            .getShouldAssertion()!!(modA, graph, v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOn("profile modules") { it.contains("profile") }
            .getShouldAssertion()!!(modA, graph, v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOn { it.contains("login") }
            .getShouldAssertion()!!(modA, graph, v10)
        assertEquals(1, v10.size)
    }

    @Test
    fun `test mayDependOn allow-list overloads and test-config skipping`() {
        val prodDep = Dependency("implementation", ":", ":core:network")
        val unallowedDep = Dependency("api", ":", ":feature:payment")
        val testDep = Dependency("testImplementation", ":", ":core:testing")
        val modA = moduleA.copy(path = ":feature:checkout", dependencies = listOf(prodDep, unallowedDep, testDep))
        val graph = ProjectGraph(mapOf(":" to listOf(modA)))

        // Single string
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayDependOn(":core:**")
            .getShouldAssertion()!!(modA, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("may only depend on modules matching [:core:**]"))
        assertTrue(v1[0].contains(":feature:payment"))

        // Vararg allowing all
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayDependOn(":core:**", ":feature:payment")
            .getShouldAssertion()!!(modA, graph, v2)
        assertTrue(v2.isEmpty())

        // List
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayDependOn(listOf(":core:network", ":feature:payment"))
            .getShouldAssertion()!!(modA, graph, v3)
        assertTrue(v3.isEmpty())

        // Predicate with description
        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayDependOn("core or payment") {
            it.startsWith(":core:") || it == ":feature:payment"
        }
            .getShouldAssertion()!!(modA, graph, v4)
        assertTrue(v4.isEmpty())

        // Predicate failing
        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayDependOn { it.startsWith(":core:") }
            .getShouldAssertion()!!(modA, graph, v5)
        assertEquals(1, v5.size)
    }

    @Test
    fun `test onlyDependOn strict allow-list overloads`() {
        val prodDep = Dependency("implementation", ":", ":core:model")
        val unallowedDep = Dependency("api", ":", ":feature:analytics")
        val modA = moduleA.copy(path = ":feature:cart", dependencies = listOf(prodDep, unallowedDep))
        val graph = ProjectGraph(mapOf(":" to listOf(modA)))

        // Single string
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOn(":core:**")
            .getShouldAssertion()!!(modA, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("should only depend on [:core:**]"))

        // Vararg
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOn(":core:**", ":feature:analytics")
            .getShouldAssertion()!!(modA, graph, v2)
        assertTrue(v2.isEmpty())

        // List
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOn(listOf(":core:model", ":feature:analytics"))
            .getShouldAssertion()!!(modA, graph, v3)
        assertTrue(v3.isEmpty())

        // Predicate
        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOn("core only") { it.startsWith(":core:") }
            .getShouldAssertion()!!(modA, graph, v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOn { it.startsWith(":core:") }
            .getShouldAssertion()!!(modA, graph, v5)
        assertEquals(1, v5.size)
    }

    @Test
    fun `test mayBeDependedOnBy inbound allow-list`() {
        val prodDep = Dependency("implementation", ":", ":core:security")
        val modApp = moduleA.copy(path = ":app", dependencies = listOf(prodDep))
        val modFeature = moduleB.copy(path = ":feature:settings", dependencies = listOf(prodDep))
        val modCore = moduleC.copy(path = ":core:security", dependencies = emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(modApp, modFeature, modCore)))

        // Single string
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayBeDependedOnBy(":app")
            .getShouldAssertion()!!(modCore, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("may only be depended on by modules matching [:app]"))

        // Vararg
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayBeDependedOnBy(":app", ":feature:**")
            .getShouldAssertion()!!(modCore, graph, v2)
        assertTrue(v2.isEmpty())

        // List
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayBeDependedOnBy(listOf(":app", ":feature:settings"))
            .getShouldAssertion()!!(modCore, graph, v3)
        assertTrue(v3.isEmpty())

        // Predicate
        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayBeDependedOnBy("app only") { it == ":app" }
            .getShouldAssertion()!!(modCore, graph, v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mayBeDependedOnBy { it == ":app" }
            .getShouldAssertion()!!(modCore, graph, v5)
        assertEquals(1, v5.size)

        // Test configuration edges are ignored for inbound allow-list
        val testDep = Dependency("testImplementation", ":", ":core:security")
        val modTestOnly = moduleA.copy(path = ":unlisted:testOnly", dependencies = listOf(testDep))
        val graphWithTest = ProjectGraph(mapOf(":" to listOf(modTestOnly, modCore)))

        val v6 = mutableListOf<String>()
        ModulesRuleBuilder(graphWithTest).should().mayBeDependedOnBy(":app")
            .getShouldAssertion()!!(modCore, graphWithTest, v6)
        assertTrue(v6.isEmpty(), "Test-only dependency should not trigger mayBeDependedOnBy violation")
    }

    @Test
    fun `test mustNotBeDependedOnBy inbound deny-list`() {
        val prodDep = Dependency("implementation", ":", ":internal:auth")
        val testDep = Dependency("testImplementation", ":", ":internal:auth")
        val modFeature = moduleA.copy(path = ":feature:home", dependencies = listOf(prodDep))
        val modLegacy = moduleB.copy(path = ":legacy:old", dependencies = listOf(testDep))
        val modAuth = moduleC.copy(path = ":internal:auth", dependencies = emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(modFeature, modLegacy, modAuth)))

        // Single string
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy(":feature:home")
            .getShouldAssertion()!!(modAuth, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("must not be depended on by modules matching [:feature:home]"))

        // Vararg matching both prod and test dependencies
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy(":feature:**", ":legacy:**")
            .getShouldAssertion()!!(modAuth, graph, v2)
        assertEquals(1, v2.size)

        // List
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy(listOf(":legacy:old"))
            .getShouldAssertion()!!(modAuth, graph, v3)
        assertEquals(1, v3.size)

        // Predicate
        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy("legacy modules") { it.startsWith(":legacy:") }
            .getShouldAssertion()!!(modAuth, graph, v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy { it.startsWith(":feature:") }
            .getShouldAssertion()!!(modAuth, graph, v5)
        assertEquals(1, v5.size)

        // Passing case
        val v6 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy(":forbidden:**")
            .getShouldAssertion()!!(modAuth, graph, v6)
        assertTrue(v6.isEmpty())
    }

    @Test
    fun `test i18n messages across multiple locales`() {
        val dep = Dependency("implementation", ":", ":feature:bad")
        val mod = moduleA.copy(path = ":app", dependencies = listOf(dep))
        val graph = ProjectGraph(mapOf(":" to listOf(mod)))

        val originalLocale = Konture.locale
        try {
            // Spanish
            Konture.locale = Locale.forLanguageTag("es")
            val vEs = mutableListOf<String>()
            ModulesRuleBuilder(graph).should().mustNotDependOn(":feature:bad")
                .getShouldAssertion()!!(mod, graph, vEs)
            assertEquals(1, vEs.size)
            assertTrue(vEs[0].contains("no debe depender de los módulos"))

            // French
            Konture.locale = Locale.forLanguageTag("fr")
            val vFr = mutableListOf<String>()
            ModulesRuleBuilder(graph).should().mustNotDependOn(":feature:bad")
                .getShouldAssertion()!!(mod, graph, vFr)
            // Vietnamese
            Konture.locale = Locale.forLanguageTag("vi")
            val vVi = mutableListOf<String>()
            ModulesRuleBuilder(graph).should().mustNotDependOn(":feature:bad")
                .getShouldAssertion()!!(mod, graph, vVi)
            assertEquals(1, vVi.size)
            assertTrue(vVi[0].contains("không được phụ thuộc vào các mô-đun"))
        } finally {
            Konture.locale = originalLocale
        }
    }

    @Test
    fun `test transitive dependency assertions and dependencyPath population`() {
        val depB = Dependency("implementation", ":", ":feature:login")
        val depC = Dependency("implementation", ":", ":core:network")
        val depD = Dependency("implementation", ":", ":core:database")

        val modApp = moduleA.copy(path = ":app", dependencies = listOf(depB))
        val modFeature = moduleB.copy(path = ":feature:login", dependencies = listOf(depC))
        val modNetwork = moduleC.copy(path = ":core:network", dependencies = listOf(depD))
        val modDb = moduleC.copy(path = ":core:database", dependencies = emptyList())

        val graph = ProjectGraph(mapOf(":" to listOf(modApp, modFeature, modNetwork, modDb)))

        // notDependOnModuleTransitively with StructuredMessageList
        val list = io.github.baole.konture.impl.StructuredMessageList()
        ModulesRuleBuilder(graph).should().notDependOnModuleTransitively(":core:database")
            .getShouldAssertion()!!(modApp, graph, list)

        assertEquals(1, list.size)
        val target = list.messageTargetMap[0]
        val path = list.messageDependencyPathMap[0]

        assertEquals(io.github.baole.konture.core.model.Subject.ModuleSubject(":core:database"), target)
        assertEquals(
            listOf(
                io.github.baole.konture.core.model.Subject.ModuleSubject(":app"),
                io.github.baole.konture.core.model.Subject.ModuleSubject(":feature:login"),
                io.github.baole.konture.core.model.Subject.ModuleSubject(":core:network"),
                io.github.baole.konture.core.model.Subject.ModuleSubject(":core:database"),
            ),
            path,
        )
    }

    @Test
    fun `test inbound dependency assertions target and dependencyPath population`() {
        val prodDep = Dependency("implementation", ":", ":core:security")
        val modApp = moduleA.copy(path = ":app", dependencies = listOf(prodDep))
        val modCore = moduleC.copy(path = ":core:security", dependencies = emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(modApp, modCore)))

        val list = io.github.baole.konture.impl.StructuredMessageList()
        ModulesRuleBuilder(graph).should().mustNotBeDependedOnBy(":app")
            .getShouldAssertion()!!(modCore, graph, list)

        assertEquals(1, list.size)
        val target = list.messageTargetMap[0]
        val path = list.messageDependencyPathMap[0]

        // Target should be the offending caller (:app), not the subject (:core:security)
        assertEquals(io.github.baole.konture.core.model.Subject.ModuleSubject(":app"), target)
        assertEquals(
            listOf(
                io.github.baole.konture.core.model.Subject.ModuleSubject(":app"),
                io.github.baole.konture.core.model.Subject.ModuleSubject(":core:security"),
            ),
            path,
        )
    }
}

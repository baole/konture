/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KontureExtensionTest {
    @Test
    fun `default extension values are correctly initialized`() {
        val project = ProjectBuilder.builder().build()
        val extension = KontureExtension(project)

        assertEquals(emptyList<String>(), extension.excludeModules.get())
        assertEquals(emptyList<String>(), extension.excludePackages.get())
        assertEquals(emptyList<String>(), extension.excludeClasses.get())
        assertEquals(listOf("test*", "benchmark*", "testedapks"), extension.excludeConfigurations.get())
        assertEquals("INFO", extension.logLevel.get())
        assertEquals("konture-baseline.json", extension.baselinePath.get())
        assertEquals("en", extension.language.get())
    }

    @Test
    fun `extension dsl methods configure properties correctly`() {
        val project = ProjectBuilder.builder().build()
        val extension = KontureExtension(project)

        extension.logLevel("DEBUG")
        assertEquals("DEBUG", extension.logLevel.get())

        extension.baselinePath("custom-baseline.json")
        assertEquals("custom-baseline.json", extension.baselinePath.get())

        extension.language("de")
        assertEquals("de", extension.language.get())

        extension.excludeConfigurations("customConfig1", "customConfig2")
        assertEquals(
            listOf("customConfig1", "customConfig2"),
            extension.excludeConfigurations.get(),
        )
    }

    @Test
    fun `vararg exclusion methods accumulate values`() {
        val project = ProjectBuilder.builder().build()
        val extension = KontureExtension(project)

        extension.excludeModules(":module-a")
        extension.excludeModules(":module-b", ":module-c")
        assertEquals(listOf(":module-a", ":module-b", ":module-c"), extension.excludeModules.get())

        extension.excludePackages("com.example.a")
        extension.excludePackages("com.example.b", "com.example.c")
        assertEquals(listOf("com.example.a", "com.example.b", "com.example.c"), extension.excludePackages.get())

        extension.excludeClasses("ClassA")
        extension.excludeClasses("ClassB", "ClassC")
        assertEquals(listOf("ClassA", "ClassB", "ClassC"), extension.excludeClasses.get())
    }
}

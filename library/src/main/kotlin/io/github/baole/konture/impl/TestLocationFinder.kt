/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

internal object TestLocationFinder {
    private const val FALLBACK_PACKAGE = "io.github.baole.konture.impl"

    private val ignoredClasses =
        setOf(
            "java.lang.Thread",
            "java.lang.Throwable",
            "io.github.baole.konture.impl.TestLocationFinder",
            "io.github.baole.konture.impl.BaselineManager",
            "io.github.baole.konture.impl.BaselineNormalizer",
            "io.github.baole.konture.impl.BaselineSerializer",
        )

    private val frameworkPackages =
        listOf(
            "org.junit.",
            "junit.",
            "org.testng.",
            "org.gradle.",
            "org.apache.maven.",
            "sun.reflect.",
            "java.lang.reflect.",
        )

    private val rootPackage =
        (TestLocationFinder::class.java.`package`?.name ?: FALLBACK_PACKAGE)
            .substringBefore(".impl")
            .substringBefore(".core")

    fun findTestLocation(): StackTraceElement? = Thread.currentThread().stackTrace.firstOrNull(::isTestLocation)

    private fun isTestLocation(element: StackTraceElement): Boolean {
        val className = element.className
        return !isIgnored(className, element.methodName) &&
            (className.contains("Test") || !isKontureImplementation(className))
    }

    private fun isIgnored(
        className: String,
        methodName: String,
    ): Boolean =
        className in ignoredClasses ||
            frameworkPackages.any(className::startsWith) ||
            methodName.contains("$")

    private fun isKontureImplementation(className: String): Boolean {
        if (!className.startsWith("$rootPackage.")) return false
        return !className.startsWith("$rootPackage.sample.") &&
            !className.startsWith("$rootPackage.test.")
    }
}

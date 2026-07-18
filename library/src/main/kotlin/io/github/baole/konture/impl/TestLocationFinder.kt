/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

internal object TestLocationFinder {
    fun findTestLocation(): StackTraceElement? {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            val className = element.className
            if (className == "java.lang.Thread" || className == "java.lang.Throwable") continue
            if (className == "io.github.baole.konture.impl.TestLocationFinder" ||
                className == "io.github.baole.konture.impl.BaselineManager" ||
                className == "io.github.baole.konture.impl.BaselineNormalizer" ||
                className == "io.github.baole.konture.impl.BaselineSerializer"
            ) {
                continue
            }
            if (className.startsWith("org.junit.") ||
                className.startsWith("junit.") ||
                className.startsWith("org.testng.") ||
                className.startsWith("org.gradle.") ||
                className.startsWith("org.apache.maven.") ||
                className.startsWith("sun.reflect.") ||
                className.startsWith("java.lang.reflect.")
            ) {
                continue
            }
            if (element.methodName.contains("$")) continue
            if (className.contains("Test")) return element
            val pkg = TestLocationFinder::class.java.`package`?.name ?: "io.github.baole.konture.impl"
            val rootPkg = pkg.substringBefore(".impl").substringBefore(".core")
            if (className.startsWith("$rootPkg.") &&
                !className.startsWith("$rootPkg.sample.") &&
                !className.startsWith("$rootPkg.test.")
            ) {
                continue
            }
            return element
        }
        return null
    }
}

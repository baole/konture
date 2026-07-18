/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import java.io.File
import kotlinx.serialization.json.Json

@Suppress("NestedBlockDepth", "TooGenericExceptionCaught")
internal object BaselineSerializer {
    val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    fun loadViolationsFromFile(file: File): Set<FlatBaselineViolation> {
        if (!file.exists()) return emptySet()
        return try {
            KontureLogger.log(LogLevel.INFO, "Loading architecture baseline from: ${file.absolutePath}")
            val content = file.readText()

            val flatList = mutableListOf<FlatBaselineViolation>()

            val data = json.decodeFromString<BaselineData>(content)

            for (testClassConfig in data.testClasses) {
                val className = testClassConfig.name
                for (method in testClassConfig.tests) {
                    val methodName = method.name
                    for (violation in method.violations) {
                        flatList.add(
                            FlatBaselineViolation(
                                testClass = className,
                                testMethod = methodName,
                                location = violation.location,
                                message = violation.message,
                            ),
                        )
                    }
                }
            }

            flatList.toSet()
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to parse baseline file: ${e.message}")
            emptySet()
        }
    }

    fun writeViolationsToFile(
        file: File,
        violations: List<FlatBaselineViolation>,
    ) {
        try {
            val classes =
                violations
                    .groupBy { it.testClass }
                    .map { (className, classViolations) ->
                        val tests =
                            classViolations
                                .groupBy { it.testMethod }
                                .map { (methodName, methodViolations) ->
                                    val finalViolations =
                                        methodViolations
                                            .map {
                                                BaselineViolation(location = it.location, message = it.message)
                                            }.sorted()
                                    TestMethodConfig(name = methodName, violations = finalViolations)
                                }.sortedBy { it.name }
                        TestClassConfig(name = className, tests = tests)
                    }.sortedBy { it.name }

            val data =
                BaselineData(
                    testClasses = classes,
                )
            val content = json.encodeToString(data)

            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            file.writeText(content)
            KontureLogger.log(LogLevel.INFO, "Successfully wrote baseline file containing ${violations.size} violations to: ${file.absolutePath}")
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to write baseline file to ${file.absolutePath}: ${e.message}")
        }
    }
}

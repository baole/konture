/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.ClassesRuleBuilder
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.Konture
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.report.KontureJsonReport
import io.github.baole.konture.core.report.sarif.SarifReport
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ReportPipelineIntegrationTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var projectGraph: ProjectGraph

    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_HTML_PATH)
        System.clearProperty(Konture.PROPERTY_BASELINE_PATH)
        System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
        BaselineManager.resetForTest()
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_HTML_PATH)
        System.clearProperty(Konture.PROPERTY_BASELINE_PATH)
        System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
        BaselineManager.resetForTest()
    }

    private fun setupMockProjectGraph() {
        val repoFile =
            File(tempDir, "domain/src/UserRepository.kt").also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }
        val serviceFile =
            File(tempDir, "domain/src/UserService.kt").also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }
        val controllerFile =
            File(tempDir, "api/src/UserController.kt").also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }

        val repoClass =
            ClassDeclaration(
                name = "UserRepository",
                fqName = "com.example.domain.UserRepository",
                packageName = "com.example.domain",
                isInterface = false, // Intentionally a class instead of interface to trigger violation
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = repoFile.absolutePath,
                sourceLine = 10,
            )

        val serviceClass =
            ClassDeclaration(
                name = "UserService",
                fqName = "com.example.domain.UserService",
                packageName = "com.example.domain",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.api.UserController"),
                referencedTypes = setOf("UserController"),
                filePath = serviceFile.absolutePath,
                sourceLine = 20,
            )

        val controllerClass =
            ClassDeclaration(
                name = "UserController",
                fqName = "com.example.api.UserController",
                packageName = "com.example.api",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = controllerFile.absolutePath,
                sourceLine = 5,
            )

        val module =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = tempDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files =
                    listOf(
                        FileDeclaration(
                            "UserRepository.kt",
                            "com.example.domain",
                            classes = listOf(repoClass),
                            filePath = repoFile.absolutePath,
                        ),
                        FileDeclaration(
                            "UserService.kt",
                            "com.example.domain",
                            classes = listOf(serviceClass),
                            filePath = serviceFile.absolutePath,
                        ),
                        FileDeclaration(
                            "UserController.kt",
                            "com.example.api",
                            classes = listOf(controllerClass),
                            filePath = controllerFile.absolutePath,
                        ),
                    ),
            )

        projectGraph = ProjectGraph(builds = mapOf(":" to listOf(module)))
        ProjectGraph.setDefault(projectGraph)
    }

    @Test
    fun `full pipeline generates JSON and SARIF reports with unsuppressed and baseline-suppressed violations`() {
        setupMockProjectGraph()

        val jsonReportFile = File(tempDir, "build/reports/konture/konture-report.json")
        val sarifReportFile = File(tempDir, "build/reports/konture/konture-report.sarif")
        val baselineFile = File(tempDir, "config/konture-baseline.json")

        Konture.jsonReportPath = jsonReportFile.absolutePath
        Konture.sarifReportPath = sarifReportFile.absolutePath
        Konture.baselinePath = baselineFile.absolutePath

        System.setProperty(Konture.PROPERTY_REPORT_JSON_PATH, jsonReportFile.absolutePath)
        System.setProperty(Konture.PROPERTY_REPORT_SARIF_PATH, sarifReportFile.absolutePath)
        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        System.setProperty(Konture.PROPERTY_BASELINE_PATH, "config/konture-baseline.json")

        // Step 1: Record baseline for the UserRepository rule
        Konture.generateBaseline = true
        ClassesRuleBuilder(projectGraph)
            .that()
            .nameEndsWith("Repository")
            .should()
            .beInterfaces()
            .check()

        // Flush baseline to disk
        BaselineManager.writeBaseline()

        // Step 2: Turn off baseline generation and reset BaselineManager to load the newly written baseline file
        Konture.generateBaseline = false
        BaselineManager.resetForTest()

        // Rule 1: UserRepository must be interface (baselined -> suppressed)
        ClassesRuleBuilder(projectGraph)
            .that()
            .nameEndsWith("Repository")
            .should()
            .beInterfaces()
            .check()

        // Rule 2: Domain service must not depend on API controller (unsuppressed -> throws AssertionError)
        assertThrows<AssertionError> {
            ClassesRuleBuilder(projectGraph)
                .that()
                .inPackage("..domain..")
                .should()
                .notDependOnPackages("..api..")
                .check()
        }

        // Verify JSON report on disk
        assertTrue(jsonReportFile.exists(), "JSON report should be generated on disk")
        val jsonText = Files.readString(jsonReportFile.toPath())
        val jsonReport = json.decodeFromString<KontureJsonReport>(jsonText)

        assertEquals("1.0.0", jsonReport.schemaVersion)
        assertEquals("Konture", jsonReport.tool.name)
        assertEquals(KontureConstants.VERSION, jsonReport.tool.version)
        assertEquals(3, jsonReport.summary.totalRules)
        assertEquals(1, jsonReport.summary.failedRules)

        // Check suppressed vs unsuppressed in violations
        val repoViolation = jsonReport.violations.find { it.subject.name == "com.example.domain.UserRepository" }
        assertNotNull(repoViolation)
        assertTrue(repoViolation!!.isSuppressed)
        assertTrue(repoViolation.sourceLocation?.filePath?.endsWith("domain/src/UserRepository.kt") == true)

        val serviceViolation = jsonReport.violations.find { it.subject.name == "com.example.domain.UserService" }
        assertNotNull(serviceViolation)
        assertFalse(serviceViolation!!.isSuppressed)
        assertTrue(serviceViolation.sourceLocation?.filePath?.endsWith("domain/src/UserService.kt") == true)

        // Verify SARIF report on disk
        assertTrue(sarifReportFile.exists(), "SARIF report should be generated on disk")
        val sarifText = Files.readString(sarifReportFile.toPath())
        val sarifReport = json.decodeFromString<SarifReport>(sarifText)

        assertEquals("https://json.schemastore.org/sarif-2.1.0.json", sarifReport.schema)
        assertEquals("2.1.0", sarifReport.version)
        val run = sarifReport.runs.first()
        assertEquals("Konture", run.tool.driver.name)

        val sarifRepoResult =
            run.results.find {
                it.locations.any { loc -> loc.physicalLocation.artifactLocation.uri.contains("UserRepository") }
            }
        assertNotNull(sarifRepoResult)
        assertNotNull(sarifRepoResult!!.suppressions)
        assertEquals("external", sarifRepoResult.suppressions!!.first().kind)
        assertEquals("accepted", sarifRepoResult.suppressions!!.first().status)

        val sarifServiceResult =
            run.results.find {
                it.locations.any { loc -> loc.physicalLocation.artifactLocation.uri.contains("UserService") }
            }
        assertNotNull(sarifServiceResult)
        assertNull(sarifServiceResult!!.suppressions)
    }
}

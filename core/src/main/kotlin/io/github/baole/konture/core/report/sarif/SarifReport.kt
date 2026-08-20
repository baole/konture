/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.report.sarif

import io.github.baole.konture.core.KontureConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level root object conforming to the OASIS Static Analysis Results Interchange Format (SARIF) v2.1.0.
 *
 * @property schema The JSON schema URI for SARIF v2.1.0 validation.
 * @property version The SARIF format version.
 * @property runs The list of analysis runs contained in this log.
 */
@Serializable
public data class SarifReport(
    @SerialName("\$schema")
    val schema: String = "https://json.schemastore.org/sarif-2.1.0.json",
    val version: String = "2.1.0",
    val runs: List<SarifRun> = emptyList(),
)

/**
 * An individual static analysis run within a SARIF report.
 *
 * @property tool Information about the tool that performed the analysis.
 * @property results List of results (findings or violations) produced by the run.
 */
@Serializable
public data class SarifRun(
    val tool: SarifTool,
    val results: List<SarifResult> = emptyList(),
)

/**
 * Information about the static analysis tool used in this run.
 *
 * @property driver Component describing the primary analyzer engine.
 */
@Serializable
public data class SarifTool(
    val driver: SarifDriver,
)

/**
 * Driver component detailing tool identity and rule definitions.
 *
 * @property name The name of the analyzer.
 * @property version The analyzer version.
 * @property informationUri Public homepage/documentation URL of the tool.
 * @property rules List of reporting descriptor rules checked during the run.
 */
@Serializable
public data class SarifDriver(
    val name: String = "Konture",
    val version: String = KontureConstants.VERSION,
    val informationUri: String = "https://github.com/baole/konture",
    val rules: List<SarifRule> = emptyList(),
)

/**
 * Metadata describing an individual rule or check.
 *
 * @property id The unique stable rule identifier.
 * @property name An optional human-readable name for the rule.
 * @property shortDescription Concise summary description.
 * @property fullDescription Comprehensive description of what the rule enforces.
 * @property defaultConfiguration Default reporting level configuration.
 * @property properties Custom properties including taxonomy or categorization tags.
 */
@Serializable
public data class SarifRule(
    val id: String,
    val name: String? = null,
    val shortDescription: SarifMessage,
    val fullDescription: SarifMessage? = null,
    val defaultConfiguration: SarifReportingConfiguration? = null,
    val properties: SarifRuleProperties? = null,
)

/**
 * Custom property bag containing categorization tags for a SARIF rule.
 *
 * @property tags Tags associated with the rule.
 */
@Serializable
public data class SarifRuleProperties(
    val tags: List<String> = emptyList(),
)

/**
 * Reporting configuration defining the severity level for a SARIF rule.
 *
 * @property level Result level (e.g. "error", "warning", "note", "none").
 */
@Serializable
public data class SarifReportingConfiguration(
    val level: String = "error",
)

/**
 * An individual finding or violation recorded by an analysis run.
 *
 * @property ruleId Identifier of the rule that produced this finding.
 * @property level Severity level of this result ("error", "warning", "note").
 * @property message The message describing the finding.
 * @property locations Physical or logical locations where the finding occurred.
 * @property codeFlows Execution or dependency traces relevant to the finding.
 * @property suppressions Information about any suppressions applied (e.g. architecture baselines).
 */
@Serializable
public data class SarifResult(
    val ruleId: String,
    val level: String,
    val message: SarifMessage,
    val locations: List<SarifLocation> = emptyList(),
    val codeFlows: List<SarifCodeFlow>? = null,
    val suppressions: List<SarifSuppression>? = null,
)

/**
 * Plain text or markdown message content.
 *
 * @property text The message string.
 */
@Serializable
public data class SarifMessage(
    val text: String,
)

/**
 * Location descriptor referencing a physical source code artifact.
 *
 * @property physicalLocation The physical location of the artifact.
 */
@Serializable
public data class SarifLocation(
    val physicalLocation: SarifPhysicalLocation,
    val message: SarifMessage? = null,
)

/**
 * Physical location containing an artifact URI and optional source region.
 *
 * @property artifactLocation Location of the file artifact.
 * @property region Optional 1-based text region inside the artifact.
 */
@Serializable
public data class SarifPhysicalLocation(
    val artifactLocation: SarifArtifactLocation,
    val region: SarifRegion? = null,
)

/**
 * URI reference to a file artifact.
 *
 * @property uri Relative or absolute URI of the file.
 * @property uriBaseId Optional root identifier (defaults to "%SRCROOT%").
 */
@Serializable
public data class SarifArtifactLocation(
    val uri: String,
    val uriBaseId: String? = "%SRCROOT%",
)

/**
 * A region within a source code artifact.
 *
 * @property startLine 1-based starting line number.
 * @property startColumn 1-based starting column number.
 * @property endLine 1-based ending line number.
 * @property endColumn 1-based ending column number.
 */
@Serializable
public data class SarifRegion(
    val startLine: Int? = null,
    val startColumn: Int? = null,
    val endLine: Int? = null,
    val endColumn: Int? = null,
)

/**
 * Set of thread flows describing a code execution or dependency path.
 *
 * @property threadFlows List of thread flows participating in the trace.
 */
@Serializable
public data class SarifCodeFlow(
    val threadFlows: List<SarifThreadFlow> = emptyList(),
)

/**
 * A temporal or logical sequence of code locations.
 *
 * @property locations Sequence of visited locations.
 */
@Serializable
public data class SarifThreadFlow(
    val locations: List<SarifThreadFlowLocation> = emptyList(),
)

/**
 * An individual step or node in a thread flow.
 *
 * @property location The code location at this step.
 * @property importance Importance rating for this step ("essential", "important", "unimportant").
 */
@Serializable
public data class SarifThreadFlowLocation(
    val location: SarifLocation,
    val importance: String? = null,
)

/**
 * Details regarding a suppression applied to a result (e.g., architecture baseline match).
 *
 * @property kind The suppression mechanism (e.g. "external", "inSource").
 * @property status The status of the suppression ("accepted", "underReview", "rejected").
 * @property justification Optional explanation why the finding was suppressed.
 */
@Serializable
public data class SarifSuppression(
    val kind: String = "external",
    val status: String = "accepted",
    val justification: String? = null,
)

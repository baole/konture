/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation

/**
 * First-class, declarative architectural-layer policy DSL.
 *
 * A layer groups a set of modules and/or packages and declares which other
 * layers it may (or may not) depend on, and which layers may (or may not) depend
 * on it. The policy is compiled down to the primitive Konture module/package
 * selectors and class dependency assertions.
 *
 * ### Example Usage:
 * ```kotlin
 * architecture {
 *     layer("feature") {
 *         selector {
 *             modules(":feature:**")
 *         }
 *         mayDependOn("core", "domain")
 *         mustNotDependOn("feature")
 *     }
 *
 *     layer("domain") {
 *         selector {
 *             packages("com.acme.domain..")
 *         }
 *         mayDependOn("core")
 *     }
 * }
 * ```
 */
@KontureDsl
public class ArchitectureLayerPolicy internal constructor(
    /** The unique, human-readable name of this layer. */
    public val name: String,
) {
    private val selector = LayerSelectorDsl()

    private var mayDependOnConfig: Set<String> = emptySet()
    private var mayDependOnConfigured = false
    private var mustNotDependOnConfig: Set<String> = emptySet()
    private var mustNotDependOnConfigured = false
    private var mayBeAccessedByConfig: Set<String> = emptySet()
    private var mayBeAccessedByConfigured = false
    private var mustNotBeAccessedByConfig: Set<String> = emptySet()
    private var mustNotBeAccessedByConfigured = false

    /**
     * Declares which modules and/or packages belong to this layer.
     *
     * - [LayerSelectorDsl.modules] accepts module glob patterns (e.g. `":feature:**"`).
     * - [LayerSelectorDsl.packages] accepts package patterns (e.g. `"com.acme.feature.."`).
     */
    public fun selector(block: LayerSelectorDsl.() -> Unit) {
        selector.apply(block)
    }

    /** Restricts the layers this layer is allowed to depend on (intra-layer dependencies are always allowed). */
    public fun mayDependOn(vararg layers: String) {
        mayDependOnConfig = layers.toSet()
        mayDependOnConfigured = true
    }

    /** Restricts the layers this layer is allowed to depend on. */
    public fun mayDependOn(layers: List<String>): Unit = mayDependOn(*layers.toTypedArray())

    /** Forbids this layer from depending on the given layers. */
    public fun mustNotDependOn(vararg layers: String) {
        mustNotDependOnConfig = layers.toSet()
        mustNotDependOnConfigured = true
    }

    /** Forbids this layer from depending on the given layers. */
    public fun mustNotDependOn(layers: List<String>): Unit = mustNotDependOn(*layers.toTypedArray())

    /** Restricts the layers that are allowed to depend on this layer (intra-layer dependencies are always allowed). */
    public fun mayBeAccessedBy(vararg layers: String) {
        mayBeAccessedByConfig = layers.toSet()
        mayBeAccessedByConfigured = true
    }

    /** Restricts the layers that are allowed to depend on this layer. */
    public fun mayBeAccessedBy(layers: List<String>): Unit = mayBeAccessedBy(*layers.toTypedArray())

    /** Forbids the given layers from depending on this layer. */
    public fun mustNotBeAccessedBy(vararg layers: String) {
        mustNotBeAccessedByConfig = layers.toSet()
        mustNotBeAccessedByConfigured = true
    }

    /** Forbids the given layers from depending on this layer. */
    public fun mustNotBeAccessedBy(layers: List<String>): Unit = mustNotBeAccessedBy(*layers.toTypedArray())

    internal fun selectorSnapshot(): SelectorSnapshot = selector.snapshot()

    internal fun mayDependOnConfig(): Set<String> = mayDependOnConfig

    internal fun hasMayDependOn(): Boolean = mayDependOnConfigured

    internal fun mustNotDependOnConfig(): Set<String> = mustNotDependOnConfig

    internal fun hasMustNotDependOn(): Boolean = mustNotDependOnConfigured

    internal fun mayBeAccessedByConfig(): Set<String> = mayBeAccessedByConfig

    internal fun hasMayBeAccessedBy(): Boolean = mayBeAccessedByConfigured

    internal fun mustNotBeAccessedByConfig(): Set<String> = mustNotBeAccessedByConfig

    internal fun hasMustNotBeAccessedBy(): Boolean = mustNotBeAccessedByConfigured

    internal fun referencedLayerNames(): Set<String> =
        buildSet {
            if (mayDependOnConfigured) addAll(mayDependOnConfig)
            if (mustNotDependOnConfigured) addAll(mustNotDependOnConfig)
            if (mayBeAccessedByConfigured) addAll(mayBeAccessedByConfig)
            if (mustNotBeAccessedByConfigured) addAll(mustNotBeAccessedByConfig)
        }
}

/**
 * Selector DSL used within [ArchitectureLayerPolicy.selector] to define a layer's
 * member modules and/or packages.
 */
@KontureDsl
public class LayerSelectorDsl internal constructor() {
    private val modulePatterns = mutableListOf<String>()
    private val packagePatterns = mutableListOf<String>()

    /** Adds module glob patterns (e.g. `":feature:**"`) that select member modules. */
    public fun modules(vararg patterns: String) {
        modulePatterns += patterns
    }

    /** Adds module glob patterns that select member modules. */
    public fun modules(patterns: List<String>): Unit = modules(*patterns.toTypedArray())

    /** Adds package patterns (e.g. `"com.acme.feature.."`) that select member classes. */
    public fun packages(vararg patterns: String) {
        packagePatterns += patterns
    }

    /** Adds package patterns that select member classes. */
    public fun packages(patterns: List<String>): Unit = packages(*patterns.toTypedArray())

    internal fun snapshot(): SelectorSnapshot = SelectorSnapshot(modulePatterns.toList(), packagePatterns.toList())
}

/** Immutable snapshot of the selector patterns declared for a layer. */
internal data class SelectorSnapshot(
    val modulePatterns: List<String>,
    val packagePatterns: List<String>,
)

/**
 * Resolves the declared [ArchitectureLayerPolicy] definitions against the live project
 * graph and reports violations on the configured dependency boundaries.
 */
internal class ArchitectureLayerRegistry(
    private val policies: List<ArchitectureLayerPolicy>,
    private val allModules: List<Module>,
    private val allClasses: List<ClassDeclaration>,
) {
    private val memberClassesByName: Map<String, List<ClassDeclaration>>
    private val layersByClassFqName: Map<String, Set<String>>

    init {
        val byName = policies.associate { it.name to it.resolveMembership() }
        validateReferencedLayerNames(byName.keys)
        memberClassesByName = byName
        val reverse = mutableMapOf<String, MutableSet<String>>()
        for ((layerName, classes) in byName) {
            for (cls in classes) reverse.getOrPut(cls.fqName) { mutableSetOf() }.add(layerName)
        }
        layersByClassFqName = reverse.mapValues { it.value.toSet() }
    }

    /**
     * Rejects references to layer names that were never declared, so a typo in a
     * `mayDependOn(...)`/`mustNotDependOn(...)`/`mayBeAccessedBy(...)`/
     * `mustNotBeAccessedBy(...)` cannot silently produce a rule that never fires.
     */
    private fun validateReferencedLayerNames(declared: Set<String>) {
        val referenced =
            policies.flatMap { policy ->
                policy.referencedLayerNames()
            }.toSet()
        val unknown = (referenced - declared).sorted()
        require(unknown.isEmpty()) {
            getMessage("architecture.policy.undefinedLayer", unknown.joinToString())
        }
    }

    /** Compiles a layer down to the set of member classes selected by its module and package patterns. */
    private fun ArchitectureLayerPolicy.resolveMembership(): List<ClassDeclaration> {
        val snapshot = selectorSnapshot()
        require(snapshot.modulePatterns.isNotEmpty() || snapshot.packagePatterns.isNotEmpty()) {
            getMessage("architecture.policy.noSelector", name)
        }
        val fromModules =
            snapshot.modulePatterns
                .flatMap { pattern -> allModules.filter { PatternMatchers.matchesModuleGlob(pattern, it.path) } }
                .flatMap { it.classes }
        val fromPackages =
            if (snapshot.packagePatterns.isNotEmpty()) {
                allClasses.filter { cls ->
                    snapshot.packagePatterns.any { PatternMatchers.matchesPackage(it, cls.packageName) }
                }
            } else {
                emptyList()
            }
        return (fromModules + fromPackages).distinctBy { it.fqName }
    }

    /** Collects all boundary violations for every declared layer into [out]. */
    public fun collectViolations(out: MutableList<Violation>) {
        for (policy in policies) {
            policy.collectForLayer(out)
        }
    }

    private fun ArchitectureLayerPolicy.collectForLayer(out: MutableList<Violation>) {
        val members = memberClassesByName[name].orEmpty()
        checkOutboundConstraints(members, out)
        checkInboundConstraints(members, out)
    }

    /** Verifies the outbound (depends-on) boundaries declared by [this] layer. */
    private fun ArchitectureLayerPolicy.checkOutboundConstraints(
        members: List<ClassDeclaration>,
        out: MutableList<Violation>,
    ) {
        if (!hasMayDependOn() && !hasMustNotDependOn()) return
        val allowed = mayDependOnConfig().takeIf { hasMayDependOn() }
        val forbidden = mustNotDependOnConfig().takeIf { hasMustNotDependOn() }
        for (source in members) {
            for (target in allClasses) {
                if (!source.dependsOn(target)) continue
                if (allowed != null) {
                    checkMayDependOn(source, target, allowed, out)
                }
                if (forbidden != null) {
                    checkMustNotDependOn(source, target, forbidden, out)
                }
            }
        }
    }

    /** Verifies the inbound (accessed-by) boundaries declared by [this] layer. */
    private fun ArchitectureLayerPolicy.checkInboundConstraints(
        members: List<ClassDeclaration>,
        out: MutableList<Violation>,
    ) {
        if (!hasMayBeAccessedBy() && !hasMustNotBeAccessedBy()) return
        val allowed = mayBeAccessedByConfig().takeIf { hasMayBeAccessedBy() }
        val forbidden = mustNotBeAccessedByConfig().takeIf { hasMustNotBeAccessedBy() }
        for (target in members) {
            for (caller in allClasses) {
                if (!caller.dependsOn(target)) continue
                if (allowed != null) {
                    checkMayBeAccessedBy(target, caller, allowed, out)
                }
                if (forbidden != null) {
                    checkMustNotBeAccessedBy(target, caller, forbidden, out)
                }
            }
        }
    }

    private fun ArchitectureLayerPolicy.checkMayDependOn(
        source: ClassDeclaration,
        target: ClassDeclaration,
        allowed: Set<String>,
        out: MutableList<Violation>,
    ) {
        val illegal = layersByClassFqName[target.fqName].orEmpty().filter { it != name && it !in allowed }
        if (illegal.isEmpty()) return
        out.add(
            newViolation(
                source = source,
                target = target,
                messageKey = "architecture.policy.mayDependOn",
                name,
                allowed.joinToString(),
                source.fqName,
                target.fqName,
                illegal.joinToString(),
                ViolationLocation.format(source),
            ),
        )
    }

    private fun ArchitectureLayerPolicy.checkMustNotDependOn(
        source: ClassDeclaration,
        target: ClassDeclaration,
        forbidden: Set<String>,
        out: MutableList<Violation>,
    ) {
        val hit = targetLayers(target).any { it in forbidden }
        if (!hit) return
        out.add(
            newViolation(
                source = source,
                target = target,
                messageKey = "architecture.policy.mustNotDependOn",
                name,
                forbidden.joinToString(),
                source.fqName,
                target.fqName,
                targetLayers(target).joinToString(),
                ViolationLocation.format(source),
            ),
        )
    }

    private fun ArchitectureLayerPolicy.checkMayBeAccessedBy(
        target: ClassDeclaration,
        caller: ClassDeclaration,
        allowed: Set<String>,
        out: MutableList<Violation>,
    ) {
        val illegal = layersByClassFqName[caller.fqName].orEmpty().filter { it != name && it !in allowed }
        if (illegal.isEmpty()) return
        out.add(
            newViolation(
                source = caller,
                target = target,
                messageKey = "architecture.policy.mayBeAccessedBy",
                name,
                allowed.joinToString(),
                caller.fqName,
                illegal.joinToString(),
                target.fqName,
                ViolationLocation.format(caller),
            ),
        )
    }

    private fun ArchitectureLayerPolicy.checkMustNotBeAccessedBy(
        target: ClassDeclaration,
        caller: ClassDeclaration,
        forbidden: Set<String>,
        out: MutableList<Violation>,
    ) {
        val hit = layersByClassFqName[caller.fqName].orEmpty().any { it in forbidden }
        if (!hit) return
        out.add(
            newViolation(
                source = caller,
                target = target,
                messageKey = "architecture.policy.mustNotBeAccessedBy",
                name,
                forbidden.joinToString(),
                caller.fqName,
                targetLayers(caller).joinToString(),
                target.fqName,
                ViolationLocation.format(caller),
            ),
        )
    }

    private fun targetLayers(cls: ClassDeclaration): Set<String> = layersByClassFqName[cls.fqName].orEmpty()

    private fun newViolation(
        source: ClassDeclaration? = null,
        target: ClassDeclaration? = null,
        messageKey: String,
        vararg args: Any?,
    ): Violation {
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        val sourceSubject =
            source?.let {
                Subject.ClassSubject(
                    fqName = it.fqName,
                    simpleName = it.name,
                    location = SourceLocation(filePath = it.filePath),
                )
            } ?: Subject.CustomSubject(name = "Architecture Policy")
        val targetSubject =
            target?.let {
                Subject.ClassSubject(
                    fqName = it.fqName,
                    simpleName = it.name,
                    location = SourceLocation(filePath = it.filePath),
                )
            }
        val dependencyPath =
            if (sourceSubject != null && targetSubject != null) {
                listOf(sourceSubject, targetSubject)
            } else {
                emptyList()
            }
        return Violation(
            ruleId = currentMeta?.id ?: "architecture.policy",
            subject = sourceSubject,
            target = targetSubject,
            dependencyPath = dependencyPath,
            sourceLocation = source?.let { SourceLocation(filePath = it.filePath) },
            message = getMessage(messageKey, *args),
            severity = currentMeta?.severity ?: Severity.ERROR,
            metadata = currentMeta,
        )
    }
}

/**
 * Runs all declared [ArchitectureLayerPolicy] definitions against [graph], reporting
 * violations through the baseline-aware [BaselineManager] and throwing on any that
 * exceed the configured fail-on-severity threshold.
 */
internal fun checkLayerPolicies(
    policies: List<ArchitectureLayerPolicy>,
    graph: ProjectGraph,
) {
    if (policies.isEmpty()) return
    val allModules = graph.getAllModules()
    val allClasses = allModules.flatMap { it.classes }
    val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
    val ruleId = currentMeta?.id ?: "architecture.policy"
    val header = currentMeta?.description ?: getMessage("architecture.policy.violationHeader")
    BaselineManager.checkRuleReport(
        ruleId = ruleId,
        violationHeader = header,
        runCheckReport = { violations ->
            ArchitectureLayerRegistry(policies, allModules, allClasses).collectViolations(violations)
        },
    )
}

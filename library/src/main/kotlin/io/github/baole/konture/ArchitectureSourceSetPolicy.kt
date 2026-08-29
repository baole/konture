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
 * Known default platform namespaces used for platform-independence checks in Kotlin Multiplatform projects.
 */
public object PlatformPackages {
    /**
     * Default banned platform package patterns for platform-independent source sets.
     */
    public val DEFAULT_BANNED: List<String> =
        listOf(
            // Java / JVM
            "java..",
            "javax..",
            "sun..",
            "com.sun..",
            // Android
            "android..",
            "androidx..",
            "com.google.android..",
            // Apple / Darwin / Native
            "platform..",
            "objc..",
            "darwin..",
            "cnames..",
            // Native / POSIX / Windows
            "winreg..",
            "posix..",
            "windows..",
        )
}

/**
 * First-class, declarative source-set architecture policy DSL.
 *
 * Enforces platform-independence, package import/dependency restrictions, and Gradle source-set
 * `dependsOn` hierarchy rules for Kotlin Multiplatform (KMP) and multi-source-set projects.
 *
 * ### Example Usage:
 * ```kotlin
 * architecture {
 *     sourceSet("commonMain") {
 *         mustBePlatformIndependent()
 *         mustNotDependOn("android.**", "java.awt.**")
 *         mustNotDependOnSourceSets("jvmMain", "androidMain", "iosMain")
 *     }
 *
 *     sourceSet("androidMain") {
 *         mayDependOn("android.**", "androidx.**")
 *     }
 * }
 * ```
 */
@KontureDsl
public class ArchitectureSourceSetPolicy internal constructor(
    /** The selector specifying which source sets this policy applies to. */
    public val selector: SourceSetSelector,
    /** The human-readable label identifying this source-set policy. */
    public val label: String,
) {
    private var platformIndependent = false
    private var additionalBannedPackages: List<String> = emptyList()
    private var excludingBannedPackages: List<String> = emptyList()

    private val mustNotDependOnPackages = mutableListOf<String>()
    private val mayDependOnPackages = mutableListOf<String>()
    private var mayDependOnConfigured = false

    private val mustNotDependOnSourceSets = mutableListOf<String>()

    /**
     * Requires the matched source set(s) to remain platform-independent by forbidding imports
     * and symbol references to known platform packages (e.g. `android.*`, `java.*`, `platform.*`).
     *
     * @param additionalBanned Extra package patterns to ban in addition to [PlatformPackages.DEFAULT_BANNED].
     * @param excluding Package patterns to exclude/whitelist from being banned.
     */
    public fun mustBePlatformIndependent(
        additionalBanned: List<String> = emptyList(),
        excluding: List<String> = emptyList(),
    ) {
        platformIndependent = true
        additionalBannedPackages = additionalBanned
        excludingBannedPackages = excluding
    }

    /**
     * Forbids the matched source set(s) from importing or depending on the specified package patterns.
     */
    public fun mustNotDependOn(vararg packagePatterns: String) {
        mustNotDependOnPackages.addAll(packagePatterns)
    }

    /**
     * Forbids the matched source set(s) from importing or depending on the specified package patterns.
     */
    public fun mustNotDependOn(packagePatterns: List<String>) {
        mustNotDependOnPackages.addAll(packagePatterns)
    }

    /**
     * Restricts the platform package patterns the matched source set(s) may depend on.
     * Any platform package reference outside of these patterns will trigger a violation.
     */
    public fun mayDependOn(vararg packagePatterns: String) {
        mayDependOnPackages.addAll(packagePatterns)
        mayDependOnConfigured = true
    }

    /**
     * Restricts the platform package patterns the matched source set(s) may depend on.
     */
    public fun mayDependOn(packagePatterns: List<String>) {
        mayDependOnPackages.addAll(packagePatterns)
        mayDependOnConfigured = true
    }

    /**
     * Forbids the matched source set(s) from depending on the specified source-set names in the Gradle model.
     */
    public fun mustNotDependOnSourceSets(vararg sourceSetNames: String) {
        mustNotDependOnSourceSets.addAll(sourceSetNames)
    }

    /**
     * Forbids the matched source set(s) from depending on the specified source-set names in the Gradle model.
     */
    public fun mustNotDependOnSourceSets(sourceSetNames: List<String>) {
        mustNotDependOnSourceSets.addAll(sourceSetNames)
    }

    internal fun isPlatformIndependent(): Boolean = platformIndependent

    internal fun additionalBanned(): List<String> = additionalBannedPackages

    internal fun excludingBanned(): List<String> = excludingBannedPackages

    internal fun mustNotDependOnPackages(): List<String> = mustNotDependOnPackages

    internal fun mayDependOnPackages(): List<String> = mayDependOnPackages

    internal fun hasMayDependOn(): Boolean = mayDependOnConfigured

    internal fun mustNotDependOnSourceSets(): List<String> = mustNotDependOnSourceSets
}

/**
 * Registry resolving and verifying [ArchitectureSourceSetPolicy] declarations against the project graph.
 */
internal class ArchitectureSourceSetRegistry(
    private val policies: List<ArchitectureSourceSetPolicy>,
    private val allModules: List<Module>,
) {
    fun collectViolations(out: MutableList<Violation>) {
        for (policy in policies) {
            policy.collectViolationsForPolicy(out)
        }
    }

    private fun ArchitectureSourceSetPolicy.collectViolationsForPolicy(out: MutableList<Violation>) {
        val forbiddenSourceSets = mustNotDependOnSourceSets().toSet()
        for (module in allModules) {
            checkModuleHierarchy(module, forbiddenSourceSets, out)
            checkModuleFiles(module, out)
        }
    }

    private fun ArchitectureSourceSetPolicy.checkModuleHierarchy(
        module: Module,
        forbiddenSourceSets: Set<String>,
        out: MutableList<Violation>,
    ) {
        if (forbiddenSourceSets.isEmpty()) return
        for (sourceSet in module.sourceSets) {
            checkSourceSetHierarchy(module, sourceSet, forbiddenSourceSets, out)
        }
    }

    private fun ArchitectureSourceSetPolicy.checkSourceSetHierarchy(
        module: Module,
        sourceSet: SourceSet,
        forbiddenSourceSets: Set<String>,
        out: MutableList<Violation>,
    ) {
        val kind =
            when (sourceSet.kind.uppercase()) {
                "KMP" -> SourceSetKind.KMP
                "ANDROID_VARIANT", "ANDROID" -> SourceSetKind.ANDROID
                else -> SourceSetKind.JVM
            }
        val role = if (sourceSet.production) SourceSetRole.PRODUCTION else SourceSetRole.TEST
        val sourceSetId = SourceSetId(module.path, sourceSet.name, kind, role)
        if (!selector.matches(sourceSetId)) return

        for (dep in sourceSet.dependsOnSourceSets) {
            if (dep in forbiddenSourceSets) {
                out.add(
                    newViolation(
                        sourceSubject = Subject.ModuleSubject(path = module.path),
                        targetSubject = Subject.CustomSubject(name = dep),
                        sourceLocation = SourceLocation(filePath = module.path),
                        messageKey = "architecture.sourceSet.mustNotDependOnSourceSets",
                        sourceSet.name,
                        module.path,
                        mustNotDependOnSourceSets().joinToString(),
                        dep,
                    ),
                )
            }
        }
    }

    private fun ArchitectureSourceSetPolicy.checkModuleFiles(
        module: Module,
        out: MutableList<Violation>,
    ) {
        for (file in module.files) {
            val matchingMemberships = file.membershipsFor(module.path).filter { selector.matches(it) }
            for (sourceSetId in matchingMemberships) {
                checkFilePackageConstraints(module, file, sourceSetId, out)
            }
        }
    }

    private fun ArchitectureSourceSetPolicy.checkFilePackageConstraints(
        module: Module,
        file: FileDeclaration,
        sourceSetId: SourceSetId,
        out: MutableList<Violation>,
    ) {
        val classesToCheck = file.classes.ifEmpty { null }
        if (classesToCheck != null) {
            for (cls in classesToCheck) {
                checkClassSymbols(module, cls, sourceSetId, out)
            }
        } else {
            checkFileLevelSymbols(module, file, sourceSetId, out)
        }
    }

    private fun ArchitectureSourceSetPolicy.checkClassSymbols(
        module: Module,
        cls: ClassDeclaration,
        sourceSetId: SourceSetId,
        out: MutableList<Violation>,
    ) {
        val symbols = collectClassSymbols(cls)
        val location = ViolationLocation.format(cls)

        // 1. Platform-independence check
        if (isPlatformIndependent()) {
            val bannedList = (PlatformPackages.DEFAULT_BANNED + additionalBanned()).distinct()
            val excludedList = excludingBanned()
            for (symbol in symbols) {
                val matchesBanned = bannedList.any { matchesPackagePattern(it, symbol) }
                val matchesExcluded = excludedList.any { matchesPackagePattern(it, symbol) }
                if (matchesBanned && !matchesExcluded) {
                    out.add(
                        newViolation(
                            sourceSubject =
                                Subject.ClassSubject(
                                    fqName = cls.fqName,
                                    simpleName = cls.name,
                                    location = SourceLocation(filePath = cls.filePath),
                                ),
                            targetSubject = Subject.CustomSubject(name = symbol),
                            sourceLocation = SourceLocation(filePath = cls.filePath),
                            messageKey = "architecture.sourceSet.mustBePlatformIndependent",
                            sourceSetId.name,
                            module.path,
                            cls.fqName,
                            symbol,
                            location,
                        ),
                    )
                }
            }
        }

        // 2. Deny-list (mustNotDependOn)
        if (mustNotDependOnPackages().isNotEmpty()) {
            for (symbol in symbols) {
                val hit = mustNotDependOnPackages().any { matchesPackagePattern(it, symbol) }
                if (hit) {
                    out.add(
                        newViolation(
                            sourceSubject =
                                Subject.ClassSubject(
                                    fqName = cls.fqName,
                                    simpleName = cls.name,
                                    location = SourceLocation(filePath = cls.filePath),
                                ),
                            targetSubject = Subject.CustomSubject(name = symbol),
                            sourceLocation = SourceLocation(filePath = cls.filePath),
                            messageKey = "architecture.sourceSet.mustNotDependOn",
                            sourceSetId.name,
                            module.path,
                            mustNotDependOnPackages().joinToString(),
                            cls.fqName,
                            symbol,
                            location,
                        ),
                    )
                }
            }
        }

        // 3. Allow-list (mayDependOn)
        if (hasMayDependOn()) {
            val allowedList = mayDependOnPackages()
            for (symbol in symbols) {
                val isPlatform = PlatformPackages.DEFAULT_BANNED.any { matchesPackagePattern(it, symbol) }
                if (isPlatform) {
                    val isAllowed = allowedList.any { matchesPackagePattern(it, symbol) }
                    if (!isAllowed) {
                        out.add(
                            newViolation(
                                sourceSubject =
                                    Subject.ClassSubject(
                                        fqName = cls.fqName,
                                        simpleName = cls.name,
                                        location = SourceLocation(filePath = cls.filePath),
                                    ),
                                targetSubject = Subject.CustomSubject(name = symbol),
                                sourceLocation = SourceLocation(filePath = cls.filePath),
                                messageKey = "architecture.sourceSet.mayDependOn",
                                sourceSetId.name,
                                module.path,
                                allowedList.joinToString(),
                                cls.fqName,
                                symbol,
                                location,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun ArchitectureSourceSetPolicy.checkFileLevelSymbols(
        module: Module,
        file: FileDeclaration,
        sourceSetId: SourceSetId,
        out: MutableList<Violation>,
    ) {
        val symbols = file.imports
        val location = ViolationLocation.format(file)

        if (isPlatformIndependent()) {
            val bannedList = (PlatformPackages.DEFAULT_BANNED + additionalBanned()).distinct()
            val excludedList = excludingBanned()
            for (symbol in symbols) {
                val matchesBanned = bannedList.any { matchesPackagePattern(it, symbol) }
                val matchesExcluded = excludedList.any { matchesPackagePattern(it, symbol) }
                if (matchesBanned && !matchesExcluded) {
                    out.add(
                        newViolation(
                            sourceSubject =
                                Subject.CustomSubject(
                                    name = file.name,
                                    location = SourceLocation(filePath = file.filePath),
                                ),
                            targetSubject = Subject.CustomSubject(name = symbol),
                            sourceLocation = SourceLocation(filePath = file.filePath),
                            messageKey = "architecture.sourceSet.mustBePlatformIndependent",
                            sourceSetId.name,
                            module.path,
                            file.name,
                            symbol,
                            location,
                        ),
                    )
                }
            }
        }

        if (mustNotDependOnPackages().isNotEmpty()) {
            for (symbol in symbols) {
                val hit = mustNotDependOnPackages().any { matchesPackagePattern(it, symbol) }
                if (hit) {
                    out.add(
                        newViolation(
                            sourceSubject =
                                Subject.CustomSubject(
                                    name = file.name,
                                    location = SourceLocation(filePath = file.filePath),
                                ),
                            targetSubject = Subject.CustomSubject(name = symbol),
                            sourceLocation = SourceLocation(filePath = file.filePath),
                            messageKey = "architecture.sourceSet.mustNotDependOn",
                            sourceSetId.name,
                            module.path,
                            mustNotDependOnPackages().joinToString(),
                            file.name,
                            symbol,
                            location,
                        ),
                    )
                }
            }
        }

        if (hasMayDependOn()) {
            val allowedList = mayDependOnPackages()
            for (symbol in symbols) {
                val isPlatform = PlatformPackages.DEFAULT_BANNED.any { matchesPackagePattern(it, symbol) }
                if (isPlatform) {
                    val isAllowed = allowedList.any { matchesPackagePattern(it, symbol) }
                    if (!isAllowed) {
                        out.add(
                            newViolation(
                                sourceSubject =
                                    Subject.CustomSubject(
                                        name = file.name,
                                        location = SourceLocation(filePath = file.filePath),
                                    ),
                                targetSubject = Subject.CustomSubject(name = symbol),
                                sourceLocation = SourceLocation(filePath = file.filePath),
                                messageKey = "architecture.sourceSet.mayDependOn",
                                sourceSetId.name,
                                module.path,
                                allowedList.joinToString(),
                                file.name,
                                symbol,
                                location,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun collectClassSymbols(cls: ClassDeclaration): List<String> =
        (cls.imports + cls.referencedTypes.filter { it.contains('.') } + cls.supertypes.filter { it.contains('.') }).distinct()

    private fun matchesPackagePattern(
        pattern: String,
        symbol: String,
    ): Boolean {
        val clean = symbol.substringBefore(" as ").trim().removeSuffix(".*")
        val normalized = normalizePackagePattern(pattern)
        return PatternMatchers.matchesPackage(normalized, clean) ||
            PatternMatchers.matchesSimpleGlob(pattern, clean) ||
            PatternMatchers.matchesSimpleGlob(normalized, clean)
    }

    private fun normalizePackagePattern(pattern: String): String =
        pattern
            .replace(".**", "..")
            .replace(".*", "..")
            .replace("**", "..")

    private fun newViolation(
        sourceSubject: Subject,
        targetSubject: Subject? = null,
        sourceLocation: SourceLocation? = null,
        messageKey: String,
        vararg args: Any?,
    ): Violation {
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        val dependencyPath =
            if (targetSubject != null) {
                listOf(sourceSubject, targetSubject)
            } else {
                emptyList()
            }
        return Violation(
            ruleId = currentMeta?.id ?: "architecture.sourceSet",
            subject = sourceSubject,
            target = targetSubject,
            dependencyPath = dependencyPath,
            sourceLocation = sourceLocation,
            message = getMessage(messageKey, *args),
            severity = currentMeta?.severity ?: Severity.ERROR,
            metadata = currentMeta,
        )
    }
}

/**
 * Runs all declared [ArchitectureSourceSetPolicy] definitions against [graph].
 */
internal fun checkSourceSetPolicies(
    policies: List<ArchitectureSourceSetPolicy>,
    graph: ProjectGraph,
) {
    if (policies.isEmpty()) return
    val allModules = graph.getAllModules()
    val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
    val ruleId = currentMeta?.id ?: "architecture.sourceSet"
    val header = currentMeta?.description ?: getMessage("architecture.sourceSet.violationHeader")
    BaselineManager.checkRuleReport(
        ruleId = ruleId,
        violationHeader = header,
        runCheckReport = { violations ->
            ArchitectureSourceSetRegistry(policies, allModules).collectViolations(violations)
        },
    )
}

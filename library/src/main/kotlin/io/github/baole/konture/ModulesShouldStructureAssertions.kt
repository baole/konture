/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.normalizeModulePath
import kotlin.reflect.KClass

interface ModulesShouldStructureAssertions {
    val builder: ModulesRuleBuilder

    infix fun applyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.appliedPlugins.contains(pluginId)) {
                violations.add(getMessage("module.should.applyPlugin", module.path, pluginId))
            }
        }
        return builder
    }

    infix fun havePlugin(pluginId: String): ModulesRuleBuilder = applyPlugin(pluginId)

    infix fun havePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val missing = pluginIds.filter { !module.appliedPlugins.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(getMessage("module.should.applyPlugin", module.path, missing.joinToString()))
            }
        }
        return builder
    }

    fun havePlugins(vararg pluginIds: String): ModulesRuleBuilder = havePlugins(pluginIds.toList())

    infix fun notApplyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.appliedPlugins.contains(pluginId)) {
                violations.add(getMessage("module.should.notApplyPlugin", module.path, pluginId))
            }
        }
        return builder
    }

    infix fun notHavePlugin(pluginId: String): ModulesRuleBuilder = notApplyPlugin(pluginId)

    infix fun notHavePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val applied = pluginIds.filter { module.appliedPlugins.contains(it) }
            if (applied.isNotEmpty()) {
                violations.add(getMessage("module.should.notApplyPlugin", module.path, applied.joinToString()))
            }
        }
        return builder
    }

    fun notHavePlugins(vararg pluginIds: String): ModulesRuleBuilder = notHavePlugins(pluginIds.toList())

    fun containClasses(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.isEmpty()) {
                violations.add(getMessage("module.should.containClasses", module.path))
            }
        }
        return builder
    }

    fun notContainClasses(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.notContainClasses", module.path))
            }
        }
        return builder
    }

    infix fun haveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val hasSourceSet = module.sourceSets.any { it.name == sourceSetName }
            if (!hasSourceSet) {
                violations.add(getMessage("module.should.haveSourceSet", module.path, sourceSetName))
            }
        }
        return builder
    }

    infix fun haveSourceSets(sourceSetNames: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val missing = sourceSetNames.filter { name -> module.sourceSets.none { it.name == name } }
            if (missing.isNotEmpty()) {
                violations.add(getMessage("module.should.haveSourceSet", module.path, missing.joinToString()))
            }
        }
        return builder
    }

    fun haveSourceSets(vararg sourceSetNames: String): ModulesRuleBuilder = haveSourceSets(sourceSetNames.toList())

    fun containFiles(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.files.isEmpty()) {
                violations.add(getMessage("module.should.containFiles", module.path))
            }
        }
        return builder
    }

    fun beEmpty(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.files.isNotEmpty() || module.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.beEmpty", module.path))
            }
        }
        return builder
    }

    infix fun haveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.buildId != buildId) {
                violations.add(getMessage("module.should.haveBuildId", module.path, module.buildId, buildId))
            }
        }
        return builder
    }

    infix fun notHaveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.buildId == buildId) {
                violations.add(getMessage("module.should.notHaveBuildId", module.path, buildId))
            }
        }
        return builder
    }

    infix fun haveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(dirPattern, module.projectDir)) {
                violations.add(getMessage("module.should.matchProjectDir", module.path, module.projectDir, dirPattern))
            }
        }
        return builder
    }

    infix fun notHaveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(dirPattern, module.projectDir)) {
                violations.add(getMessage("module.should.matchProjectDir", module.path, module.projectDir, dirPattern))
            }
        }
        return builder
    }

    infix fun containClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }) {
                violations.add(getMessage("module.should.containClassesInPackagePattern", module.path, packagePattern))
            }
        }
        return builder
    }

    infix fun notContainClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }) {
                violations.add(
                    getMessage("module.should.notContainClassesInPackagePattern", module.path, packagePattern),
                )
            }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val matches =
                module.classes.any { cls ->
                    cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                }
            if (!matches) {
                violations.add(
                    getMessage("module.should.containClassesWithAnnotationFqName", module.path, annotationFqName),
                )
            }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val matches =
                module.classes.any { cls ->
                    cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                }
            if (matches) {
                violations.add(
                    getMessage("module.should.containClassesWithAnnotationFqName", module.path, annotationFqName),
                )
            }
        }
        return builder
    }

    infix fun containClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.classes.any { it.fqName == fqName || it.name == fqName }) {
                violations.add(getMessage("module.should.containClassFqName", module.path, fqName))
            }
        }
        return builder
    }

    infix fun notContainClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.any { it.fqName == fqName || it.name == fqName }) {
                violations.add(getMessage("module.should.notContainClassFqName", module.path, fqName))
            }
        }
        return builder
    }

    infix fun containClass(type: KClass<*>): ModulesRuleBuilder = containClass(type.kontureQualifiedName())

    infix fun notContainClass(type: KClass<*>): ModulesRuleBuilder = notContainClass(type.kontureQualifiedName())

    infix fun containClassesWithAnnotation(annotation: KClass<out Annotation>): ModulesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notContainClassesWithAnnotation(annotation: KClass<out Annotation>): ModulesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notHaveName(path: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(path)
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (module.path == normalized || simpleName == path) {
                violations.add(getMessage("module.should.notHaveName", module.path, path))
            }
        }
        return builder
    }

    infix fun notHaveName(paths: List<String>): ModulesRuleBuilder {
        val normalized = paths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (normalized.contains(module.path) || paths.contains(simpleName)) {
                violations.add(getMessage("module.should.notHaveNameAny", module.path, paths.joinToString()))
            }
        }
        return builder
    }

    fun notHaveName(vararg paths: String): ModulesRuleBuilder = notHaveName(paths.toList())

    infix fun notHaveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (simpleName.startsWith(prefix) || module.path.startsWith(prefix)) {
                violations.add(getMessage("module.should.notHaveNameStartingWith", module.path, prefix))
            }
        }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching = prefixes.filter { simpleName.startsWith(it) || module.path.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("module.should.notHaveNameStartingWithAny", module.path, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): ModulesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    infix fun notHaveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (simpleName.endsWith(suffix) || module.path.endsWith(suffix)) {
                violations.add(getMessage("module.should.notHaveNameEndingWith", module.path, suffix))
            }
        }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching = suffixes.filter { simpleName.endsWith(it) || module.path.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("module.should.notHaveNameEndingWithAny", module.path, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): ModulesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun notHaveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (PatternMatchers.matchesModuleGlob(pattern, module.path) || PatternMatchers.matchesSimpleGlob(pattern, simpleName)) {
                violations.add(getMessage("module.should.notHaveNameMatching", module.path, pattern))
            }
        }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching =
                patterns.filter {
                    PatternMatchers.matchesModuleGlob(it, module.path) || PatternMatchers.matchesSimpleGlob(it, simpleName)
                }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("module.should.notHaveNameMatchingAny", module.path, matching.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): ModulesRuleBuilder = notHaveNameMatching(patterns.toList())

    fun notCall(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (file in module.files) {
                val calls = file.usages.filter { PatternMatchers.isCallUsageMatch(it, fqName) }
                for (usage in calls) {
                    violations.add(
                        getMessage("module.should.notCall", module.path, file.name, fqName, usage.rawExpression),
                    )
                }
            }
        }
        return builder
    }

    fun notCall(kClass: KClass<*>): ModulesRuleBuilder = notCall(kClass.kontureQualifiedName())

    fun notReferenceClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (file in module.files) {
                val refs = file.usages.filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                for (usage in refs) {
                    violations.add(
                        getMessage("module.should.notReferenceClass", module.path, file.name, fqName),
                    )
                }
            }
        }
        return builder
    }

    fun notReferenceClass(kClass: KClass<*>): ModulesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())
}

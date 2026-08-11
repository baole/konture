/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation
import kotlin.reflect.KClass

@Suppress("ComplexInterface")
interface PropertiesShouldTypeAssertions {
    val builder: PropertiesRuleBuilder

    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    getMessage("property.should.resideInPackage", prop.qualifiedName, packagePattern, prop.packageName),
                )
            }
        }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, prop.packageName) }
            if (!matches) {
                violations.add(
                    getMessage(
                        "property.should.resideInPackageAny",
                        prop.qualifiedName,
                        packagePatterns.joinToString(),
                        prop.packageName,
                    ),
                )
            }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!predicate(prop.packageName)) {
                violations.add(
                    getMessage("property.should.resideInPackageMatching", prop.qualifiedName, prop.packageName),
                )
            }
        }
        return builder
    }

    infix fun resideInPackage(packagePattern: String): PropertiesRuleBuilder = resideInAPackage(packagePattern)

    infix fun resideInPackage(packagePatterns: List<String>): PropertiesRuleBuilder = resideInAPackage(packagePatterns)

    fun resideInPackage(vararg packagePatterns: String): PropertiesRuleBuilder = resideInAPackage(*packagePatterns)

    infix fun resideInPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder = resideInAPackage(predicate)

    infix fun notResideInPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    getMessage("property.should.notResideInPackage", prop.qualifiedName, packagePattern),
                )
            }
        }
        return builder
    }

    infix fun notResideInPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (packagePatterns.any { PatternMatchers.matchesPackage(it, prop.packageName) }) {
                violations.add(
                    getMessage(
                        "property.should.notResideInPackageAny",
                        prop.qualifiedName,
                        packagePatterns.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    fun notResideInPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInPackage(
            packagePatterns.toList(),
        )

    infix fun notResideInAPackage(packagePattern: String): PropertiesRuleBuilder = notResideInPackage(packagePattern)

    infix fun notResideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder =
        notResideInPackage(
            packagePatterns,
        )

    fun notResideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInPackage(
            *packagePatterns,
        )

    infix fun resideInModule(modulePath: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.modulePath != modulePath && !PatternMatchers.matchesModuleGlob(modulePath, prop.modulePath)) {
                violations.add(
                    getMessage("property.should.resideInModule", prop.qualifiedName, modulePath, prop.modulePath),
                )
            }
        }
        return builder
    }

    infix fun resideInModule(modulePaths: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (modulePaths.none { prop.modulePath == it || PatternMatchers.matchesModuleGlob(it, prop.modulePath) }) {
                violations.add(
                    getMessage("property.should.resideInModuleAny", prop.qualifiedName, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    fun resideInModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(modulePaths.toList())

    infix fun resideInModules(modulePaths: List<String>): PropertiesRuleBuilder = resideInModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(*modulePaths)

    infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder = resideInModule(modulePath)

    infix fun resideInAModule(modulePaths: List<String>): PropertiesRuleBuilder = resideInModule(modulePaths)

    fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(*modulePaths)

    infix fun notResideInModule(modulePath: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.modulePath == modulePath || PatternMatchers.matchesModuleGlob(modulePath, prop.modulePath)) {
                violations.add(
                    getMessage("property.should.notResideInModule", prop.qualifiedName, modulePath),
                )
            }
        }
        return builder
    }

    infix fun notResideInModule(modulePaths: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (modulePaths.any { prop.modulePath == it || PatternMatchers.matchesModuleGlob(it, prop.modulePath) }) {
                violations.add(
                    getMessage("property.should.notResideInModuleAny", prop.qualifiedName, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    fun notResideInModule(vararg modulePaths: String): PropertiesRuleBuilder = notResideInModule(modulePaths.toList())

    infix fun notResideInModules(modulePaths: List<String>): PropertiesRuleBuilder = notResideInModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): PropertiesRuleBuilder = notResideInModule(*modulePaths)

    infix fun notResideInAModule(modulePath: String): PropertiesRuleBuilder = notResideInModule(modulePath)

    infix fun notResideInAModule(modulePaths: List<String>): PropertiesRuleBuilder = notResideInModule(modulePaths)

    fun notResideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = notResideInModule(*modulePaths)

    infix fun haveName(name: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name != name) {
                violations.add(
                    getMessage("property.should.haveName", prop.qualifiedName, name),
                )
            }
        }
        return builder
    }

    infix fun haveName(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!names.contains(prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameAny", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveName(vararg names: String): PropertiesRuleBuilder = haveName(names.toList())

    infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!predicate(prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameMatching", prop.qualifiedName, prop.declaration.name),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("property.should.haveNameEndingWith", prop.qualifiedName, suffix),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameEndingWithAny", prop.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("property.should.haveNameStartingWith", prop.qualifiedName, prefix),
                )
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = prefixes.any { prop.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameStartingWithAny", prop.qualifiedName, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameMatching", prop.qualifiedName, pattern),
                )
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameMatchingAny", prop.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun notHaveName(name: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name == name) {
                violations.add(getMessage("property.should.notHaveName", prop.qualifiedName, name))
            }
        }
        return builder
    }

    infix fun notHaveName(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (names.contains(prop.declaration.name)) {
                violations.add(getMessage("property.should.notHaveNameIn", prop.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    fun notHaveName(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveNameIn(names: List<String>): PropertiesRuleBuilder = notHaveName(names)

    fun notHaveNameIn(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add(getMessage("property.should.notHaveNameMatching", prop.qualifiedName, pattern))
            }
        }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (matches) {
                violations.add(
                    getMessage("property.should.notHaveNameMatchingAny", prop.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): PropertiesRuleBuilder = notHaveNameMatching(patterns.toList())

    infix fun notHaveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("property.should.notHaveNameStartingWith", prop.qualifiedName, prefix))
            }
        }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = prefixes.any { prop.declaration.name.startsWith(it) }
            if (matches) {
                violations.add(
                    getMessage(
                        "property.should.notHaveNameStartingWithAny",
                        prop.qualifiedName,
                        prefixes.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    infix fun notHaveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("property.should.notHaveNameEndingWith", prop.qualifiedName, suffix))
            }
        }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (matches) {
                violations.add(
                    getMessage("property.should.notHaveNameEndingWithAny", prop.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val actualType = prop.declaration.type
            val matches = actualType == typeFqName || actualType.endsWith(".$typeFqName") || typeFqName.endsWith(".$actualType")
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveType", prop.qualifiedName, typeFqName, actualType),
                )
            }
        }
        return builder
    }

    infix fun haveType(type: KClass<*>): PropertiesRuleBuilder = haveType(type.kontureQualifiedName())

    infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder = haveTypeIn(typeFqNames)

    fun haveType(vararg typeFqNames: String): PropertiesRuleBuilder = haveTypeIn(typeFqNames.toList())

    infix fun haveTypeIn(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val actualType = prop.declaration.type
            val matches =
                typeFqNames.any {
                        fq ->
                    actualType == fq || actualType.endsWith(".$fq") || fq.endsWith(".$actualType")
                }
            if (!matches) {
                violations.add(
                    getMessage(
                        "property.should.haveTypeAny",
                        prop.qualifiedName,
                        typeFqNames.joinToString(),
                        actualType,
                    ),
                )
            }
        }
        return builder
    }

    fun haveTypeIn(vararg typeFqNames: String): PropertiesRuleBuilder = haveTypeIn(typeFqNames.toList())

    infix fun haveAnnotationOf(annotationFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val isAnnotated =
                prop.declaration.annotations.any {
                    it.name == annotationFqName || it.fqName == annotationFqName
                }
            if (!isAnnotated) {
                violations.add(
                    getMessage("property.should.haveAnnotation", prop.qualifiedName, annotationFqName),
                )
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    infix fun haveAnnotationOf(annotationNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val present =
                prop.declaration.annotations.map { it.name }.toSet() +
                    prop.declaration.annotations.map { it.fqName }.toSet()
            if (annotationNames.none { it in present }) {
                violations.add(
                    getMessage("property.should.haveAnnotationAny", prop.qualifiedName, annotationNames.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder =
        haveAnnotationOf(
            annotationNames.toList(),
        )

    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String? = null,
        argValue: String,
    ): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches =
                prop.declaration.annotations.any { ann ->
                    (ann.name == annotationName || ann.fqName == annotationName) &&
                        ann.arguments.any { arg ->
                            (argName == null || arg.name == argName) && arg.value == argValue
                        }
                }

            if (!matches) {
                violations.add(
                    getMessage(
                        "property.should.haveAnnotationWithArg",
                        prop.qualifiedName,
                        annotationName,
                        argName ?: "",
                        argValue,
                    ),
                )
            }
        }
        return builder
    }

    infix fun haveAllAnnotationsOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val present =
                prop.declaration.annotations.map { it.name }.toSet() +
                    prop.declaration.annotations.map { it.fqName }.toSet()
            if (!names.all { it in present }) {
                violations.add(
                    getMessage("property.should.haveAllAnnotations", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAllAnnotationsOf(vararg names: String): PropertiesRuleBuilder = haveAllAnnotationsOf(names.asList())

    infix fun haveAnyAnnotationOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val present =
                prop.declaration.annotations.map { it.name }.toSet() +
                    prop.declaration.annotations.map { it.fqName }.toSet()
            if (names.none { it in present }) {
                violations.add(
                    getMessage("property.should.haveAnyAnnotation", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAnyAnnotationOf(vararg names: String): PropertiesRuleBuilder = haveAnyAnnotationOf(names.asList())

    fun beTopLevel(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.className != null) {
                violations.add(
                    getMessage("property.should.beTopLevel", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beMember(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.className == null) {
                violations.add(
                    getMessage("property.should.beMember", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("property.should.beDocumented", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun notCall(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            val propUsages =
                fileUsages.filter { usage ->
                    usage.isEnclosedInProperty(
                        propertyName = prop.declaration.name,
                        classFqName = if (prop.className != null) prop.qualifiedName.substringBeforeLast(".") else null,
                        className = prop.className,
                    )
                }

            propUsages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        "${getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.format(prop.filePath, usage.line, usage.column, prop.modulePath, prop.sourceSet?.name, fqName = prop.className?.let { "${prop.packageName}.$it" } ?: prop.packageName, packageName = prop.packageName)})",
                    )
                }
        }
        return builder
    }

    fun notCall(kClass: KClass<*>): PropertiesRuleBuilder = notCall(kClass.kontureQualifiedName())

    fun notReferenceClass(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            val propUsages =
                fileUsages.filter { usage ->
                    usage.isEnclosedInProperty(
                        propertyName = prop.declaration.name,
                        classFqName = if (prop.className != null) prop.qualifiedName.substringBeforeLast(".") else null,
                        className = prop.className,
                    )
                }

            propUsages
                .filter { usage ->
                    usage.kind == UsageKind.CLASS_REFERENCE &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName.endsWith("." + usage.targetFqName) || usage.rawExpression == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    violations.add(
                        "${getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.format(prop.filePath, usage.line, usage.column, prop.modulePath, prop.sourceSet?.name, fqName = prop.className?.let { "${prop.packageName}.$it" } ?: prop.packageName, packageName = prop.packageName)})",
                    )
                }
        }
        return builder
    }

    fun notReferenceClass(kClass: KClass<*>): PropertiesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    infix fun haveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            val matches = imports.any { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
            if (!matches) {
                violations.add(getMessage("property.should.importFile", prop.qualifiedName, importFqName))
            }
        }
        return builder
    }

    infix fun haveImportOf(type: KClass<*>): PropertiesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    infix fun notHaveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            val matches = imports.any { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
            if (matches) {
                violations.add(getMessage("property.should.notImportFile", prop.qualifiedName, importFqName))
            }
        }
        return builder
    }

    infix fun notHaveImportOf(type: KClass<*>): PropertiesRuleBuilder = notHaveImportOf(type.kontureQualifiedName())

    fun haveNoWildcardImports(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            val wildcards = imports.filter { it.endsWith(".*") }
            if (wildcards.isNotEmpty()) {
                violations.add(
                    getMessage("property.should.notContainWildcardImports", prop.qualifiedName, wildcards.toString()),
                )
            }
        }
        return builder
    }
}

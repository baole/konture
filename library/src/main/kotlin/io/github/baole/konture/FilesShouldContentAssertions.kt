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

interface FilesShouldContentAssertions {
    val builder: FilesRuleBuilder

    fun containClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isEmpty()) {
                violations.add(getMessage("files.rule.containClasses", file.declaration.name))
            }
        }
        return builder
    }

    fun haveClasses(): FilesRuleBuilder = containClasses()

    fun notContainClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.notContainClasses", file.declaration.name))
            }
        }
        return builder
    }

    fun notHaveClasses(): FilesRuleBuilder = notContainClasses()

    fun haveOnlyOneClassPerFile(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.size > 1) {
                violations.add(
                    getMessage(
                        "file.should.containAtMostOneClass",
                        file.declaration.name,
                        file.declaration.classes.size,
                        file.declaration.classes.joinToString { it.name },
                    ),
                )
            }
        }
        return builder
    }

    fun haveNameMatchingClassName(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val fileNameWithoutExt = file.declaration.name.substringBeforeLast(".")
            val matches = file.declaration.classes.any { it.name == fileNameWithoutExt }
            if (!matches) {
                violations.add(getMessage("file.should.matchClassName", file.declaration.name))
            }
        }
        return builder
    }

    fun haveNoWildcardImports(): FilesRuleBuilder = notContainWildcardImports()

    fun notHaveWildcardImports(): FilesRuleBuilder = notContainWildcardImports()

    fun haveTopLevelFunctions(): FilesRuleBuilder = containTopLevelFunctions()

    fun notHaveTopLevelFunctions(): FilesRuleBuilder = notContainTopLevelFunctions()

    fun haveTopLevelProperties(): FilesRuleBuilder = containTopLevelProperties()

    fun notHaveTopLevelProperties(): FilesRuleBuilder = notContainTopLevelProperties()

    fun notCall(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        "${getMessage(
                            "usage.notCall",
                            unresolved,
                            fqName,
                            usage.rawExpression,
                            usage.line,
                            usage.column,
                        )} (at ${ViolationLocation.format(
                            filePath = usage.filePath,
                            line = usage.line,
                            column = usage.column,
                            modulePath = file.modulePath,
                            sourceSetName = file.sourceSet?.name,
                            packageName = file.declaration.packageName,
                        )})",
                    )
                }
        }
        return builder
    }

    fun notCall(kClass: KClass<*>): FilesRuleBuilder = notCall(kClass.kontureQualifiedName())

    fun notReferenceClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                .forEach { usage ->
                    violations.add(
                        "${getMessage(
                            "usage.notReferenceClass",
                            fqName,
                            usage.rawExpression,
                            usage.line,
                            usage.column,
                        )} (at ${ViolationLocation.format(
                            filePath = usage.filePath,
                            line = usage.line,
                            column = usage.column,
                            modulePath = file.modulePath,
                            sourceSetName = file.sourceSet?.name,
                            packageName = file.declaration.packageName,
                        )})",
                    )
                }
        }
        return builder
    }

    fun notReferenceClass(kClass: KClass<*>): FilesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    infix fun notContainClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (contains) {
                violations.add(getMessage("file.should.notContainClass", file.declaration.name, fqName))
            }
        }
        return builder
    }

    infix fun notContainClass(kClass: KClass<*>): FilesRuleBuilder = notContainClass(kClass.kontureQualifiedName())

    infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (!contains) {
                violations.add(getMessage("file.should.containClasses", file.declaration.name, fqName))
            }
        }
        return builder
    }

    infix fun containClass(kClass: KClass<*>): FilesRuleBuilder = containClass(kClass.kontureQualifiedName())

    infix fun haveAllClassesEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val offending = file.declaration.classes.filterNot { it.name.endsWith(suffix) }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "file.should.haveAllClassesEndingWith",
                        file.declaration.name,
                        suffix,
                        offending.joinToString { it.name },
                    ),
                )
            }
        }
        return builder
    }

    infix fun haveAllClassesStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val offending = file.declaration.classes.filterNot { it.name.startsWith(prefix) }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "file.should.haveAllClassesStartingWith",
                        file.declaration.name,
                        prefix,
                        offending.joinToString { it.name },
                    ),
                )
            }
        }
        return builder
    }

    infix fun haveAllClassesMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val offending = file.declaration.classes.filterNot { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "file.should.haveAllClassesMatching",
                        file.declaration.name,
                        pattern,
                        offending.joinToString { it.name },
                    ),
                )
            }
        }
        return builder
    }

    infix fun haveImportOf(importFqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches =
                file.declaration.imports.any {
                    it == importFqName || PatternMatchers.matchesPackage(importFqName, it)
                }
            if (!matches) {
                violations.add(getMessage("file.should.haveImports", file.declaration.name, importFqName))
            }
        }
        return builder
    }

    infix fun haveImportOf(kClass: KClass<*>): FilesRuleBuilder = haveImportOf(kClass.kontureQualifiedName())

    infix fun notHaveImportOf(importFqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches =
                file.declaration.imports.any {
                    it == importFqName || PatternMatchers.matchesPackage(importFqName, it)
                }
            if (matches) {
                violations.add(getMessage("file.should.notHaveImport", file.declaration.name, importFqName))
            }
        }
        return builder
    }

    infix fun notHaveImportOf(kClass: KClass<*>): FilesRuleBuilder = notHaveImportOf(kClass.kontureQualifiedName())

    fun notContainWildcardImports(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val wildcards = file.declaration.imports.filter { it.endsWith(".*") }
            if (wildcards.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "file.should.notContainWildcardImports",
                        file.declaration.name,
                        wildcards.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isEmpty()) {
                violations.add(getMessage("file.should.containTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    fun notContainTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isNotEmpty()) {
                violations.add(getMessage("file.should.notContainTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isEmpty()) {
                violations.add(getMessage("file.should.containTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasAnnotation =
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == annotationName || it.fqName == annotationName }
                }
            if (!hasAnnotation) {
                violations.add(getMessage("file.should.haveAnnotationOf", file.declaration.name, annotationName))
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FilesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    fun notContainTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isNotEmpty()) {
                violations.add(getMessage("file.should.notContainTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.kdocText.isNullOrBlank()) {
                violations.add(getMessage("file.should.beDocumented", file.declaration.name))
            }
        }
        return builder
    }

    infix fun containClass(fqNames: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val contains =
                fqNames.all { expected ->
                    file.declaration.classes.any { it.fqName == expected || it.name == expected }
                }
            if (!contains) {
                violations.add(getMessage("file.should.containClasses", file.declaration.name, fqNames.joinToString()))
            }
        }
        return builder
    }

    fun containClass(vararg fqNames: String): FilesRuleBuilder = containClass(fqNames.toList())

    infix fun haveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasImports =
                imports.all { imp ->
                    file.declaration.imports.any { PatternMatchers.matchesPackage(imp, it) || it == imp }
                }
            if (!hasImports) {
                violations.add(getMessage("file.should.haveImports", file.declaration.name, imports.joinToString()))
            }
        }
        return builder
    }

    fun haveImportOf(vararg imports: String): FilesRuleBuilder = haveImportOf(imports.toList())

    infix fun notHaveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasImports =
                imports.any { imp ->
                    file.declaration.imports.any { PatternMatchers.matchesPackage(imp, it) || it == imp }
                }
            if (hasImports) {
                violations.add(getMessage("file.should.notHaveImports", file.declaration.name, imports.joinToString()))
            }
        }
        return builder
    }

    fun notHaveImportOf(vararg imports: String): FilesRuleBuilder = notHaveImportOf(imports.toList())

    infix fun onlyDependOnPackages(packages: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val referencedPackages =
                file.declaration.imports.map { imp -> imp.substringBeforeLast(".") } +
                    file.declaration.usages.map { it.targetFqName.substringBeforeLast(".") }
            val offending =
                referencedPackages.filterNot { pkg ->
                    pkg == file.declaration.packageName ||
                        packages.any { PatternMatchers.matchesPackage(it, pkg) }
                }.distinct()
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("file.should.onlyDependOnPackages", file.declaration.name, offending.joinToString()),
                )
            }
        }
        return builder
    }

    fun onlyDependOnPackages(vararg packages: String): FilesRuleBuilder = onlyDependOnPackages(packages.toList())

    infix fun notDependOnPackages(packages: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val referencedPackages =
                file.declaration.imports.map { imp -> imp.substringBeforeLast(".") } +
                    file.declaration.usages.map { it.targetFqName.substringBeforeLast(".") }
            val offending =
                referencedPackages.filter { pkg ->
                    packages.any { PatternMatchers.matchesPackage(it, pkg) }
                }.distinct()
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("file.should.notDependOnPackages", file.declaration.name, offending.joinToString()),
                )
            }
        }
        return builder
    }

    fun notDependOnPackages(vararg packages: String): FilesRuleBuilder = notDependOnPackages(packages.toList())

    infix fun onlyDependOnModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val targetModules =
                file.declaration.usages.mapNotNull { usage ->
                    val fqName = usage.targetFqName
                    builder.graph.getAllModules().find { module ->
                        module.files.any { f -> f.classes.any { it.fqName == fqName } }
                    }?.path
                }.distinct()
            val offending =
                targetModules.filterNot { m ->
                    m == file.modulePath || modulePaths.any { PatternMatchers.matchesModuleGlob(it, m) || it == m }
                }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("file.should.onlyDependOnModules", file.declaration.name, offending.joinToString()),
                )
            }
        }
        return builder
    }

    fun onlyDependOnModules(vararg modulePaths: String): FilesRuleBuilder = onlyDependOnModules(modulePaths.toList())

    infix fun notDependOnModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val targetModules =
                file.declaration.usages.mapNotNull { usage ->
                    val fqName = usage.targetFqName
                    builder.graph.getAllModules().find { module ->
                        module.files.any { f -> f.classes.any { it.fqName == fqName } }
                    }?.path
                }.distinct()

            val offending =
                targetModules.filter { m ->
                    modulePaths.any { PatternMatchers.matchesModuleGlob(it, m) || it == m }
                }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("file.should.notDependOnModules", file.declaration.name, offending.joinToString()),
                )
            }
        }
        return builder
    }

    fun notDependOnModules(vararg modulePaths: String): FilesRuleBuilder = notDependOnModules(modulePaths.toList())
}

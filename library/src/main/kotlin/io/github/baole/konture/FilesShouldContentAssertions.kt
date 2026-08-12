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

/** Content-based assertions for file rules. */
public interface FilesShouldContentAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FilesRuleBuilder

    /** Filter or assertion criteria for contain classes. */
    public fun containClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isEmpty()) {
                violations.add(getMessage("files.rule.containClasses", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have classes. */
    public fun haveClasses(): FilesRuleBuilder = containClasses()

    /** Filter or assertion criteria for not contain classes. */
    public fun notContainClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.notContainClasses", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have classes. */
    public fun notHaveClasses(): FilesRuleBuilder = notContainClasses()

    /** Filter or assertion criteria for have only one class per file. */
    public fun haveOnlyOneClassPerFile(): FilesRuleBuilder {
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

    /** Filter or assertion criteria for have name matching class name. */
    public fun haveNameMatchingClassName(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for file name without ext. */
            val fileNameWithoutExt = file.declaration.name.substringBeforeLast(".")

            /** Filter or assertion criteria for matches. */
            val matches = file.declaration.classes.any { it.name == fileNameWithoutExt }
            if (!matches) {
                violations.add(getMessage("file.should.matchClassName", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have no wildcard imports. */
    public fun haveNoWildcardImports(): FilesRuleBuilder = notContainWildcardImports()

    /** Filter or assertion criteria for not have wildcard imports. */
    public fun notHaveWildcardImports(): FilesRuleBuilder = notContainWildcardImports()

    /** Filter or assertion criteria for have top level functions. */
    public fun haveTopLevelFunctions(): FilesRuleBuilder = containTopLevelFunctions()

    /** Filter or assertion criteria for not have top level functions. */
    public fun notHaveTopLevelFunctions(): FilesRuleBuilder = notContainTopLevelFunctions()

    /** Filter or assertion criteria for have top level properties. */
    public fun haveTopLevelProperties(): FilesRuleBuilder = containTopLevelProperties()

    /** Filter or assertion criteria for not have top level properties. */
    public fun notHaveTopLevelProperties(): FilesRuleBuilder = notContainTopLevelProperties()

    /** Filter or assertion criteria for not call. */
    public fun notCall(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    /** Filter or assertion criteria for unresolved. */
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

    /** Filter or assertion criteria for not call. */
    public fun notCall(kClass: KClass<*>): FilesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for not reference class. */
    public fun notReferenceClass(fqName: String): FilesRuleBuilder {
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

    /** Filter or assertion criteria for not reference class. */
    public fun notReferenceClass(kClass: KClass<*>): FilesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for contains. */
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (contains) {
                violations.add(getMessage("file.should.notContainClass", file.declaration.name, fqName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(kClass: KClass<*>): FilesRuleBuilder =
        notContainClass(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for contains. */
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (!contains) {
                violations.add(getMessage("file.should.containClasses", file.declaration.name, fqName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(kClass: KClass<*>): FilesRuleBuilder = containClass(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for have all classes ending with. */
    public infix fun haveAllClassesEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for have all classes starting with. */
    public infix fun haveAllClassesStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for have all classes matching. */
    public infix fun haveAllClassesMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(importFqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(kClass: KClass<*>): FilesRuleBuilder = haveImportOf(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(importFqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(kClass: KClass<*>): FilesRuleBuilder =
        notHaveImportOf(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for not contain wildcard imports. */
    public fun notContainWildcardImports(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for wildcards. */
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

    /** Filter or assertion criteria for contain top level functions. */
    public fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isEmpty()) {
                violations.add(getMessage("file.should.containTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain top level functions. */
    public fun notContainTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isNotEmpty()) {
                violations.add(getMessage("file.should.notContainTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain top level properties. */
    public fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isEmpty()) {
                violations.add(getMessage("file.should.containTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for has annotation. */
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

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FilesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for not contain top level properties. */
    public fun notContainTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isNotEmpty()) {
                violations.add(getMessage("file.should.notContainTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    public fun beDocumentedWithKDoc(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.kdocText.isNullOrBlank()) {
                violations.add(getMessage("file.should.beDocumented", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqNames: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for contains. */
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

    /** Filter or assertion criteria for contain class. */
    public fun containClass(vararg fqNames: String): FilesRuleBuilder = containClass(fqNames.toList())

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for has imports. */
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

    /** Filter or assertion criteria for have import of. */
    public fun haveImportOf(vararg imports: String): FilesRuleBuilder = haveImportOf(imports.toList())

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for has imports. */
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

    /** Filter or assertion criteria for not have import of. */
    public fun notHaveImportOf(vararg imports: String): FilesRuleBuilder = notHaveImportOf(imports.toList())

    /** Filter or assertion criteria for only depend on packages. */
    public infix fun onlyDependOnPackages(packages: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for referenced packages. */
            val referencedPackages =
                file.declaration.imports.map { imp -> imp.substringBeforeLast(".") } +
                    file.declaration.usages.map { it.targetFqName.substringBeforeLast(".") }

            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for only depend on packages. */
    public fun onlyDependOnPackages(vararg packages: String): FilesRuleBuilder = onlyDependOnPackages(packages.toList())

    /** Filter or assertion criteria for not depend on packages. */
    public infix fun notDependOnPackages(packages: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for referenced packages. */
            val referencedPackages =
                file.declaration.imports.map { imp -> imp.substringBeforeLast(".") } +
                    file.declaration.usages.map { it.targetFqName.substringBeforeLast(".") }

            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for not depend on packages. */
    public fun notDependOnPackages(vararg packages: String): FilesRuleBuilder = notDependOnPackages(packages.toList())

    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for target modules. */
            val targetModules =
                file.declaration.usages.mapNotNull { usage ->
                    /** Filter or assertion criteria for fq name. */
                    val fqName = usage.targetFqName
                    builder.graph.getAllModules().find { module ->
                        module.files.any { f -> f.classes.any { it.fqName == fqName } }
                    }?.path
                }.distinct()

            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for only depend on modules. */
    public fun onlyDependOnModules(vararg modulePaths: String): FilesRuleBuilder =
        onlyDependOnModules(modulePaths.toList())

    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(modulePaths: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            /** Filter or assertion criteria for target modules. */
            val targetModules =
                file.declaration.usages.mapNotNull { usage ->
                    /** Filter or assertion criteria for fq name. */
                    val fqName = usage.targetFqName
                    builder.graph.getAllModules().find { module ->
                        module.files.any { f -> f.classes.any { it.fqName == fqName } }
                    }?.path
                }.distinct()

            /** Filter or assertion criteria for offending. */
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

    /** Filter or assertion criteria for not depend on modules. */
    public fun notDependOnModules(vararg modulePaths: String): FilesRuleBuilder =
        notDependOnModules(modulePaths.toList())
}

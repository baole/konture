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

/** Type and package assertions for property rules. */
@Suppress("ComplexInterface", "LargeClass")
public interface PropertiesShouldTypeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: PropertiesRuleBuilder

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    getMessage("property.should.resideInPackage", prop.qualifiedName, packagePattern, prop.packageName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!predicate(prop.packageName)) {
                violations.add(
                    getMessage("property.should.resideInPackageMatching", prop.qualifiedName, prop.packageName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in package. */
    public infix fun resideInPackage(packagePattern: String): PropertiesRuleBuilder = resideInAPackage(packagePattern)

    /** Filter or assertion criteria for reside in package. */
    public infix fun resideInPackage(packagePatterns: List<String>): PropertiesRuleBuilder =
        resideInAPackage(packagePatterns)

    /** Filter or assertion criteria for reside in package. */
    public fun resideInPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(*packagePatterns)

    /** Filter or assertion criteria for reside in package. */
    public infix fun resideInPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder =
        resideInAPackage(predicate)

    /** Filter or assertion criteria for not reside in package. */
    public infix fun notResideInPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    getMessage("property.should.notResideInPackage", prop.qualifiedName, packagePattern),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in package. */
    public infix fun notResideInPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
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

    /** Filter or assertion criteria for not reside in package. */
    public fun notResideInPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePattern: String): PropertiesRuleBuilder =
        notResideInPackage(packagePattern)

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder =
        notResideInPackage(
            packagePatterns,
        )

    /** Filter or assertion criteria for not reside in a package. */
    public fun notResideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInPackage(
            *packagePatterns,
        )

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.modulePath != modulePath && !PatternMatchers.matchesModuleGlob(modulePath, prop.modulePath)) {
                violations.add(
                    getMessage("property.should.resideInModule", prop.qualifiedName, modulePath, prop.modulePath),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePaths: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (modulePaths.none { prop.modulePath == it || PatternMatchers.matchesModuleGlob(it, prop.modulePath) }) {
                violations.add(
                    getMessage("property.should.resideInModuleAny", prop.qualifiedName, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in module. */
    public fun resideInModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(modulePaths.toList())

    /** Filter or assertion criteria for reside in modules. */
    public infix fun resideInModules(modulePaths: List<String>): PropertiesRuleBuilder = resideInModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(*modulePaths)

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder = resideInModule(modulePath)

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePaths: List<String>): PropertiesRuleBuilder = resideInModule(modulePaths)

    /** Filter or assertion criteria for reside in a module. */
    public fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInModule(*modulePaths)

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.modulePath == modulePath || PatternMatchers.matchesModuleGlob(modulePath, prop.modulePath)) {
                violations.add(
                    getMessage("property.should.notResideInModule", prop.qualifiedName, modulePath),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePaths: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (modulePaths.any { prop.modulePath == it || PatternMatchers.matchesModuleGlob(it, prop.modulePath) }) {
                violations.add(
                    getMessage("property.should.notResideInModuleAny", prop.qualifiedName, modulePaths.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in module. */
    public fun notResideInModule(vararg modulePaths: String): PropertiesRuleBuilder =
        notResideInModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in modules. */
    public infix fun notResideInModules(modulePaths: List<String>): PropertiesRuleBuilder =
        notResideInModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): PropertiesRuleBuilder = notResideInModule(*modulePaths)

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): PropertiesRuleBuilder = notResideInModule(modulePath)

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePaths: List<String>): PropertiesRuleBuilder =
        notResideInModule(modulePaths)

    /** Filter or assertion criteria for not reside in a module. */
    public fun notResideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = notResideInModule(*modulePaths)

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(name: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name != name) {
                violations.add(
                    getMessage("property.should.haveName", prop.qualifiedName, name),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!names.contains(prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameAny", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): PropertiesRuleBuilder = haveName(names.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!predicate(prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameMatching", prop.qualifiedName, prop.declaration.name),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("property.should.haveNameEndingWith", prop.qualifiedName, suffix),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameEndingWithAny", prop.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder =
        haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("property.should.haveNameStartingWith", prop.qualifiedName, prefix),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = prefixes.any { prop.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameStartingWithAny", prop.qualifiedName, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameMatching", prop.qualifiedName, pattern),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameMatchingAny", prop.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(name: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name == name) {
                violations.add(getMessage("property.should.notHaveName", prop.qualifiedName, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (names.contains(prop.declaration.name)) {
                violations.add(getMessage("property.should.notHaveNameIn", prop.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name in. */
    public infix fun notHaveNameIn(names: List<String>): PropertiesRuleBuilder = notHaveName(names)

    /** Filter or assertion criteria for not have name in. */
    public fun notHaveNameIn(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add(getMessage("property.should.notHaveNameMatching", prop.qualifiedName, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (matches) {
                violations.add(
                    getMessage("property.should.notHaveNameMatchingAny", prop.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notHaveNameMatching(vararg patterns: String): PropertiesRuleBuilder =
        notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("property.should.notHaveNameStartingWith", prop.qualifiedName, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for not have name starting with. */
    public fun notHaveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("property.should.notHaveNameEndingWith", prop.qualifiedName, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (matches) {
                violations.add(
                    getMessage("property.should.notHaveNameEndingWithAny", prop.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notHaveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have type. */
    public infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for actual type. */
            val actualType = prop.declaration.type

            /** Filter or assertion criteria for matches. */
            val matches = actualType == typeFqName || actualType.endsWith(".$typeFqName") || typeFqName.endsWith(".$actualType")
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveType", prop.qualifiedName, typeFqName, actualType),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have type. */
    public infix fun haveType(type: KClass<*>): PropertiesRuleBuilder = haveType(type.kontureQualifiedName())

    /** Filter or assertion criteria for have type. */
    public infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder = haveTypeIn(typeFqNames)

    /** Filter or assertion criteria for have type. */
    public fun haveType(vararg typeFqNames: String): PropertiesRuleBuilder = haveTypeIn(typeFqNames.toList())

    /** Filter or assertion criteria for have type in. */
    public infix fun haveTypeIn(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for actual type. */
            val actualType = prop.declaration.type

            /** Filter or assertion criteria for matches. */
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

    /** Filter or assertion criteria for have type in. */
    public fun haveTypeIn(vararg typeFqNames: String): PropertiesRuleBuilder = haveTypeIn(typeFqNames.toList())

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for is annotated. */
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

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for present. */
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

    /** Filter or assertion criteria for have annotation of. */
    public fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder =
        haveAnnotationOf(
            annotationNames.toList(),
        )

    /** Filter or assertion criteria for have annotation with argument. */
    public fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String? = null,
        argValue: String,
    ): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches =
                prop.declaration.annotations.any { ann ->
                    (ann.name == annotationName || ann.fqName == annotationName) &&
                        ann.arguments.any { arg ->
                            (argName == null || arg.name == argName) &&
                                (
                                    arg.value == argValue ||
                                        arg.value.removeSurrounding("\"") == argValue ||
                                        arg.value.removeSurrounding("'") == argValue
                                )
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

    /** Filter or assertion criteria for have all annotations of. */
    public infix fun haveAllAnnotationsOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for present. */
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

    /** Filter or assertion criteria for have all annotations of. */
    public fun haveAllAnnotationsOf(vararg names: String): PropertiesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Filter or assertion criteria for have any annotation of. */
    public infix fun haveAnyAnnotationOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for present. */
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

    /** Filter or assertion criteria for have any annotation of. */
    public fun haveAnyAnnotationOf(vararg names: String): PropertiesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filter or assertion criteria for be top level. */
    public fun beTopLevel(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.className != null) {
                violations.add(
                    getMessage("property.should.beTopLevel", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be member. */
    public fun beMember(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.className == null) {
                violations.add(
                    getMessage("property.should.beMember", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    public fun beDocumentedWithKDoc(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("property.should.beDocumented", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not call. */
    public fun notCall(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for file usages. */
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            /** Filter or assertion criteria for prop usages. */
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
                    /** Filter or assertion criteria for unresolved. */
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.addViolationMessage(
                        message =
                            "${getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column)} " +
                                "(at ${ViolationLocation.format(prop.filePath, usage.line, usage.column, prop.modulePath, prop.sourceSet?.name, fqName = prop.className?.let { "${prop.packageName}.$it" } ?: prop.packageName, packageName = prop.packageName)})",
                        sourceLocation = toSourceLocation(usage.filePath, usage.line, usage.column),
                    )
                }
        }
        return builder
    }

    /** Filter or assertion criteria for not call. */
    public fun notCall(kClass: KClass<*>): PropertiesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for not reference class. */
    public fun notReferenceClass(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for file usages. */
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            /** Filter or assertion criteria for prop usages. */
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
                    violations.addViolationMessage(
                        message =
                            "${getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column)} " +
                                "(at ${ViolationLocation.format(prop.filePath, usage.line, usage.column, prop.modulePath, prop.sourceSet?.name, fqName = prop.className?.let { "${prop.packageName}.$it" } ?: prop.packageName, packageName = prop.packageName)})",
                        sourceLocation = toSourceLocation(usage.filePath, usage.line, usage.column),
                    )
                }
        }
        return builder
    }

    /** Filter or assertion criteria for not reference class. */
    public fun notReferenceClass(kClass: KClass<*>): PropertiesRuleBuilder =
        notReferenceClass(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for imports. */
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()

            /** Filter or assertion criteria for matches. */
            val matches = imports.any { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
            if (!matches) {
                violations.add(getMessage("property.should.importFile", prop.qualifiedName, importFqName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(type: KClass<*>): PropertiesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for imports. */
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()

            /** Filter or assertion criteria for matches. */
            val matches = imports.any { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
            if (matches) {
                violations.add(getMessage("property.should.notImportFile", prop.qualifiedName, importFqName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(type: KClass<*>): PropertiesRuleBuilder =
        notHaveImportOf(type.kontureQualifiedName())

    /** Filter or assertion criteria for have no wildcard imports. */
    public fun haveNoWildcardImports(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for imports. */
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find { file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()

            /** Filter or assertion criteria for wildcards. */
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

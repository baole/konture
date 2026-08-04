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

@KontureDsl
class PropertiesShould internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
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
                    getMessage("property.should.resideInPackageAny", prop.qualifiedName, packagePatterns.joinToString(), prop.packageName),
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

    fun bePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PUBLIC) {
                violations.add(
                    getMessage("property.should.bePublic", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.INTERNAL) {
                violations.add(
                    getMessage("property.should.beInternal", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun bePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PRIVATE) {
                violations.add(
                    getMessage("property.should.bePrivate", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PROTECTED) {
                violations.add(
                    getMessage("property.should.beProtected", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beVar(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isVar) {
                violations.add(
                    getMessage("property.should.beVar", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beVal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isVar) {
                violations.add(
                    getMessage("property.should.beVal", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.type != typeFqName) {
                violations.add(
                    getMessage("property.should.haveType", prop.qualifiedName, typeFqName, prop.declaration.type),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties have the specified raw type. */
    infix fun haveType(type: KClass<*>): PropertiesRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setShould { property, _, violations ->
            if (property.declaration.resolvedType?.let { matchesKotlinType(it, expectedType) } != true) {
                violations.add(
                    getMessage("property.should.haveType", property.declaration.name, type.kontureQualifiedName(), property.declaration.type),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties have the specified raw type. */
    inline fun <reified T : Any> haveTypeOf(): PropertiesRuleBuilder = haveType(T::class)

    infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!typeFqNames.contains(prop.declaration.type)) {
                violations.add(
                    getMessage("property.should.haveTypeAny", prop.qualifiedName, typeFqNames.joinToString(), prop.declaration.type),
                )
            }
        }
        return builder
    }

    fun haveType(vararg typeFqNames: String): PropertiesRuleBuilder = haveType(typeFqNames.asList())

    infix fun haveAnnotationOf(annotationName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val hasAnnotation =
                prop.declaration.annotations.any {
                    it.name == annotationName || it.fqName == annotationName
                }
            if (!hasAnnotation) {
                violations.add(
                    getMessage("property.should.haveAnnotation", prop.qualifiedName, annotationName),
                )
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotationNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val hasAnnotation =
                prop.declaration.annotations.any { ann ->
                    annotationNames.any { it == ann.name || it == ann.fqName }
                }
            if (!hasAnnotation) {
                violations.add(
                    getMessage("property.should.haveAnnotationAny", prop.qualifiedName, annotationNames.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder =
        haveAnnotationOf(
            annotationNames.asList(),
        )

    /**
     * Asserts that selected properties are annotated with all of the specified annotations.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.hasAllAnnotations(names)) {
                violations.add(
                    getMessage("property.should.haveAllAnnotations", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(vararg names: String): PropertiesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Asserts that selected properties are annotated with any of the specified annotations.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.hasAnyAnnotation(names)) {
                violations.add(
                    getMessage("property.should.haveAnyAnnotation", prop.qualifiedName, names.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(vararg names: String): PropertiesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Asserts that selected properties contain the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(modifier)) {
                violations.add(
                    getMessage("property.should.haveModifier", prop.qualifiedName, modifier),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val missing = modifiers.filter { !prop.declaration.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage("property.should.haveAllModifiers", prop.qualifiedName, modifiers.joinToString(), missing.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Asserts that selected properties have at least one of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!modifiers.any { prop.declaration.modifiers.contains(it) }) {
                violations.add(
                    getMessage("property.should.haveAnyModifier", prop.qualifiedName, modifiers.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have at least one of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Asserts that selected properties have the specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != visibility) {
                violations.add(
                    getMessage("property.should.haveVisibility", prop.qualifiedName, visibility, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!visibilities.contains(prop.declaration.visibility)) {
                violations.add(
                    getMessage("property.should.haveAnyVisibility", prop.qualifiedName, visibilities.joinToString(), prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder = haveAnyVisibility(visibilities.asList())

    fun beExtension(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isExtension) {
                violations.add(
                    getMessage("property.should.beExtension", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add(
                    getMessage("property.should.beConst", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add(
                    getMessage("property.should.beLateinit", prop.qualifiedName),
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

    infix fun satisfy(assertion: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder =
        satisfy("custom condition") { p, _ -> assertion(p) }

    private fun satisfy(
        description: String,
        assertion: (PropertyDeclarationContext, List<PropertyDeclarationContext>) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            if (!assertion(prop, allProps)) {
                violations.add(
                    getMessage("property.should.satisfyCustom", prop.qualifiedName, description),
                )
            }
        }
        return builder
    }

    fun satisfy(assertion: (PropertyDeclarationContext, MutableList<String>) -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations -> assertion(prop, violations) }
        return builder
    }

    /** Fails when the selected property initializer or delegate invokes [fqName]. */
    fun notCall(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            val propUsages =
                fileUsages.filter { usage ->
                    usage.enclosingProperty == prop.declaration.name &&
                        (
                            prop.className == null ||
                                usage.enclosingClass == prop.className ||
                                (usage.enclosingClass != null && prop.className != null && (usage.enclosingClass == prop.qualifiedName.substringBeforeLast(".") || usage.enclosingClass.endsWith(".${prop.className}")))
                        )
                }

            propUsages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        "${getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.of(prop.modulePath, prop.sourceSet?.name, prop.filePath, usage.line, usage.column)})",
                    )
                }
        }
        return builder
    }

    /** Fails when the selected property initializer or delegate invokes [kClass]. */
    fun notCall(kClass: KClass<*>): PropertiesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [fqName] in the selected property; imports alone do not match. */
    fun notReferenceClass(fqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val fileUsages =
                builder.graph.getAllModules()
                    .flatMap { it.files }
                    .find { file -> file.filePath == prop.filePath || file.classes.any { it.name == prop.className } }
                    ?.usages.orEmpty()

            val propUsages =
                fileUsages.filter { usage ->
                    usage.enclosingProperty == prop.declaration.name &&
                        (
                            prop.className == null ||
                                usage.enclosingClass == prop.className ||
                                (usage.enclosingClass != null && prop.className != null && (usage.enclosingClass == prop.qualifiedName.substringBeforeLast(".") || usage.enclosingClass.endsWith(".${prop.className}")))
                        )
                }

            propUsages
                .filter { usage ->
                    usage.kind == UsageKind.CLASS_REFERENCE &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName.endsWith("." + usage.targetFqName) || usage.rawExpression == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    violations.add(
                        "${getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.of(prop.modulePath, prop.sourceSet?.name, prop.filePath, usage.line, usage.column)})",
                    )
                }
        }
        return builder
    }

    /** Fails for every actual class/type use of [kClass] in the selected property; imports alone do not match. */
    fun notReferenceClass(kClass: KClass<*>): PropertiesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    fun anyOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(prop, allProps, subViolations)
                    subViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add("Property ${prop.qualifiedName} does not satisfy any of the specified conditions")
            }
        }
        return builder
    }

    fun allOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            blocks.forEach { block ->
                val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                PropertiesShould(subBuilder).apply(block)
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(prop, allProps, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(prop, allProps, subViolations)
                    subViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add("Property ${prop.qualifiedName} satisfies one of the forbidden conditions")
            }
        }
        return builder
    }
}

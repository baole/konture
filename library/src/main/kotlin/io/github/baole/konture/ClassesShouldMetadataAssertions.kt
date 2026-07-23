/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
internal interface ClassesShouldMetadataAssertions {
    val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes are annotated with the specified annotation.
     * Matches either the annotation's simple name or its FQN.
     *
     * @param annotationFqName The annotation name or fully qualified name.
     */
    infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val hasAnnotation = cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
            if (!hasAnnotation) {
                violations.add(getMessage("class.should.haveAnnotation", cls.fqName, annotationFqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are annotated with the specified annotation.
     * Matches either simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = haveAllAnnotationsOf(listOf(name))

    /**
     * Asserts that selected classes are annotated with all of the specified annotations.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.hasAllAnnotations(names)) {
                violations.add(getMessage("class.should.haveAllAnnotations", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Asserts that selected classes are annotated with the specified annotation.
     * Matches either simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = haveAnyAnnotationOf(listOf(name))

    /**
     * Asserts that selected classes are annotated with any of the specified annotations.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.hasAnyAnnotation(names)) {
                violations.add(getMessage("class.should.haveAtLeastOneAnnotationOf", cls.fqName, names.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Asserts that selected classes have the specified annotation with a matching argument name and value.
     */
    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches =
                cls.annotations.any { ann ->
                    (ann.name == annotationName || ann.fqName == annotationName) &&
                        ann.arguments.any { arg ->
                            (argName == null || arg.name == argName) && arg.value == argValue
                        }
                }
            if (!matches) {
                val detail =
                    if (argName !=
                        null
                    ) {
                        "argument '$argName' with value '$argValue'"
                    } else {
                        "argument value '$argValue'"
                    }
                violations.add(getMessage("class.should.haveAnnotationWithDetail", cls.fqName, annotationName, detail))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are interface definitions.
     */
    fun beInterfaces(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.isInterface) {
                violations.add(getMessage("class.should.beInterface", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are enum classes.
     */
    fun beEnums(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.isEnum) {
                violations.add(getMessage("class.should.beEnum", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are abstract classes.
     */
    fun beAbstract(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.isAbstract && !cls.isInterface) {
                violations.add(getMessage("class.should.beAbstract", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are sealed.
     */
    fun beSealed(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.modifiers.contains(Modifier.SEALED)) {
                violations.add(getMessage("class.should.beSealed", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are data classes.
     */
    fun beData(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.modifiers.contains(Modifier.DATA)) {
                violations.add(getMessage("class.should.beData", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are inline/value classes.
     */
    fun beInline(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.modifiers.contains(Modifier.INLINE) && !cls.modifiers.contains(Modifier.VALUE)) {
                violations.add(getMessage("class.should.beInline", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.modifiers.contains(modifier)) {
                violations.add(getMessage("class.should.haveModifier", cls.fqName, modifier))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have the specified modifier.
     *
     * @param modifier The modifier that must be present.
     */
    infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    /**
     * Asserts that selected classes have all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val missing = modifiers.filter { !cls.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage("class.should.haveAllModifiers", cls.fqName, modifiers.joinToString(), missing.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Asserts that selected classes have the specified modifier.
     *
     * @param modifier The modifier that must be present.
     */
    infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    /**
     * Asserts that selected classes have at least one of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!modifiers.any { cls.modifiers.contains(it) }) {
                violations.add(getMessage("class.should.haveAnyModifiers", cls.fqName, modifiers.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have at least one of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Asserts that selected classes have specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.visibility != visibility) {
                violations.add(getMessage("class.should.haveVisibility", cls.fqName, visibility, cls.visibility))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have specified visibility.
     *
     * @param visibility The acceptable visibility.
     */
    infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder = haveAnyVisibility(listOf(visibility))

    /**
     * Asserts that selected classes have any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!visibilities.contains(cls.visibility)) {
                violations.add(
                    getMessage("class.should.haveAnyVisibility", cls.fqName, visibilities.joinToString(), cls.visibility),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder = haveAnyVisibility(visibilities.asList())

    fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /**
     * Asserts that selected classes extend or implement specified supertype.
     */
    infix fun beAssignableTo(superType: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            if (!cls.isAssignableTo(superType, allClasses)) {
                violations.add(getMessage("class.should.beAssignableTo", cls.fqName, superType))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are assignable to specified supertype.
     *
     * @param superType The supertype that must be matched.
     */
    infix fun beAssignableToAnyOf(superType: String): ClassesRuleBuilder = beAssignableToAnyOf(listOf(superType))

    /**
     * Asserts that selected classes are assignable to any of the specified supertypes.
     *
     * @param superTypes The list of supertypes, at least one of which must be matched.
     */
    infix fun beAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            if (!superTypes.any { cls.isAssignableTo(it, allClasses) }) {
                violations.add(getMessage("class.should.beAssignableToAny", cls.fqName, superTypes.joinToString()))
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are assignable to any of the specified supertypes.
     *
     * @param superTypes The vararg list of supertypes, at least one of which must be matched.
     */
    fun beAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder = beAssignableToAnyOf(superTypes.asList())

    /**
     * Asserts that selected classes are assignable to specified supertype.
     *
     * @param superType The supertype that must be matched.
     */
    infix fun beAssignableToAllOf(superType: String): ClassesRuleBuilder = beAssignableToAllOf(listOf(superType))

    /**
     * Asserts that selected classes are assignable to all of the specified supertypes.
     *
     * @param superTypes The list of supertypes that must all be matched.
     */
    infix fun beAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val missing = superTypes.filter { !cls.isAssignableTo(it, allClasses) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage("class.should.beAssignableToAll", cls.fqName, superTypes.joinToString(), missing.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are assignable to all of the specified supertypes.
     *
     * @param superTypes The vararg list of supertypes that must all be matched.
     */
    fun beAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder = beAssignableToAllOf(superTypes.asList())

    /**
     * Asserts that selected classes are assignable from the specified subtype.
     *
     * @param subType The subtype that must extend or implement the selected classes.
     */
    infix fun beAssignableFrom(subType: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
            val isAssignable =
                if (subTypeDecl != null) {
                    subTypeDecl.fqName == cls.fqName ||
                        subTypeDecl.isAssignableTo(cls.fqName, allClasses) ||
                        subTypeDecl.isAssignableTo(cls.name, allClasses)
                } else {
                    subType == cls.fqName || subType == cls.name
                }
            if (!isAssignable) {
                violations.add(getMessage("class.should.beAssignableFrom", cls.fqName, subType))
            }
        }
        return builder
    }
}

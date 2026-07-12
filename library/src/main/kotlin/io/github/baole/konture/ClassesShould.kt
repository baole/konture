package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
@KontureDsl
class ClassesShould internal constructor(
    private val builder: ClassesRuleBuilder,
) {
    /**
     * Asserts that selected classes reside in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, cls.packageName)) {
                violations.add(
                    "Class ${cls.fqName} should reside in package '$packagePattern' but resides in '${cls.packageName}'",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns List of package matching patterns.
     */
    infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, cls.packageName) }
            if (!matches) {
                violations.add(
                    "Class ${cls.fqName} should reside in package in [${packagePatterns.joinToString()}] but resides in '${cls.packageName}'",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes reside in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns Package matching patterns.
     */
    fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder = resideInAPackage(packagePatterns.toList())

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param predicate Predicate checking package name.
     */
    infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder = resideInAPackage("custom package predicate", predicate)

    /**
     * Asserts that selected classes reside in packages matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking package name.
     */
    fun resideInAPackage(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!predicate(cls.packageName)) {
                violations.add(
                    "Class ${cls.fqName} should reside in package matching: $description, but resides in '${cls.packageName}'",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names ending with the specified suffix.
     *
     * @param suffix The expected name suffix.
     */
    infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.name.endsWith(suffix)) {
                violations.add("Class ${cls.fqName} should have name ending with '$suffix'")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names ending with any of the specified suffixes.
     *
     * @param suffixes The expected name suffixes.
     */
    infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = suffixes.any { cls.name.endsWith(it) }
            if (!matches) {
                violations.add("Class ${cls.fqName} should have name ending with any of [${suffixes.joinToString()}]")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names ending with any of the specified suffixes.
     *
     * @param suffixes The expected name suffixes.
     */
    fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Asserts that selected classes have simple names starting with the specified prefix.
     *
     * @param prefix The expected name prefix.
     */
    infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.name.startsWith(prefix)) {
                violations.add("Class ${cls.fqName} should have name starting with '$prefix'")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names starting with any of the specified prefixes.
     *
     * @param prefixes The expected name prefixes.
     */
    infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = prefixes.any { cls.name.startsWith(it) }
            if (!matches) {
                violations.add("Class ${cls.fqName} should have name starting with any of [${prefixes.joinToString()}]")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names starting with any of the specified prefixes.
     *
     * @param prefixes The expected name prefixes.
     */
    fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param predicate Predicate checking class simple name.
     */
    infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder = haveName("custom name predicate", predicate)

    /**
     * Asserts that selected classes have simple names matching the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking class simple name.
     */
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!predicate(cls.name)) {
                violations.add("Class ${cls.fqName} should have name matching: $description, but name is '${cls.name}'")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching the specified glob pattern.
     * Supports '*' wildcards.
     *
     * @param pattern Glob pattern (e.g. "*UseCase", "*Repository").
     */
    infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, cls.name)) {
                violations.add("Class ${cls.fqName} should have name matching '$pattern'")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, cls.name) }
            if (!matches) {
                violations.add("Class ${cls.fqName} should have name matching any of [${patterns.joinToString()}]")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes have simple names matching any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

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
                violations.add("Class ${cls.fqName} should have annotation '$annotationFqName'")
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
                violations.add("Class ${cls.fqName} should have all annotations: ${names.joinToString()}")
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
                violations.add("Class ${cls.fqName} should have at least one annotation of: ${names.joinToString()}")
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
                violations.add("Class ${cls.fqName} should have annotation '$annotationName' with $detail")
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
                violations.add("Class ${cls.fqName} should be an interface")
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
                violations.add("Class ${cls.fqName} should be an enum")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are abstract classes.
     */
    fun beAbstract(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.isAbstract) {
                violations.add("Class ${cls.fqName} should be abstract")
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
                violations.add("Class ${cls.fqName} should be sealed")
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
                violations.add("Class ${cls.fqName} should be a data class")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are inline/value classes.
     */
    fun beInline(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (!cls.modifiers.contains(Modifier.INLINE)) {
                violations.add("Class ${cls.fqName} should be an inline class")
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
                violations.add("Class ${cls.fqName} should have modifier '$modifier'")
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
                    "Class ${cls.fqName} should have all modifiers: ${modifiers.joinToString()}, but is missing: ${missing.joinToString()}",
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
                violations.add("Class ${cls.fqName} should have any of the modifiers: ${modifiers.joinToString()}")
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
                violations.add("Class ${cls.fqName} should have $visibility visibility, but is ${cls.visibility}")
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
                    "Class ${cls.fqName} should have any of visibilities: ${visibilities.joinToString()}, but is ${cls.visibility}",
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
        builder.setShould { cls, _, violations ->
            if (!cls.supertypes.contains(superType)) {
                violations.add("Class ${cls.fqName} should be assignable to $superType")
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
        builder.setShould { cls, _, violations ->
            if (!superTypes.any { cls.supertypes.contains(it) }) {
                violations.add("Class ${cls.fqName} should be assignable to any of: ${superTypes.joinToString()}")
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
        builder.setShould { cls, _, violations ->
            val missing = superTypes.filter { !cls.supertypes.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    "Class ${cls.fqName} should be assignable to all of: ${superTypes.joinToString()}, but is missing: ${missing.joinToString()}",
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
     * Asserts that selected classes have KDoc documentation.
     */
    fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            if (cls.kdocText?.isBlank() != false) {
                violations.add("Class ${cls.fqName} should be documented with a KDoc comment")
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing allowed accessing classes.
     */
    fun onlyBeAccessedByAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        builder.setShould { targetCls, allClasses, violations ->
            val accessingClasses =
                allClasses.filter { other ->
                    other.fqName != targetCls.fqName && other.dependsOn(targetCls)
                }
            for (accessor in accessingClasses) {
                val isAllowed =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, accessor.packageName)
                    }
                if (!isAllowed) {
                    violations.add(
                        "Class ${targetCls.fqName} is accessed by ${accessor.fqName} (in package ${accessor.packageName}), which is not allowed by package pattern(s): ${packagePatterns.joinToString()}",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified pattern.
     */
    infix fun onlyBeAccessedByAnyPackage(packagePattern: String): ClassesRuleBuilder = onlyBeAccessedByAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes are accessed only by classes residing in packages matching the specified patterns.
     */
    infix fun onlyBeAccessedByAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        onlyBeAccessedByAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing allowed dependency packages.
     */
    fun onlyDependOnClassesInAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val dependencies =
                allClasses.filter { other ->
                    other.fqName != cls.fqName && cls.dependsOn(other)
                }
            for (dep in dependencies) {
                val isAllowed =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, dep.packageName)
                    }
                if (!isAllowed) {
                    violations.add(
                        "Class ${cls.fqName} depends on ${dep.fqName} (in package ${dep.packageName}), which is not allowed by package pattern(s): ${packagePatterns.joinToString()}",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified pattern.
     */
    infix fun onlyDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder = onlyDependOnClassesInAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes depend only on classes residing in packages matching the specified patterns.
     */
    infix fun onlyDependOnClassesInAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        onlyDependOnClassesInAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified patterns.
     *
     * @param packagePatterns Package wildcard patterns representing forbidden dependency packages.
     */
    fun notDependOnClassesInAnyPackage(vararg packagePatterns: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val dependencies =
                allClasses.filter { other ->
                    other.fqName != cls.fqName && cls.dependsOn(other)
                }
            for (dep in dependencies) {
                val isForbidden =
                    packagePatterns.any { pattern ->
                        PatternMatchers.matchesPackage(pattern, dep.packageName)
                    }
                if (isForbidden) {
                    violations.add(
                        "Class ${cls.fqName} depends on ${dep.fqName} (in package ${dep.packageName}), which is forbidden by package pattern(s): ${packagePatterns.joinToString()}",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified pattern.
     */
    infix fun notDependOnClassesInAnyPackage(packagePattern: String): ClassesRuleBuilder = notDependOnClassesInAnyPackage(listOf(packagePattern))

    /**
     * Asserts that selected classes do not depend on classes residing in packages matching the specified patterns.
     */
    infix fun notDependOnClassesInAnyPackage(packagePatterns: List<String>): ClassesRuleBuilder =
        notDependOnClassesInAnyPackage(*packagePatterns.toTypedArray())

    /**
     * Asserts that selected classes do not expose types annotated with the specified annotations
     * in their property, function return, or parameter signatures.
     *
     * @param annotationNames Annotation simple or fully qualified names that must not appear on signature types.
     */
    fun notHaveSignaturesWithTypesAnnotatedWith(vararg annotationNames: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val signatureTypes = cls.collectSignatureTypeNames()
            for (typeName in signatureTypes) {
                val resolved = cls.resolveTypeReference(typeName, allClasses) ?: continue
                val forbiddenAnnotation =
                    resolved.annotations.find { annotation ->
                        annotationNames.any { target -> annotation.matchesName(target) }
                    }
                if (forbiddenAnnotation != null) {
                    violations.add(
                        "Class ${cls.fqName} exposes signature type ${resolved.fqName} annotated with @${forbiddenAnnotation.name}, which is forbidden",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes do not expose types annotated with the specified annotation
     * in their property, function return, or parameter signatures.
     */
    infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationName: String): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(listOf(annotationName))

    /**
     * Asserts that selected classes do not expose types annotated with any of the specified annotations
     * in their property, function return, or parameter signatures.
     */
    infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationNames: List<String>): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(*annotationNames.toTypedArray())

    /**
     * Asserts that selected classes satisfy a custom condition.
     *
     * @param assertion Custom assertion checking the class.
     */
    infix fun satisfy(assertion: (ClassDeclaration) -> Boolean): ClassesRuleBuilder = satisfy("custom condition") { cls, _ -> assertion(cls) }

    /**
     * Asserts that selected classes satisfy a custom condition.
     *
     * @param description A descriptive string for the custom condition used in violations.
     * @param assertion Custom assertion checking the class.
     */
    infix fun satisfy(description: String): ClassesRuleBuilder = satisfy(description) { cls, _ -> false }

    private fun satisfy(
        description: String,
        assertion: (ClassDeclaration, List<ClassDeclaration>) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            if (!assertion(cls, allClasses)) {
                violations.add("Class ${cls.fqName} should satisfy: $description")
            }
        }
        return builder
    }

    /**
     * Satisfies an arbitrary custom assertion logic with custom violations builder.
     */
    fun satisfy(assertion: (ClassDeclaration, MutableList<String>) -> Unit): ClassesRuleBuilder {
        builder.setShould { cls, _, violations -> assertion(cls, violations) }
        return builder
    }

    /**
     * Asserts that at least one of the nested assertion blocks is satisfied.
     */
    fun anyOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            val tempViolationsList =
                assertions.map { assertion ->
                    val temp = mutableListOf<String>()
                    assertion(cls, allCls, temp)
                    temp
                }
            if (tempViolationsList.all { it.isNotEmpty() }) {
                violations.add("Class ${cls.fqName} should satisfy at least one of the nested assertions.")
            }
        }
        return builder
    }

    /**
     * Asserts that all of the nested assertion blocks are satisfied.
     */
    fun allOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            assertions.forEach { assertion ->
                assertion(cls, allCls, violations)
            }
        }
        return builder
    }

    /**
     * Asserts that none of the nested assertion blocks are satisfied.
     */
    fun noneOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            assertions.forEach { assertion ->
                val temp = mutableListOf<String>()
                assertion(cls, allCls, temp)
                if (temp.isEmpty()) {
                    violations.add("Class ${cls.fqName} should not satisfy the nested assertion.")
                }
            }
        }
        return builder
    }

    /**
     * Asserts that all member functions in selected classes satisfy the assertions specified in the [block].
     */
    fun allFunctions(block: FunctionAssertionScope.() -> Unit): ClassesRuleBuilder {
        val scope = FunctionAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (func in cls.functions) {
                val funcViolations = mutableListOf<String>()
                for (assertion in scope.assertions) {
                    assertion(func, funcViolations)
                }
                if (funcViolations.isNotEmpty()) {
                    violations.add(
                        "Function ${func.name} in class ${cls.fqName} has violations:\n" +
                            funcViolations.joinToString("\n") {
                                "  - $it"
                            },
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that all member properties in selected classes satisfy the assertions specified in the [block].
     */
    fun allProperties(block: PropertyAssertionScope.() -> Unit): ClassesRuleBuilder {
        val scope = PropertyAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (prop in cls.properties) {
                val propViolations = mutableListOf<String>()
                for (assertion in scope.assertions) {
                    assertion(prop, propViolations)
                }
                if (propViolations.isNotEmpty()) {
                    violations.add(
                        "Property ${prop.name} in class ${cls.fqName} has violations:\n" +
                            propViolations.joinToString("\n") {
                                "  - $it"
                            },
                    )
                }
            }
        }
        return builder
    }
}

package io.github.baole.konture

/**
 * Checks whether this class declaration depends on the given [target] class declaration.
 */
internal fun ClassDeclaration.dependsOn(target: ClassDeclaration): Boolean {
    if (this.fqName == target.fqName) return false

    // Resolve references and supertypes using import aliases
    val resolvedReferences = referencedTypes.map { importAliases[it] ?: it }.toSet()
    val resolvedSupertypes = supertypes.map { importAliases[it] ?: it }.toSet()

    // Direct import of target FQ name
    if (imports.contains(target.fqName)) return true

    // Explicit FQ name reference
    if (resolvedReferences.contains(target.fqName)) return true

    // Check simple name reference
    if (hasSimpleNameReference(target, resolvedReferences)) return true

    // Supertypes inheritance references
    if (resolvedSupertypes.contains(target.name) || resolvedSupertypes.contains(target.fqName)) return true

    // Annotations dependency reference
    if (annotations.any { it.fqName == target.fqName || it.name == target.name }) return true

    return false
}

private fun ClassDeclaration.hasSimpleNameReference(
    target: ClassDeclaration,
    resolvedReferences: Set<String>,
): Boolean {
    if (!resolvedReferences.contains(target.name)) return false

    // If same package, it's a reference
    if (packageName == target.packageName) return true

    // If there's a star import for target package
    val targetParentPackage = target.packageName
    if (targetParentPackage.isNotEmpty() && imports.contains("$targetParentPackage.*")) {
        return true
    }

    // If imported via exact import
    return imports.any { it == target.fqName || it.endsWith(".${target.name}") }
}

/**
 * Collects simple type names appearing in this class's public API surface
 * (properties, function return types, and parameter types).
 */
internal fun ClassDeclaration.collectSignatureTypeNames(): Set<String> {
    val types = mutableSetOf<String>()
    for (property in properties) {
        types.addAll(extractTypeTokens(property.type))
    }
    for (function in functions) {
        types.addAll(extractTypeTokens(function.returnType))
        for (parameter in function.parameters) {
            types.addAll(extractTypeTokens(parameter.type))
        }
    }
    primaryConstructor?.let { constructor ->
        for (parameter in constructor.parameters) {
            types.addAll(extractTypeTokens(parameter.type))
        }
    }
    for (constructor in secondaryConstructors) {
        for (parameter in constructor.parameters) {
            types.addAll(extractTypeTokens(parameter.type))
        }
    }
    return types
}

private fun extractTypeTokens(typeText: String): List<String> {
    val withoutGenerics = typeText.substringBefore("<").removeSuffix("?").trim()
    if (withoutGenerics.isEmpty()) return emptyList()

    val genericArgs =
        typeText
            .substringAfter("<", "")
            .substringBeforeLast(">", "")
            .split(",")
            .map { it.trim().substringBefore("<").removeSuffix("?").trim() }
            .filter { it.isNotEmpty() }

    return listOf(withoutGenerics) + genericArgs
}

/**
 * Resolves a type reference string to a [ClassDeclaration] using imports and [allClasses].
 */
internal fun ClassDeclaration.resolveTypeReference(
    typeName: String,
    allClasses: List<ClassDeclaration>,
): ClassDeclaration? {
    val simpleName = typeName.substringBefore("<").removeSuffix("?").trim()
    if (simpleName.isEmpty()) return null

    allClasses.find { it.fqName == simpleName }?.let { return it }

    val aliased = importAliases[simpleName] ?: simpleName
    allClasses.find { it.fqName == aliased }?.let { return it }

    if (simpleName.contains(".")) {
        return allClasses.find { it.fqName == simpleName }
    }

    val importMatch = imports.find { it == simpleName || it.endsWith(".$simpleName") }
    if (importMatch != null) {
        allClasses.find { it.fqName == importMatch }?.let { return it }
    }

    return allClasses.find { it.name == simpleName && it.packageName == packageName }
}

/**
 * Returns true when [annotationName] matches the given [AnnotationDeclaration].
 */
internal fun AnnotationDeclaration.matchesName(annotationName: String): Boolean =
    fqName == annotationName ||
        name == annotationName ||
        fqName.endsWith(".$annotationName")

/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private annotation class TypeSafeMarker

private class User

private class Outer {
    class Inner
}

class TypeSafeOverloadsTest {
    @Test
    fun `typed annotations select matching classes`() {
        val declaration =
            ClassDeclaration(
                name = "Annotated",
                fqName = "example.Annotated",
                packageName = "example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Annotated.kt",
            )
        val graph = graphWith(declaration)

        val rule = ClassesRuleBuilder(graph).that().haveAnnotationOf<TypeSafeMarker>()

        assertTrue(rule.getThatPredicate()!!(declaration))
    }

    @Test
    fun `typed function signatures match simple and parameterized source types`() {
        val function =
            FunctionDeclaration(
                name = "load",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "List<String>",
                parameters =
                    listOf(
                        ParameterDeclaration("id", "String", hasDefaultValue = false, annotations = emptyList(), resolvedType = "kotlin.String"),
                    ),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.collections.List",
            )
        val graph = graphWith(function = function)
        val context = FunctionDeclarationContext(function, "example", null, ":app", "/src/Example.kt")

        val returnRule = FunctionsRuleBuilder(graph).that().haveReturnType(List::class)
        val parameterRule = FunctionsRuleBuilder(graph).that().haveParameterTypes(String::class)

        assertTrue(returnRule.getThatPredicate()!!(context))
        assertTrue(parameterRule.getThatPredicate()!!(context))
    }

    @Test
    fun `typed property types delegate to normalized matching`() {
        val property =
            PropertyDeclaration(
                name = "name",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                resolvedType = "kotlin.String",
            )
        val graph = graphWith(property = property)
        val context = PropertyDeclarationContext(property, "example", null, ":app", "/src/Example.kt")

        val rule = PropertiesRuleBuilder(graph).should().haveTypeOf<String>()
        val violations = mutableListOf<String>()
        rule.getShouldAssertion()!!(context, emptyList(), violations)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `typed assignability delegates to class names`() {
        val declaration =
            ClassDeclaration(
                name = "TextChild",
                fqName = "example.TextChild",
                packageName = "example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TextChild.kt",
                supertypes = listOf("kotlin.CharSequence"),
            )
        val graph = graphWith(declaration)

        val rule = ClassesRuleBuilder(graph).that().areAssignableTo<CharSequence>()

        assertTrue(rule.getThatPredicate()!!(declaration))
    }

    @Test
    fun `typed scoped usage overloads match class references`() {
        val usage = SourceUsage(UsageKind.CLASS_REFERENCE, "kotlin.String", "/src/Example.kt", 1, 1)
        val file = FileDeclaration("Example.kt", "example", filePath = "/src/Example.kt", usages = listOf(usage))
        val graph = ProjectGraph(mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))))

        val error =
            org.junit.jupiter.api.Assertions.assertThrows(AssertionError::class.java) {
                FilesRuleBuilder(graph).should().notReferenceClass<String>().check()
            }

        assertTrue(error.message!!.contains("kotlin.String"))
    }

    @Test
    fun `package of type helpers use the type package`() {
        val declaration =
            ClassDeclaration(
                name = "Marker",
                fqName = TypeSafeMarker::class.qualifiedName!!,
                packageName = TypeSafeMarker::class.java.packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Text.kt",
            )
        val rule = ClassesRuleBuilder(graphWith(declaration)).that().resideInPackageOf<TypeSafeMarker>()

        assertTrue(rule.getThatPredicate()!!(declaration))
    }

    @Test
    fun `typed matching rejects explicitly qualified types with the same simple name`() {
        assertTrue(!matchesKotlinType("other.User", User::class))
    }

    @Test
    fun `typed matching supports nested Kotlin type syntax`() {
        assertTrue(matchesKotlinType(Outer.Inner::class.qualifiedName!!, Outer.Inner::class))
    }

    @Test
    fun `reified direct scope assertions delegate to typed overloads`() {
        val declaration =
            ClassDeclaration(
                name = "Annotated",
                fqName = "example.Annotated",
                packageName = "example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Annotated.kt",
                supertypes = listOf("kotlin.CharSequence"),
            )
        val scope = KontureScope(listOf(declaration))

        scope.assertHaveAnnotationOfType<TypeSafeMarker>()
        scope.assertAreAssignableToType<CharSequence>(allClasses = listOf(declaration))
    }

    @Test
    fun `scoped usage rejects local classes without a stable qualified name`() {
        class Local

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            FilesRuleBuilder(graphWith()).should().notCall(Local::class)
        }
    }

    @Test
    fun `existing empty string vararg overloads remain callable`() {
        val graph = graphWith()

        FunctionsRuleBuilder(graph).that().haveParameterTypes()
        FunctionsRuleBuilder(graph).should().haveParameterTypes()
        PropertiesRuleBuilder(graph).that().haveType()
        PropertiesRuleBuilder(graph).should().haveType()
    }

    @Test
    fun `comprehensive classes type-safe overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val declaration =
            ClassDeclaration(
                name = "MyClass",
                fqName = "$packageName.MyClass",
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            "TypeSafeMarker",
                            TypeSafeMarker::class.qualifiedName!!,
                            listOf(AnnotationArgumentDeclaration("name", "value")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyClass.kt",
                supertypes = listOf("kotlin.CharSequence", "kotlin.Cloneable"),
            )
        val graph = graphWith(declaration, packageName = packageName)

        // ClassesThat overloads
        assertTrue(ClassesRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(declaration))
        assertTrue(
            ClassesRuleBuilder(graph).that().haveAnnotationWithArgument(TypeSafeMarker::class, "name", "value").getThatPredicate()!!(declaration),
        )
        assertTrue(ClassesRuleBuilder(graph).that().areAssignableTo(CharSequence::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().areAssignableToAnyOf(CharSequence::class, Number::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().areAssignableToAllOf(CharSequence::class, Cloneable::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(declaration))
        assertTrue(ClassesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(declaration))

        // ClassesShould overloads
        val violations = mutableListOf<String>()

        ClassesRuleBuilder(graph).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().haveAnnotationOf<TypeSafeMarker>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().haveAllAnnotationsOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().haveAnyAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationWithArgument(TypeSafeMarker::class, "name", "value").getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().beAssignableTo(CharSequence::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().beAssignableTo<CharSequence>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAnyOf(CharSequence::class, Number::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAllOf(CharSequence::class, Cloneable::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(graph).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `comprehensive functions type-safe overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val function =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.String",
            )
        val graph = graphWith(function = function, packageName = packageName)
        val context = FunctionDeclarationContext(function, packageName, null, ":app", "/src/Example.kt")

        // FunctionsThat
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnnotationOfType<TypeSafeMarker>().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context))

        // FunctionsShould
        val violations = mutableListOf<String>()

        FunctionsRuleBuilder(graph).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(graph).should().haveAnnotationOfType<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(graph).should().haveAnyAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(graph).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(graph).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `comprehensive properties type-safe overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val property =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                kdocText = null,
                resolvedType = "kotlin.String",
            )
        val graph = graphWith(property = property, packageName = packageName)
        val context = PropertyDeclarationContext(property, packageName, null, ":app", "/src/Example.kt")

        // PropertiesThat
        assertTrue(PropertiesRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(PropertiesRuleBuilder(graph).that().haveAnnotationOfType<TypeSafeMarker>().getThatPredicate()!!(context))
        assertTrue(PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(PropertiesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(PropertiesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context))

        // PropertiesShould
        val violations = mutableListOf<String>()

        PropertiesRuleBuilder(graph).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(graph).should().haveAnnotationOfType<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(graph).should().haveAllAnnotationsOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(graph).should().haveAnyAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(graph).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(graph).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `comprehensive files type-safe overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val file =
            FileDeclaration(
                name = "Example.kt",
                packageName = packageName,
                classes = emptyList(),
                filePath = "/src/Example.kt",
            )
        val graph = ProjectGraph(mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))))
        val context = FileDeclarationContext(file, ":app")

        assertTrue(FilesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context))
        assertTrue(FilesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context))

        val violations = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FilesRuleBuilder(graph).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `comprehensive list and konturescope overloads test`() {
        val declaration =
            ClassDeclaration(
                name = "MyClass",
                fqName = "example.MyClass",
                packageName = "example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            "TypeSafeMarker",
                            TypeSafeMarker::class.qualifiedName!!,
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyClass.kt",
                supertypes = listOf("kotlin.CharSequence"),
            )
        val classes = listOf(declaration)

        // List<ClassDeclaration> overloads
        assertTrue(classes.withAnnotationOf(TypeSafeMarker::class).contains(declaration))
        assertTrue(classes.withAnnotationOf<TypeSafeMarker>().contains(declaration))
        assertTrue(classes.withoutAnnotationOf(TypeSafeMarker::class).isEmpty())
        assertTrue(classes.withoutAnnotationOf<TypeSafeMarker>().isEmpty())
        assertTrue(classes.withParentOf(CharSequence::class).contains(declaration))
        assertTrue(classes.withParentOf<CharSequence>().contains(declaration))

        classes.assertHaveAnnotationOf(TypeSafeMarker::class)
        classes.assertHaveAnnotationOfType<TypeSafeMarker>()
        classes.assertAreAssignableTo(CharSequence::class, allClasses = classes)
        classes.assertAreAssignableToType<CharSequence>(allClasses = classes)

        // KontureScope overloads
        val scope = KontureScope(classes)
        assertTrue(scope.withAnnotationOf(TypeSafeMarker::class).classes.contains(declaration))
        assertTrue(scope.withAnnotationOf<TypeSafeMarker>().classes.contains(declaration))
        assertTrue(scope.withoutAnnotationOf(TypeSafeMarker::class).classes.isEmpty())
        assertTrue(scope.withoutAnnotationOf<TypeSafeMarker>().classes.isEmpty())
        assertTrue(scope.withParentOf(CharSequence::class).classes.contains(declaration))
        assertTrue(scope.withParentOf<CharSequence>().classes.contains(declaration))

        scope.assertHaveAnnotationOf(TypeSafeMarker::class)
        scope.assertHaveAnnotationOfType<TypeSafeMarker>()
        scope.assertAreAssignableTo(CharSequence::class, allClasses = classes)
        scope.assertAreAssignableToType<CharSequence>(allClasses = classes)
    }

    @Test
    fun `comprehensive konture and assertion scope overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val declaration =
            ClassDeclaration(
                name = "Marker",
                fqName = TypeSafeMarker::class.qualifiedName!!,
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Text.kt",
            )
        val graph = graphWith(declaration, packageName = packageName)

        val previous = if (ProjectGraph.isDefaultInitialized()) ProjectGraph.getDefault() else null
        try {
            ProjectGraph.setDefault(graph)

            // Konture package scope helpers
            val scope1 = Konture.scopeFromPackageOf(TypeSafeMarker::class)
            val scope2 = Konture.scopeFromPackageOf<TypeSafeMarker>()
            val fileScope1 = Konture.fileScopeFromPackageOf(TypeSafeMarker::class)
            val fileScope2 = Konture.fileScopeFromPackageOf<TypeSafeMarker>()

            assertTrue(scope1.classes.any { it.name == "Marker" })
            assertTrue(scope2.classes.any { it.name == "Marker" })
            assertTrue(fileScope1.files.any { it.packageName == TypeSafeMarker::class.java.packageName })
            assertTrue(fileScope2.files.any { it.packageName == TypeSafeMarker::class.java.packageName })
        } finally {
            if (previous != null) {
                ProjectGraph.setDefault(previous)
            }
        }

        // Assertion scopes
        val funcScope = FunctionAssertionScope()
        funcScope.haveReturnType(String::class)
        funcScope.haveReturnTypeOf<String>()
        funcScope.haveAnnotationOf(TypeSafeMarker::class)
        funcScope.haveAnnotationOfType<TypeSafeMarker>()

        val mockFunc =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.String",
            )
        val funcViolations = mutableListOf<String>()
        funcScope.assertions.forEach { it(mockFunc, funcViolations) }
        assertTrue(funcViolations.isEmpty())

        val propScope = PropertyAssertionScope()
        propScope.haveType(String::class)
        propScope.haveTypeOf<String>()
        propScope.haveAnnotationOf(TypeSafeMarker::class)
        propScope.haveAnnotationOfType<TypeSafeMarker>()

        val mockProp =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                kdocText = null,
                resolvedType = "kotlin.String",
            )
        val propViolations = mutableListOf<String>()
        propScope.assertions.forEach { it(mockProp, propViolations) }
        assertTrue(propViolations.isEmpty())
    }

    private fun graphWith(
        declaration: ClassDeclaration? = null,
        function: FunctionDeclaration? = null,
        property: PropertyDeclaration? = null,
        packageName: String = "example",
    ): ProjectGraph {
        val file =
            FileDeclaration(
                name = "Example.kt",
                packageName = packageName,
                classes = declaration?.let(::listOf) ?: emptyList(),
                topLevelFunctions = function?.let(::listOf) ?: emptyList(),
                topLevelProperties = property?.let(::listOf) ?: emptyList(),
                filePath = "/src/Example.kt",
            )
        val module = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file))
        return ProjectGraph(mapOf(":" to listOf(module)))
    }
}

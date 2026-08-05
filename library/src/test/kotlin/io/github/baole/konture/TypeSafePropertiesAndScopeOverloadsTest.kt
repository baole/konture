/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeSafePropertiesAndScopeOverloadsTest {
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
    fun `typed scoped usage overloads match class references`() {
        val usage = SourceUsage(UsageKind.CLASS_REFERENCE, "kotlin.String", "/src/Example.kt", 1, 1)
        val file = FileDeclaration("Example.kt", "example", filePath = "/src/Example.kt", usages = listOf(usage))
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )

        val error =
            assertThrows(AssertionError::class.java) {
                FilesRuleBuilder(graph).should().notReferenceClass<String>().check()
            }

        assertTrue(error.message!!.contains("kotlin.String"))
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
        assertTrue(
            PropertiesRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            PropertiesRuleBuilder(graph).that().haveAnnotationOfType<TypeSafeMarker>().getThatPredicate()!!(context),
        )
        assertTrue(
            PropertiesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            PropertiesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            PropertiesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context),
        )

        // PropertiesShould
        val violations = mutableListOf<String>()

        PropertiesRuleBuilder(
            graph,
        ).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(
            graph,
        ).should().haveAnnotationOfType<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(
            graph,
        ).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        PropertiesRuleBuilder(
            graph,
        ).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
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
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )
        val context = FileDeclarationContext(file, ":app")

        assertTrue(
            FilesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(FilesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context))

        val violations = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FilesRuleBuilder(
            graph,
        ).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
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

        // Assertion scope violations when return type doesn't match
        val badFuncScope = FunctionAssertionScope()
        badFuncScope.haveReturnTypeOf<String>()
        val mockMismatchedFunc = mockFunc.copy(resolvedReturnType = "kotlin.Int")
        val mismatchedFuncViolations = mutableListOf<String>()
        badFuncScope.assertions.forEach { it(mockMismatchedFunc, mismatchedFuncViolations) }
        assertTrue(mismatchedFuncViolations.isNotEmpty())

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

        // Assertion scope violations when property type doesn't match
        val badPropScope = PropertyAssertionScope()
        badPropScope.haveTypeOf<String>()
        val mockMismatchedProp = mockProp.copy(resolvedType = "kotlin.Int")
        val mismatchedPropViolations = mutableListOf<String>()
        badPropScope.assertions.forEach { it(mockMismatchedProp, mismatchedPropViolations) }
        assertTrue(mismatchedPropViolations.isNotEmpty())
    }
}

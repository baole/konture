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

    private fun graphWith(
        declaration: ClassDeclaration? = null,
        function: FunctionDeclaration? = null,
        property: PropertyDeclaration? = null,
    ): ProjectGraph {
        val file =
            FileDeclaration(
                name = "Example.kt",
                packageName = "example",
                classes = declaration?.let(::listOf) ?: emptyList(),
                topLevelFunctions = function?.let(::listOf) ?: emptyList(),
                topLevelProperties = property?.let(::listOf) ?: emptyList(),
                filePath = "/src/Example.kt",
            )
        val module = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file))
        return ProjectGraph(mapOf(":" to listOf(module)))
    }
}

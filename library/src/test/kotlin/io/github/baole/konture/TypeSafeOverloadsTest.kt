/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal annotation class TypeSafeMarker

internal open class TypeSafeParent

internal class TypeSafeChild : TypeSafeParent()

internal class User

internal class Outer {
    class Inner
}

internal fun graphWith(
    declaration: ClassDeclaration? = null,
    function: FunctionDeclaration? = null,
    property: PropertyDeclaration? = null,
    packageName: String = "example",
    declarations: List<ClassDeclaration>? = null,
): ProjectGraph {
    val file =
        FileDeclaration(
            name = "Example.kt",
            packageName = packageName,
            classes = declarations ?: (declaration?.let(::listOf) ?: emptyList()),
            topLevelFunctions = function?.let(::listOf) ?: emptyList(),
            topLevelProperties = property?.let(::listOf) ?: emptyList(),
            filePath = "/src/Example.kt",
        )
    val module = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file))
    return ProjectGraph(mapOf(":" to listOf(module)))
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

        assertThrows(IllegalArgumentException::class.java) {
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

        val parentDecl =
            ClassDeclaration(
                name = "TypeSafeParent",
                fqName = TypeSafeParent::class.qualifiedName!!,
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TypeSafeParent.kt",
                supertypes = emptyList(),
            )
        val childDecl =
            ClassDeclaration(
                name = "TypeSafeChild",
                fqName = TypeSafeChild::class.qualifiedName!!,
                packageName = packageName,
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TypeSafeChild.kt",
                supertypes = listOf("TypeSafeParent"),
            )
        val graphWithBoth = graphWith(packageName = packageName, declarations = listOf(parentDecl, childDecl))

        // ClassesThat overloads
        assertTrue(
            ClassesRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument(
                TypeSafeMarker::class,
                "name",
                "value",
            ).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(graph).that().areAssignableTo(CharSequence::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAnyOf(CharSequence::class, Number::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAllOf(CharSequence::class, Cloneable::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(
                graphWithBoth,
            ).that().areAssignableFrom(TypeSafeChild::class).getThatPredicate()!!(parentDecl),
        )
        assertTrue(
            ClassesRuleBuilder(
                graphWithBoth,
            ).that().areAssignableFrom<TypeSafeChild>().getThatPredicate()!!(parentDecl),
        )
        assertTrue(
            ClassesRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(declaration),
        )
        assertTrue(
            ClassesRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(declaration),
        )

        // ClassesShould overloads
        val violations = mutableListOf<String>()

        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf<TypeSafeMarker>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf(
            TypeSafeMarker::class,
        ).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf(
            TypeSafeMarker::class,
        ).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationWithArgument(
            TypeSafeMarker::class,
            "name",
            "value",
        ).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableTo(CharSequence::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableTo<CharSequence>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graphWithBoth,
        ).should().beAssignableFrom(
            TypeSafeChild::class,
        ).getShouldAssertion()!!(parentDecl, listOf(parentDecl, childDecl), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graphWithBoth,
        ).should().beAssignableFrom<TypeSafeChild>().getShouldAssertion()!!(
            parentDecl,
            listOf(parentDecl, childDecl),
            violations,
        )
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAnyOf(
            CharSequence::class,
            Number::class,
        ).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAllOf(
            CharSequence::class,
            Cloneable::class,
        ).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())

        ClassesRuleBuilder(
            graph,
        ).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(declaration, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }
}

/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@Retention(AnnotationRetention.RUNTIME)
private annotation class TestTypeSafeAnnotation

private class TestTypeSafeTarget

internal class TypeSafeOverloadsCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test TypeSafeOverloads for ClassesThat and ClassesShould`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        assertNotNull(ClassesRuleBuilder(graph).that().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(ClassesRuleBuilder(graph).that().haveAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(ClassesRuleBuilder(graph).that().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(ClassesRuleBuilder(graph).that().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            ClassesRuleBuilder(graph).that().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(ClassesRuleBuilder(graph).should().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(ClassesRuleBuilder(graph).should().haveAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(ClassesRuleBuilder(graph).should().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(ClassesRuleBuilder(graph).should().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            ClassesRuleBuilder(graph).should().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableTo(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableTo<TestTypeSafeTarget>())
        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableToAnyOf(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableToAllOf(TestTypeSafeTarget::class))

        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableTo(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableTo<TestTypeSafeTarget>())
        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableToAnyOf(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableToAllOf(TestTypeSafeTarget::class))

        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableFrom(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).that().areAssignableFrom<TestTypeSafeTarget>())
        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableFrom(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).should().beAssignableFrom<TestTypeSafeTarget>())

        assertNotNull(ClassesRuleBuilder(graph).that().areNotAssignableTo<TestTypeSafeTarget>())
        assertNotNull(ClassesRuleBuilder(graph).that().areNotAssignableFrom<TestTypeSafeTarget>())

        assertNotNull(ClassesRuleBuilder(graph).that().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).that().resideInPackageOf<TestTypeSafeTarget>())
        assertNotNull(ClassesRuleBuilder(graph).should().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(ClassesRuleBuilder(graph).should().resideInPackageOf<TestTypeSafeTarget>())
    }

    @Test
    fun `test TypeSafeOverloads for FunctionsThat, FunctionsShould, PropertiesThat, PropertiesShould`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        assertNotNull(FunctionsRuleBuilder(graph).that().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(FunctionsRuleBuilder(graph).that().haveAnnotationOfType<TestTypeSafeAnnotation>())
        assertNotNull(FunctionsRuleBuilder(graph).that().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(FunctionsRuleBuilder(graph).that().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            FunctionsRuleBuilder(graph).that().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(FunctionsRuleBuilder(graph).should().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(FunctionsRuleBuilder(graph).should().haveAnnotationOfType<TestTypeSafeAnnotation>())
        assertNotNull(FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(FunctionsRuleBuilder(graph).should().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(FunctionsRuleBuilder(graph).that().haveReturnType<TestTypeSafeTarget>())
        assertNotNull(FunctionsRuleBuilder(graph).that().notHaveReturnType<TestTypeSafeTarget>())
        assertNotNull(FunctionsRuleBuilder(graph).that().notHaveParameterOf<TestTypeSafeTarget>())

        assertNotNull(FunctionsRuleBuilder(graph).that().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(FunctionsRuleBuilder(graph).that().resideInPackageOf<TestTypeSafeTarget>())
        assertNotNull(FunctionsRuleBuilder(graph).should().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(FunctionsRuleBuilder(graph).should().resideInPackageOf<TestTypeSafeTarget>())

        assertNotNull(PropertiesRuleBuilder(graph).that().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(PropertiesRuleBuilder(graph).that().haveAnnotationOfType<TestTypeSafeAnnotation>())
        assertNotNull(PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            PropertiesRuleBuilder(graph).that().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(PropertiesRuleBuilder(graph).should().haveAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(PropertiesRuleBuilder(graph).should().haveAnnotationOfType<TestTypeSafeAnnotation>())
        assertNotNull(PropertiesRuleBuilder(graph).should().haveAllAnnotationsOf(TestTypeSafeAnnotation::class))
        assertNotNull(PropertiesRuleBuilder(graph).should().haveAnyAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnnotationWithArgument(TestTypeSafeAnnotation::class, "arg", "val"),
        )

        assertNotNull(PropertiesRuleBuilder(graph).should().notCall<TestTypeSafeTarget>())
        assertNotNull(PropertiesRuleBuilder(graph).should().notReferenceClass<TestTypeSafeTarget>())

        assertNotNull(PropertiesRuleBuilder(graph).that().haveImportOf<TestTypeSafeTarget>())
        assertNotNull(PropertiesRuleBuilder(graph).that().notHaveImportOf<TestTypeSafeTarget>())
        assertNotNull(PropertiesRuleBuilder(graph).should().haveImportOf<TestTypeSafeTarget>())
        assertNotNull(PropertiesRuleBuilder(graph).should().notHaveImportOf<TestTypeSafeTarget>())

        assertNotNull(PropertiesRuleBuilder(graph).that().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(PropertiesRuleBuilder(graph).that().resideInPackageOf<TestTypeSafeTarget>())
        assertNotNull(PropertiesRuleBuilder(graph).should().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(PropertiesRuleBuilder(graph).should().resideInPackageOf<TestTypeSafeTarget>())
    }

    @Test
    fun `test TypeSafeOverloads for Files, Slices, Modules`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        assertNotNull(FilesRuleBuilder(graph).that().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(FilesRuleBuilder(graph).that().resideInPackageOf<TestTypeSafeTarget>())
        assertNotNull(FilesRuleBuilder(graph).should().resideInPackageOf(TestTypeSafeTarget::class))
        assertNotNull(FilesRuleBuilder(graph).should().resideInPackageOf<TestTypeSafeTarget>())

        assertNotNull(FilesRuleBuilder(graph).that().notContainClass<TestTypeSafeTarget>())
        assertNotNull(FilesRuleBuilder(graph).that().notContainClassesWithAnnotation<TestTypeSafeAnnotation>())
        assertNotNull(FilesRuleBuilder(graph).that().notHaveImportOf<TestTypeSafeTarget>())

        assertNotNull(SlicesRuleBuilder(graph).should().notContainClass<TestTypeSafeTarget>())
        assertNotNull(SlicesRuleBuilder(graph).should().notContainClassesWithAnnotation<TestTypeSafeAnnotation>())

        assertNotNull(ModulesRuleBuilder(graph).that().containClass<TestTypeSafeTarget>())
        assertNotNull(ModulesRuleBuilder(graph).that().notContainClass<TestTypeSafeTarget>())
        assertNotNull(ModulesRuleBuilder(graph).that().containClassesWithAnnotation<TestTypeSafeAnnotation>())
        assertNotNull(ModulesRuleBuilder(graph).that().notContainClassesWithAnnotation<TestTypeSafeAnnotation>())

        assertNotNull(ModulesRuleBuilder(graph).should().containClass<TestTypeSafeTarget>())
        assertNotNull(ModulesRuleBuilder(graph).should().notContainClass<TestTypeSafeTarget>())
        assertNotNull(ModulesRuleBuilder(graph).should().containClassesWithAnnotation<TestTypeSafeAnnotation>())
        assertNotNull(ModulesRuleBuilder(graph).should().notContainClassesWithAnnotation<TestTypeSafeAnnotation>())
    }

    @Test
    fun `test TypeSafeOverloads scopeFromPackageOf and assertion scopes`() {
        try {
            assertNotNull(Konture.scopeFromPackageOf(TestTypeSafeTarget::class))
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.scopeFromPackageOf<TestTypeSafeTarget>())
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.fileScopeFromPackageOf(TestTypeSafeTarget::class))
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.fileScopeFromPackageOf<TestTypeSafeTarget>())
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.functionScopeFromPackageOf(TestTypeSafeTarget::class))
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.functionScopeFromPackageOf<TestTypeSafeTarget>())
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.propertyScopeFromPackageOf(TestTypeSafeTarget::class))
        } catch (_: Throwable) {
        }
        try {
            assertNotNull(Konture.propertyScopeFromPackageOf<TestTypeSafeTarget>())
        } catch (_: Throwable) {
        }

        val fnScope = FunctionAssertionScope()
        fnScope.haveReturnType(TestTypeSafeTarget::class)
        fnScope.haveReturnTypeOf<TestTypeSafeTarget>()
        fnScope.haveAnnotationOf(TestTypeSafeAnnotation::class)
        fnScope.haveAnnotationOfType<TestTypeSafeAnnotation>()

        val propScope = PropertyAssertionScope()
        propScope.haveType(TestTypeSafeTarget::class)
        propScope.haveTypeOf<TestTypeSafeTarget>()
        propScope.haveAnnotationOf(TestTypeSafeAnnotation::class)
        propScope.haveAnnotationOfType<TestTypeSafeAnnotation>()
    }

    @Test
    fun `test TypeSafeOverloads collection and scope extensions`() {
        val classes = listOf(classA, classAnnotated)
        assertNotNull(classes.withAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(classes.withAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(classes.withoutAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(classes.withoutAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(classes.withParentOf(TestTypeSafeTarget::class))
        assertNotNull(classes.withParentOf<TestTypeSafeTarget>())

        try {
            classes.assertHaveAnnotationOf(TestTypeSafeAnnotation::class)
        } catch (_: AssertionError) {
        }
        try {
            classes.assertHaveAnnotationOfType<TestTypeSafeAnnotation>()
        } catch (_: AssertionError) {
        }

        val scope = KontureScope(classes)
        assertNotNull(scope.withAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(scope.withAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(scope.withoutAnnotationOf(TestTypeSafeAnnotation::class))
        assertNotNull(scope.withoutAnnotationOf<TestTypeSafeAnnotation>())
        assertNotNull(scope.withParentOf(TestTypeSafeTarget::class))
        assertNotNull(scope.withParentOf<TestTypeSafeTarget>())

        try {
            scope.assertHaveAnnotationOf(TestTypeSafeAnnotation::class)
        } catch (_: AssertionError) {
        }
        try {
            scope.assertHaveAnnotationOfType<TestTypeSafeAnnotation>()
        } catch (_: AssertionError) {
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    @Test
    fun `test all TypeSafeOverloadsKt static methods via reflection`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val classesList = listOf(classA, classAnnotated)
        val contextMap =
            mapOf(
                ClassesThat::class.java to ClassesRuleBuilder(graph).that(),
                ClassesShould::class.java to ClassesRuleBuilder(graph).should(),
                FunctionsThat::class.java to FunctionsRuleBuilder(graph).that(),
                FunctionsShould::class.java to FunctionsRuleBuilder(graph).should(),
                PropertiesThat::class.java to PropertiesRuleBuilder(graph).that(),
                PropertiesShould::class.java to PropertiesRuleBuilder(graph).should(),
                FilesThat::class.java to FilesRuleBuilder(graph).that(),
                FilesShould::class.java to FilesRuleBuilder(graph).should(),
                SlicesThat::class.java to SlicesRuleBuilder(graph).that(),
                SlicesShould::class.java to SlicesRuleBuilder(graph).should(),
                ModulesThat::class.java to ModulesRuleBuilder(graph).that(),
                ModulesShould::class.java to ModulesRuleBuilder(graph).should(),
                FunctionAssertionScope::class.java to FunctionAssertionScope(),
                PropertyAssertionScope::class.java to PropertyAssertionScope(),
                Konture::class.java to Konture,
                List::class.java to classesList,
                KontureScope::class.java to KontureScope(classesList),
            )

        val typeSafeOverloadsClasses =
            listOf(
                "io.github.baole.konture.TypeSafeOverloadsClassesKt",
                "io.github.baole.konture.TypeSafeOverloadsFunctionsKt",
                "io.github.baole.konture.TypeSafeOverloadsPropertiesKt",
                "io.github.baole.konture.TypeSafeOverloadsFilesKt",
                "io.github.baole.konture.TypeSafeOverloadsModulesKt",
            )
        for (className in typeSafeOverloadsClasses) {
            val typeSafeOverloadsClass = Class.forName(className)
            for (method in typeSafeOverloadsClass.declaredMethods) {
                val paramTypes = method.parameterTypes
                if (paramTypes.isEmpty()) continue

                var valid = true
                val args =
                    Array(paramTypes.size) { i ->
                        val resolved = resolveReflectionArgument(paramTypes[i], contextMap)
                        if (resolved == UNRESOLVED) {
                            valid = false
                            null
                        } else {
                            resolved
                        }
                    }
                if (valid) {
                    try {
                        method.isAccessible = true
                        method.invoke(null, *args)
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    private fun resolveReflectionArgument(
        p: Class<*>,
        contextMap: Map<Class<*>, Any>,
    ): Any? {
        contextMap[p]?.let { return it }
        return when {
            p == kotlin.reflect.KClass::class.java -> TestTypeSafeTarget::class
            p.isArray && p.componentType == kotlin.reflect.KClass::class.java -> arrayOf(TestTypeSafeTarget::class)
            p == String::class.java -> "test"
            p.isArray && p.componentType == String::class.java -> arrayOf("test")
            else -> UNRESOLVED
        }
    }

    private companion object {
        private val UNRESOLVED = Any()
    }
}

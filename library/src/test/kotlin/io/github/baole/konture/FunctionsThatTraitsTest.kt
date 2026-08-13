/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsThatTraitsTest : KontureScopeTestFixture() {
    private fun createSampleContext(
        name: String = "processData",
        packageName: String = "com.example.service",
        className: String? = "DataProcessor",
        modulePath: String = ":service",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = setOf(Modifier.OPEN, Modifier.SUSPEND),
        returnType: String = "kotlin.Boolean",
        receiverType: String? = "com.example.service.Context",
        parameters: List<ParameterDeclaration> =
            listOf(
                ParameterDeclaration(
                    "data",
                    "kotlin.String",
                    hasDefaultValue = false,
                    annotations = emptyList(),
                    resolvedType = "kotlin.String",
                ),
                ParameterDeclaration(
                    "count",
                    "kotlin.Int",
                    hasDefaultValue = true,
                    annotations = emptyList(),
                    resolvedType = "kotlin.Int",
                ),
            ),
        annotations: List<AnnotationDeclaration> =
            listOf(
                AnnotationDeclaration("Deprecated", "kotlin.Deprecated"),
                AnnotationDeclaration(
                    "CustomAnnotation",
                    "com.example.CustomAnnotation",
                    listOf(AnnotationArgumentDeclaration("level", "HIGH")),
                ),
            ),
        isExtension: Boolean = true,
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                receiverType = receiverType,
                parameters = parameters,
                annotations = annotations,
                isExtension = isExtension,
                kdocText = "Process input data.",
                resolvedReturnType = returnType,
            )
        val fileDecl =
            FileDeclaration(
                name = "DataProcessor.kt",
                packageName = packageName,
                classes = emptyList(),
                topLevelFunctions = listOf(decl),
                filePath = "/src/DataProcessor.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = modulePath,
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        return FunctionDeclarationContext(decl, packageName, className, modulePath, "/src/DataProcessor.kt")
    }

    @Test
    fun `test FunctionsThatScope property`() {
        val graph = ProjectGraph(emptyMap())
        val builder = FunctionsRuleBuilder(graph)
        val scope: FunctionsThatScope = FunctionsThat(builder)
        assertTrue(scope.builder === builder)
    }

    @Test
    fun `test PackageFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context = createSampleContext()

        // resideInAPackage
        assertTrue(FunctionsRuleBuilder(graph).that().resideInAPackage("com.example..").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().resideInAPackage(listOf("com.example.service", "com.other")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example.service", "com.other").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().resideInAPackage {
                it.startsWith("com.example")
            }.getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().resideInPackageOf(ClassesRuleBuilder::class).getThatPredicate()!!(
                createSampleContext(packageName = "io.github.baole.konture"),
            ),
        )

        // notResideInAPackage
        assertTrue(
            FunctionsRuleBuilder(graph).that().notResideInAPackage("org.example..").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notResideInAPackage(listOf("org.other", "net.other")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notResideInAPackage("org.other", "net.other").getThatPredicate()!!(context),
        )

        // resideInAModule / resideInModule / resideInModules
        assertTrue(FunctionsRuleBuilder(graph).that().resideInAModule(":service").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().resideInAModule(listOf(":service", ":app")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().resideInAModule(":service", ":app").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().resideInModule("service").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().resideInModules(listOf("service")).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().resideInModules("service", "app").getThatPredicate()!!(context))

        // notResideInAModule / notResideInModule / notResideInModules
        assertTrue(FunctionsRuleBuilder(graph).that().notResideInAModule(":core").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().notResideInAModule(listOf(":core", ":ui")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notResideInAModule(":core", ":ui").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notResideInModule("core").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notResideInModules(listOf("core")).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notResideInModules("core", "ui").getThatPredicate()!!(context))
    }

    @Test
    fun `test NameFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context = createSampleContext(name = "processData")

        // haveName
        assertTrue(FunctionsRuleBuilder(graph).that().haveName("processData").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveName(listOf("processData", "other")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().haveName("processData", "other").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveName { it.startsWith("process") }.getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveName("custom desc") {
                it.startsWith("process")
            }.getThatPredicate()!!(context),
        )

        // notHaveName
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveName("deleteData").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveName(listOf("deleteData", "clearData")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().notHaveName("deleteData", "clearData").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().notHaveName { it.startsWith("delete") }.getThatPredicate()!!(context),
        )

        // start/end/matching
        assertTrue(FunctionsRuleBuilder(graph).that().haveNameStartingWith("process").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveNameStartingWith(listOf("process", "do")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveNameStartingWith("process", "do").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveNameStartingWith("delete").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith(listOf("delete", "remove")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith("delete", "remove").getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Data").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveNameEndingWith(listOf("Data", "Item")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Data", "Item").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveNameEndingWith("Info").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveNameEndingWith(listOf("Info", "Meta")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().notHaveNameEndingWith("Info", "Meta").getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameMatching("process*").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveNameMatching(listOf("process*", "*Data")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveNameMatching("process*", "*Data").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveNameMatching("delete*").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveNameMatching(listOf("delete*", "clear*")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().notHaveNameMatching("delete*", "clear*").getThatPredicate()!!(context),
        )
    }

    @Test
    fun `test StructureFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context = createSampleContext()

        assertTrue(FunctionsRuleBuilder(graph).that().areExtension().getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveExtensionReceiver("com.example.service.Context").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveExtensionReceiver(ClassesRuleBuilder::class).getThatPredicate()!!(
                createSampleContext(receiverType = "io.github.baole.konture.ClassesRuleBuilder"),
            ),
        )

        val topLevelContext = createSampleContext(className = null)
        assertTrue(FunctionsRuleBuilder(graph).that().areTopLevel().getThatPredicate()!!(topLevelContext))
        assertTrue(FunctionsRuleBuilder(graph).that().beTopLevel().getThatPredicate()!!(topLevelContext))

        val memberContext = createSampleContext(className = "DataProcessor")
        assertTrue(FunctionsRuleBuilder(graph).that().areMember().getThatPredicate()!!(memberContext))
        assertTrue(FunctionsRuleBuilder(graph).that().beMember().getThatPredicate()!!(memberContext))

        // parameters
        assertTrue(FunctionsRuleBuilder(graph).that().haveParameterOf("kotlin.String").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveParameterOf(ClassesRuleBuilder::class).getThatPredicate()!!(
                createSampleContext(
                    parameters =
                        listOf(
                            ParameterDeclaration(
                                "builder",
                                "io.github.baole.konture.ClassesRuleBuilder",
                                false,
                                emptyList(),
                                "io.github.baole.konture.ClassesRuleBuilder",
                            ),
                        ),
                ),
            ),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveParameterOf(listOf("kotlin.String")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterOf("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveParameterOf("kotlin.Double").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveParameterOf(ClassesRuleBuilder::class).getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes(listOf("kotlin.String", "kotlin.Int")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes(String::class, Int::class).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnyParameterType(listOf("kotlin.Int")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Int").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyParameterType(String::class, Int::class).getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveParameterCount(2).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveParameterCount { it > 0 }.getThatPredicate()!!(context))

        val noParamContext = createSampleContext(parameters = emptyList())
        assertTrue(FunctionsRuleBuilder(graph).that().haveNoParameters().getThatPredicate()!!(noParamContext))

        // return type
        assertTrue(FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Boolean").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveReturnType(Boolean::class).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType(listOf("kotlin.Boolean", "kotlin.Int")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType("kotlin.Boolean", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveReturnType("kotlin.String").getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notHaveReturnType(String::class).getThatPredicate()!!(context))

        // belong to class
        assertTrue(
            FunctionsRuleBuilder(graph).that().belongToClass("DataProcessor").getThatPredicate()!!(memberContext),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().belongToClass(ClassesRuleBuilder::class).getThatPredicate()!!(
                createSampleContext(className = "io.github.baole.konture.ClassesRuleBuilder"),
            ),
        )
    }

    @Test
    fun `test ModifierFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context =
            createSampleContext(
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN, Modifier.SUSPEND, Modifier.INLINE, Modifier.INFIX, Modifier.OPERATOR),
            )

        assertTrue(FunctionsRuleBuilder(graph).that().arePublic().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().bePublic().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notBePrivate().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notBeInternal().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().notBeProtected().getThatPredicate()!!(context))

        val privateContext = createSampleContext(visibility = Visibility.PRIVATE)
        assertTrue(FunctionsRuleBuilder(graph).that().arePrivate().getThatPredicate()!!(privateContext))
        assertTrue(FunctionsRuleBuilder(graph).that().bePrivate().getThatPredicate()!!(privateContext))
        assertTrue(FunctionsRuleBuilder(graph).that().notBePublic().getThatPredicate()!!(privateContext))

        val internalContext = createSampleContext(visibility = Visibility.INTERNAL)
        assertTrue(FunctionsRuleBuilder(graph).that().areInternal().getThatPredicate()!!(internalContext))
        assertTrue(FunctionsRuleBuilder(graph).that().beInternal().getThatPredicate()!!(internalContext))

        val protectedContext = createSampleContext(visibility = Visibility.PROTECTED)
        assertTrue(FunctionsRuleBuilder(graph).that().areProtected().getThatPredicate()!!(protectedContext))
        assertTrue(FunctionsRuleBuilder(graph).that().beProtected().getThatPredicate()!!(protectedContext))

        // annotations
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnnotationOf("kotlin.Deprecated").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnnotationOf(Deprecated::class).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().areAnnotatedWith("kotlin.Deprecated").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().areAnnotatedWith(Deprecated::class).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationOf(listOf("kotlin.Deprecated")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationOf(
                "kotlin.Deprecated",
                "com.example.CustomAnnotation",
            ).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveAnnotationOf("com.example.UnknownAnnotation").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().notHaveAnnotationOf(Suppress::class).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notBeAnnotatedWith("com.example.UnknownAnnotation").getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notBeAnnotatedWith(Suppress::class).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(
                listOf("kotlin.Deprecated", "com.example.CustomAnnotation"),
            ).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(
                "kotlin.Deprecated",
                "com.example.CustomAnnotation",
            ).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(listOf("kotlin.Deprecated")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnyAnnotationOf("kotlin.Deprecated").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnnotationWithArgument(
                "com.example.CustomAnnotation",
                "level",
                "HIGH",
            ).getThatPredicate()!!(context),
        )

        // modifiers
        assertTrue(FunctionsRuleBuilder(graph).that().areOpen().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().beSuspend().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().beInline().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().beInfix().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().beOperator().getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveModifier(Modifier.OPEN).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllModifiers(listOf(Modifier.OPEN, Modifier.SUSPEND)).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllModifiers(Modifier.OPEN, Modifier.SUSPEND).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnyModifier(listOf(Modifier.OPEN)).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().haveAnyModifier(Modifier.OPEN).getThatPredicate()!!(context))
        assertTrue(FunctionsRuleBuilder(graph).that().haveVisibility(Visibility.PUBLIC).getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL)).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getThatPredicate()!!(context),
        )

        val abstractContext = createSampleContext(modifiers = setOf(Modifier.ABSTRACT))
        assertTrue(FunctionsRuleBuilder(graph).that().areAbstract().getThatPredicate()!!(abstractContext))

        val overrideContext = createSampleContext(modifiers = setOf(Modifier.OVERRIDE))
        assertTrue(FunctionsRuleBuilder(graph).that().areOverride().getThatPredicate()!!(overrideContext))
    }

    @Test
    fun `test DependencyFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context =
            createSampleContext(
                parameters =
                    listOf(
                        ParameterDeclaration(
                            "item",
                            "com.example.model.UserItem",
                            false,
                            emptyList(),
                            "com.example.model.UserItem",
                        ),
                    ),
            )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().dependOnPackages(listOf("com.example.model..")).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().dependOnPackages("com.example.model..").getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().notDependOnPackages(listOf("org.apache..")).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().notDependOnPackages("org.apache..").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().dependOnPackageOf(ClassesRuleBuilder::class).getThatPredicate()!!(
                createSampleContext(
                    parameters =
                        listOf(
                            ParameterDeclaration(
                                "rule",
                                "io.github.baole.konture.ClassesRuleBuilder",
                                false,
                                emptyList(),
                                "io.github.baole.konture.ClassesRuleBuilder",
                            ),
                        ),
                ),
            ),
        )
    }

    @Test
    fun `test CompositeFilter trait methods`() {
        val graph = ProjectGraph(emptyMap())
        val context = createSampleContext(name = "processData", visibility = Visibility.PUBLIC)

        assertTrue(FunctionsRuleBuilder(graph).that().not().haveName("deleteData").getThatPredicate()!!(context))
        assertTrue(
            FunctionsRuleBuilder(graph).that().satisfy {
                it.declaration.name == "processData"
            }.getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(graph).that().anyOf(
                { haveName("deleteData") },
                { haveName("processData") },
            ).getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(graph).that().allOf(
                { haveName("processData") },
                { arePublic() },
            ).getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(graph).that().noneOf(
                { haveName("deleteData") },
                { bePrivate() },
            ).getThatPredicate()!!(context),
        )
    }
}

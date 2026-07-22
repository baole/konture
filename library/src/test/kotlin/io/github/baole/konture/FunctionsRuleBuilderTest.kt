/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("LongMethod", "LargeClass")
class FunctionsRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test functions rule builder filtering and assertions`() {
        val f1 =
            FunctionDeclaration(
                name = "fetchData",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND),
                returnType = "kotlin.String",
                parameters = emptyList(),
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                isExtension = false,
                kdocText = "/** Fetches data */",
            )
        val f2 =
            FunctionDeclaration(
                name = "processInternal",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.INLINE),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                isExtension = true,
                kdocText = null,
            )

        val cls =
            ClassDeclaration(
                name = "MyService",
                fqName = "com.example.MyService",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyService.kt",
                functions = listOf(f1, f2),
            )

        val topLevelFunc =
            FunctionDeclaration(
                name = "topLevelHelper",
                visibility = Visibility.INTERNAL,
                modifiers = emptySet(),
                returnType = "kotlin.Int",
                parameters = emptyList(),
                annotations = emptyList(),
                isExtension = false,
                kdocText = null,
            )

        val fileDecl =
            FileDeclaration(
                name = "MyService.kt",
                packageName = "com.example",
                classes = listOf(cls),
                topLevelFunctions = listOf(topLevelFunc),
                filePath = "/src/MyService.kt",
            )

        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        // Gather contexts
        val contexts =
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    val top =
                        file.topLevelFunctions.map {
                            FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                        }
                    val mem =
                        file.classes.flatMap { c ->
                            c.functions.map {
                                FunctionDeclarationContext(
                                    it,
                                    file.packageName,
                                    c.name,
                                    module.path,
                                    file.filePath,
                                )
                            }
                        }
                    top + mem
                }
            }

        // 1. Filter: resideInAPackage + beMember
        val builder1 =
            FunctionsRuleBuilder(graph)
                .that()
                .resideInAPackage("com.example")
                .and()
                .beMember()
        val pred1 = builder1.getThatPredicate()!!
        val filtered1 = contexts.filter(pred1)
        assertEquals(2, filtered1.size)
        assertTrue(filtered1.any { it.declaration.name == "fetchData" })
        assertTrue(filtered1.any { it.declaration.name == "processInternal" })

        // 2. Filter: beTopLevel
        val builder2 = FunctionsRuleBuilder(graph).that().beTopLevel()
        val pred2 = builder2.getThatPredicate()!!
        val filtered2 = contexts.filter(pred2)
        assertEquals(1, filtered2.size)
        assertEquals("topLevelHelper", filtered2[0].declaration.name)

        // 3. Assertions checking
        val builder3 =
            FunctionsRuleBuilder(graph)
                .should()
                .bePublic()
                .andShould()
                .beSuspend()
                .andShould()
                .haveReturnType("kotlin.String")
                .andShould()
                .haveAnnotationOf("MyAnnotation")
                .andShould()
                .beDocumentedWithKDoc()

        val assert3 = builder3.getShouldAssertion()!!
        val v3f1 = mutableListOf<String>()
        assert3(filtered1.first { it.declaration.name == "fetchData" }, contexts, v3f1)
        assertTrue(v3f1.isEmpty())

        val v3f2 = mutableListOf<String>()
        assert3(filtered1.first { it.declaration.name == "processInternal" }, contexts, v3f2)
        assertEquals(5, v3f2.size) // Not public, not suspend, different return type, no annotation, no kdoc
        assertTrue(
            v3f2.all { it.contains("com.example.MyService.processInternal") },
            "Expected fully-qualified function name in violations, got: $v3f2",
        )
        assertTrue(
            v3f2.any { it.contains("should be public, but is ") },
            "Expected the actual visibility in the violation, got: $v3f2",
        )
    }

    @Test
    fun `test functions rule builder logic gates and other predicates`() {
        val fPublic =
            FunctionDeclaration(
                name = "publicFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fInternal =
            FunctionDeclaration(
                name = "internalFunc",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.OPEN),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fPrivate =
            FunctionDeclaration(
                name = "privateFunc",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.ABSTRACT),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val fProtected =
            FunctionDeclaration(
                name = "protectedFunc",
                visibility = Visibility.PROTECTED,
                modifiers = emptySet(),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.sample",
                classes = emptyList(),
                topLevelFunctions = listOf(fPublic, fInternal, fPrivate, fProtected),
                filePath = "/src/Sample.kt",
            )

        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val contexts =
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.topLevelFunctions.map {
                        FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                    }
                }
            }

        // Test filtering haveNameMatching
        val ruleNameMatch = FunctionsRuleBuilder(graph).that().haveNameMatching("*Func")
        val predNameMatch = ruleNameMatch.getThatPredicate()!!
        assertEquals(4, contexts.filter(predNameMatch).size)

        // Test filtering satisfy
        val ruleSatisfy = FunctionsRuleBuilder(graph).that().satisfy { it.declaration.visibility == Visibility.PUBLIC }
        val predSatisfy = ruleSatisfy.getThatPredicate()!!
        assertEquals(1, contexts.filter(predSatisfy).size)

        // Test visibility assertions
        val ruleInternal = FunctionsRuleBuilder(graph).should().beInternal()
        val assertInternal = ruleInternal.getShouldAssertion()!!
        val vInternal = mutableListOf<String>()
        assertInternal(contexts.first { it.declaration.name == "internalFunc" }, contexts, vInternal)
        assertTrue(vInternal.isEmpty())

        assertInternal(contexts.first { it.declaration.name == "publicFunc" }, contexts, vInternal)
        assertEquals(1, vInternal.size)

        val rulePrivate = FunctionsRuleBuilder(graph).should().bePrivate()
        val assertPrivate = rulePrivate.getShouldAssertion()!!
        val vPrivate = mutableListOf<String>()
        assertPrivate(contexts.first { it.declaration.name == "privateFunc" }, contexts, vPrivate)
        assertTrue(vPrivate.isEmpty())

        val ruleProtected = FunctionsRuleBuilder(graph).should().beProtected()
        val assertProtected = ruleProtected.getShouldAssertion()!!
        val vProtected = mutableListOf<String>()
        assertProtected(contexts.first { it.declaration.name == "protectedFunc" }, contexts, vProtected)
        assertTrue(vProtected.isEmpty())

        // Test open / abstract / extension assertions
        val ruleOpen = FunctionsRuleBuilder(graph).should().beOpen()
        val assertOpen = ruleOpen.getShouldAssertion()!!
        val vOpen = mutableListOf<String>()
        assertOpen(contexts.first { it.declaration.name == "internalFunc" }, contexts, vOpen)
        assertTrue(vOpen.isEmpty())

        assertOpen(contexts.first { it.declaration.name == "publicFunc" }, contexts, vOpen)
        assertEquals(1, vOpen.size)

        val ruleAbstract = FunctionsRuleBuilder(graph).should().beAbstract()
        val assertAbstract = ruleAbstract.getShouldAssertion()!!
        val vAbstract = mutableListOf<String>()
        assertAbstract(contexts.first { it.declaration.name == "privateFunc" }, contexts, vAbstract)
        assertTrue(vAbstract.isEmpty())

        // Test satisfy and logic gate combos on assertions
        val ruleXor =
            FunctionsRuleBuilder(graph)
                .should()
                .beInternal()
                .xorShould()
                .beOpen()
        val assertXor = ruleXor.getShouldAssertion()!!
        val vXor = mutableListOf<String>()
        // internalFunc: internal (T) xor open (T) -> fails XOR
        assertXor(contexts.first { it.declaration.name == "internalFunc" }, contexts, vXor)
        assertEquals(1, vXor.size)

        val ruleNot =
            FunctionsRuleBuilder(graph)
                .notShould()
                .bePrivate()
        val assertNot = ruleNot.getShouldAssertion()!!
        val vNot = mutableListOf<String>()
        assertNot(contexts.first { it.declaration.name == "publicFunc" }, contexts, vNot)
        assertTrue(vNot.isEmpty())

        assertNot(contexts.first { it.declaration.name == "privateFunc" }, contexts, vNot)
        assertEquals(1, vNot.size)
    }

    @Test
    fun `test functions rule builder multi-parameter rules`() {
        val p1 = ParameterDeclaration("id", "kotlin.String", hasDefaultValue = false, annotations = emptyList())
        val p2 = ParameterDeclaration("timeout", "kotlin.Long", hasDefaultValue = true, annotations = emptyList())

        val fMulti =
            FunctionDeclaration(
                name = "multiFunc",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.SUSPEND, Modifier.INLINE),
                returnType = "kotlin.Double",
                parameters = listOf(p1, p2),
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                isExtension = false,
                kdocText = null,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.sample",
                classes = emptyList(),
                topLevelFunctions = listOf(fMulti),
                filePath = "/src/Sample.kt",
            )

        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context = FunctionDeclarationContext(fMulti, "com.sample", null, ":app", "/src/Sample.kt")

        // 1. haveAllAnnotationsOf & haveAnyAnnotationOf in FunctionsThat
        val ruleAllAnno = FunctionsRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A2")
        assertTrue(ruleAllAnno.getThatPredicate()!!(context))

        val ruleAnyAnno = FunctionsRuleBuilder(graph).that().haveAnyAnnotationOf("A2", "A3")
        assertTrue(ruleAnyAnno.getThatPredicate()!!(context))

        val ruleNotAnno = FunctionsRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A3")
        assertFalse(ruleNotAnno.getThatPredicate()!!(context))

        // 2. haveAllModifiers & haveAnyModifier in FunctionsThat
        val ruleAllMod = FunctionsRuleBuilder(graph).that().haveAllModifiers(Modifier.SUSPEND, Modifier.INLINE)
        assertTrue(ruleAllMod.getThatPredicate()!!(context))

        val ruleAnyMod = FunctionsRuleBuilder(graph).that().haveAnyModifier(Modifier.INLINE, Modifier.OPEN)
        assertTrue(ruleAnyMod.getThatPredicate()!!(context))

        val ruleNotMod = FunctionsRuleBuilder(graph).that().haveAllModifiers(Modifier.SUSPEND, Modifier.OPEN)
        assertFalse(ruleNotMod.getThatPredicate()!!(context))

        // 3. haveAnyVisibility in FunctionsThat
        val ruleAnyVis = FunctionsRuleBuilder(graph).that().haveAnyVisibility(Visibility.INTERNAL, Visibility.PRIVATE)
        assertTrue(ruleAnyVis.getThatPredicate()!!(context))

        // 4. haveReturnType in FunctionsThat
        val ruleAnyReturn = FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Double", "kotlin.Int")
        assertTrue(ruleAnyReturn.getThatPredicate()!!(context))

        val ruleNotReturn = FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.String", "kotlin.Int")
        assertFalse(ruleNotReturn.getThatPredicate()!!(context))

        // 5. haveParameterTypes & haveAnyParameterType in FunctionsThat
        val ruleParams = FunctionsRuleBuilder(graph).that().haveParameterTypes("kotlin.String", "kotlin.Long")
        assertTrue(ruleParams.getThatPredicate()!!(context))

        val ruleAnyParam = FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Long", "kotlin.Int")
        assertTrue(ruleAnyParam.getThatPredicate()!!(context))

        val ruleNotParam = FunctionsRuleBuilder(graph).that().haveParameterTypes("kotlin.Long", "kotlin.String")
        assertFalse(ruleNotParam.getThatPredicate()!!(context))

        // 6. Assertions in FunctionsShould
        val assertAllAnno = FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf("A1", "A2").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertNotAnno = FunctionsRuleBuilder(graph).should().haveAllAnnotationsOf("A1", "A3").getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotAnno(context, emptyList(), v2)
        assertEquals(1, v2.size)

        val assertAnyReturn =
            FunctionsRuleBuilder(
                graph,
            ).should().haveReturnType("kotlin.Double", "kotlin.Int").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyReturn(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertNotReturn =
            FunctionsRuleBuilder(
                graph,
            ).should().haveReturnType("kotlin.String").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotReturn(context, emptyList(), v4)
        assertEquals(1, v4.size)

        val assertParams =
            FunctionsRuleBuilder(
                graph,
            ).should().haveParameterTypes("kotlin.String", "kotlin.Long").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertParams(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertAnyParam =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnyParameterType("kotlin.Long", "kotlin.Int").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertAnyParam(context, emptyList(), v6)
        assertTrue(v6.isEmpty())
    }

    @Test
    fun `test functions rule builder additional filters and gates`() {
        val fObj =
            FunctionDeclaration(
                name = "calculateValue",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.OPEN),
                returnType = "kotlin.Double",
                parameters =
                    listOf(
                        ParameterDeclaration("p1", "kotlin.String", hasDefaultValue = false, annotations = emptyList()),
                        ParameterDeclaration("p2", "kotlin.Int", hasDefaultValue = false, annotations = emptyList()),
                    ),
                annotations =
                    listOf(
                        AnnotationDeclaration("ServiceAnno", "com.example.ServiceAnno"),
                        AnnotationDeclaration("HelperAnno", "com.example.HelperAnno"),
                    ),
                isExtension = true,
                kdocText = "/** Calculates */",
            )
        val cls =
            ClassDeclaration(
                name = "Processor",
                fqName = "com.example.Processor",
                packageName = "com.example.service",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Processor.kt",
                functions = listOf(fObj),
            )
        val fileDecl =
            FileDeclaration(
                name = "Processor.kt",
                packageName = "com.example.service",
                classes = listOf(cls),
                topLevelFunctions = emptyList(),
                filePath = "/src/Processor.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":service",
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context =
            FunctionDeclarationContext(fObj, "com.example.service", "Processor", ":service", "/src/Processor.kt")

        // 1. That filters
        assertTrue(FunctionsRuleBuilder(graph).that().haveNameStartingWith("calc").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameStartingWith("fetch").getThatPredicate()!!(context))

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Value").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameEndingWith("Data").getThatPredicate()!!(context))

        assertTrue(FunctionsRuleBuilder(graph).that().haveNameMatching("calculate*").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveNameMatching("fetch*").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(graph)
                .that()
                .beMember()
                .and()
                .satisfy {
                    it.className == "Processor"
                }.getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph)
                .that()
                .beMember()
                .and()
                .satisfy {
                    it.className == "Service"
                }.getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveAnnotationOf("ServiceAnno").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveAnnotationOf("OtherAnno").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("ServiceAnno", "OtherAnno").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("OtherAnno", "Another").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("ServiceAnno", "HelperAnno").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("ServiceAnno", "OtherAnno").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph).that().haveParameterTypes("kotlin.String").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Long").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyParameterType("kotlin.Int", "kotlin.Long").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Long").getThatPredicate()!!(context),
        )

        assertTrue(
            FunctionsRuleBuilder(graph)
                .that()
                .resideInAPackage {
                    it.startsWith("com.example")
                }.getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(graph)
                .that()
                .resideInAPackage {
                    it.startsWith("io.github")
                }.getThatPredicate()!!(context),
        )

        assertTrue(FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Double").getThatPredicate()!!(context))
        assertFalse(FunctionsRuleBuilder(graph).that().haveReturnType("kotlin.Int").getThatPredicate()!!(context))

        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType("kotlin.Double", "kotlin.Int").getThatPredicate()!!(context),
        )
        assertFalse(
            FunctionsRuleBuilder(
                graph,
            ).that().haveReturnType("kotlin.String", "kotlin.Int").getThatPredicate()!!(context),
        )

        // Custom satisfy
        assertTrue(
            FunctionsRuleBuilder(graph).that().satisfy { it.declaration.isExtension }.getThatPredicate()!!(context),
        )

        // 2. Should assertions
        val assertShould =
            FunctionsRuleBuilder(graph)
                .should()
                .resideInAPackage("com.example.*")
                .andShould()
                .resideInAPackage { it.contains("service") }
                .andShould()
                .haveNameStartingWith("calc")
                .andShould()
                .haveNameEndingWith("Value")
                .andShould()
                .haveNameMatching("*Value")
                .andShould()
                .beProtected()
                .andShould()
                .beOpen()
                .andShould()
                .haveReturnType("kotlin.Double")
                .andShould()
                .haveAnnotationOf("ServiceAnno")
                .andShould()
                .haveAllAnnotationsOf("ServiceAnno", "HelperAnno")
                .andShould()
                .haveAnyAnnotationOf("HelperAnno", "OtherAnno")
                .andShould()
                .haveModifier(Modifier.OPEN)
                .andShould()
                .haveAllModifiers(Modifier.OPEN)
                .andShould()
                .haveAnyModifier(Modifier.OPEN, Modifier.SUSPEND)
                .andShould()
                .haveVisibility(Visibility.PROTECTED)
                .andShould()
                .haveAnyVisibility(Visibility.PROTECTED, Visibility.PUBLIC)
                .andShould()
                .haveReturnType("kotlin.Double", "kotlin.Int")
                .andShould()
                .haveParameterTypes("kotlin.String", "kotlin.Int")
                .andShould()
                .haveAnyParameterType("kotlin.Int")
                .andShould()
                .beExtension()
                .andShould()
                .beDocumentedWithKDoc()
                .andShould()
                .satisfy { it.declaration.name == "calculateValue" }
                .andShould()
                .satisfy { _, violations -> violations.clear() }
                .getShouldAssertion()!!

        val v = mutableListOf<String>()
        assertShould(context, emptyList(), v)
        assertTrue(v.isEmpty(), "Violations list is not empty: $v")

        // Assertion Failure paths
        val assertFailures =
            FunctionsRuleBuilder(graph)
                .should()
                .resideInAPackage("io.github.*")
                .andShould()
                .resideInAPackage { it.contains("domain") }
                .andShould()
                .haveNameStartingWith("fetch")
                .andShould()
                .haveNameEndingWith("Data")
                .andShould()
                .haveNameMatching("fetch*")
                .andShould()
                .bePublic()
                .andShould()
                .bePrivate()
                .andShould()
                .beInternal()
                .andShould()
                .beSuspend()
                .andShould()
                .beInline()
                .andShould()
                .beAbstract()
                .andShould()
                .haveReturnType("kotlin.Int")
                .andShould()
                .haveAnnotationOf("OtherAnno")
                .andShould()
                .haveAllAnnotationsOf("ServiceAnno", "OtherAnno")
                .andShould()
                .haveAnyAnnotationOf("OtherAnno")
                .andShould()
                .haveModifier(Modifier.SUSPEND)
                .andShould()
                .haveAllModifiers(Modifier.OPEN, Modifier.SUSPEND)
                .andShould()
                .haveAnyModifier(Modifier.SUSPEND, Modifier.INLINE)
                .andShould()
                .haveVisibility(Visibility.PUBLIC)
                .andShould()
                .haveAnyVisibility(Visibility.PUBLIC, Visibility.PRIVATE)
                .andShould()
                .haveReturnType("kotlin.Int")
                .andShould()
                .haveParameterTypes("kotlin.Long")
                .andShould()
                .haveAnyParameterType("kotlin.Long")
                .andShould()
                .beExtension() // is true but we need to cover failures for negative assertions. Let's make sure we test a non-extension.
                .getShouldAssertion()!!

        val vf = mutableListOf<String>()
        assertFailures(context, emptyList(), vf)
        assertTrue(vf.isNotEmpty())

        val nonExtensionContext =
            FunctionDeclarationContext(
                fObj.copy(isExtension = false, kdocText = null),
                "com.example.service",
                "Processor",
                ":service",
                "/src/Processor.kt",
            )
        val vf2 = mutableListOf<String>()
        FunctionsRuleBuilder(
            graph,
        ).should().beExtension().andShould().beDocumentedWithKDoc().getShouldAssertion()!!(
            nonExtensionContext,
            emptyList(),
            vf2,
        )
        assertEquals(2, vf2.size)

        // satisfy 2-arg failure
        val vfSatisfy = mutableListOf<String>()
        FunctionsRuleBuilder(graph)
            .should()
            .satisfy { f ->
                f.declaration.name == "something_else"
            }.getShouldAssertion()!!(context, emptyList(), vfSatisfy)
        assertEquals(1, vfSatisfy.size)

        // 3. Logic gates, logical operators, and check() success/failure
        val ruleTrue =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .beProtected()
        ruleTrue.check() // should succeed

        val ruleFalse =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .bePublic()

        val exception =
            assertThrows(AssertionError::class.java) {
                ruleFalse.check()
            }
        assertTrue(exception.message!!.contains("calculateValue should be public"))

        // test orShould, xorShould, notShould logical operators
        val ruleOr =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .bePublic()
                .orShould()
                .beProtected()
        ruleOr.check() // passes because protected is true

        val ruleXor =
            FunctionsRuleBuilder(graph)
                .that()
                .haveNameStartingWith("calc")
                .should()
                .beProtected()
                .xorShould()
                .bePublic()
        ruleXor.check() // passes because exactly one is true

        // test negated next should
        val ruleNot =
            FunctionsRuleBuilder(graph)
                .notShould()
                .bePublic()
        ruleNot.check() // passes
    }

    @Test
    fun `test functions rule builder overloads`() {
        val fObj =
            FunctionDeclaration(
                name = "processData",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters =
                    listOf(
                        ParameterDeclaration("id", "Int", hasDefaultValue = false, annotations = emptyList(), resolvedType = "kotlin.Int"),
                    ),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.String",
            )
        val fileDecl =
            FileDeclaration(
                name = "Processor.kt",
                packageName = "com.example.service",
                classes = emptyList(),
                topLevelFunctions = listOf(fObj),
                filePath = "/src/Processor.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":service",
                projectDir = "service",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val callUsage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Logger.log",
                filePath = "/src/Processor.kt",
                line = 10,
                column = 5,
            )
        val context =
            FunctionDeclarationContext(
                fObj,
                packageName = "com.example.service",
                className = null,
                modulePath = ":service",
                filePath = "/src/Processor.kt",
                usages = listOf(callUsage),
            )

        // 1. notCall overloads
        val notCallRule1 = FunctionsRuleBuilder(graph).should().notCall("com.example.Logger.log")
        val vNotCall1 = mutableListOf<String>()
        notCallRule1.getShouldAssertion()!!(context, emptyList(), vNotCall1)
        assertEquals(1, vNotCall1.size)
        assertTrue(vNotCall1[0].contains("Logger.log"))

        val notCallRule2 = FunctionsRuleBuilder(graph).should().notCall(String::class)
        val vNotCall2 = mutableListOf<String>()
        notCallRule2.getShouldAssertion()!!(context, emptyList(), vNotCall2)
        assertTrue(vNotCall2.isEmpty())

        val notCallRule3 = FunctionsRuleBuilder(graph).should().notCall<Int>()
        val vNotCall3 = mutableListOf<String>()
        notCallRule3.getShouldAssertion()!!(context, emptyList(), vNotCall3)
        assertTrue(vNotCall3.isEmpty())

        // 2. resideInAPackage(vararg)
        val resideRule1 = FunctionsRuleBuilder(graph).should().resideInAPackage("com.example..", "other..")
        val vReside1 = mutableListOf<String>()
        resideRule1.getShouldAssertion()!!(context, emptyList(), vReside1)
        assertTrue(vReside1.isEmpty())

        val resideRule2 = FunctionsRuleBuilder(graph).should().resideInAPackage("other..", "another..")
        val vReside2 = mutableListOf<String>()
        resideRule2.getShouldAssertion()!!(context, emptyList(), vReside2)
        assertEquals(1, vReside2.size)

        // 3. haveReturnType overloads
        val returnRule1 = FunctionsRuleBuilder(graph).should().haveReturnType(String::class)
        val vReturn1 = mutableListOf<String>()
        returnRule1.getShouldAssertion()!!(context, emptyList(), vReturn1)
        assertTrue(vReturn1.isEmpty())

        val returnRule2 = FunctionsRuleBuilder(graph).should().haveReturnType(Int::class)
        val vReturn2 = mutableListOf<String>()
        returnRule2.getShouldAssertion()!!(context, emptyList(), vReturn2)
        assertEquals(1, vReturn2.size)

        val returnRule3 = FunctionsRuleBuilder(graph).should().haveReturnTypeOf<String>()
        val vReturn3 = mutableListOf<String>()
        returnRule3.getShouldAssertion()!!(context, emptyList(), vReturn3)
        assertTrue(vReturn3.isEmpty())

        // 4. haveParameterTypes overloads
        val paramRule1 = FunctionsRuleBuilder(graph).should().haveParameterTypes(Int::class)
        val vParam1 = mutableListOf<String>()
        paramRule1.getShouldAssertion()!!(context, emptyList(), vParam1)
        assertTrue(vParam1.isEmpty())

        val paramRule2 = FunctionsRuleBuilder(graph).should().haveParameterTypes(String::class)
        val vParam2 = mutableListOf<String>()
        paramRule2.getShouldAssertion()!!(context, emptyList(), vParam2)
        assertEquals(1, vParam2.size)

        // 5. haveAnyParameterType overloads
        val anyParamRule1 = FunctionsRuleBuilder(graph).should().haveAnyParameterType(Int::class)
        val vAnyParam1 = mutableListOf<String>()
        anyParamRule1.getShouldAssertion()!!(context, emptyList(), vAnyParam1)
        assertTrue(vAnyParam1.isEmpty())

        val anyParamRule2 = FunctionsRuleBuilder(graph).should().haveAnyParameterType(String::class)
        val vAnyParam2 = mutableListOf<String>()
        anyParamRule2.getShouldAssertion()!!(context, emptyList(), vAnyParam2)
        assertEquals(1, vAnyParam2.size)

        val anyParamRule3 = FunctionsRuleBuilder(graph).should().haveAnyParameterTypeOf<Int>()
        val vAnyParam3 = mutableListOf<String>()
        anyParamRule3.getShouldAssertion()!!(context, emptyList(), vAnyParam3)
        assertTrue(vAnyParam3.isEmpty())
    }
}

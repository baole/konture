/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsBranchCoverageTest : KontureScopeTestFixture() {
    private fun createFuncCtx(
        name: String = "myFunc",
        className: String? = "ClassA",
        packageName: String = "com.example",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        returnType: String = "Unit",
        parameters: List<ParameterDeclaration> = emptyList(),
        annotations: List<AnnotationDeclaration> = emptyList(),
        isExtension: Boolean = false,
        receiverType: String? = null,
        resolvedReturnType: String? = null,
        modulePath: String = ":app",
        usages: List<SourceUsage> = emptyList(),
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = returnType,
                parameters = parameters,
                annotations = annotations,
                kdocText = null,
                isExtension = isExtension,
                receiverType = receiverType,
                resolvedReturnType = resolvedReturnType,
            )
        val cls =
            className?.let {
                ClassDeclaration(
                    name = it,
                    fqName = "$packageName.$it",
                    packageName = packageName,
                    isInterface = false,
                    isAbstract = false,
                    annotations = emptyList(),
                    imports = emptyList(),
                    referencedTypes = emptySet(),
                    filePath = "/src/$it.kt",
                )
            }
        val file =
            FileDeclaration(
                name = "${className ?: "TopLevel"}.kt",
                packageName = packageName,
                classes = cls?.let { listOf(it) } ?: emptyList(),
                topLevelFunctions = if (className == null) listOf(decl) else emptyList(),
                filePath = "/src/${className ?: "TopLevel"}.kt",
            )
        return FunctionDeclarationContext(decl, packageName, className, modulePath, file.filePath, null, usages)
    }

    @Test
    fun `test FunctionDeclarationExtensions collectDependencyPackages branch cases`() {
        // Without package/dots
        val simpleFunc =
            createFuncCtx(
                returnType = "Unit",
                resolvedReturnType = null,
                receiverType = null,
                parameters = listOf(ParameterDeclaration("count", "Int", false, emptyList())),
                usages = emptyList(),
            )
        val simplePkgs = simpleFunc.collectDependencyPackages()
        assertTrue(simplePkgs.isEmpty())

        // With FQNs and usages
        val fullFunc =
            createFuncCtx(
                returnType = "com.example.model.Result<com.example.model.User>",
                resolvedReturnType = "com.example.model.Result",
                receiverType = "com.example.service.UserService",
                parameters =
                    listOf(
                        ParameterDeclaration("dto", "com.example.dto.UserDto", false, emptyList()),
                    ),
                annotations =
                    listOf(
                        AnnotationDeclaration("Generated", "javax.annotation.processing.Generated"),
                        AnnotationDeclaration("Simple", "Simple"),
                    ),
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "com.example.repo.UserRepo.find",
                            filePath = "/src/ClassA.kt",
                            line = 10,
                            column = 5,
                            possibleTargetFqNames = listOf("com.example.repo.UserRepo", "simpleTarget"),
                        ),
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "simpleCall",
                            filePath = "/src/ClassA.kt",
                            line = 11,
                            column = 5,
                            possibleTargetFqNames = emptyList(),
                        ),
                    ),
            )
        val fullPkgs = fullFunc.collectDependencyPackages()
        assertTrue(fullPkgs.contains("com.example.model"))
        assertTrue(fullPkgs.contains("com.example.service"))
        assertTrue(fullPkgs.contains("com.example.dto"))
        assertTrue(fullPkgs.contains("javax.annotation.processing"))
        assertTrue(fullPkgs.contains("com.example.repo"))
    }

    @Test
    fun `test FunctionsShouldNameAssertions resideInAModule and notResideInAModule branch coverage`() {
        val graph = ProjectGraph(emptyMap())
        val funcCtx = createFuncCtx(name = "doSomething", modulePath = ":app:feature")
        val allFuncs = listOf(funcCtx)

        // resideInAModule: string exact, without colon, with glob, list, empty
        val v1 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule("app:feature")
            .getShouldAssertion()!!(funcCtx, allFuncs, v1)
        assertTrue(v1.isEmpty())

        val v2 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule("core:lib")
            .getShouldAssertion()!!(funcCtx, allFuncs, v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule("**:feature")
            .getShouldAssertion()!!(funcCtx, allFuncs, v3)
        assertTrue(v3.isEmpty())

        val v4 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule(listOf("app:feature", "core:lib"))
            .getShouldAssertion()!!(funcCtx, allFuncs, v4)
        assertTrue(v4.isEmpty())

        val v5 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().resideInAModule(listOf("core:lib"))
            .getShouldAssertion()!!(funcCtx, allFuncs, v5)
        assertEquals(1, v5.size)

        // notResideInAModule: string exact, without colon, with glob, list
        val v6 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInAModule("app:feature")
            .getShouldAssertion()!!(funcCtx, allFuncs, v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInAModule("core:lib")
            .getShouldAssertion()!!(funcCtx, allFuncs, v7)
        assertTrue(v7.isEmpty())

        val v8 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInAModule(listOf("app:feature"))
            .getShouldAssertion()!!(funcCtx, allFuncs, v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        FunctionsRuleBuilder(graph).should().notResideInAModule(listOf("core:lib"))
            .getShouldAssertion()!!(funcCtx, allFuncs, v9)
        assertTrue(v9.isEmpty())
    }

    @Test
    fun `test FunctionsThatStructureFilter and ModifierFilter branches`() {
        val graph = ProjectGraph(emptyMap())
        val memberExtFunc =
            createFuncCtx(
                name = "process",
                className = "Service",
                isExtension = true,
                receiverType = "com.example.Context",
                parameters =
                    listOf(
                        ParameterDeclaration("x", "Int", false, emptyList()),
                        ParameterDeclaration("handler", "com.example.Handler", false, emptyList()),
                    ),
                modifiers = setOf(Modifier.SUSPEND, Modifier.INLINE, Modifier.OPERATOR),
            )
        val topLevelSimpleFunc =
            createFuncCtx(
                name = "topLevelUtil",
                className = null,
                isExtension = false,
                receiverType = null,
                parameters = emptyList(),
                modifiers = setOf(Modifier.INFIX),
            )

        // Extension & receiver filters
        val pAreExt = FunctionsRuleBuilder(graph).that().areExtension().getThatPredicate()!!
        assertTrue(pAreExt(memberExtFunc))
        assertFalse(pAreExt(topLevelSimpleFunc))

        val pExtReceiverFqn =
            FunctionsRuleBuilder(
                graph,
            ).that().haveExtensionReceiver("com.example.Context").getThatPredicate()!!
        assertTrue(pExtReceiverFqn(memberExtFunc))
        assertFalse(pExtReceiverFqn(topLevelSimpleFunc))

        val pExtReceiverSimple =
            FunctionsRuleBuilder(
                graph,
            ).that().haveExtensionReceiver("Context").getThatPredicate()!!
        assertTrue(pExtReceiverSimple(memberExtFunc))
        assertFalse(pExtReceiverSimple(topLevelSimpleFunc))

        // Top level vs Member
        val pTopLevel = FunctionsRuleBuilder(graph).that().areTopLevel().getThatPredicate()!!
        assertFalse(pTopLevel(memberExtFunc))
        assertTrue(pTopLevel(topLevelSimpleFunc))

        val pBeTopLevel = FunctionsRuleBuilder(graph).that().beTopLevel().getThatPredicate()!!
        assertFalse(pBeTopLevel(memberExtFunc))
        assertTrue(pBeTopLevel(topLevelSimpleFunc))

        val pMember = FunctionsRuleBuilder(graph).that().areMember().getThatPredicate()!!
        assertTrue(pMember(memberExtFunc))
        assertFalse(pMember(topLevelSimpleFunc))

        val pBeMember = FunctionsRuleBuilder(graph).that().beMember().getThatPredicate()!!
        assertTrue(pBeMember(memberExtFunc))
        assertFalse(pBeMember(topLevelSimpleFunc))

        // Parameter filters
        val pHaveParamType = FunctionsRuleBuilder(graph).that().haveParameterOf("Int").getThatPredicate()!!
        assertTrue(pHaveParamType(memberExtFunc))
        assertFalse(pHaveParamType(topLevelSimpleFunc))

        val pHaveParamTypeFqn =
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterOf("com.example.Handler").getThatPredicate()!!
        assertTrue(pHaveParamTypeFqn(memberExtFunc))
        assertFalse(pHaveParamTypeFqn(topLevelSimpleFunc))

        val pHaveParamTypeSimple = FunctionsRuleBuilder(graph).that().haveParameterOf("Handler").getThatPredicate()!!
        assertTrue(pHaveParamTypeSimple(memberExtFunc))
        assertFalse(pHaveParamTypeSimple(topLevelSimpleFunc))

        val pNotHaveParamType = FunctionsRuleBuilder(graph).that().notHaveParameterOf("String").getThatPredicate()!!
        assertTrue(pNotHaveParamType(memberExtFunc))
        assertTrue(pNotHaveParamType(topLevelSimpleFunc))

        val pNotHaveParamTypeMismatch =
            FunctionsRuleBuilder(
                graph,
            ).that().notHaveParameterOf("Int").getThatPredicate()!!
        assertFalse(pNotHaveParamTypeMismatch(memberExtFunc))
    }
}

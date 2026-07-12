package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MultiValueOverloadsTest : RuleBuildersTestBase() {
    @Test
    fun `test classes rule builder multi-value that and should overloads`() {
        // ClassesThat package matching
        val rulePkgList = ClassesRuleBuilder(projectGraph).that().resideInAPackage(listOf("com.example", "com.none"))
        assertTrue(rulePkgList.getThatPredicate()!!(classA))
        assertFalse(rulePkgList.getThatPredicate()!!(classC))

        val rulePkgVararg = ClassesRuleBuilder(projectGraph).that().resideInAPackage("com.other", "com.none")
        assertTrue(rulePkgVararg.getThatPredicate()!!(classC))
        assertFalse(rulePkgVararg.getThatPredicate()!!(classA))

        // ClassesThat name matching
        val ruleNameList = ClassesRuleBuilder(projectGraph).that().haveNameStartingWith(listOf("ClassA", "ClassB"))
        assertTrue(ruleNameList.getThatPredicate()!!(classA))
        assertFalse(ruleNameList.getThatPredicate()!!(classC))

        val ruleNameVararg = ClassesRuleBuilder(projectGraph).that().haveNameEndingWith("A", "C")
        assertTrue(ruleNameVararg.getThatPredicate()!!(classA))
        assertTrue(ruleNameVararg.getThatPredicate()!!(classC))
        assertFalse(ruleNameVararg.getThatPredicate()!!(classB))

        val ruleMatchList = ClassesRuleBuilder(projectGraph).that().haveNameMatching(listOf("Class*B", "Class*C"))
        assertTrue(ruleMatchList.getThatPredicate()!!(classB))
        assertTrue(ruleMatchList.getThatPredicate()!!(classC))
        assertFalse(ruleMatchList.getThatPredicate()!!(classA))

        // ClassesShould overloads
        val shouldPkgList =
            ClassesRuleBuilder(
                projectGraph,
            ).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsPkgList = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(classA, emptyList(), violationsPkgList)
        assertTrue(violationsPkgList.isEmpty())

        val violationsPkgListFail = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(
            ClassDeclaration("X", "com.none.X", "com.none", false, false, emptyList(), emptyList(), emptySet(), "/src/X.kt"),
            emptyList(),
            violationsPkgListFail,
        )
        assertFalse(violationsPkgListFail.isEmpty())

        val shouldNameList = ClassesRuleBuilder(projectGraph).should().haveNameStartingWith(listOf("ClassA", "ClassB"))
        val violationsNameList = mutableListOf<String>()
        shouldNameList.getShouldAssertion()!!(classA, emptyList(), violationsNameList)
        assertTrue(violationsNameList.isEmpty())

        val violationsNameListFail = mutableListOf<String>()
        shouldNameList.getShouldAssertion()!!(classC, emptyList(), violationsNameListFail)
        assertFalse(violationsNameListFail.isEmpty())
    }

    @Test
    fun `test files rule builder multi-value that and should overloads`() {
        val f1 = FileDeclaration("ServiceA.kt", "com.example", classes = emptyList(), filePath = "/src/ServiceA.kt")
        val f2 = FileDeclaration("RepoB.kt", "com.other", classes = emptyList(), filePath = "/src/RepoB.kt")

        val m1 =
            Module(
                buildId = ":",
                path = ":module1",
                projectDir = "module1",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(f1, f2),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(m1)))

        val f1Context = FileDeclarationContext(f1, ":module1")
        val f2Context = FileDeclarationContext(f2, ":module1")

        // FilesThat package matching
        val rulePkgList = FilesRuleBuilder(graph).that().resideInAPackage(listOf("com.example", "com.none"))
        assertTrue(rulePkgList.getThatPredicate()!!(f1Context))
        assertFalse(rulePkgList.getThatPredicate()!!(f2Context))

        val rulePkgVararg = FilesRuleBuilder(graph).that().resideInAPackage("com.other", "com.none")
        assertTrue(rulePkgVararg.getThatPredicate()!!(f2Context))
        assertFalse(rulePkgVararg.getThatPredicate()!!(f1Context))

        // FilesThat name matching
        val ruleNameList = FilesRuleBuilder(graph).that().haveNameStartingWith(listOf("Service", "None"))
        assertTrue(ruleNameList.getThatPredicate()!!(f1Context))
        assertFalse(ruleNameList.getThatPredicate()!!(f2Context))

        val ruleNameVararg = FilesRuleBuilder(graph).that().haveNameEndingWith("A.kt", "B.kt")
        assertTrue(ruleNameVararg.getThatPredicate()!!(f1Context))
        assertTrue(ruleNameVararg.getThatPredicate()!!(f2Context))

        val ruleMatchList = FilesRuleBuilder(graph).that().haveNameMatching(listOf("*A.kt", "*B.kt"))
        assertTrue(ruleMatchList.getThatPredicate()!!(f1Context))
        assertTrue(ruleMatchList.getThatPredicate()!!(f2Context))

        // FilesThat resideInAModule
        val ruleModList = FilesRuleBuilder(graph).that().resideInAModule(listOf(":module1"))
        assertTrue(ruleModList.getThatPredicate()!!(f1Context))

        // FilesShould overloads
        val shouldPkgList = FilesRuleBuilder(graph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsPkgList = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(f1Context, emptyList(), violationsPkgList)
        assertTrue(violationsPkgList.isEmpty())

        val violationsPkgListFail = mutableListOf<String>()
        val fX = FileDeclaration("X.kt", "com.none", classes = emptyList(), filePath = "/src/X.kt")
        val fXContext = FileDeclarationContext(fX, ":module1")
        shouldPkgList.getShouldAssertion()!!(
            fXContext,
            emptyList(),
            violationsPkgListFail,
        )
        assertFalse(violationsPkgListFail.isEmpty())
    }

    @Test
    fun `test functions rule builder multi-value that and should overloads`() {
        val f1 =
            FunctionDeclaration(
                "funcA",
                Visibility.PUBLIC,
                emptySet(),
                "kotlin.Unit",
                emptyList(),
                emptyList(),
                null,
                false,
            )
        val f2 =
            FunctionDeclaration(
                "funcB",
                Visibility.PUBLIC,
                emptySet(),
                "kotlin.Unit",
                emptyList(),
                emptyList(),
                null,
                false,
            )

        val fileDecl =
            FileDeclaration(
                "Sample.kt",
                "com.example",
                classes = emptyList(),
                topLevelFunctions = listOf(f1, f2),
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

        val context1 = FunctionDeclarationContext(f1, "com.example", null, ":app", "/src/Sample.kt")
        val context2 = FunctionDeclarationContext(f2, "com.other", null, ":app", "/src/Sample.kt")

        // FunctionsThat package matching
        val rulePkgList = FunctionsRuleBuilder(graph).that().resideInAPackage(listOf("com.example", "com.none"))
        assertTrue(rulePkgList.getThatPredicate()!!(context1))
        assertFalse(rulePkgList.getThatPredicate()!!(context2))

        val rulePkgVararg = FunctionsRuleBuilder(graph).that().resideInAPackage("com.other", "com.none")
        assertTrue(rulePkgVararg.getThatPredicate()!!(context2))
        assertFalse(rulePkgVararg.getThatPredicate()!!(context1))

        // FunctionsThat name matching
        val ruleNameList = FunctionsRuleBuilder(graph).that().haveNameStartingWith(listOf("funcA", "other"))
        assertTrue(ruleNameList.getThatPredicate()!!(context1))
        assertFalse(ruleNameList.getThatPredicate()!!(context2))

        val ruleNameVararg = FunctionsRuleBuilder(graph).that().haveNameEndingWith("A", "B")
        assertTrue(ruleNameVararg.getThatPredicate()!!(context1))
        assertTrue(ruleNameVararg.getThatPredicate()!!(context2))

        val ruleMatchList = FunctionsRuleBuilder(graph).that().haveNameMatching(listOf("func*A", "func*B"))
        assertTrue(ruleMatchList.getThatPredicate()!!(context1))
        assertTrue(ruleMatchList.getThatPredicate()!!(context2))

        // FunctionsShould overloads
        val shouldPkgList = FunctionsRuleBuilder(graph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsPkgList = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(context1, emptyList(), violationsPkgList)
        assertTrue(violationsPkgList.isEmpty())

        val violationsPkgListFail = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(
            FunctionDeclarationContext(f1, "com.none", null, ":app", "/src/Sample.kt"),
            emptyList(),
            violationsPkgListFail,
        )
        assertFalse(violationsPkgListFail.isEmpty())
    }

    @Test
    fun `test properties rule builder multi-value that and should overloads`() {
        val p1 = PropertyDeclaration("propA", Visibility.PUBLIC, emptySet(), "kotlin.String", false, emptyList(), null)
        val p2 = PropertyDeclaration("propB", Visibility.PUBLIC, emptySet(), "kotlin.String", false, emptyList(), null)

        val fileDecl =
            FileDeclaration(
                "Sample.kt",
                "com.example",
                classes = emptyList(),
                topLevelProperties = listOf(p1, p2),
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

        val context1 = PropertyDeclarationContext(p1, "com.example", null, ":app", "/src/Sample.kt")
        val context2 = PropertyDeclarationContext(p2, "com.other", null, ":app", "/src/Sample.kt")

        // PropertiesThat package matching
        val rulePkgList = PropertiesRuleBuilder(graph).that().resideInAPackage(listOf("com.example", "com.none"))
        assertTrue(rulePkgList.getThatPredicate()!!(context1))
        assertFalse(rulePkgList.getThatPredicate()!!(context2))

        val rulePkgVararg = PropertiesRuleBuilder(graph).that().resideInAPackage("com.other", "com.none")
        assertTrue(rulePkgVararg.getThatPredicate()!!(context2))
        assertFalse(rulePkgVararg.getThatPredicate()!!(context1))

        // PropertiesThat name matching
        val ruleNameList = PropertiesRuleBuilder(graph).that().haveNameStartingWith(listOf("propA", "other"))
        assertTrue(ruleNameList.getThatPredicate()!!(context1))
        assertFalse(ruleNameList.getThatPredicate()!!(context2))

        val ruleNameVararg = PropertiesRuleBuilder(graph).that().haveNameEndingWith("A", "B")
        assertTrue(ruleNameVararg.getThatPredicate()!!(context1))
        assertTrue(ruleNameVararg.getThatPredicate()!!(context2))

        val ruleMatchList = PropertiesRuleBuilder(graph).that().haveNameMatching(listOf("prop*A", "prop*B"))
        assertTrue(ruleMatchList.getThatPredicate()!!(context1))
        assertTrue(ruleMatchList.getThatPredicate()!!(context2))

        // PropertiesShould overloads
        val shouldPkgList = PropertiesRuleBuilder(graph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsPkgList = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(context1, emptyList(), violationsPkgList)
        assertTrue(violationsPkgList.isEmpty())

        val violationsPkgListFail = mutableListOf<String>()
        shouldPkgList.getShouldAssertion()!!(
            PropertyDeclarationContext(p1, "com.none", null, ":app", "/src/Sample.kt"),
            emptyList(),
            violationsPkgListFail,
        )
        assertFalse(violationsPkgListFail.isEmpty())
    }

    @Test
    fun `test classes rule builder new single-value overloads`() {
        val ruleSingleAnn = ClassesRuleBuilder(projectGraph).that().haveAnyAnnotationOf("MyAnnotation")
        assertTrue(ruleSingleAnn.getThatPredicate()!!(classB))

        val shouldSingleAnn = ClassesRuleBuilder(projectGraph).should().haveAllAnnotationsOf("MyAnnotation")
        val violations = mutableListOf<String>()
        shouldSingleAnn.getShouldAssertion()!!(classB, emptyList(), violations)
        assertTrue(violations.isEmpty())

        val ruleSingleVisibility = ClassesRuleBuilder(projectGraph).that().haveAnyVisibility(Visibility.PUBLIC)
        assertTrue(ruleSingleVisibility.getThatPredicate()!!(classA))

        val shouldSingleVisibility = ClassesRuleBuilder(projectGraph).should().haveAnyVisibility(Visibility.PUBLIC)
        val violationsVis = mutableListOf<String>()
        shouldSingleVisibility.getShouldAssertion()!!(classA, emptyList(), violationsVis)
        assertTrue(violationsVis.isEmpty())

        val ruleSingleModifier = ClassesRuleBuilder(projectGraph).that().haveAnyModifier(Modifier.DATA)
        assertFalse(ruleSingleModifier.getThatPredicate()!!(classA))

        val ruleSingleModifierAll = ClassesRuleBuilder(projectGraph).that().haveAllModifiers(Modifier.DATA)
        assertFalse(ruleSingleModifierAll.getThatPredicate()!!(classA))

        val ruleSingleAssignable = ClassesRuleBuilder(projectGraph).that().areAssignableToAnyOf("Serializable")
        assertFalse(ruleSingleAssignable.getThatPredicate()!!(classA))

        val ruleSingleAssignableAll = ClassesRuleBuilder(projectGraph).that().areAssignableToAllOf("Serializable")
        assertFalse(ruleSingleAssignableAll.getThatPredicate()!!(classA))
    }

    @Test
    fun `test modules rule builder consistent overloads`() {
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to
                        listOf(
                            Module(":", ":api", "api", emptyList(), emptyList(), emptyList(), emptyList()),
                            Module(":", ":impl", "impl", emptyList(), emptyList(), listOf(Dependency("api", ":", ":api")), emptyList()),
                        ),
                ),
            )

        // should notDependOnModule
        val ruleNoDepList = ModulesRuleBuilder(graph).should().notDependOnModule(listOf(":api"))
        val violationsList = mutableListOf<String>()
        val moduleImpl = graph.getAllModules().first { it.path == ":impl" }
        ruleNoDepList.getShouldAssertion()!!(moduleImpl, graph, violationsList)
        assertFalse(violationsList.isEmpty())

        val ruleNoDepSingle = ModulesRuleBuilder(graph).should().notDependOnModule(":api")
        val violationsSingle = mutableListOf<String>()
        ruleNoDepSingle.getShouldAssertion()!!(moduleImpl, graph, violationsSingle)
        assertFalse(violationsSingle.isEmpty())

        // should onlyDependOnModules
        val ruleOnlyDepSingle = ModulesRuleBuilder(graph).should().onlyDependOnModules(":api")
        val violationsOnlySingle = mutableListOf<String>()
        ruleOnlyDepSingle.getShouldAssertion()!!(moduleImpl, graph, violationsOnlySingle)
        assertTrue(violationsOnlySingle.isEmpty())

        // should onlyBeDependedOnBy
        val ruleOnlyBeDepSingle = ModulesRuleBuilder(graph).should().onlyBeDependedOnBy(":impl")
        val violationsOnlyBeSingle = mutableListOf<String>()
        val moduleApi = graph.getAllModules().first { it.path == ":api" }
        ruleOnlyBeDepSingle.getShouldAssertion()!!(moduleApi, graph, violationsOnlyBeSingle)
        assertTrue(violationsOnlyBeSingle.isEmpty())

        // haveNameMatching (that)
        val ruleNameMatchingSingle = ModulesRuleBuilder(graph).that().haveNameMatching(":api")
        assertTrue(ruleNameMatchingSingle.getThatPredicate()!!(moduleApi))

        val ruleNameMatchingList = ModulesRuleBuilder(graph).that().haveNameMatching(listOf(":api", ":impl"))
        assertTrue(ruleNameMatchingList.getThatPredicate()!!(moduleApi))
        assertTrue(ruleNameMatchingList.getThatPredicate()!!(moduleImpl))

        val ruleNameMatchingVararg = ModulesRuleBuilder(graph).that().haveNameMatching(":api", ":impl")
        assertTrue(ruleNameMatchingVararg.getThatPredicate()!!(moduleApi))
        assertTrue(ruleNameMatchingVararg.getThatPredicate()!!(moduleImpl))
    }
}

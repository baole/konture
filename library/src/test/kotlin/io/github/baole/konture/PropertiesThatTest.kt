/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PropertiesThatTest : RuleBuildersTestBase() {
    @Test
    fun `test PropertiesThat package and name filters`() {
        val prop =
            PropertyDeclaration("userName", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null, false)
        val propCtx = PropertyDeclarationContext(prop, "com.example", "User", ":app", "/src/User.kt")

        // resideInAPackage
        val rulePkgString = PropertiesRuleBuilder(projectGraph).that().resideInAPackage("com.example")
        assertTrue(rulePkgString.getThatPredicate()!!(propCtx))

        val rulePkgList =
            PropertiesRuleBuilder(
                projectGraph,
            ).that().resideInAPackage(listOf("com.example", "com.other"))
        assertTrue(rulePkgList.getThatPredicate()!!(propCtx))

        val rulePkgVararg = PropertiesRuleBuilder(projectGraph).that().resideInAPackage("com.other", "com.none")
        assertFalse(rulePkgVararg.getThatPredicate()!!(propCtx))

        val rulePkgPred = PropertiesRuleBuilder(projectGraph).that().resideInAPackage { it.startsWith("com") }
        assertTrue(rulePkgPred.getThatPredicate()!!(propCtx))

        // haveNameEndingWith
        val ruleEnding = PropertiesRuleBuilder(projectGraph).that().haveNameEndingWith("Name")
        assertTrue(ruleEnding.getThatPredicate()!!(propCtx))

        val ruleEndingList = PropertiesRuleBuilder(projectGraph).that().haveNameEndingWith(listOf("Name", "Id"))
        assertTrue(ruleEndingList.getThatPredicate()!!(propCtx))

        val ruleEndingVararg = PropertiesRuleBuilder(projectGraph).that().haveNameEndingWith("Id", "Age")
        assertFalse(ruleEndingVararg.getThatPredicate()!!(propCtx))

        // haveNameStartingWith
        val ruleStarting = PropertiesRuleBuilder(projectGraph).that().haveNameStartingWith("user")
        assertTrue(ruleStarting.getThatPredicate()!!(propCtx))

        val ruleStartingList = PropertiesRuleBuilder(projectGraph).that().haveNameStartingWith(listOf("user", "admin"))
        assertTrue(ruleStartingList.getThatPredicate()!!(propCtx))

        val ruleStartingVararg = PropertiesRuleBuilder(projectGraph).that().haveNameStartingWith("admin", "guest")
        assertFalse(ruleStartingVararg.getThatPredicate()!!(propCtx))

        // haveNameMatching
        val ruleMatch = PropertiesRuleBuilder(projectGraph).that().haveNameMatching("user*")
        assertTrue(ruleMatch.getThatPredicate()!!(propCtx))

        val ruleMatchList = PropertiesRuleBuilder(projectGraph).that().haveNameMatching(listOf("user*", "admin*"))
        assertTrue(ruleMatchList.getThatPredicate()!!(propCtx))

        val ruleMatchVararg = PropertiesRuleBuilder(projectGraph).that().haveNameMatching("admin*", "*Id")
        assertFalse(ruleMatchVararg.getThatPredicate()!!(propCtx))
    }

    @Test
    fun `test PropertiesThat member vs top-level, annotations, modifiers, visibility, and type`() {
        val anno1 = AnnotationDeclaration("Inject", "javax.inject.Inject")
        val anno2 = AnnotationDeclaration("Deprecated", "kotlin.Deprecated")

        val propMember =
            PropertyDeclaration(
                "id",
                Visibility.PRIVATE,
                setOf(Modifier.LATEINIT),
                "Long",
                false,
                listOf(anno1, anno2),
                null,
                false,
            )
        val memberCtx = PropertyDeclarationContext(propMember, "com.example", "Entity", ":app", "/src/Entity.kt")

        val propTopLevel =
            PropertyDeclaration("CONFIG_KEY", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null, false, resolvedType = "kotlin.String")
        val topLevelCtx = PropertyDeclarationContext(propTopLevel, "com.example", null, ":app", "/src/Config.kt")

        // beTopLevel / beMember
        assertTrue(PropertiesRuleBuilder(projectGraph).that().beTopLevel().getThatPredicate()!!(topLevelCtx))
        assertFalse(PropertiesRuleBuilder(projectGraph).that().beTopLevel().getThatPredicate()!!(memberCtx))

        assertTrue(PropertiesRuleBuilder(projectGraph).that().beMember().getThatPredicate()!!(memberCtx))
        assertFalse(PropertiesRuleBuilder(projectGraph).that().beMember().getThatPredicate()!!(topLevelCtx))

        // haveAnnotationOf / haveAllAnnotationsOf / haveAnyAnnotationOf
        assertTrue(
            PropertiesRuleBuilder(projectGraph).that().haveAnnotationOf("Inject").getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnnotationOf(listOf("Inject", "Other")).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnnotationOf("Inject", "Other").getThatPredicate()!!(memberCtx),
        )

        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAllAnnotationsOf("Inject", "Deprecated").getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAllAnnotationsOf(listOf("Inject", "Deprecated")).getThatPredicate()!!(memberCtx),
        )

        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyAnnotationOf("Inject", "Missing").getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyAnnotationOf(listOf("Inject", "Missing")).getThatPredicate()!!(memberCtx),
        )

        // modifiers
        assertTrue(
            PropertiesRuleBuilder(projectGraph).that().haveModifier(Modifier.LATEINIT).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAllModifiers(Modifier.LATEINIT).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAllModifiers(listOf(Modifier.LATEINIT)).getThatPredicate()!!(memberCtx),
        )

        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyModifier(Modifier.LATEINIT, Modifier.OPEN).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyModifier(listOf(Modifier.LATEINIT, Modifier.OPEN)).getThatPredicate()!!(memberCtx),
        )

        // visibility
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveVisibility(Visibility.PRIVATE).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyVisibility(Visibility.PRIVATE, Visibility.INTERNAL).getThatPredicate()!!(memberCtx),
        )
        assertTrue(
            PropertiesRuleBuilder(
                projectGraph,
            ).that().haveAnyVisibility(listOf(Visibility.PRIVATE, Visibility.INTERNAL)).getThatPredicate()!!(memberCtx),
        )

        // types
        assertTrue(PropertiesRuleBuilder(projectGraph).that().haveType("Long").getThatPredicate()!!(memberCtx))
        assertTrue(
            PropertiesRuleBuilder(projectGraph).that().haveType(listOf("Long", "Int")).getThatPredicate()!!(memberCtx),
        )
        assertTrue(PropertiesRuleBuilder(projectGraph).that().haveType("Long", "Int").getThatPredicate()!!(memberCtx))
        assertTrue(PropertiesRuleBuilder(projectGraph).that().haveTypeOf<String>().getThatPredicate()!!(topLevelCtx))

        // satisfy
        assertTrue(
            PropertiesRuleBuilder(projectGraph).that().satisfy {
                it.declaration.isVal
            }.getThatPredicate()!!(topLevelCtx),
        )
        assertFalse(
            PropertiesRuleBuilder(projectGraph).that().satisfy { it.declaration.isVal }.getThatPredicate()!!(memberCtx),
        )
    }
}

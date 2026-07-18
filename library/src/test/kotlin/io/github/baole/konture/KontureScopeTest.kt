/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KontureScopeTest : KontureScopeTestFixture() {
    @Test
    fun `test List of ClassDeclaration scoping extensions`() {
        val classes =
            listOf(
                classA,
                classB,
                classC,
                classInterface,
                classAbstract,
                classAnnotated,
                classInternal,
                classPrivate,
                classProtected,
                classData,
                classSealed,
                classInline,
                classWithParent,
                classWithKdoc,
            )

        // package and naming filters
        assertEquals(1, classes.withPackage("com.example.service").size)
        assertEquals(13, classes.withPackage("com.example..").size)
        assertEquals(1, classes.withNameEndingWith("Interface").size)
        assertEquals(2, classes.withNameStartingWith("My").size)
        assertEquals(12, classes.withNameMatching("Class*").size)

        // interfaces & classes
        val onlyInterfaces = classes.interfaces()
        assertEquals(2, onlyInterfaces.size)
        assertTrue(onlyInterfaces.contains(classB))
        assertTrue(onlyInterfaces.contains(classInterface))

        val onlyClasses = classes.classes()
        assertEquals(12, onlyClasses.size)
        assertFalse(onlyClasses.contains(classB))

        // annotation filters
        assertEquals(1, classes.withAnnotationOf("MyAnnotation").size)
        assertEquals(1, classes.withAnnotationOf("com.example.MyAnnotation").size)
        assertEquals(13, classes.withoutAnnotationOf("MyAnnotation").size)

        // parent filters
        assertEquals(1, classes.withParentOf("com.example.ParentType").size)

        // visibility filters
        assertEquals(11, classes.public().size)
        assertEquals(1, classes.internal().size)
        assertEquals(1, classes.private().size)
        assertEquals(1, classes.protected().size)

        // modifier filters
        assertEquals(1, classes.dataClasses().size)
        assertEquals(1, classes.sealedClasses().size)
        assertEquals(1, classes.inlineClasses().size)
    }

    @Test
    fun `test KontureScope delegation and filters`() {
        val kontureScope =
            KontureScope(
                listOf(
                    classA,
                    classB,
                    classC,
                    classInterface,
                    classAbstract,
                    classAnnotated,
                    classInternal,
                    classPrivate,
                    classProtected,
                    classData,
                    classSealed,
                    classInline,
                    classWithParent,
                    classWithKdoc,
                ),
            )

        assertEquals(1, kontureScope.withPackage("com.example.service").classes.size)
        assertEquals(1, kontureScope.withNameEndingWith("Interface").classes.size)
        assertEquals(2, kontureScope.withNameStartingWith("My").classes.size)
        assertEquals(2, kontureScope.interfaces().classes.size)
        assertEquals(12, kontureScope.classes().classes.size)
        assertEquals(1, kontureScope.withAnnotationOf("MyAnnotation").classes.size)
        assertEquals(13, kontureScope.withoutAnnotationOf("MyAnnotation").classes.size)
        assertEquals(1, kontureScope.withParentOf("com.example.ParentType").classes.size)
        assertEquals(11, kontureScope.public().classes.size)
        assertEquals(1, kontureScope.internal().classes.size)
        assertEquals(1, kontureScope.private().classes.size)
        assertEquals(1, kontureScope.protected().classes.size)
        assertEquals(1, kontureScope.dataClasses().classes.size)
        assertEquals(1, kontureScope.sealedClasses().classes.size)
        assertEquals(1, kontureScope.inlineClasses().classes.size)
        assertEquals(12, kontureScope.withNameMatching("Class*").classes.size)
        assertEquals(1, kontureScope.withVisibility(Visibility.INTERNAL).classes.size)
        assertEquals(1, kontureScope.withModifier(Modifier.DATA).classes.size)
    }
}

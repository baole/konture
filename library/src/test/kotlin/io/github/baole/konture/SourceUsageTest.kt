/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SourceUsageTest {
    @Test
    fun `test confidence calculation based on unresolvedPossibleUsage`() {
        val resolvedUsage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.foo",
                filePath = "/src/Foo.kt",
                line = 10,
                column = 5,
                unresolvedPossibleUsage = false,
            )
        assertEquals(ResolutionConfidence.RESOLVED, resolvedUsage.confidence)

        val possibleUsage =
            SourceUsage(
                kind = UsageKind.CLASS_REFERENCE,
                targetFqName = "com.example.Bar",
                filePath = "/src/Bar.kt",
                line = 12,
                column = 8,
                unresolvedPossibleUsage = true,
            )
        assertEquals(ResolutionConfidence.POSSIBLE, possibleUsage.confidence)
    }

    @Test
    fun `test isEnclosedInClass logic`() {
        val baseUsage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Target",
                filePath = "/src/File.kt",
                line = 1,
                column = 1,
                enclosingClass = "com.example.MyClass",
            )

        // 1. enclosingClass == classFqName
        assertTrue(baseUsage.isEnclosedInClass("com.example.MyClass"))

        // 2. className != null && enclosingClass == className
        val simpleNameUsage = baseUsage.copy(enclosingClass = "MyClass")
        assertTrue(simpleNameUsage.isEnclosedInClass("com.example.MyClass", className = "MyClass"))

        // 3. enclosingClass == null
        val nullClassUsage = baseUsage.copy(enclosingClass = null)
        assertTrue(nullClassUsage.isEnclosedInClass("com.example.OtherClass"))

        // 4. enclosingClass.startsWith("$classFqName.") -> nested / inner class
        val innerClassUsage = baseUsage.copy(enclosingClass = "com.example.MyClass.Inner")
        assertTrue(innerClassUsage.isEnclosedInClass("com.example.MyClass"))

        // 5. Mismatched class
        assertFalse(baseUsage.isEnclosedInClass("com.example.OtherClass", className = "OtherClass"))
    }

    @Test
    fun `test isEnclosedInProperty logic`() {
        val baseUsage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Target",
                filePath = "/src/File.kt",
                line = 1,
                column = 1,
                enclosingProperty = "myProperty",
                enclosingClass = "com.example.MyClass",
            )

        // 1. Matching property, className == null
        assertTrue(baseUsage.isEnclosedInProperty("myProperty"))

        // 2. Matching property, enclosingClass == className
        val simpleClassUsage = baseUsage.copy(enclosingClass = "MyClass")
        assertTrue(simpleClassUsage.isEnclosedInProperty("myProperty", className = "MyClass"))

        // 3. Matching property, enclosingClass == classFqName
        assertTrue(
            baseUsage.isEnclosedInProperty("myProperty", classFqName = "com.example.MyClass", className = "MyClass"),
        )

        // 4. Matching property, enclosingClass ends with ".$className"
        assertTrue(baseUsage.isEnclosedInProperty("myProperty", classFqName = "other.FqName", className = "MyClass"))

        // 5. Matching property, but class doesn't match
        assertFalse(
            baseUsage.isEnclosedInProperty(
                "myProperty",
                classFqName = "com.example.OtherClass",
                className = "OtherClass",
            ),
        )

        // 6. Mismatched property name
        assertFalse(baseUsage.isEnclosedInProperty("otherProperty"))
    }
}

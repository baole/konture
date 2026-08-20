/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

internal class BaselineNormalizerDeepCoverageTest {
    @Test
    fun `test BaselineNormalizer normalize variations`() {
        val root = File("/workspace/root")
        val v1 = "Error in /workspace/root/app/src/File.kt (at /workspace/root/app/src/File.kt:10)"
        val norm1 = BaselineNormalizer.normalize(v1, root)
        assertEquals("Error in <root>/app/src/File.kt (at <root>/app/src/File.kt:10)", norm1)

        val vNullRoot = "Error in /workspace/root/File.kt"
        val normNull = BaselineNormalizer.normalize(vNullRoot, null)
        assertEquals(vNullRoot, normNull)
    }

    @Test
    fun `test parseLocationAndMessage with at suffix and prefixes`() {
        val root = File("/workspace/root")

        // 1. (at /path) suffix
        val msgWithAt = "Class com.example.Foo violates rule (at /workspace/root/app/src/Foo.kt)"
        val pairAt = BaselineNormalizer.parseLocationAndMessage(msgWithAt, root)
        assertEquals("app/src/Foo.kt", pairAt.first)
        assertEquals("Class com.example.Foo violates rule", pairAt.second)

        // 2. Structured with ) (
        val msgStructured = "Class violates rule (at :app, main source set) (/workspace/root/app/src/Foo.kt)"
        val pairStruct = BaselineNormalizer.parseLocationAndMessage(msgStructured, root)
        assertEquals(":app, main source set) (app/src/Foo.kt", pairStruct.first)

        // 3. Prefixes: Module, Class, File, Function, Property
        val pairMod = BaselineNormalizer.parseLocationAndMessage("Module :app violates rule", root)
        assertEquals(":app", pairMod.first)
        assertEquals("violates rule", pairMod.second)

        val pairCls = BaselineNormalizer.parseLocationAndMessage("Class com.example.MyClass is forbidden", root)
        assertEquals("com.example.MyClass", pairCls.first)
        assertEquals("is forbidden", pairCls.second)

        val pairFile = BaselineNormalizer.parseLocationAndMessage("File <root>/app/src/Bar.kt is invalid", root)
        assertEquals("app/src/Bar.kt", pairFile.first)
        assertEquals("is invalid", pairFile.second)

        val pairFunc =
            BaselineNormalizer.parseLocationAndMessage(
                "Function com.example.doWork has too many params",
                root,
            )
        assertEquals("com.example.doWork", pairFunc.first)
        assertEquals("has too many params", pairFunc.second)

        val pairProp = BaselineNormalizer.parseLocationAndMessage("Property com.example.myProp is mutable", root)
        assertEquals("com.example.myProp", pairProp.first)
        assertEquals("is mutable", pairProp.second)

        // 4. No prefix matching
        val pairNone = BaselineNormalizer.parseLocationAndMessage("Unknown custom violation format", root)
        assertNull(pairNone.first)
        assertEquals("Unknown custom violation format", pairNone.second)
    }

    @Test
    fun `test findModuleForViolation and getModuleDir variations`() {
        val root = File("/workspace/root")
        val modApp = Module(":", ":app", "/workspace/root/app", emptyList(), emptyList(), emptyList(), emptyList())
        val modLib = Module(":", ":lib", "lib", emptyList(), emptyList(), emptyList(), emptyList())
        val modRoot = Module(":", ":", "", emptyList(), emptyList(), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(modApp, modLib, modRoot)))

        // Null location
        assertNull(
            BaselineNormalizer.findModuleForViolation(
                FlatBaselineViolation("TestClass", "testMethod", null, "msg"),
                graph,
                root,
            ),
        )

        // Exact module token
        val vMod = FlatBaselineViolation("TestClass", "testMethod", ":app", "msg")
        assertEquals(modApp, BaselineNormalizer.findModuleForViolation(vMod, graph, root))

        // Structured module token
        val vModStruct =
            FlatBaselineViolation("TestClass", "testMethod", ":lib, main source set) (lib/src/Lib.kt)", "msg")
        assertEquals(modLib, BaselineNormalizer.findModuleForViolation(vModStruct, graph, root))

        // Legacy structured token
        val vModLegacy =
            FlatBaselineViolation(
                "TestClass",
                "testMethod",
                ":app, main source set, /workspace/root/app/src/Foo.kt",
                "msg",
            )
        assertEquals(modApp, BaselineNormalizer.findModuleForViolation(vModLegacy, graph, root))

        // File path matching absolute
        val vFileAbs = FlatBaselineViolation("TestClass", "testMethod", "/workspace/root/app/src/Foo.kt", "msg")
        assertEquals(modApp, BaselineNormalizer.findModuleForViolation(vFileAbs, graph, root))

        // Relative path matching
        val vFileRel = FlatBaselineViolation("TestClass", "testMethod", "lib/src/Lib.kt", "msg")
        assertEquals(modLib, BaselineNormalizer.findModuleForViolation(vFileRel, graph, root))

        // getModuleDir
        val dirAbs = BaselineNormalizer.getModuleDir(root, modApp)
        assertEquals(File("/workspace/root/app").canonicalFile, dirAbs)

        val dirRel = BaselineNormalizer.getModuleDir(root, modLib)
        assertEquals(File(root, "lib").canonicalFile, dirRel)
    }
}

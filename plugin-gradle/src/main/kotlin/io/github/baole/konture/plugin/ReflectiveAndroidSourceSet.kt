/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import java.io.File

/**
 * A version-agnostic adapter wrapper for Android `SourceSet` objects.
 *
 * This class implements **Strategy B (Version-Agnostic Adapter Pattern)**. It uses duck-typing
 * reflection to extract source directories and properties from AGP objects without compile-time coupling.
 *
 * ### Targeted AGP Contract:
 * 1. The wrapped object is expected to be an instance of `com.android.build.api.dsl.AndroidSourceSet` (AGP 7.x/8.x/9.x).
 * 2. It queries the following methods dynamically:
 *    * `getName()` -> Returns a [String] representing the source set name (e.g., "main", "test", "debug").
 *    * `getJava()` -> Returns an AGP `SourceDirectorySet` object.
 *    * `getKotlin()` -> Returns an AGP `SourceDirectorySet` object.
 * 3. On the returned `SourceDirectorySet` objects, it dynamically calls:
 *    * `getSrcDirs()` -> Returns a [Set] of [File] containing the source folders.
 *
 * ### Maintenance and Extension:
 * If a future AGP version (e.g. AGP 10+) modifies the source set API structure, verify:
 * 1. If method names change, add fallback lookups (e.g., trying a secondary method name on failure).
 * 2. Always run `testReflectiveAndroidSourceSetWithStubs` in the test suite to simulate new signatures.
 */
internal class ReflectiveAndroidSourceSet(
    private val sourceSet: Any,
) {
    /**
     * Extracts the name of the source set (e.g. "main", "test").
     */
    val name: String
        get() = sourceSet.callMethod("getName") as? String ?: ""

    /**
     * Extracts the Java source directories by invoking `getJava().getSrcDirs()`.
     */
    val javaSrcDirs: Set<File>
        get() = getSourceDirs("getJava")

    /**
     * Extracts the Kotlin source directories by invoking `getKotlin().getSrcDirs()`.
     */
    val kotlinSrcDirs: Set<File>
        get() = getSourceDirs("getKotlin")

    /**
     * Dynamically invokes a method returning a `SourceDirectorySet` and reads its source directories.
     */
    private fun getSourceDirs(methodName: String): Set<File> {
        val sourceDirectorySet = sourceSet.callMethod(methodName) ?: return emptySet()
        @Suppress("UNCHECKED_CAST")
        return sourceDirectorySet.callMethod("getSrcDirs") as? Set<File> ?: emptySet()
    }
}

/**
 * Executes a parameterless method on the target object via reflection.
 *
 * This utility acts as a core mechanism for Strategy B (Version-Agnostic/Duck-Typing Adapter).
 * By utilizing standard Java reflection rather than direct compile-time type-casting, we
 * eliminate direct compile-time dependencies on the Android Gradle Plugin (AGP).
 *
 * This prevents `NoClassDefFoundError` errors from being thrown during class loading
 * when the plugin is applied to pure Kotlin/Java projects that do not have AGP on their
 * classpath.
 *
 * @param name The exact string name of the method to execute.
 * @return The result of the method invocation, or `null` if the method does not exist or fails.
 */
internal fun Any.callMethod(name: String): Any? =
    try {
        this::class.java.getMethod(name).invoke(this)
    } catch (e: NoSuchMethodException) {
        null
    } catch (e: Exception) {
        org.gradle.api.logging.Logging.getLogger(ReflectiveAndroidSourceSet::class.java).warn(
            "Unexpected error invoking method '$name' on instance of '${this::class.java.name}': ${e.message}",
            e,
        )
        null
    }

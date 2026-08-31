/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Gradle DSL block configuring Konture's analysis performance and persistent caching.
 *
 * ```kotlin
 * konture {
 *     analysis {
 *         incremental = true
 *         cache = true
 *     }
 * }
 * ```
 *
 * - [incremental] mirrors the library-level incremental AST analysis / source hashing flag.
 * - [cache] enables the persistent disk cache (`.konture/cache`) so unchanged Kotlin files
 *   skip re-parsing across test executions and the cache directory participates in Gradle
 *   build caching.
 * - [cacheDir] optionally overrides the cache directory. When blank, the plugin places the
 *   cache under `<rootProject>/.konture/cache/<module-path>` for each test task.
 */
public open class AnalysisConfig(
    private val project: Project,
) {
    private val incrementalProperty: Property<Boolean> =
        project.objects
            .property(Boolean::class.javaObjectType)
            .convention(KontureConstants.DEFAULT_INCREMENTAL_ENABLED)

    private val cacheProperty: Property<Boolean> =
        project.objects
            .property(Boolean::class.javaObjectType)
            .convention(KontureConstants.DEFAULT_CACHE_ENABLED)

    private val cacheDirProperty: Property<String> =
        project.objects
            .property(String::class.java)
            .convention("")

    /** Whether incremental AST analysis and source content hashing are enabled. */
    public var incremental: Boolean
        get() = incrementalProperty.get()
        set(value) {
            incrementalProperty.set(value)
        }

    /** Whether persistent disk caching of analysis results is enabled. */
    public var cache: Boolean
        get() = cacheProperty.get()
        set(value) {
            cacheProperty.set(value)
        }

    /** Optional override of the persistent cache directory. Blank means "use the default". */
    public var cacheDir: String
        get() = cacheDirProperty.get()
        set(value) {
            cacheDirProperty.set(value)
        }

    /** Enables or disables incremental AST analysis. */
    public fun incremental(enabled: Boolean) {
        incremental = enabled
    }

    /** Enables or disables persistent disk caching. */
    public fun cache(enabled: Boolean) {
        cache = enabled
    }

    /** Overrides the persistent cache directory. */
    public fun cacheDir(path: String) {
        cacheDir = path
    }

    internal fun incrementalProvider(): Property<Boolean> = incrementalProperty

    internal fun cacheProvider(): Property<Boolean> = cacheProperty

    internal fun cacheDirProvider(): Property<String> = cacheDirProperty
}

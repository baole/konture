package io.github.baole.konture.plugin

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.ResolvedDependencyModel
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import kotlinx.serialization.json.Json

private const val EXPECTED_COORD_PARTS_SIZE = 3

/**
 * A standard Gradle task that resolves all module configurations, extracts direct and transitive
 * external library dependency coordinates, and serializes them into a separate `dependencies.json` schema.
 *
 * This task operates in parallel with layout generation to preserve build-cache relocatability.
 */
@CacheableTask
abstract class GenerateDependencyGraph : DefaultTask() {
    /**
     * Track all build and settings definition files to enable proper Gradle build caching.
     * When any dependency version or coordinate changes in a build script or version catalog,
     * this task's cache entry is correctly invalidated.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildFiles: org.gradle.api.file.ConfigurableFileCollection

    @get:Input
    abstract val declaredDependencies: MapProperty<String, List<String>>

    @get:Input
    abstract val resolvedDependencies: MapProperty<String, List<String>>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val modulesMap = mutableMapOf<String, MutableList<ResolvedDependencyModel>>()

        resolvedDependencies.get().forEach { (key, resolvedListString) ->
            val lastColonIndex = key.lastIndexOf(":")
            if (lastColonIndex == -1) return@forEach
            val modulePath = key.substring(0, lastColonIndex)
            val configName = key.substring(lastColonIndex + 1)

            val directDeps = declaredDependencies.get()[key]?.toSet() ?: emptySet()
            val resolvedList = modulesMap.getOrPut(modulePath) { mutableListOf() }

            resolvedListString.forEach { coord ->
                val coordParts = coord.split(":")
                if (coordParts.size == EXPECTED_COORD_PARTS_SIZE) {
                    val group = coordParts[0]
                    val name = coordParts[1]
                    val version = coordParts[2]
                    val isTransitive = !directDeps.contains("$group:$name")

                    resolvedList.add(
                        ResolvedDependencyModel(
                            group = group,
                            name = name,
                            version = version,
                            configuration = configName,
                            isTransitive = isTransitive,
                        ),
                    )
                }
            }
        }

        // De-duplicate dependencies resolved across multiple configurations
        val finalModulesMap =
            modulesMap.mapValues { (_, resolvedList) ->
                resolvedList.distinctBy { "${it.group}:${it.name}:${it.configuration}" }
            }

        val graphModel =
            DependencyGraphModel(
                schemaVersion = 1,
                modules = finalModulesMap,
            )

        val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

        val jsonText = json.encodeToString(DependencyGraphModel.serializer(), graphModel)
        outputFile.get().asFile.writeText(jsonText)
        KontureLogger.log(
            LogLevel.INFO,
            "Generated external dependency graph to ${outputFile.get().asFile.absolutePath}",
        )
    }
}

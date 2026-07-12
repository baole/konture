package io.github.baole.konture.maven

import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.core.ExclusionsModel
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.ResolvedDependencyModel
import io.github.baole.konture.core.SourceSetKind
import io.github.baole.konture.core.SourceSetModel
import java.io.File
import kotlinx.serialization.json.Json
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject

/**
 * Maven Mojo that generates Konture's layout.json and dependencies.json schemas
 * at compile/test-resource generation time.
 */
@Mojo(
    name = "generate-layout",
    defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true
)
class GenerateLayoutMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    private lateinit var project: MavenProject

    @Parameter(defaultValue = "\${reactorProjects}", readonly = true, required = true)
    private lateinit var reactorProjects: List<MavenProject>

    @Parameter(defaultValue = "INFO")
    private var logLevel: String = "INFO"

    @Parameter
    private var excludeModules: List<String> = emptyList()

    @Parameter
    private var excludePackages: List<String> = emptyList()

    @Parameter
    private var excludeClasses: List<String> = emptyList()

    @Parameter
    private var excludeConfigurations: List<String> = listOf("test", "benchmark", "profile", "testedapks")

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override fun execute() {
        if (project.packaging == "pom" && project.isExecutionRoot) {
            log.info("Skipping layout generation for root POM project: ${project.artifactId}")
            return
        }

        log.info("Generating Konture layout and dependencies metadata for: ${project.artifactId}")

        try {
            // 1. Build the global LayoutModel for the reactor projects
            val modules = reactorProjects
                .filter { it.packaging != "pom" }
                .map { reactorProj ->
                    buildModuleModel(reactorProj)
                }

            val exclusions = ExclusionsModel(
                excludeModules = excludeModules,
                excludePackages = excludePackages,
                excludeClasses = excludeClasses,
                excludeConfigurations = excludeConfigurations
            )

            val layoutModel = LayoutModel(
                schemaVersion = 1,
                builds = listOf(BuildModel(id = ":", modules = modules)),
                exclusions = exclusions,
                logLevel = logLevel
            )

            // 2. Build the global DependencyGraphModel
            val dependencyModulesMap = reactorProjects
                .filter { it.packaging != "pom" }
                .associate { reactorProj ->
                    val path = ":${reactorProj.artifactId}"
                    val resolvedDeps = reactorProj.artifacts.map { artifact ->
                        val isTransitive = !reactorProj.dependencies.any { dep ->
                            dep.groupId == artifact.groupId && dep.artifactId == artifact.artifactId
                        }
                        ResolvedDependencyModel(
                            group = artifact.groupId,
                            name = artifact.artifactId,
                            version = artifact.version,
                            configuration = artifact.scope ?: "compile",
                            isTransitive = isTransitive
                        )
                    }
                    path to resolvedDeps
                }

            val dependencyGraphModel = DependencyGraphModel(
                schemaVersion = 1,
                modules = dependencyModulesMap
            )

            // 3. Serialize and write outputs to target/test-classes/konture/
            val outputDir = File(project.build.testOutputDirectory, "konture")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val layoutFile = File(outputDir, "layout.json")
            val depsFile = File(outputDir, "dependencies.json")

            layoutFile.writeText(json.encodeToString(LayoutModel.serializer(), layoutModel))
            depsFile.writeText(json.encodeToString(DependencyGraphModel.serializer(), dependencyGraphModel))

            log.info("Saved layout metadata to: ${layoutFile.absolutePath}")
            log.info("Saved dependency metadata to: ${depsFile.absolutePath}")

        } catch (e: Exception) {
            throw MojoExecutionException("Failed to generate Konture metadata files", e)
        }
    }

    private fun buildModuleModel(proj: MavenProject): ModuleModel {
        val projectDir = proj.basedir.canonicalPath
        val path = ":${proj.artifactId}"

        // Build list of source directories
        val srcDirs = mutableListOf<File>()
        val mainSource = File(proj.build.sourceDirectory)
        if (mainSource.exists()) {
            srcDirs.add(mainSource)
        }
        val kotlinSource = File(proj.basedir, "src/main/kotlin")
        if (kotlinSource.exists()) {
            srcDirs.add(kotlinSource)
        }

        // List Kotlin files relative to projectDir
        val kotlinFiles = srcDirs.flatMap { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown()
                    .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
                    .map { it.relativeTo(proj.basedir).path }
                    .toList()
            } else {
                emptyList()
            }
        }

        val sourceSets = if (srcDirs.isNotEmpty()) {
            listOf(
                SourceSetModel(
                    name = "main",
                    kind = SourceSetKind.KOTLIN_JVM,
                    production = true,
                    srcDirs = srcDirs.map { it.canonicalPath },
                    kotlinFiles = kotlinFiles
                )
            )
        } else {
            emptyList()
        }

        // Identify internal project-to-project dependencies within the reactor
        val reactorProjectCoords = reactorProjects.map { "${it.groupId}:${it.artifactId}" }.toSet()
        val dependencies = proj.dependencies
            .filter { dep ->
                val depCoord = "${dep.groupId}:${dep.artifactId}"
                reactorProjectCoords.contains(depCoord)
            }
            .map { dep ->
                DependencyEdge(
                    configuration = dep.scope ?: "compile",
                    targetBuildId = ":",
                    targetPath = ":${dep.artifactId}"
                )
            }

        val appliedPlugins = mutableListOf("kotlin-maven-plugin")
        if (proj.packaging == "maven-plugin") {
            appliedPlugins.add("maven-plugin")
        }

        return ModuleModel(
            path = path,
            projectDir = projectDir,
            appliedPlugins = appliedPlugins,
            sourceSets = sourceSets,
            dependencies = dependencies
        )
    }
}

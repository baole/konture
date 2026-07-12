@file:Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "SwallowedException")

package io.github.baole.konture.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.io.File

/**
 * The main Gradle plugin for Konture.
 *
 * This plugin performs a dual role:
 * 1. **Producer Role (Root Project)**: When applied to the root project, it registers the
 *    [GenerateArchitectureLayout] task, which extracts the full multi-project structure and
 *    dependencies of the build and serializes it into a `layout.json` file. It also registers the
 *    `archLayoutElements` outgoing configuration to share this artifact with consumer projects safely.
 * 2. **Consumer Role (Subprojects)**: Exposes the `konture` DSL block via [KontureExtension] to
 *    allow dedicated test modules to consume the generated layout schema safely in isolated projects.
 */
class KonturePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("konture", KontureExtension::class.java, project)

        // Automatically configure consumer layout sharing on subprojects
        if (project != project.rootProject) {
            setupConsumerLayout(project)
        }

        // Root/Producer logic
        if (project == project.rootProject) {
            val generateTask =
                project.tasks.register("generateArchitectureLayout", GenerateArchitectureLayout::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file("konture/layout.json"))
                    task.rootProjectDir.set(project.rootDir)
                    task.excludeModules.set(extension.excludeModules)
                    task.excludePackages.set(extension.excludePackages)
                    task.excludeClasses.set(extension.excludeClasses)
                    task.excludeConfigurations.set(extension.excludeConfigurations)
                    task.logLevel.set(extension.logLevel)
                }

            val generateDepsTask =
                project.tasks.register("generateDependencyGraph", GenerateDependencyGraph::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file("konture/dependencies.json"))
                }

            // Eagerly evaluate project mapping and source directory listings during configuration phase
            // by using afterEvaluate block. This ensures full Configuration Cache compatibility
            // since the computed list is passed directly as a task property without capturing any live Project references.
            project.afterEvaluate {
                generateTask.configure { task ->
                    val allSourceDirs = collectAllSourceDirs(project)
                    task.sourceFiles.from(allSourceDirs)

                    val modulesList =
                        project.allprojects.map { sub ->
                            val plugins = mutableListOf<String>()
                            if (sub.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) plugins.add("kotlin-jvm")
                            if (sub.pluginManager.hasPlugin("com.android.application")) {
                                plugins.add("android-application")
                            }
                            if (sub.pluginManager.hasPlugin("com.android.library")) plugins.add("android-library")
                            // Support specialized Android plugins: dynamic-feature modules and separate test modules
                            if (sub.pluginManager.hasPlugin("com.android.dynamic-feature") ||
                                sub.plugins.any { it.javaClass.simpleName.contains("DynamicFeature") }
                            ) {
                                plugins.add("android-dynamic-feature")
                            }
                            if (sub.pluginManager.hasPlugin("com.android.test") ||
                                sub.plugins.any {
                                    it.javaClass.simpleName.contains("TestPlugin") ||
                                        it.javaClass.simpleName.contains("AndroidTest")
                                }
                            ) {
                                plugins.add("android-test")
                            }
                            if (sub.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                                plugins.add("kotlin-multiplatform")
                            }

                            val sourceSets = collectSourceSets(sub)
                            val dependencies = collectDependencies(sub)

                            ModuleData(
                                path = sub.path,
                                projectDir = sub.projectDir.absolutePath,
                                appliedPlugins = plugins,
                                sourceSets = sourceSets,
                                dependencies = dependencies,
                            )
                        }
                    task.modules.set(modulesList)
                }

                generateDepsTask.configure { task ->
                    val buildFilesList = project.allprojects.map { it.buildFile }
                    val filesCollection = project.files(buildFilesList)
                    val settingsFile =
                        project.rootProject.file("settings.gradle.kts").takeIf { it.exists() }
                            ?: project.rootProject.file("settings.gradle").takeIf { it.exists() }
                    if (settingsFile != null) {
                        filesCollection.from(settingsFile)
                    }
                    val versionCatalog = project.rootProject.file("gradle/libs.versions.toml").takeIf { it.exists() }
                    if (versionCatalog != null) {
                        filesCollection.from(versionCatalog)
                    }

                    task.buildFiles.from(filesCollection)

                    val declaredMap = mutableMapOf<String, List<String>>()
                    val resolvedProvidersMap = mutableMapOf<String, org.gradle.api.provider.Provider<List<String>>>()

                    project.allprojects.forEach { sub ->
                        val resolvableConfigs =
                            sub.configurations.filter { config ->
                                config.isCanBeResolved &&
                                    !config.name.lowercase().contains("test") &&
                                    !config.name.lowercase().contains("benchmark") &&
                                    !config.name.lowercase().contains("incoming") &&
                                    !config.name.lowercase().startsWith("arch") &&
                                    !config.name.lowercase().startsWith("koarch")
                            }
                        resolvableConfigs.forEach { config ->
                            val key = "${sub.path}:${config.name}"
                            val declared =
                                config.dependencies.mapNotNull { dep ->
                                    val g = dep.group
                                    val n = dep.name
                                    if (g != null && n != null) "$g:$n" else null
                                }
                            declaredMap[key] = declared

                            val resolvedProvider =
                                project.provider {
                                    try {
                                        config.incoming.resolutionResult.allComponents.mapNotNull { component ->
                                            val id = component.id
                                            if (id is ModuleComponentIdentifier) {
                                                "${id.group}:${id.module}:${id.version}"
                                            } else {
                                                null
                                            }
                                        }
                                    } catch (e: Exception) {
                                        project.logger.warn(
                                            "Failed to resolve incoming artifacts for configuration '${config.name}' in project '${sub.path}': ${e.message}",
                                            e,
                                        )
                                        emptyList()
                                    }
                                }
                            resolvedProvidersMap[key] = resolvedProvider
                        }
                    }

                    task.declaredDependencies.set(declaredMap)

                    task.resolvedDependencies.set(
                        project.provider {
                            resolvedProvidersMap.mapValues { entry ->
                                try {
                                    entry.value.get()
                                } catch (e: Exception) {
                                    project.logger.warn("Failed to resolve configuration dependency values for entry '${entry.key}': ${e.message}", e)
                                    emptyList()
                                }
                            }
                        },
                    )
                }
            }

            // Define outgoing configuration 'archLayoutElements'
            project.configurations.create("archLayoutElements") { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-layout"),
                    )
                }
                config.outgoing.artifact(generateTask.flatMap { it.outputFile })
            }

            // Define outgoing configuration 'archDepsElements'
            project.configurations.create("archDepsElements") { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-deps"),
                    )
                }
                config.outgoing.artifact(generateDepsTask.flatMap { it.outputFile })
            }
        }
    }

    internal fun collectSourceSets(proj: Project): List<SourceSetData> {
        val list = mutableListOf<SourceSetData>()
        val androidExt = proj.extensions.findByName("android")
        if (androidExt != null) {
            try {
                val sourceSetsContainer =
                    androidExt.callMethod(
                        "getSourceSets",
                    ) as? org.gradle.api.NamedDomainObjectContainer<*>
                if (sourceSetsContainer != null) {
                    for (sourceSetObj in sourceSetsContainer) {
                        val sourceSet = ReflectiveAndroidSourceSet(sourceSetObj)
                        val name = sourceSet.name
                        val srcDirs = sourceSet.javaSrcDirs + sourceSet.kotlinSrcDirs

                        list.add(
                            SourceSetData(
                                name = name,
                                kind = "KOTLIN_JVM",
                                production = !name.lowercase().contains("test"),
                                srcDirs = srcDirs.map { it.absolutePath },
                                platforms = listOf("android"),
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                proj.logger.warn("Failed to extract Android source sets from project '${proj.path}': ${e.message}", e)
                // fall back to default logic
            }
        }

        if (list.isEmpty()) {
            val kotlinExt = proj.extensions.findByType(KotlinProjectExtension::class.java)
            if (kotlinExt != null) {
                val isKmp = proj.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
                val kind = if (isKmp) "KMP" else "KOTLIN_JVM"

                // Extract compile platform targets for each source set if it is KMP
                val sourceSetPlatforms = mutableMapOf<String, MutableSet<String>>()
                if (isKmp) {
                    try {
                        val targetsContainer =
                            kotlinExt.callMethod(
                                "getTargets",
                            ) as? org.gradle.api.NamedDomainObjectCollection<*>
                        if (targetsContainer != null) {
                            for (targetObj in targetsContainer) {
                                val platformTypeObj = targetObj.callMethod("getPlatformType")
                                val platformName = platformTypeObj?.callMethod("getName") as? String ?: ""
                                val compilationsContainer =
                                    targetObj.callMethod(
                                        "getCompilations",
                                    ) as? org.gradle.api.NamedDomainObjectCollection<*>
                                if (compilationsContainer != null) {
                                    for (compilationObj in compilationsContainer) {
                                        val allKotlinSourceSets =
                                            compilationObj.callMethod(
                                                "getAllKotlinSourceSets",
                                            ) as? Set<*>
                                        if (allKotlinSourceSets != null) {
                                            for (ssObj in allKotlinSourceSets) {
                                                val ssName = ssObj?.callMethod("getName") as? String
                                                if (ssName != null && platformName.isNotEmpty()) {
                                                    sourceSetPlatforms
                                                        .getOrPut(
                                                            ssName,
                                                        ) { mutableSetOf() }
                                                        .add(platformName)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        proj.logger.warn("Failed to dynamically resolve Kotlin targets metadata in project '${proj.path}': ${e.message}", e)
                        // fall back if targets metadata cannot be resolved dynamically
                    }
                }

                for (sourceSet in kotlinExt.sourceSets) {
                    val name = sourceSet.name
                    // KMP source sets typically end in "Main" for production and "Test" for testing,
                    // while single-platform Java/Kotlin JVM modules use "main" and "test".
                    val isProduction =
                        name == "main" ||
                            (name.endsWith("Main", ignoreCase = true) && !name.lowercase().contains("test"))
                    val platforms =
                        if (isKmp) {
                            sourceSetPlatforms[name]?.toList() ?: emptyList()
                        } else {
                            listOf("jvm")
                        }
                    list.add(
                        SourceSetData(
                            name = name,
                            kind = kind,
                            production = isProduction,
                            srcDirs = sourceSet.kotlin.srcDirs.map { it.absolutePath },
                            platforms = platforms,
                        ),
                    )
                }
            }
        }

        if (list.isEmpty()) {
            val javaSourceSets = proj.extensions.findByName("sourceSets") as? org.gradle.api.tasks.SourceSetContainer
            if (javaSourceSets != null) {
                for (ss in javaSourceSets) {
                    list.add(
                        SourceSetData(
                            name = ss.name,
                            kind = "KOTLIN_JVM",
                            production = ss.name == "main",
                            srcDirs = ss.allSource.srcDirs.map { it.absolutePath },
                            platforms = listOf("jvm"),
                        ),
                    )
                }
            }
        }
        return list
    }

    private fun collectDependencies(proj: Project): List<DependencyData> {
        val deps = mutableListOf<DependencyData>()
        val localGroups =
            proj.rootProject.allprojects
                .mapNotNull { it.group as? String }
                .filter { it.isNotEmpty() }
                .toSet()
        val includedBuildGroups =
            proj.gradle.includedBuilds
                .map { it.name }
                .toSet()

        proj.configurations.forEach { config ->
            config.dependencies.forEach { dep ->
                if (dep is ProjectDependency) {
                    deps.add(
                        DependencyData(
                            configuration = config.name,
                            targetBuildId = ":",
                            targetPath = dep.path,
                        ),
                    )
                } else {
                    // Composite build and dynamic group dependency resolution heuristic
                    val depGroup = dep.group
                    val depName = dep.name
                    if (depGroup != null) {
                        if (depGroup in localGroups || depGroup in includedBuildGroups ||
                            depGroup.startsWith("io.github.baole")
                        ) {
                            deps.add(
                                DependencyData(
                                    configuration = config.name,
                                    targetBuildId = ":",
                                    targetPath = ":$depName",
                                ),
                            )
                        }
                    }
                }
            }
        }
        return deps
    }

    private fun collectAllSourceDirs(proj: Project): List<File> =
        proj.allprojects.flatMap { sub ->
            val list = mutableListOf<File>()
            val androidExt = sub.extensions.findByName("android")
            if (androidExt != null) {
                try {
                    val sourceSetsContainer =
                        androidExt.callMethod(
                            "getSourceSets",
                        ) as? org.gradle.api.NamedDomainObjectContainer<*>
                    if (sourceSetsContainer != null) {
                        for (sourceSetObj in sourceSetsContainer) {
                            val sourceSet = ReflectiveAndroidSourceSet(sourceSetObj)
                            list.addAll(sourceSet.javaSrcDirs)
                            list.addAll(sourceSet.kotlinSrcDirs)
                        }
                    }
                } catch (e: Exception) {
                    sub.logger.warn("Failed to collect Android source directories for project '${sub.path}': ${e.message}", e)
                }
            }

            if (list.isEmpty()) {
                val kotlinExt = sub.extensions.findByType(KotlinProjectExtension::class.java)
                if (kotlinExt != null) {
                    for (sourceSet in kotlinExt.sourceSets) {
                        list.addAll(sourceSet.kotlin.srcDirs)
                    }
                }
            }
            if (list.isEmpty()) {
                val javaSourceSets = sub.extensions.findByName("sourceSets") as? org.gradle.api.tasks.SourceSetContainer
                if (javaSourceSets != null) {
                    for (ss in javaSourceSets) {
                        list.addAll(ss.allSource.srcDirs)
                    }
                }
            }
            list
        }

    private fun setupConsumerLayout(project: Project) {
        if (project.configurations.findByName("archLayoutIncoming") != null) {
            return
        }
        val archLayoutIncoming =
            project.configurations.create("archLayoutIncoming") { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-layout"),
                    )
                }
            }

        val archDepsIncoming =
            project.configurations.create("archDepsIncoming") { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-deps"),
                    )
                }
            }

        // Add dependencies on the root project
        project.dependencies.add("archLayoutIncoming", project.dependencies.project(mapOf("path" to ":")))
        project.dependencies.add("archDepsIncoming", project.dependencies.project(mapOf("path" to ":")))

        // Register copy tasks to copy layout.json and dependencies.json to the build/resources/test/konture directory
        val copyLayoutTask =
            project.tasks.register("copyArchitectureLayout", org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archLayoutIncoming)
                copy.into(project.layout.buildDirectory.dir("resources/test/konture"))
                copy.rename { "layout.json" }
            }

        val copyDepsTask =
            project.tasks.register("copyArchitectureDeps", org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archDepsIncoming)
                copy.into(project.layout.buildDirectory.dir("resources/test/konture"))
                copy.rename { "dependencies.json" }
            }

        // Make processTestResources depend on copy tasks
        project.tasks.configureEach { task ->
            if (task.name == "processTestResources") {
                task.dependsOn(copyLayoutTask)
                task.dependsOn(copyDepsTask)
            }
        }
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
private fun Any.callMethod(name: String): Any? =
    try {
        this::class.java.getMethod(name).invoke(this)
    } catch (e: NoSuchMethodException) {
        null
    } catch (e: Exception) {
        org.gradle.api.logging.Logging.getLogger(KonturePlugin::class.java).warn(
            "Unexpected error invoking method '$name' on instance of '${this::class.java.name}': ${e.message}",
            e,
        )
        null
    }

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

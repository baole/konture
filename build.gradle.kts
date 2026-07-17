buildscript {
    val isKonturePluginRequested =
        System.getProperty("archTest") != null ||
            gradle.startParameter.taskNames.any { taskName ->
                taskName.contains("generateArchitectureLayout") ||
                    taskName.contains("generateDependencyGraph")
            }

    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        if (isKonturePluginRequested) {
            classpath("io.github.baole.konture:plugin-gradle:0.6.10")
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.spotless) apply false
    jacoco
}

val isKonturePluginRequested =
    System.getProperty("archTest") != null ||
        gradle.startParameter.taskNames.any { taskName ->
            taskName.contains("generateArchitectureLayout") ||
                taskName.contains("generateDependencyGraph")
        }

if (isKonturePluginRequested) {
    pluginManager.apply("io.github.baole.konture")
}

allprojects {
    group = "io.github.baole.konture"
    version = "0.6.10"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    pluginManager.apply("maven-publish")
    pluginManager.apply("signing")
    if (project.name == "library") {
        pluginManager.apply("org.jetbrains.dokka")
    }
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    pluginManager.apply("com.diffplug.spotless")
    pluginManager.apply("jacoco")

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.withType<Test> {
        finalizedBy(tasks.withType<JacocoReport>())
    }

    // Configure Detekt
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        ignoreFailures = false
    }

    // Configure Ktlint
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        }
    }

    // Configure Spotless for license headers
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            licenseHeader("""/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

""")
        }
    }

    // Suppress internal implementation packages in KDoc/Dokka documentation
    plugins.withId("org.jetbrains.dokka") {
        tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
            dokkaSourceSets.configureEach {
                perPackageOption {
                    matchingRegex.set("io\\.github\\.baole\\.konture\\.impl(\\..*)?")
                    suppress.set(true)
                }
            }
        }
    }

    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        configure<PublishingExtension> {
            if (name != "plugin-gradle" && name != "konture-test") {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                    }
                }
            }
        }
    }

    configure<PublishingExtension> {
        // Configure POM for ALL publications (including those automatically created by java-gradle-plugin)
        publications.withType<MavenPublication>().configureEach {
            pom {
                name.set(project.name)
                description.set(project.description ?: "Kotlin Architecture Testing Tool - ${project.name}")
                url.set("https://baole.github.io/konture")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("baole")
                        name.set("Bao Le Duc")
                        email.set("leducbao@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/baole/konture.git")
                    developerConnection.set("scm:git:ssh://github.com/baole/konture.git")
                    url.set("https://baole.github.io/konture")
                }
            }
        }

        repositories {
            if (project.name != "plugin-gradle" && project.name != "konture-test") {
                maven {
                    name = "MavenCentral"
                    val releasesRepoUrl =
                        uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
                    url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    credentials {
                        username =
                            providers.gradleProperty("mavenCentralUsername").orNull
                                ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                        password =
                            providers.gradleProperty("mavenCentralPassword").orNull
                                ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
                    }
                }
            }
        }
    }

    plugins.withId("signing") {
        configure<SigningExtension> {
            val signingKey = providers.gradleProperty("signingKey").orNull ?: System.getenv("GPG_SIGNING_KEY")
            val signingPassword =
                providers.gradleProperty("signingPassword").orNull ?: System.getenv("GPG_SIGNING_PASSWORD")
            val isRelease = providers.gradleProperty("releaseBuild").orNull?.toBoolean() ?: false
            isRequired = isRelease
            if (isRelease) {
                if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                }
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
}

// Register merged HTML test report task for core library tests
tasks.register<TestReport>("testReport") {
    destinationDirectory.set(layout.buildDirectory.dir("reports/all-tests"))

    // Aggregate test results from all subprojects
    val testTasks = subprojects.flatMap { sub -> sub.tasks.withType<Test>() }
    dependsOn(testTasks)
    testResults.from(testTasks.map { it.binaryResultsDirectory })
}

// Register aggregated code coverage report task
tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Generates an aggregate Jacoco coverage report for all subprojects."
    group = "Verification"

    val coverageProjects = subprojects.filter { it.name != "konture-test" }

    val testTasks = coverageProjects.map { sub -> sub.tasks.withType<Test>() }
    dependsOn(testTasks)

    val classDirs =
        coverageProjects.map { sub ->
            sub.providers.provider {
                val sourceSets = sub.extensions.findByType<SourceSetContainer>()
                val mainSourceSet = sourceSets?.findByName("main")
                mainSourceSet?.output?.classesDirs ?: sub.files()
            }
        }
    classDirectories.setFrom(files(classDirs))

    val srcDirs =
        coverageProjects.map { sub ->
            sub.providers.provider {
                val sourceSets = sub.extensions.findByType<SourceSetContainer>()
                val mainSourceSet = sourceSets?.findByName("main")
                mainSourceSet?.allSource?.srcDirs ?: sub.files()
            }
        }
    sourceDirectories.setFrom(files(srcDirs))

    val execFiles =
        coverageProjects.map { sub ->
            sub.fileTree(sub.layout.buildDirectory) {
                include("jacoco/*.exec")
            }
        }
    executionData.setFrom(files(execFiles))

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/all/html"))
    }
}

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(layout.projectDirectory.dir("docs/kdoc"))
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

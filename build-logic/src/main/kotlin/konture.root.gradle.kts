/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

import io.github.baole.konture.buildlogic.UpdateKotlinContributors
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    base
    jacoco
}

tasks.register<UpdateKotlinContributors>("updateKotlinContributors") {
    group = "formatting"
    description = "Adds the current developer to headers of changed Kotlin files."
    repositoryDirectory.set(layout.projectDirectory)
    contributorPropertiesFile.set(layout.projectDirectory.file("local.properties"))
    contributorSourceDirectories.set(
        subprojects.map { it.projectDir.absolutePath } +
            layout.projectDirectory
                .dir("build-logic")
                .asFile.absolutePath,
    )
}

tasks.register<TestReport>("testReport") {
    description = "Generates a merged HTML test report for all subprojects."
    group = "Verification"
    destinationDirectory.set(layout.buildDirectory.dir("reports/all-tests"))

    val testTasks = subprojects.flatMap { sub -> sub.tasks.withType<Test>() }
    dependsOn(testTasks)
    testResults.from(testTasks.map { it.binaryResultsDirectory })
}

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

tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
    description = "Verifies aggregate code coverage threshold across all subprojects."
    group = "Verification"

    val coverageProjects = subprojects.filter { it.name != "konture-test" }

    dependsOn(tasks.named("jacocoRootReport"))
    val reportFile = layout.buildDirectory.file("reports/jacoco/all/html/index.html")
    doFirst {
        logger.lifecycle("Aggregate Coverage Report: file://${reportFile.get().asFile.absolutePath}")
    }

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

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.84".toBigDecimal()
            }
        }
    }
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn(tasks.named("jacocoRootCoverageVerification"))
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(layout.projectDirectory.dir("docs/kdoc"))
}

tasks.named<Delete>("clean") {
    delete(layout.buildDirectory)
}

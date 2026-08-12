/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

import io.github.baole.konture.buildlogic.UpdateKotlinContributors
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
        subprojects.map { it.isolated.projectDirectory.asFile.absolutePath } +
            layout.projectDirectory
                .dir("build-logic")
                .asFile.absolutePath,
    )
}

tasks.register<TestReport>("testReport") {
    description = "Generates a merged HTML test report for all subprojects."
    group = "Verification"
    destinationDirectory.set(layout.buildDirectory.dir("reports/all-tests"))

    dependsOn(subprojects.map { "${it.path}:test" })
    testResults.from(subprojects.map { it.layout.buildDirectory.dir("test-results/test/binary") })
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Generates an aggregate Jacoco coverage report for all subprojects."
    group = "Verification"

    val coverageProjects = subprojects.filter { it.name != "konture-test" }

    dependsOn(coverageProjects.map { "${it.path}:classes" })
    dependsOn(coverageProjects.map { "${it.path}:test" })

    val classDirs =
        coverageProjects.map { sub ->
            sub.fileTree(sub.layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude("**/DefaultImpls*", "**/*\$DefaultImpls*")
            }
        }
    classDirectories.setFrom(classDirs)

    val srcDirs =
        coverageProjects.map { sub ->
            sub.isolated.projectDirectory.dir("src/main/kotlin")
        }
    sourceDirectories.setFrom(files(srcDirs))

    val execFiles =
        coverageProjects.map { sub ->
            sub.layout.buildDirectory.file("jacoco/test.exec")
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
            sub.fileTree(sub.layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude("**/DefaultImpls*", "**/*\$DefaultImpls*")
            }
        }
    classDirectories.setFrom(classDirs)


    val srcDirs =
        coverageProjects.map { sub ->
            sub.isolated.projectDirectory.dir("src/main/kotlin")
        }
    sourceDirectories.setFrom(files(srcDirs))

    val execFiles =
        coverageProjects.map { sub ->
            sub.layout.buildDirectory.file("jacoco/test.exec")
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

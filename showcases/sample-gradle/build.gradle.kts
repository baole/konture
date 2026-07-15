plugins {
    kotlin("jvm") version "2.4.0" apply false
    id("io.github.baole.konture") version "0.6.9"
}

allprojects {
    group = "io.github.baole.konture.sample"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

// Register merged HTML test report task
tasks.register<TestReport>("testReport") {
    destinationDirectory.set(layout.buildDirectory.dir("reports/all-tests"))

    // Aggregate test results from all subprojects
    val testTasks = subprojects.flatMap { sub -> sub.tasks.withType<Test>() }
    dependsOn(testTasks)
    testResults.from(testTasks.map { it.binaryResultsDirectory })
}

// Make the root test task trigger testReport
tasks.register("test") {
    dependsOn(tasks.named("testReport"))
}

konture {
    excludePackages("io.github.baole.konture.sample.domain.exclude..")
    excludeClasses("ExcludedService")
}



plugins {
    kotlin("jvm")
}

if (System.getProperty("archTest") != null) {
    pluginManager.apply("io.github.baole.konture")
}

dependencies {
    // Local project dependency ensures compiles always succeed without requiring publishToMavenLocal first
    testImplementation(project(":library"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    onlyIf {
        System.getProperty("archTest") != null
    }
}

plugins {
    kotlin("jvm")
}

pluginManager.apply("io.github.baole.konture")

dependencies {
    // Rely on published SNAPSHOT artifacts from mavenLocal()
    testImplementation("io.github.baole:konture:${libs.versions.konture.get()}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

plugins {
    kotlin("jvm")
}


dependencies {
    testImplementation("io.github.baole:konture:0.8.2")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

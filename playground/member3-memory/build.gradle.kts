plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":contracts"))
    testImplementation(kotlin("test"))
    testImplementation(project(":storage"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}

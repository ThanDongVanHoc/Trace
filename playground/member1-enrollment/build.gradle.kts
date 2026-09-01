plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":contracts"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

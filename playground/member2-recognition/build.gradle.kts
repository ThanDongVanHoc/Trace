plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":contracts"))
    implementation("com.microsoft.onnxruntime:onnxruntime:1.18.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

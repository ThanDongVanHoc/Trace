plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":contracts"))
    testImplementation(kotlin("test"))
}

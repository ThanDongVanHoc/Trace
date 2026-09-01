plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":contracts"))
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    testImplementation(kotlin("test"))
}

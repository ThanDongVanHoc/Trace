import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(project(":contracts"))
    implementation(project(":storage"))
    implementation(project(":member1-enrollment"))
    implementation(project(":member2-recognition"))
    implementation(project(":member3-memory"))
    implementation(project(":member4-vault"))

    implementation("io.ktor:ktor-server-core:3.5.2")
    implementation("io.ktor:ktor-server-netty:3.5.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")
    implementation("io.ktor:ktor-server-status-pages:3.5.2")
    implementation("io.ktor:ktor-server-call-logging:3.5.2")
    implementation("io.ktor:ktor-server-swagger:3.5.2")
    implementation("io.ktor:ktor-server-config-yaml:3.5.2")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:3.5.2")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.named<JavaExec>("run") {
    val reloadableProjectPaths = listOf(
        ":contracts",
        ":storage",
        ":member1-enrollment",
        ":member2-recognition",
        ":member3-memory",
        ":member4-vault",
    )
    val reloadableProjects = reloadableProjectPaths.map(::project)
    val reloadableJarNames = reloadableProjects.map { "${it.name}-${it.version}.jar" }.toSet()
    val reloadableOutputs = reloadableProjects.map { dependencyProject ->
        dependencyProject.extensions.getByType<SourceSetContainer>()["main"].output
    }
    dependsOn(reloadableProjectPaths.map { "$it:classes" })
    classpath = files(
        sourceSets["main"].output,
        reloadableOutputs,
        sourceSets["main"].runtimeClasspath.filterNot { it.name in reloadableJarNames },
    )
    systemProperty(
        "trace.data.dir",
        rootProject.layout.projectDirectory.dir("data").asFile.absolutePath,
    )
    systemProperty("io.ktor.development", "true")
}

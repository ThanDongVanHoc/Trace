pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "trace-kotlin-playground"

include(
    ":contracts",
    ":storage",
    ":member1-enrollment",
    ":member2-recognition",
    ":member3-memory",
    ":member4-vault",
    ":dev-server",
)

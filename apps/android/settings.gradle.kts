pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TraceAndroid"

include(
    ":app",
    ":core:contracts",
    ":core:database",
    ":core:network",
    ":feature:enrollment",
    ":feature:recognition",
    ":feature:memory",
    ":feature:securevault",
)

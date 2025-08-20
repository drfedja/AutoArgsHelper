pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Navgraph Helper"
include(":app")
include(":Autoargsdestination")
include(":features")
include(":features:treatment_manager")
include(":features:treatment_manager:ui")
include(":features:treatment_manager:domain")
include(":features:treatment_manager:data")
include(":UiCore")

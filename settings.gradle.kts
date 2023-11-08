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
        maven (url = "https://maven.google.com")
        maven ( url = "https://jitpack.io" )
        maven (url = "https://repository-achartengine.forge.cloudbees.com/snapshot/")

        gradlePluginPortal()
    }
}

rootProject.name = "iVPN"
include(":app", ":vpnLib")
 
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    /*
       This plugin is required to properly resolve toolchain. It's not required on local M1  run, but is required on CI
       > "If you use Gradle 8.0.2 or higher, you also need to add a toolchain resolver plugin. This type of plugin manages which repositories to
       > download a toolchain from. As an example, add to your settings.gradle(.kts) the following plugin:"
       source: https://kotlinlang.org/docs/gradle-configure-project.html?utm_campaign=gradle-jvm-toolchain&utm_medium=kgp&utm_source=warnings#gradle-java-toolchains-support
     */
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.5.0")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":core:common")
include(":core:db")
include(":core:repository")
include(":core:resource")

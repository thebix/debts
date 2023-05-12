// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply (false)
    alias(libs.plugins.kotlin.android) apply (false)
    alias(libs.plugins.kotlin.kapt) apply (false)
    alias(libs.plugins.google.services) apply (false)
    alias(libs.plugins.triplet.play) apply (false)
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    source = files(projectDir)
    config = files("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true

    reports {
        xml {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.xml"))
        }
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.html"))
        }
        txt {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.txt"))
        }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
    /*
    Rules in this rule set report issues related to libraries API exposure. https://detekt.dev/docs/rules/libraries
    Enabling this rule raised a lot of issues. Mostly because the current project structure includes just one module
    and therefore there is no need for most of the classes to be public. There is no need to fix that as this project
    should be modularized anyway.
     */
    //detektPlugins "io.gitlab.arturbosch.detekt:detekt-rules-libraries:${libs.versions.detekt.get()}"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

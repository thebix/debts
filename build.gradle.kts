import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply (false)
    alias(libs.plugins.kotlin.android) apply (false)
    alias(libs.plugins.firebase.crashlytics) apply (false)
    alias(libs.plugins.ksp) apply (false)
    alias(libs.plugins.google.services) apply (false)
    alias(libs.plugins.triplet.play) apply (false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.android.library) apply false
}

detekt {
    toolVersion = libs.versions.detekt.get()
    source.setFrom(projectDir)
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        sarif.required.set(false)
        xml.required.set(false)
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
    // detektPlugins "io.gitlab.arturbosch.detekt:detekt-rules-libraries:${libs.versions.detekt.get()}"
}

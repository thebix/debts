plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.kover)
}

android {
    namespace = "net.thebix.debts.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:db"))

    implementation(libs.bundles.rxjava)
}

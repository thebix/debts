@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:db"))

    implementation(libs.bundles.rxjava)
}

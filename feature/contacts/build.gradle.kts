@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.image.loader)
}

android {
    namespace = "net.thebix.debts.feature.contacts"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:resource"))

    implementation(libs.androidx.recyclerview)
}

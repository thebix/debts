@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.image.loader)
}

android {
    namespace = "net.thebix.debts.feature.adddebt"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:resource"))

    implementation(project(":feature:contacts"))

    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.bundles.rxjava)
}

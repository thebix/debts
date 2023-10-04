plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.feature.preferences"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:resource"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.google.android.material)
    implementation(libs.koin)
    implementation(libs.bundles.rxjava)
}

plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.image.loader)
}

android {
    namespace = "net.thebix.debts.feature.home"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:resource"))

    implementation(project(":feature:contacts"))
    implementation(project(":feature:adddebt"))

    implementation(libs.google.android.material)
    implementation(libs.koin)
    implementation(libs.bundles.rxjava)
}

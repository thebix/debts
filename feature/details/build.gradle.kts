plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.image.loader)
}

android {
    namespace = "net.thebix.debts.feature.details"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:resource"))
    implementation(project(":core:repository"))

    implementation(project(":feature:adddebt"))

    // required to use tools:context="debts.feature.home.HomeActivity"
    debugImplementation(project(":feature:home"))

    implementation(libs.koin)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.bundles.rxjava)
}

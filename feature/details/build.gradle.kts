plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.library.compose)
}

android {
    namespace = "net.thebix.debts.feature.details"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:resource"))
    implementation(project(":core:repository"))

    implementation(project(":feature:adddebt"))

    implementation(libs.koin)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.material.icons.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

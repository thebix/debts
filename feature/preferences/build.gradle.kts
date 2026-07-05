plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.library.compose)
}

android {
    namespace = "net.thebix.debts.feature.preferences"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:resource"))

    implementation(libs.koin)
    implementation(libs.androidx.compose.material.icons.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

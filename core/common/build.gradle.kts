plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.core.common"
}

dependencies {

    implementation(project(":core:resource"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.google.android.material)
    implementation(libs.bundles.rxjava)
    implementation(libs.androidx.preference)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.core.common"
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.google.android.material)
    implementation(libs.bundles.rxjava)
    implementation(libs.androidx.preference)
}

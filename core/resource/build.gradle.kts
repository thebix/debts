plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.library.compose)
}

android {
    namespace = "net.thebix.debts.core.resource"
}

dependencies {
    implementation(libs.google.android.material)
}

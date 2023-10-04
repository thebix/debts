plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.core.resource"
}

dependencies {
    implementation(libs.google.android.material)
}

plugins {
    alias(libs.plugins.debts.android.library)
}

android {
    namespace = "net.thebix.debts.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:db"))

    implementation(libs.bundles.rxjava)
    // Temporary interop bridge: DAO is on coroutines, repository public API is still RxJava.
    // Removed in D1.2 when the repository is migrated to suspend/Flow.
    implementation(libs.kotlinx.coroutines.rx2)
}

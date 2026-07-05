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
    // Temporary interop bridge: use cases still expose RxJava API.
    // Removed in D1.3 when use cases are migrated to suspend/Flow.
    implementation(libs.kotlinx.coroutines.rx2)

    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}

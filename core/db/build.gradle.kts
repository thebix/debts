@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.debts.android.library)
    alias(libs.plugins.debts.android.room)
}

android {
    // TODO: rename to net.thebix.debts.core.db
    namespace = "net.thebix.debts.db"
}

dependencies {
}

plugins {
    alias(libs.plugins.debts.android.application)
    alias(libs.plugins.debts.android.application.firebase)
    alias(libs.plugins.debts.android.room) // TODO: search project for "move room out of the app module". should be removed during DI refactoring
    alias(libs.plugins.triplet.play)
    alias(libs.plugins.debts.android.image.loader)
}

android {
    namespace = AppConfig.applicationId

    signingConfigs {
        create("release") {
            val credentials = credentials()
            storeFile = file(credentials.storeKeyFile)
            storePassword = credentials.storeKeyPassword
            keyAlias = credentials.storeKeyAlias
            keyPassword = credentials.storeKeyAliasPassword
        }
    }
    defaultConfig {
        applicationId = AppConfig.applicationId
        versionCode = AppConfig.Version.code
        versionName = AppConfig.Version.name

        println("App Id=$applicationId\nApp code version=${versionCode}\nApp name=$versionName")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:db"))
    implementation(project(":core:repository"))
    implementation(project(":core:resource"))

    implementation(project(":feature:home"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:adddebt"))
    implementation(project(":feature:preferences"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.google.android.material)
    implementation(libs.koin)
    implementation(libs.bundles.rxjava)

    debugImplementation(libs.leak.canary)
    debugImplementation(libs.bundles.hyperion)
}

play {
    releaseName.set("Internal track release name")
    // Note: the userFraction is only applicable where releaseStatus=[IN_PROGRESS/HALTED]
    // userFraction = 1.0
    defaultToAppBundles.set(true)
    serviceAccountCredentials.set(file("google-play-publisher.json"))

    // defaults, just to make explicit
    track.set("internal")
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED)
}

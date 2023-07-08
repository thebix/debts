plugins {
    alias(libs.plugins.debts.android.application)
    alias(libs.plugins.debts.android.application.firebase)
    alias(libs.plugins.debts.android.room) // TODO: should be moved to the db module
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

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.google.android.material)
    implementation(libs.androidx.preference)
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

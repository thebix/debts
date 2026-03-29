plugins {
    alias(libs.plugins.debts.android.application)
    alias(libs.plugins.debts.android.application.firebase)
    alias(libs.plugins.debts.android.room) // TODO: search project for "move room out of the app module". should be removed during DI refactoring
    alias(libs.plugins.triplet.play)
    alias(libs.plugins.debts.android.image.loader)
}

// The google-services plugin doesn't support configuring a custom path for google-services.json.
// By default it searches inside the app folder. To support keeping the file outside the project
// (e.g. in a private folder not tracked by git), we override the task's input via reflection after
// the plugin has set its defaults. If the external file is not found, the default search paths
// inside the app folder remain in effect (to use on CI).
afterEvaluate {
    tasks.matching { it.name.startsWith("process") && it.name.endsWith("GoogleServices") }.configureEach {
        val externalJson = File("${System.getProperty("user.home")}/private/macbook/debts/app/google-services.json")
        if (externalJson.exists()) {
            @Suppress("UNCHECKED_CAST")
            (javaClass.getMethod("getGoogleServicesJsonFiles").invoke(this) as org.gradle.api.provider.Property<Collection<File>>)
                .set(listOf(externalJson))
        }
    }
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
    implementation(project(":feature:details"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:adddebt"))
    implementation(project(":feature:preferences"))

    implementation(libs.koin)

    debugImplementation(libs.leak.canary)
    debugImplementation(libs.bundles.hyperion)
}

play {
    releaseName.set("Internal track release name")
    // Note: the userFraction is only applicable where releaseStatus=[IN_PROGRESS/HALTED]
    // userFraction = 1.0
    defaultToAppBundles.set(true)
    // TODO: 2026 03 30 Didn't touch this when was moving creds out of the repository folder. This needs to be adjusted: point to the new location locally, but keep the old one on CI.
    serviceAccountCredentials.set(file("google-play-publisher.json"))

    // defaults, just to make explicit
    track.set("internal")
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED)
}

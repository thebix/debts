plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.triplet.play)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

android {
    val credentials = credentials()
    signingConfigs {
        create("release") {
            storeFile = file(credentials.storeKeyFile)
            storePassword = credentials.storeKeyPassword
            keyAlias = credentials.storeKeyAlias
            keyPassword = credentials.storeKeyAliasPassword
        }
    }
    compileSdk = AndroidConfig.compileSdkVersion
    defaultConfig {
        applicationId = AppConfig.applicationId
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
        versionCode = AppConfig.Version.code
        versionName = AppConfig.Version.name

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas".toString())
            }
        }
        // region This section is added because com.android.tools.build:gradle:7.4.2 is used
        // this region should be removed completely when plugin is updated to 8.1.0-alpha09 and upper
        // link: https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
            targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        }
        // endregion

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

    // used by Room, to test migrations
    sourceSets {
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }
    namespace = AppConfig.applicationId
    // TODO: enable lint local checks and remove this
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.google.android.material)
    kapt(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.preference)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.glide.core)
    kapt(libs.glide.compiler)
    implementation(libs.koin)
    implementation(libs.kotlin.stdlib.jdk8)
    debugImplementation(libs.leak.canary)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.rxjava2)
    testImplementation(libs.room.testing)
    implementation(libs.rxbinding)
    implementation(libs.rxjava2.rxandroid)
    implementation(libs.rxjava2.rxjava)
    implementation(libs.timber)

    debugImplementation(libs.hyperion.core)
    debugImplementation(libs.hyperion.attr)
    debugImplementation(libs.hyperion.build.config)
    debugImplementation(libs.hyperion.disk)
    debugImplementation(libs.hyperion.geiger.counter)
    debugImplementation(libs.hyperion.measurement)
    debugImplementation(libs.hyperion.phoenix)
    debugImplementation(libs.hyperion.recorder)
    debugImplementation(libs.hyperion.shared.preferences)
    debugImplementation(libs.hyperion.timber)
}

//import com.github.triplet.gradle.androidpublisher.ReleaseStatus
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

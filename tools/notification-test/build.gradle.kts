plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "eu.hxreborn.notiftest"
    compileSdk = 37

    defaultConfig {
        applicationId = "eu.hxreborn.notiftest"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        disable +=
            setOf(
                "MissingApplicationIcon",
                "DataExtractionRules",
                "AllowBackup",
                "UnusedAttribute",
                "OldTargetApi",
            )
    }
}

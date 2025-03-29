plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

apply {
    from(file("${rootDir}/gradle/artifact-settings.gradle"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "org.maplibre.navigation.android.example"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "org.maplibre.navigation.android.example"
        compileSdk = 35
        minSdk = 21

        versionCode = 1
        versionName = project.properties.get("versionName") as String? ?: "0.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = false
        }

        release {
            isMinifyEnabled = true
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":libandroid-navigation-ui"))

    implementation(libs.maplibre) {
        // Exclude old version of GeoJSON libs
        // At the moment a newer version - that supports Kotlin Multiplatform - is required to run navigation
        exclude(group = "org.maplibre.gl", module = "android-sdk-geojson")
        exclude(group = "org.maplibre.gl", module = "android-sdk-turf")
    }
    implementation ("org.maplibre.navigation:ui:2.3.0")  // 최신 버전 확인
    implementation ("org.maplibre.navigation:core:2.3.0")
    implementation ("org.maplibre.navigation:android-core:3.1.0")

    // Support libraries
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)

    implementation(libs.play.services.location)

    // Logging
    implementation(libs.timber)

    // Leak Canary
    implementation(libs.leakcanary)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.multidex)
    implementation ("com.jakewharton.timber:timber:4.7.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    // Mapbox Navigation Core 라이브러리
    implementation ("com.mapbox.navigation:android:2.11.1")

    // 지도 및 UI 컴포넌트
    implementation ("com.mapbox.navigation:ui-maps:2.11.1")
    implementation ("com.mapbox.navigation:ui-components:2.11.1")

    // 기타 유틸리티 (선택적)
    implementation ("com.mapbox.navigation:tripdata:2.11.1")
    implementation ("com.mapbox.navigation:voice:2.11.1")
}

apply {
    from(file("${rootDir}/gradle/developer-config.gradle"))
}

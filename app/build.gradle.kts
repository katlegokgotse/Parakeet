plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
    // Add the Performance Monitoring Gradle plugin
    id("com.google.firebase.firebase-perf")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.parakeet_application"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.parakeet_application"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Firebase authentication
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")

    //analytics
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    //Fonts
    implementation ("com.google.android.gms:play-services-base:18.5.0")

    //Performance
    implementation("com.google.firebase:firebase-perf")

    //Circular ImageView library
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Image Cropper
    api("com.theartofdev.edmodo:android-image-cropper:")
    implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")
    implementation("com.soundcloud.android:android-crop:1.0.1@aar")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    //ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    //Circular Indicator library
    implementation("com.victor:lib:1.0.4")
    implementation ("com.github.yalantis:ucrop:2.2.8")
}
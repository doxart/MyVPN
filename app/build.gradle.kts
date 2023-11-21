plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

@Suppress("unstableapiusage")
android {
    namespace = "com.doxart.ivpn"
    compileSdk = 34

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    defaultConfig {
        applicationId = "com.doxart.ivpn"
        minSdk = 24
        targetSdk = 34
        versionCode = 25
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            javaCompileOptions {
                annotationProcessorOptions {
                    arguments["room.schemaLocation"] =
                            "$projectDir/schemas"
                }
            }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.android.gms:play-services-ads:22.5.0")

    implementation ("androidx.room:room-runtime:2.6.0")
    implementation("com.google.firebase:firebase-messaging:23.3.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation(project(mapOf("path" to ":nativetemplates")))
    annotationProcessor ("androidx.room:room-compiler:2.6.0")

    implementation ("org.osmdroid:osmdroid-android:6.1.17")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart-Realm:v3.0.3@aar")

    implementation ("com.github.skydoves:progressview:1.1.3")

    implementation ("commons-io:commons-io:2.6")

    implementation ("com.airbnb.android:lottie:6.1.0")

    implementation ("io.adapty:android-sdk:2.7.0")
    implementation ("io.adapty:android-ui:2.0.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation ("com.jakewharton.picasso:picasso2-okhttp3-downloader:1.1.0")

    implementation ("fr.bmartel:jspeedtest:1.32.1")
    implementation ("com.github.Gruzer:simple-gauge-android:0.3.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    implementation(project(mapOf("path" to ":vpnLib")))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
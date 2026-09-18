plugins { id("com.android.application") }

android {
    buildFeatures { buildConfig = true }
    namespace = "com.safetrust.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.safetrust.android"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            val baseUrl = project.findProperty("safetrustBaseUrl")?.toString() ?: "https://safetrust.hatchable.site"
            val cloudProjectNumber = project.findProperty("safetrustPlayIntegrityCloudProjectNumber")?.toString()?.trim() ?: ""
            buildConfigField("String", "SAFETRUST_BASE_URL", "\"$baseUrl\"")
            buildConfigField("String", "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "\"$cloudProjectNumber\"")
        }
        release {
            isMinifyEnabled = true
            val baseUrl = project.findProperty("safetrustBaseUrl")?.toString() ?: "https://safetrust.hatchable.site"
            val cloudProjectNumber = project.findProperty("safetrustPlayIntegrityCloudProjectNumber")?.toString()?.trim() ?: ""
            buildConfigField("String", "SAFETRUST_BASE_URL", "\"$baseUrl\"")
            buildConfigField("String", "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "\"$cloudProjectNumber\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.android.play:integrity:1.6.0")
    testImplementation("junit:junit:4.13.2")
}

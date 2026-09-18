import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

/**
 * Which architectures the build carries.
 *
 * arm64-v8a is what essentially every phone from 2017 onward runs, and is the
 * only architecture a build being tried locally needs to spend time on.
 * -PAURORA_ABIS=arm64-v8a narrows it; release builds pass nothing and get all
 * three, so this cannot quietly ship a partial APK.
 */
val androidAbis = providers.gradleProperty("AURORA_ABIS")
  .map { asked -> asked.split(",").map(String::trim).filter(String::isNotEmpty) }
  .orElse(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))
  .get()

android {
  namespace = "com.auroravpn.app"
  compileSdk = 36
  ndkVersion = "29.0.14206865"

  defaultConfig {
    applicationId = "com.auroravpn.app"
    minSdk = 26
    targetSdk = 36
    versionCode = 2
    versionName = "2.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }

    ndk {
      abiFilters += androidAbis
    }
  }

  splits {
    abi {
      isEnable = true
      reset()
      include(*androidAbis.toTypedArray())
      isUniversalApk = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
    debug {
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    // Compressed in the APK and extracted at install.
    //
    // AGP's default since minSdk 23 is the opposite: store every .so
    // uncompressed and page-aligned so the loader can map it straight out of
    // the APK. That trade is right for an app downloaded from a store over wifi
    // and wrong for this one. The Aether engine is several megabytes per ABI
    // and stored uncompressed that becomes the download; it deflates far
    // better than 3:1.
    //
    // What it costs is disk on the device, because the extracted copy sits
    // beside the APK that still holds it, and a second or two at install. What
    // it buys is the download, over a metered mobile connection, in a country
    // where the app is often fetched again after every block.
    jniLibs.useLegacyPackaging = true
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  sourceSets {
    getByName("debug").jniLibs.directories.add(
      layout.buildDirectory.dir("generated/rustJniLibs/debug").get().asFile.absolutePath,
    )
    getByName("release").jniLibs.directories.add(
      layout.buildDirectory.dir("generated/rustJniLibs/release").get().asFile.absolutePath,
    )
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.navigation.compose)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.datastore.preferences)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

/**
 * Builds the Rust JNI bridge for every requested ABI into the directory the
 * sourceSets above hand to AGP.
 *
 * cargo-ndk handles the NDK compiler and sysroot plumbing; the CMake and Ninja
 * that boring-sys (BoringSSL) needs come from the Android SDK's own copies
 * rather than a separate system install.
 *
 * --locked is deliberately not passed. The bridge's Cargo.lock is vendored from
 * the reference build, but the patch on boring-sys and the removed chain
 * modules change the graph, so cargo needs to rewrite it. Passing --locked
 * fails the build on that instead of building.
 */
fun registerCargoNdkTask(name: String, release: Boolean) = tasks.register<Exec>(name) {
  val outputVariant = if (release) "release" else "debug"
  val outputDir = layout.buildDirectory.dir("generated/rustJniLibs/$outputVariant")

  val androidHome = providers.environmentVariable("ANDROID_HOME")
  val ndkHome = providers.environmentVariable("ANDROID_NDK_HOME")
    .orElse(androidHome.map { "$it/ndk/29.0.14206865" })
    .map { it.replace('\\', '/') }
  val cmakeBin = androidHome.map { "$it/cmake/3.22.1/bin".replace('\\', '/') }

  workingDir(rootProject.file("native/android-bridge"))
  environment("ANDROID_NDK_HOME", ndkHome.get())
  environment("CMAKE", cmakeBin.map { "$it/cmake" }.get())
  environment("CMAKE_GENERATOR", "Ninja")
  environment("CMAKE_MAKE_PROGRAM", cmakeBin.map { "$it/ninja" }.get())

  commandLine(
    buildList {
      addAll(
        listOf(
          "cargo",
          "ndk",
          "--platform",
          "26",
          *androidAbis.flatMap { listOf("-t", it) }.toTypedArray(),
          "-o",
          outputDir.get().asFile.absolutePath,
          "build",
        ),
      )
      if (release) add("--release")
    },
  )

  inputs.files(
    rootProject.fileTree("native/android-bridge/src"),
    rootProject.file("native/android-bridge/Cargo.toml"),
    rootProject.file("native/android-bridge/Cargo.lock"),
    rootProject.file("native/rust-toolchain.toml"),
    rootProject.fileTree("native/aether") {
      exclude("**/target/**", "**/.git/**")
    },
    rootProject.fileTree("native/third-party/boring-sys") {
      exclude("**/target/**", "**/.git/**")
    },
  )
  inputs.property("androidAbis", androidAbis)
  outputs.dir(outputDir)
}

val cargoBuildAndroidDebug = registerCargoNdkTask("cargoBuildAndroidDebug", release = false)
val cargoBuildAndroidRelease = registerCargoNdkTask("cargoBuildAndroidRelease", release = true)

tasks.matching { it.name.matches(Regex("mergeDebugJniLibFolders")) }
  .configureEach { dependsOn(cargoBuildAndroidDebug) }
tasks.matching { it.name.matches(Regex("mergeReleaseJniLibFolders")) }
  .configureEach { dependsOn(cargoBuildAndroidRelease) }

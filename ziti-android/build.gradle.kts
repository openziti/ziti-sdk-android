/*
 * Copyright (c) 2018-2021 NetFoundry, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.lib)
    id("maven-publish")
}

description = "Ziti SDK for Android"

// git details, set on the root project by gradle/git-version.gradle.kts
val gitCommit = rootProject.extra["gitCommit"] as String
val gitBranch = rootProject.extra["gitBranch"] as String

android {
    namespace = "org.openziti.android"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        buildConfigField("String", "GIT_COMMIT", "\"$gitCommit\"")
        buildConfigField("String", "GIT_BRANCH", "\"${gitBranch.replace('/', '-')}\"")
        buildConfigField("String", "ZITI_VERSION", "\"${project.version}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        targetSdk = 37
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            // the instrumented tests are an app: license and manifest files that several of the SDK's dependencies carry
            excludes += listOf(
                "META-INF/LICENSE*", "META-INF/NOTICE*", "META-INF/DEPENDENCIES",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
            )
        }
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    api(libs.ziti) {
        exclude(module = "lazysodium-java")
        exclude(module = "jna")
    }
    api(libs.lazysodium.android) {
        // JNA comes as an aar below, it carries the native libraries
        exclude(group = "net.java.dev.jna", module = "jna")
    }
    api(variantOf(libs.jna) { artifactType("aar") })

    implementation(libs.kotlin)
    implementation(libs.coroutines.android)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)

    androidTestImplementation(libs.junit)
    // the instrumentation runner named in defaultConfig, it no longer comes with androidx.test.ext:junit
    androidTestImplementation(libs.test.runner)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("zitiAndroid") {
                groupId = project.group.toString()
                version = project.version.toString()
                from(components["release"])
            }
        }
    }

    apply(from = rootProject.file("publish.gradle.kts"))
}

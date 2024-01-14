/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
}

val libs = versionCatalogs.named("libs")

/*
 * Convention plugin that configures unit tests in projects with the Kotlin Multiplatform plugin
 */
testing {
    suites {
        getByName<JvmTestSuite>("test") {
            useJUnitJupiter(libs.findVersion("junit5").get().toString())
            targets {
                all {
                    testTask.configure {
                        maxHeapSize = "2G"
                        jvmArgs = listOf("-XX:MaxMetaspaceSize=768M")
                        testLogging {
                            if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
                                events = setOf(
                                    TestLogEvent.FAILED,
                                    TestLogEvent.STANDARD_ERROR,
                                    TestLogEvent.STANDARD_OUT,
                                )
                            } else {
                                events = setOf(TestLogEvent.FAILED)
                            }
                        }
                        javaLauncher = javaToolchains.launcherFor {
                            languageVersion = providers.environmentVariable("TEST_JDK_VERSION")
                                .map { JavaLanguageVersion.of(it.toInt()) }
                                .orElse(JavaLanguageVersion.of(17))
                        }
                    }
                }
            }
            dependencies {
                implementation(platform(libs.findLibrary("junit.bom").get()))
                listOf(
                    "assertk",
                    "junit.jupiter.api",
                    "junit.jupiter.params",
                ).forEach {
                    implementation(libs.findLibrary(it).get())
                }
                runtimeOnly(libs.findLibrary("junit.jupiter.engine").get())
            }
        }
    }
}

/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.test")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
    kotlin("kapt")
}

group = "ru.pixnews.anvil.codegen.experiment.generator"
version = "0.1-SNAPSHOT"

dependencies {
    api(libs.anvil.compiler.api)
    implementation(libs.anvil.compiler.utils)
    implementation(libs.kotlinpoet) { exclude(module = "kotlin-reflect") }
    implementation(projects.common)

    compileOnly(libs.auto.service.annotations)
    kapt(libs.auto.service.compiler)

    testImplementation(libs.anvil.annotations.optional)
    testImplementation(libs.assertk)
    testImplementation(libs.dagger)
    testImplementation(projects.testUtils)
    testImplementation(testFixtures(libs.anvil.compiler.utils))
}

/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.gradle.maven.publish.plugin.base) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlinx.binary.compatibility.validator) apply false
    id("ru.pixnews.anvil.codegen.buildlogic.project.kotlin.library") apply false
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish") apply false
    id("ru.pixnews.anvil.codegen.buildlogic.project.test") apply false
    id("ru.pixnews.anvil.codegen.buildlogic.project.lint.detekt")
    id("ru.pixnews.anvil.codegen.buildlogic.project.lint.diktat")
    id("ru.pixnews.anvil.codegen.buildlogic.project.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests and Android Lint)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}

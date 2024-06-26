/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.anvil.codegen.buildlogic.project.lint"

dependencies {
    implementation(libs.detekt.plugin)
    implementation(libs.diktat.plugin)
    implementation(libs.spotless.plugin)
}

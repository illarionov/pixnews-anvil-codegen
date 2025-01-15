/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.android.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish")
    alias(libs.plugins.compose.compiler)
}

version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_viewmodel_inject_version",
    envVariableName = "ANVIL_CODEGEN_VIEWMODEL_INJECT_VERSION",
).get()

android {
    namespace = "ru.pixnews.anvil.codegen.viewmodel"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    api(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.fragment.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.anvil.annotations)
    api(libs.dagger)
    api(libs.inject)
}

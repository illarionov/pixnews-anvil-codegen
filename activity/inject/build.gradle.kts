/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.android.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
}

group = "ru.pixnews.anvil.codegen.activity"
version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_activity_inject_version",
    envVariableName = "ANVIL_CODEGEN_ACTIVITY_INJECT_VERSION",
).get()

android {
    namespace = "ru.pixnews.anvil.codegen.activity"
}

dependencies {
    api(libs.anvil.annotations)
    api(libs.anvil.annotations.optional)
    api(libs.dagger)
    api(libs.inject)
}

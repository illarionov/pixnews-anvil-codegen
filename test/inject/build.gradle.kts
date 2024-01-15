/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.build-logic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
    id("ru.pixnews.anvil.codegen.build-logic.project.test")
}

group = "ru.pixnews.anvil.codegen.test"
version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_test_inject_version",
    envVariableName = "ANVIL_CODEGEN_TEST_INJECT_VERSION",
).get()

dependencies {
    api(libs.dagger)
}

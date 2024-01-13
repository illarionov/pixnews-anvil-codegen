/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.test")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
}

group = "ru.pixnews.anvil.codegen.experiment"
version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_experiment_inject_version",
    envVariableName = "ANVIL_CODEGEN_EXPERIMENT_INJECT_VERSION",
).get()

dependencies {
}

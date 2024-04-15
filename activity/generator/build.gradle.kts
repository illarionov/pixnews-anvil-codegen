/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.generator")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
}

version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_activity_generator_version",
    envVariableName = "ANVIL_CODEGEN_ACTIVITY_GENERATOR_VERSION",
).get()

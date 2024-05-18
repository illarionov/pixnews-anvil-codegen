/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.generator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish")
}

version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_viewmodel_generator_version",
    envVariableName = "ANVIL_CODEGEN_VIEWMODEL_GENERATOR_VERSION",
).get()

/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.generator.ksp")
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish.ksp")
}

version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_ksp_codegen_workmanager_generator_version",
    envVariableName = "ANVIL_KSP_CODEGEN_WORKMANAGER_GENERATOR_VERSION",
).get()

/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.codegen.buildlogic.project

/*
 * Convention plugin that configures Kotlinx Binary Compatibility Validator
 */
plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apiValidation {
    nonPublicMarkers.add("ru.pixnews.anvil.ksp.codegen.common.InternalPixnewsAnvilCodegenApi")
}

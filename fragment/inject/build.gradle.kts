/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.android.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.dokka.subproject")
}

group = "ru.pixnews.anvil.ksp.codegen"
version = "0.1-WIP"

android {
    namespace = "ru.pixnews.anvil.codegen.fragment"
}

dependencies {
    api(libs.androidx.fragment.ktx)
    api(libs.dagger)
}

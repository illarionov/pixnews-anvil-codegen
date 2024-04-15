/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.android.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.binary.compatibility.validator")
}

group = "ru.pixnews.anvil.codegen"
version = "0.1-WIP"

android {
    namespace = "ru.pixnews.anvil.codegen.activity"
}

dependencies {
    api(libs.androidx.fragment.ktx)
    api(libs.dagger)
}

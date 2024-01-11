/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
pluginManagement {
    includeBuild("gradle/build-logic/settings")
}

plugins {
    id("ru.pixnews.anvil.codegen.build-logic.settings.root")
}

rootProject.name = "pixnews-anvil-codegen"
include("lib")

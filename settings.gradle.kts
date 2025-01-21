/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("gradle/build-logic/settings")
}

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.settings.root")
}

rootProject.name = "pixnews-anvil-codegen"

include("doc:aggregate-documentation")
include("common")
include("test-utils")

listOf(
    "activity",
    "experiment",
    "fragment",
    "initializer",
    "test",
    "viewmodel",
    "workmanager",
).forEach { subproject ->
    "$subproject-generator".let {
        include(it)
        project(":$it").projectDir = file("$subproject/generator")
    }
    "$subproject-inject".let {
        include(it)
        project(":$it").projectDir = file("$subproject/inject")
    }
}

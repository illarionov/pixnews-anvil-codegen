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

dependencies {
    api(libs.kotlinpoet) {
        exclude(module = "kotlin-reflect")
    }
    api(libs.anvil.compiler.api)
    api(libs.anvil.compiler.utils)
}

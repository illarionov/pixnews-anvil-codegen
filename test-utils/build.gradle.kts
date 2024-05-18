/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.test")
}

dependencies {
    api(libs.anvil.compiler.api)
    api(libs.anvil.compiler.utils)
    api(libs.assertk)
    api(libs.kotlinpoet) { exclude(module = "kotlin-reflect") }
}

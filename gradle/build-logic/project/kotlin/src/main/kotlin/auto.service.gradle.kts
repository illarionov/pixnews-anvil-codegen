/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.codegen.buildlogic.project

import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

/**
 * Convention plugin that adds Auto-Service
 */
plugins {
    `java-library`
    kotlin("kapt")
}

plugins.withType<KotlinBasePlugin> {
    dependencies {
        val libs = versionCatalogs.named("libs")
        compileOnly(libs.findLibrary("auto.service.annotations").get())
        add("kapt", libs.findLibrary("auto.service.compiler").get())
    }
}

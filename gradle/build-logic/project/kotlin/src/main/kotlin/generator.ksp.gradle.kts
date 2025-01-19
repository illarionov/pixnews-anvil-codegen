/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.codegen.buildlogic.project

import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

/**
 * Convention plugin for use in kotlin generator modules
 */
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.auto.service")
    id("ru.pixnews.anvil.codegen.buildlogic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.test")
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.RequiresOptIn",
            "ru.pixnews.anvil.ksp.codegen.common.InternalPixnewsAnvilCodegenApi",
        )
    }
}

dependencies {
    val libs = versionCatalogs.named("libs")

    api(libs.findLibrary("anvil.ksp.compiler.api").get())
    addDependencyTo<ModuleDependency>(
        this,
        "implementation",
        libs.findLibrary("kotlinpoet").get().get(),
    ) {
        exclude(module = "kotlin-reflect")
    }
    compileOnly(libs.findLibrary("anvil.ksp.annotations").get())
    implementation(project(":common"))

    testImplementation(libs.findLibrary("anvil.ksp.annotations.optional").get())
    testImplementation(libs.findLibrary("assertk").get())
    testImplementation(libs.findLibrary("dagger").get())
    testImplementation(project(":test-utils"))
    testImplementation(testFixtures(libs.findLibrary("anvil.ksp.compiler.utils").get()))
}

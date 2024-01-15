/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

/**
 * Convention plugin for use in kotlin generator modules
 */
plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.auto.service")
    id("ru.pixnews.anvil.codegen.build-logic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.build-logic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
    id("ru.pixnews.anvil.codegen.build-logic.project.test")
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.RequiresOptIn",
            "ru.pixnews.anvil.codegen.common.InternalPixnewsAnvilCodegenApi",
        )
    }
}

dependencies {
    val libs = versionCatalogs.named("libs")

    api(libs.findLibrary("anvil.compiler.api").get())
    addDependencyTo<ModuleDependency>(
        this,
        "implementation",
        libs.findLibrary("kotlinpoet").get().get(),
    ) {
        exclude(module = "kotlin-reflect")
    }
    compileOnly(libs.findLibrary("anvil.annotations").get())
    implementation(project(":common"))

    testImplementation(libs.findLibrary("anvil.annotations.optional").get())
    testImplementation(libs.findLibrary("assertk").get())
    testImplementation(libs.findLibrary("dagger").get())
    testImplementation(project(":test-utils"))
    testImplementation(testFixtures(libs.findLibrary("anvil.compiler.utils").get()))
}

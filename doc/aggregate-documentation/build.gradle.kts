/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Module responsible for aggregating Dokka documentation from subprojects
 */
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.dokka.base")
}

group = "ru.pixnews.anvil.codegen"

private val websiteOutputDirectory = layout.buildDirectory.dir("outputs/website")

dokka {
    dokkaPublications.configureEach {
        moduleName.set("Pixnews Anvil-KSP Code Generators")
        includes.from("FRONTPAGE.md")
        outputDirectory.set(websiteOutputDirectory)
    }
}

tasks.named("build").configure {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
}

dependencies {
    listOf(
        "activity",
        "experiment",
        "initializer",
        "test",
        "viewmodel",
        "workmanager",
    ).forEach {
        dokka(project(":$it-generator"))
        dokka(project(":$it-inject"))
    }
}

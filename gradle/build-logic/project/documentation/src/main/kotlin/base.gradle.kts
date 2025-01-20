/*
 * Copyright (c) 2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.codegen.buildlogic.project.dokka

/*
 * Base configuration of Dokka
 */
plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaPublications.configureEach {
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(true)
    }

    dokkaSourceSets.configureEach {
        includes.from(
            "MODULE.md",
        )
        sourceLink {
            localDirectory.set(project.layout.projectDirectory)
            val remoteUrlSubpath = project.path.replace(':', '/')
                .replace("-generator", "/generator")
                .replace("-inject", "/inject")
            remoteUrl("https://github.com/illarionov/pixnews-anvil-codegen/tree/main$remoteUrlSubpath")
        }
    }

    pluginsConfiguration.html {
        homepageLink.set("https://github.com/illarionov/pixnews-anvil-codegen")
        footerMessage.set("(C) pixnews-anvil-codegen project authors and contributors")
    }
}

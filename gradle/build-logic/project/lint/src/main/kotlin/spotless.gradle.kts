/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.buildlogic.project.lint

/*
 * Convention plugin that configures Spotless
 */
plugins {
    id("com.diffplug.spotless")
}

spotless {
    isEnforceCheck = false

    val rootDir = lintedFileTree

    format("properties") {
        // "**/.gitignore" does not work: https://github.com/diffplug/spotless/issues/1146
        val propertyFiles = setOf(
            ".gitignore",
            ".gitattributes",
            ".editorconfig",
            "gradle.properties",
        )
        target(rootDir.filter { it.name in propertyFiles })

        trimTrailingWhitespace()
        endWithNewline()
    }
    format("toml") {
        target(rootDir.filter { it.name.endsWith(".toml") })

        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(2)
    }
    format("markdown") {
        target(rootDir.filter { it.name.endsWith(".md") || it.name.endsWith(".markdown") })

        endWithNewline()
    }
}

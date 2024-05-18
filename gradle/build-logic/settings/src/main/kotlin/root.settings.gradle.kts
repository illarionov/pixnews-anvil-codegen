/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.buildlogic.settings

/*
 * Base settings convention plugin for the use in library modules
 */
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.settings.repositories")
    id("ru.pixnews.anvil.codegen.buildlogic.settings.gradle-enterprise")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

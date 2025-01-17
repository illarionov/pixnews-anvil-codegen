/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.codegen.buildlogic.settings

/*
 * Base settings convention plugin for the use in library modules
 */
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.settings.repositories")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

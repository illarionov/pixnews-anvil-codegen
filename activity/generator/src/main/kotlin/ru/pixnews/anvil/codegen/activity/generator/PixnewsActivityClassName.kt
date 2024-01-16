/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.activity.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsActivityClassName {
    internal const val ACTIVITY_PACKAGE = "ru.pixnews.anvil.codegen.activity.inject"
    internal val activityMapKey = ClassName("ru.pixnews.anvil.codegen.activity.inject.wiring", "ActivityMapKey")
    internal val activityScope = ClassName(ACTIVITY_PACKAGE, "ActivityScope")
    internal val contributesActivity = FqName("$ACTIVITY_PACKAGE.ContributesActivity")
}

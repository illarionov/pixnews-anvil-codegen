/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.common.classname

import com.squareup.kotlinpoet.ClassName
import ru.pixnews.anvil.ksp.codegen.common.InternalPixnewsAnvilCodegenApi

@InternalPixnewsAnvilCodegenApi
public object AnvilClassName {
    private const val ANVIL_ANNOTATIONS_PACKAGE = "com.squareup.anvil.annotations"

    @JvmStatic
    internal val contributesMultibinding: ClassName = ClassName(ANVIL_ANNOTATIONS_PACKAGE, "ContributesMultibinding")

    @JvmStatic
    internal val contributesTo: ClassName = ClassName(ANVIL_ANNOTATIONS_PACKAGE, "ContributesTo")

    @JvmStatic
    @InternalPixnewsAnvilCodegenApi
    public val singleIn: ClassName = ClassName(
        "com.squareup.anvil.annotations.optional",
        "SingleIn",
    )
}

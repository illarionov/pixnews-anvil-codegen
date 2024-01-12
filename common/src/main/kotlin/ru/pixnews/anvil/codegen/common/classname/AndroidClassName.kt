/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.common.classname

import com.squareup.kotlinpoet.ClassName

public object AndroidClassName {
    @JvmStatic
    public val context: ClassName = ClassName("android.content", "Context")

    @JvmStatic
    public val savedStateHandle: ClassName = ClassName("androidx.lifecycle", "SavedStateHandle")

    @JvmStatic
    public val workerParameters: ClassName = ClassName("androidx.work", "WorkerParameters")
}

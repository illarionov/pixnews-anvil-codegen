/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.viewmodel.generator

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal object PixnewsViewModelClassName {
    internal val contributesViewModelFqName = FqName(
        "ru.pixnews.foundation.di.ui.base.viewmodel.ContributesViewModel",
    )
    internal val viewModelFactory: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelFactory",
    )
    internal val viewModelMapKey: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelMapKey",
    )
    internal val viewModelScope: ClassName = ClassName(
        "ru.pixnews.foundation.di.ui.base.viewmodel",
        "ViewModelScope",
    )
}

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
public object DaggerClassName {
    @JvmStatic
    public val assistedFactory: ClassName = ClassName("dagger.assisted", "AssistedFactory")

    @JvmStatic
    public val binds: ClassName = ClassName("dagger", "Binds")

    @JvmStatic
    public val classKey: ClassName = ClassName("dagger.multibindings", "ClassKey")

    @JvmStatic
    public val intoMap: ClassName = ClassName("dagger.multibindings", "IntoMap")

    @JvmStatic
    public val intoSet: ClassName = ClassName("dagger.multibindings", "IntoSet")

    @JvmStatic
    public val membersInjector: ClassName = ClassName("dagger", "MembersInjector")

    @JvmStatic
    public val module: ClassName = ClassName("dagger", "Module")

    @JvmStatic
    public val provides: ClassName = ClassName("dagger", "Provides")

    @JvmStatic
    public val reusable: ClassName = ClassName("dagger", "Reusable")
}

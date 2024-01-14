/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.viewmodel.inject.util

import androidx.lifecycle.ViewModelProvider
import java.util.concurrent.atomic.AtomicReference

public object RootViewModelFactoryHolder {
    private val _factory: AtomicReference<ViewModelProvider.Factory> = AtomicReference()

    public val factory: ViewModelProvider.Factory
        get() = requireNotNull(_factory.get()) {
            "initRootFactory() must be called before using this method"
        }

    public fun initRootFactory(
        factory: ViewModelProvider.Factory,
    ) {
        _factory.set(factory)
    }
}
